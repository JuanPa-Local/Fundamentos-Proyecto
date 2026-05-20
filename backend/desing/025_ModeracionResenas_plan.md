# Plan de Implementación: Moderación de Reseñas por el Administrador
**US-025 | Épica 6 – Reviews & Calificaciones**

---

## 1. Análisis y Modelado de Dominio

La moderación es una operación de dominio sobre la entidad `Resena` (creada en US-024). Añade dos transiciones de estado nuevas: `PENDIENTE` → `APROBADA` y `PENDIENTE` → `RECHAZADA`. Cada acción queda en auditoría.

### 1.1 Nuevos comportamientos en `Resena` (Aggregate Root)

- `void aprobar()` — cambia `estado` a `APROBADA`. Lanza `ResenaNoEnEstadoPendienteException` si no está en `PENDIENTE`.
- `void rechazar(String motivo)` — cambia `estado` a `RECHAZADA`. Lanza `ResenaNoEnEstadoPendienteException` si no está en `PENDIENTE`. Lanza `MotivoModeracioObligatorioException` si el motivo está vacío.

**Excepciones nuevas** (en `domain/review/exception/`):
- `ResenaNoEnEstadoPendienteException`
- `MotivoModeracionObligatorioException`
- `ResenaNoEncontradaException`

---

## 2. Capa de Aplicación (Casos de Uso)

### `ListarResenasPendientesUseCase`

**DTO de salida:** `Page<ResenaAdminResponse>`
- `UUID resenaId`, `UUID libroId`, `String tituloBrLibro`, `String nombreBuyer`, `int calificacion`, `String comentario`, `LocalDateTime fecha`

**Flujo:**
1. Verificar rol `ADMIN`.
2. Recuperar reseñas con `ResenaRepository.buscarPendientes(pageable)`.
3. Mapear y retornar.

### `AprobarResenaUseCase`

**DTO de entrada:** `AprobarResenaCommand` (`UUID resenaId`, `UUID adminId`)

**Flujo:**
1. Verificar rol `ADMIN`.
2. Recuperar la reseña con `ResenaRepository.buscarPorId()`. Si no existe, lanzar `ResenaNoEncontradaException`.
3. Invocar `resena.aprobar()`.
4. Persistir con `ResenaRepository.guardar()`.
5. Registrar en auditoría.
6. Invalidar caché de reseñas del libro si existe (la calificación promedio cambia).

### `RechazarResenaUseCase`

**DTO de entrada:** `RechazarResenaCommand` (`UUID resenaId`, `UUID adminId`, `String motivo`)

**Flujo:**
1. Verificar rol `ADMIN`.
2. Recuperar la reseña.
3. Invocar `resena.rechazar(motivo)`.
4. Persistir.
5. Registrar en auditoría.

---

## 3. Capa de Infraestructura

No se requieren migraciones nuevas; la tabla `resena` ya existe desde US-024 (`V13`).

### 3.1 Actualización del Repositorio JPA

**`SpringResenaRepository`** ya tiene `findByEstado()` desde US-024. Verificar que se puede paginar correctamente.

Nuevo método en `SpringResenaRepository` para obtener el nombre del buyer y el título del libro en la misma consulta (evitar N+1):

```java
@Query("""
    SELECT new com.openlib.backend.infrastructure.persistence.jpa.ResenaAdminProjection(
        r.id, r.libroId, l.titulo, u.nombre, r.calificacion, r.comentario, r.fechaCreacion
    )
    FROM ResenaJpaEntity r
    JOIN LibroJpaEntity l ON r.libroId = l.id
    JOIN UsuarioJpaEntity u ON r.buyerId = u.id
    WHERE r.estado = 'PENDIENTE'
    ORDER BY r.fechaCreacion ASC
""")
Page<ResenaAdminProjection> buscarPendientesConDetalle(Pageable pageable);
```

### 3.2 Invalidación del caché de detalle del libro

Al aprobar una reseña, el promedio de calificación del libro cambia. Si se usa caché para el detalle del libro, invalidarlo:

```java
// En AprobarResenaUseCase, después de persistir:
catalogoCache.invalidar("libro-detalle:" + resena.getLibroId());
```

Agregar este método a `CatalogoCache` o crear una interface `LibroDetalleCache` separada.

---

## 4. Capa de UI (JavaFX)

**`admin-view.fxml`** (nueva sección "Moderación de reseñas"):
- Tabla con columnas: título del libro, nombre del comprador, calificación (estrellas), fragmento del comentario, fecha.
- Botones "Aprobar" y "Rechazar" para la reseña seleccionada.
- Al rechazar: diálogo que solicita el motivo (campo de texto obligatorio).
- Paginación en la parte inferior.

**`AdminController.java`** (actualizado):
- Al cargar la sección: invoca `ListarResenasPendientesUseCase`.
- Al aprobar: invoca `AprobarResenaUseCase` y recarga.
- Al rechazar: muestra diálogo, recoge el motivo e invoca `RechazarResenaUseCase`.

---

## 5. Plan de Pruebas (TDD)

### `ResenaTest` (ampliado)
- `aprobar_desdeEstadoPendiente_debeCambiarEstadoAAprobada()`
- `aprobar_desdeEstadoAprobada_debeLanzarResenaNoEnEstadoPendienteException()`
- `rechazar_conMotivo_debeCambiarEstadoYAsignarMotivo()`
- `rechazar_sinMotivo_debeLanzarMotivoModeracionObligatorioException()`

### `AprobarResenaUseCaseTest` (con Mockito)
- `ejecutar_conResenaPendiente_debeAprobarYRegistrarAuditoria()`
- `ejecutar_sinRolAdmin_debeLanzarAccesoDenegadoException()`
- `ejecutar_conResenaNoExistente_debeLanzarResenaNoEncontradaException()`

### `RechazarResenaUseCaseTest` (con Mockito)
- `ejecutar_conMotivo_debeRechazarYRegistrarAuditoria()`
- `ejecutar_sinMotivo_debeLanzarMotivoModeracionObligatorioException()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Los casos de uso de moderación viven en el contexto `review`. Usan `AuditoriaRepository` del contexto `user` (definida en US-008) a través de su interface, sin acoplar los contextos. |
| **DDD** | Las transiciones de estado (`aprobar()`, `rechazar()`) son comportamientos de la entidad `Resena`. El caso de uso no manipula el estado directamente. |
| **TDD** | Ampliar `ResenaTest` con los nuevos comportamientos antes de modificar la entidad. Los tests deben ejecutarse en orden: pendiente → aprobar → intentar aprobar de nuevo (excepción). |
| **SOLID (SRP)** | `AprobarResenaUseCase` y `RechazarResenaUseCase` son clases separadas, aunque la estructura sea similar. |
| **SOLID (OCP)** | Si en el futuro se agrega el estado `OCULTA` (ocultar sin rechazar), se agregan nuevos métodos a `Resena` y nuevos casos de uso sin modificar los existentes. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear las excepciones `ResenaNoEnEstadoPendienteException`, `MotivoModeracionObligatorioException` y `ResenaNoEncontradaException`.
- [ ] 2. Ampliar `ResenaTest` con los tests de `aprobar()` y `rechazar()` (deben fallar).
- [ ] 3. Agregar los métodos `aprobar()` y `rechazar()` a la entidad `Resena`.
- [ ] 4. Verificar que los tests de dominio pasan.
- [ ] 5. Escribir los tests de `AprobarResenaUseCase`, `RechazarResenaUseCase` y `ListarResenasPendientesUseCase`.
- [ ] 6. Implementar los tres casos de uso y verificar.
- [ ] 7. Actualizar `SpringResenaRepository` con la query de detalle para el admin.
- [ ] 8. Actualizar `ResenaRepositoryAdapter` con el nuevo método.
- [ ] 9. Actualizar `admin-view.fxml` y `AdminController.java`.
- [ ] 10. Verificar que `mvn verify` pasa completo.

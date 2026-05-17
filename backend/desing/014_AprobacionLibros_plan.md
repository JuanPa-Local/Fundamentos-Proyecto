# Plan de Implementación: Aprobación y Rechazo de Libros por el Administrador
**US-014 | Épica 2 – Catálogo de Libros**

---

## 1. Análisis y Modelado de Dominio

La transición de estado del `Libro` (`PENDIENTE` → `APROBADO` o `RECHAZADO`) es una operación de dominio que vive en la entidad. El administrador es el único actor autorizado. Cada decisión queda registrada en auditoría y el Seller recibe notificación.

### 1.1 Nuevos comportamientos en `Libro` (Aggregate Root)

**Métodos a agregar:**
- `void aprobar()` — cambia `estado` a `APROBADO`. Lanza `LibroNoEnEstadoPendienteException` si el libro no está en `PENDIENTE`.
- `void rechazar(String motivo)` — cambia `estado` a `RECHAZADO` y asigna `motivoRechazo`. Lanza `LibroNoEnEstadoPendienteException` si no está en `PENDIENTE`. Lanza `MotivoRechazoObligatorioException` si el motivo está vacío.

**Excepciones nuevas** (en `domain/book/exception/`):
- `LibroNoEnEstadoPendienteException`
- `MotivoRechazoObligatorioException`

### 1.2 Interface `NotificacionGateway` (puerto de salida)

```java
// domain/book/NotificacionGateway.java
public interface NotificacionGateway {
    void notificarAprobacion(UUID sellerId, String tituloLibro);
    void notificarRechazo(UUID sellerId, String tituloLibro, String motivo);
}
```

### 1.3 Consulta de libros pendientes

Nuevo método en `LibroRepository`:
```java
Page<Libro> buscarPorEstado(EstadoLibro estado, Pageable pageable);
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `ListarLibrosPendientesUseCase`

**Flujo:**
1. Verificar que el `adminId` tiene rol `ADMIN`.
2. Recuperar libros con `LibroRepository.buscarPorEstado(PENDIENTE, pageable)`.
3. Retornar lista de `LibroAdminResponse` con `id`, `titulo`, `autor`, `isbn`, `sellerId`, `fechaPublicacion`.

### `AprobarLibroUseCase`

**DTO de entrada:** `AprobarLibroCommand` (`UUID libroId`, `UUID adminId`)

**Flujo:**
1. Verificar rol `ADMIN`.
2. Recuperar el libro por id. Si no existe, lanzar `LibroNoDisponibleException`.
3. Invocar `libro.aprobar()`.
4. Persistir con `LibroRepository.guardar()`.
5. Registrar en auditoría con `AuditoriaRepository.registrar()`.
6. Notificar al Seller con `NotificacionGateway.notificarAprobacion()`.
7. Invalidar caché del catálogo con `CatalogoCache.invalidar("catalogo:*")`.

### `RechazarLibroUseCase`

**DTO de entrada:** `RechazarLibroCommand` (`UUID libroId`, `UUID adminId`, `String motivo`)

**Flujo:**
1. Verificar rol `ADMIN`.
2. Recuperar el libro por id.
3. Invocar `libro.rechazar(motivo)`.
4. Persistir.
5. Registrar en auditoría.
6. Notificar al Seller con `NotificacionGateway.notificarRechazo()`.

---

## 3. Capa de Infraestructura

### 3.1 Implementación de `NotificacionGateway`

**`ConsolaNotificacionGateway.java`** (en `infrastructure/notification/`):
- Implementación provisional que imprime la notificación en consola/log.
- Se puede reemplazar por un adaptador de email en el futuro sin modificar los casos de uso.

### 3.2 Actualización de `LibroRepositoryAdapter`

Implementar `buscarPorEstado()` usando:
```java
Page<LibroJpaEntity> findByEstado(String estado, Pageable pageable);
```

---

## 4. Capa de UI (JavaFX)

**`admin-view.fxml`** (nueva sección "Revisión de contenido"):
- Tabla con columnas: `titulo`, `autor`, `isbn`, `sello editorial`, `fecha de envío`.
- Botones "Aprobar" y "Rechazar" para el libro seleccionado.
- Al rechazar: ventana de diálogo que solicita el motivo (campo de texto obligatorio).

**`AdminController.java`** (actualizado):
- Al cargar la sección: invoca `ListarLibrosPendientesUseCase`.
- Al aprobar: invoca `AprobarLibroUseCase` y recarga la tabla.
- Al rechazar: muestra diálogo, recoge el motivo e invoca `RechazarLibroUseCase`.

---

## 5. Plan de Pruebas (TDD)

### `LibroTest` (ampliado)
- `aprobar_desdeEstadoPendiente_debeCambiarEstadoAAprobado()`
- `aprobar_desdeEstadoAprobado_debeLanzarLibroNoEnEstadoPendienteException()`
- `rechazar_conMotivo_debeCambiarEstadoYAsignarMotivo()`
- `rechazar_sinMotivo_debeLanzarMotivoRechazoObligatorioException()`
- `rechazar_desdeEstadoAprobado_debeLanzarLibroNoEnEstadoPendienteException()`

### `AprobarLibroUseCaseTest` (con Mockito)
- `ejecutar_conLibroPendiente_debeAprobarNotificarEInvalidarCache()`
- `ejecutar_sinRolAdmin_debeLanzarAccesoDenegadoException()`
- `ejecutar_conLibroNoExistente_debeLanzarLibroNoDisponibleException()`

### `RechazarLibroUseCaseTest` (con Mockito)
- `ejecutar_conMotivo_debeRechazarYNotificarSeller()`
- `ejecutar_sinMotivo_debeLanzarMotivoRechazoObligatorioException()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `AprobarLibroUseCase` y `RechazarLibroUseCase` no dependen de `ConsolaNotificacionGateway` ni de Redis directamente. Dependen de las interfaces `NotificacionGateway` y `CatalogoCache`. |
| **DDD** | Las transiciones de estado (`aprobar()`, `rechazar()`) son comportamientos del Aggregate Root `Libro`. El caso de uso solo orquesta; no contiene lógica de estado. |
| **TDD** | Ampliar `LibroTest` con los nuevos comportamientos antes de modificar la entidad. |
| **SOLID (SRP)** | Un caso de uso por acción: `AprobarLibroUseCase` y `RechazarLibroUseCase` son clases separadas. |
| **SOLID (OCP)** | `ConsolaNotificacionGateway` es reemplazable por un `EmailNotificacionGateway` sin tocar los casos de uso. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear las excepciones `LibroNoEnEstadoPendienteException` y `MotivoRechazoObligatorioException`.
- [ ] 2. Ampliar `LibroTest` con los tests de `aprobar()` y `rechazar()` (deben fallar).
- [ ] 3. Agregar métodos `aprobar()` y `rechazar()` a la entidad `Libro`.
- [ ] 4. Verificar que los tests de dominio pasan.
- [ ] 5. Definir la interface `NotificacionGateway` en `domain/book/`.
- [ ] 6. Agregar `buscarPorEstado()` a `LibroRepository`.
- [ ] 7. Escribir los tests de `AprobarLibroUseCase` y `RechazarLibroUseCase`.
- [ ] 8. Implementar ambos casos de uso y `ListarLibrosPendientesUseCase`.
- [ ] 9. Verificar que los tests pasan.
- [ ] 10. Crear `ConsolaNotificacionGateway` en `infrastructure/notification/`.
- [ ] 11. Actualizar `LibroRepositoryAdapter` con `buscarPorEstado()`.
- [ ] 12. Actualizar `admin-view.fxml` y `AdminController.java`.
- [ ] 13. Verificar que `mvn verify` pasa completo.

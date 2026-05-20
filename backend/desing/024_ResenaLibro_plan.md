# Plan de Implementación: Reseña de un Libro Adquirido
**US-024 | Épica 6 – Reviews & Calificaciones**

---

## 1. Análisis y Modelado de Dominio

La `Resena` es el Aggregate Root del contexto `review`. Solo los Buyers que tienen el libro en su biblioteca pueden escribir una reseña, y cada comprador puede escribir solo una por libro. Nace en estado `PENDIENTE` hasta que el admin la apruebe.

### 1.1 Enumeración `EstadoResena`

**Archivo:** `domain/review/EstadoResena.java`
- Valores: `PENDIENTE`, `APROBADA`, `RECHAZADA`

### 1.2 Entidad `Resena` (Aggregate Root)

**Archivo:** `domain/review/Resena.java`

**Atributos:**
- `UUID id`
- `UUID buyerId`
- `UUID libroId`
- `int calificacion` — entre 1 y 5 (obligatorio)
- `String comentario` — opcional
- `EstadoResena estado` — `PENDIENTE` al crear
- `LocalDateTime fechaCreacion`

**Método fábrica:**
- `static Resena crear(UUID buyerId, UUID libroId, int calificacion, String comentario)` — valida que la calificación esté entre 1 y 5. Lanza `CalificacionInvalidaException` si está fuera del rango.

**Excepciones de dominio** (en `domain/review/exception/`):
- `CalificacionInvalidaException`
- `ResenaYaExistenteException`
- `LibroNoAdquiridoException`

### 1.3 Interface `ResenaRepository` (implementación completa)

Reemplaza la interface provisional definida en US-012:

```java
// domain/review/ResenaRepository.java
public interface ResenaRepository {
    Resena guardar(Resena resena);
    boolean existePorBuyerIdYLibroId(UUID buyerId, UUID libroId);
    Page<Resena> buscarAprobadasPorLibro(UUID libroId, Pageable pageable);
    double calcularPromedioCalificacion(UUID libroId);
    Page<Resena> buscarPendientes(Pageable pageable);
    Optional<Resena> buscarPorId(UUID id);
}
```

---

## 2. Capa de Aplicación (Caso de Uso)

### `CrearResenaUseCase`

**DTO de entrada:** `CrearResenaCommand`
- `UUID buyerId`
- `UUID libroId`
- `int calificacion`
- `String comentario`

**Flujo:**
1. Verificar rol `BUYER`.
2. Verificar que el libro está en la biblioteca del buyer con `BibliotecaRepository.existeEnBiblioteca()`. Si no, lanzar `LibroNoAdquiridoException`.
3. Verificar que no existe ya una reseña del buyer para ese libro con `ResenaRepository.existePorBuyerIdYLibroId()`. Si existe, lanzar `ResenaYaExistenteException`.
4. Crear la `Resena` con `Resena.crear(buyerId, libroId, calificacion, comentario)`.
5. Persistir con `ResenaRepository.guardar()`.
6. Retornar `ResenaResponse` con el `id` y el estado `PENDIENTE`.

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V13__crear_tabla_resena.sql`

```sql
CREATE TABLE resena (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id      UUID NOT NULL REFERENCES usuario(id),
    libro_id      UUID NOT NULL REFERENCES libro(id),
    calificacion  SMALLINT NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
    comentario    TEXT,
    estado        VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (buyer_id, libro_id)
);

CREATE INDEX idx_resena_libro ON resena (libro_id, estado);
```

### 3.2 Entidades y Repositorio JPA

- `ResenaJpaEntity.java` con `@Table(name = "resena")`.
- `SpringResenaRepository.java`:

```java
boolean existsByBuyerIdAndLibroId(UUID buyerId, UUID libroId);

Page<ResenaJpaEntity> findByLibroIdAndEstado(UUID libroId, String estado, Pageable pageable);

@Query("SELECT AVG(r.calificacion) FROM ResenaJpaEntity r WHERE r.libroId = :libroId AND r.estado = 'APROBADA'")
Double calcularPromedioCalificacion(@Param("libroId") UUID libroId);

Page<ResenaJpaEntity> findByEstado(String estado, Pageable pageable);
```

- `ResenaRepositoryAdapter.java` implementando `ResenaRepository` del dominio.
- `ResenaMapper.java`.

### 3.3 Actualización del adaptador provisional en `domain/book/`

La interface `ResenaRepository` provisional definida en `domain/book/` (US-012) debe eliminarse. El contexto `book` accede a las reseñas a través de `ResenaRepository` del contexto `review`.

> **Nota de arquitectura:** Para que `VerDetalleLibroUseCase` (dominio `book`) acceda a reseñas (dominio `review`) sin cruzar contextos, se crea un servicio de aplicación `ObtenerResenasDeLibroService` en la capa de aplicación que coordina ambos dominios.

---

## 4. Capa de UI (JavaFX)

**`detalle-libro-view.fxml`** (actualizado):
- Formulario de reseña visible solo si el Buyer tiene el libro en su biblioteca.
- Selector de calificación: 5 estrellas clicables.
- Campo de texto opcional para el comentario.
- Botón "Enviar reseña".
- Si el Buyer ya tiene una reseña para ese libro: mostrar la reseña existente con su estado.

**`DetalleLibroController.java`** (actualizado):
- Verifica si el buyer tiene el libro en biblioteca para mostrar/ocultar el formulario.
- Al enviar: invoca `CrearResenaUseCase`.

---

## 5. Plan de Pruebas (TDD)

### `ResenaTest` (dominio)
- `crear_conCalificacionValida_debeCrearResenaEnEstadoPendiente()`
- `crear_conCalificacionCero_debeLanzarCalificacionInvalidaException()`
- `crear_conCalificacionSeis_debeLanzarCalificacionInvalidaException()`
- `crear_sinComentario_debePermitirlo()`

### `CrearResenaUseCaseTest` (con Mockito)
- `ejecutar_conLibroEnBibliotecaYSinResena_debeCrearYPersistir()`
- `ejecutar_conLibroFueraDeBiblioteca_debeLanzarLibroNoAdquiridoException()`
- `ejecutar_conResenaYaExistente_debeLanzarResenaYaExistenteException()`
- `ejecutar_conCalificacionInvalida_debeLanzarCalificacionInvalidaException()`

### `ResenaRepositoryAdapterTest` (`@DataJpaTest`)
- `guardar_debePersistirResenaConEstadoPendiente()`
- `existePorBuyerIdYLibroId_despuesDeGuardar_debeRetornarTrue()`
- `buscarAprobadasPorLibro_debeRetornarSoloAprobadas()`
- `calcularPromedioCalificacion_debeRetornarPromedioCorrectamente()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `CrearResenaUseCase` pertenece al contexto `review`. Accede al contexto `order` (Biblioteca) a través de la interface `BibliotecaRepository`, nunca directamente a sus clases de dominio. |
| **DDD** | `Resena` encapsula su propia validación de calificación. El contexto `review` es independiente de `order` y `book`; se comunica a través de IDs. |
| **TDD** | Escribir `ResenaTest` antes de implementar la entidad, con tests paramétricos para calificaciones de 0 a 6. |
| **SOLID (SRP)** | `CrearResenaUseCase` tiene una única responsabilidad: crear y persistir la reseña. La moderación es responsabilidad de `ModerarResenaUseCase` (US-025). |
| **SOLID (OCP)** | La implementación real de `ResenaRepository` reemplaza el adaptador provisional sin modificar `VerDetalleLibroUseCase`. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear el contexto `domain/review/` con el Enum `EstadoResena` y las excepciones.
- [ ] 2. Escribir `ResenaTest` (debe fallar).
- [ ] 3. Crear la entidad `Resena` con el método fábrica `crear()`.
- [ ] 4. Verificar que `ResenaTest` pasa.
- [ ] 5. Definir la interface `ResenaRepository` en `domain/review/`.
- [ ] 6. Escribir `CrearResenaUseCaseTest` con Mockito (debe fallar).
- [ ] 7. Implementar `CrearResenaUseCase` y verificar.
- [ ] 8. Crear el script `V13__crear_tabla_resena.sql`.
- [ ] 9. Crear `ResenaJpaEntity`, `SpringResenaRepository`, `ResenaRepositoryAdapter` y `ResenaMapper`.
- [ ] 10. Eliminar el adaptador provisional de `ResenaRepository` del contexto `book`.
- [ ] 11. Crear `ObtenerResenasDeLibroService` en la capa de aplicación para conectar los contextos.
- [ ] 12. Actualizar `VerDetalleLibroUseCase` para usar el nuevo servicio.
- [ ] 13. Escribir y verificar `ResenaRepositoryAdapterTest`.
- [ ] 14. Actualizar `detalle-libro-view.fxml` y `DetalleLibroController.java`.
- [ ] 15. Verificar que `mvn verify` pasa completo.

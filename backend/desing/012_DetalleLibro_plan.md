# Plan de Implementación: Vista de Detalle de un Libro
**US-012 | Épica 2 – Catálogo de Libros**

---

## 1. Análisis y Modelado de Dominio

La vista de detalle agrega información de reseñas a la vista básica del libro. Se introduce el concepto de calificación promedio calculado a partir de las `Resena` aprobadas (dominio `review`, US-024). En esta historia solo se lee ese dato; la escritura de reseñas se implementa en US-024.

### 1.1 Value Object `DetalleLibro`

**Archivo:** `domain/book/DetalleLibro.java`
- `UUID id`
- `String titulo`, `String autor`, `String isbn`
- `String descripcion`, `String categoria`
- `List<String> etiquetas`
- `String portadaUrl`
- `double calificacionPromedio` — 0.0 si no hay reseñas
- `int totalResenas`
- `List<ResenaResumen> resenas` — paginadas

### 1.2 Record `ResenaResumen`

**Archivo:** `domain/book/ResenaResumen.java`
- `String nombreComprador`
- `int calificacion`
- `String comentario`
- `LocalDateTime fecha`

---

## 2. Capa de Aplicación (Caso de Uso)

### `VerDetalleLibroUseCase`

**DTO de entrada:**
- `UUID libroId`
- `int paginaResenas` (default 0)

**DTO de salida:** `DetalleLibro`

**Flujo:**
1. Recuperar el libro con `LibroRepository.buscarPorId(libroId)`. Si no existe o su estado no es `APROBADO`, lanzar `LibroNoDisponibleException`.
2. Recuperar las reseñas aprobadas paginadas con `ResenaRepository.buscarAprobadasPorLibro(libroId, pageable)`.
3. Calcular el promedio de calificaciones.
4. Construir y retornar el `DetalleLibro`.

### Interface nueva `ResenaRepository` (puerto de salida — preliminar)

```java
// domain/book/ResenaRepository.java
public interface ResenaRepository {
    Page<Resena> buscarAprobadasPorLibro(UUID libroId, Pageable pageable);
    double calcularPromedioCalificacion(UUID libroId);
}
```

> Esta interface se implementará completamente en US-024. En esta historia se usa un adaptador provisional que retorna listas vacías.

**Excepción nueva:** `LibroNoDisponibleException` (en `domain/book/exception/`)

---

## 3. Capa de Infraestructura

### 3.1 Campo `descripcion` y `portadaUrl` en `Libro`

Agregar los campos a la entidad `Libro` y a la tabla mediante migración:

**Archivo nuevo:** `V7__agregar_campos_detalle_libro.sql`
```sql
ALTER TABLE libro
    ADD COLUMN descripcion TEXT,
    ADD COLUMN portada_url VARCHAR(500);
```

Actualizar `LibroJpaEntity` y `LibroMapper` para incluir estos campos.

### 3.2 Adaptador provisional de reseñas

**`ResenaRepositoryAdapterProvisional.java`** (en `infrastructure/persistence/`):
- Implementa `ResenaRepository`.
- `buscarAprobadasPorLibro()` retorna `Page.empty()`.
- `calcularPromedioCalificacion()` retorna `0.0`.
- Se reemplazará en US-024 por la implementación real.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/detalle-libro-view.fxml`
- Sección superior: portada (imagen), título, autor, ISBN, categoría, etiquetas.
- Sección de calificación: estrellas (1-5) que reflejan el promedio.
- Sección de reseñas: lista paginada con nombre del comprador, calificación y comentario.
- Botón "Volver al catálogo".
- Botón "Agregar al carrito" (habilitado solo si el usuario está autenticado como Buyer).

**Controlador:** `UI/controllers/DetalleLibroController.java`
- Recibe el `libroId` al navegar desde `BuyerController`.
- Invoca `VerDetalleLibroUseCase`.
- Gestiona la paginación de reseñas.

---

## 5. Plan de Pruebas (TDD)

### `VerDetalleLibroUseCaseTest` (con Mockito)
- `ejecutar_conLibroAprobado_debeRetornarDetalleCompleto()`
- `ejecutar_conLibroNoExistente_debeLanzarLibroNoDisponibleException()`
- `ejecutar_conLibroPendiente_debeLanzarLibroNoDisponibleException()`
- `ejecutar_sinResenas_debeRetornarCalificacionPromedioEnCero()`
- `ejecutar_conResenas_debeCalcularPromedioCorrectamente()`

### `LibroRepositoryAdapterTest` (ampliado)
- `buscarPorId_conLibroAprobado_debeRetornarLibro()`
- `buscarPorId_conIdInexistente_debeRetornarEmpty()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `VerDetalleLibroUseCase` depende de `LibroRepository` y `ResenaRepository` (interfaces). El adaptador provisional es transparente para el caso de uso. |
| **DDD** | `DetalleLibro` es un Value Object de lectura (proyección). No es la entidad `Libro`; es una vista enriquecida para el caso de uso de detalle. |
| **TDD** | Escribir `VerDetalleLibroUseCaseTest` antes de implementar el caso de uso. Usar el adaptador provisional de reseñas en los tests. |
| **SOLID (OCP)** | El adaptador provisional de reseñas será reemplazado en US-024 sin modificar `VerDetalleLibroUseCase` (principio abierto/cerrado). |
| **SOLID (SRP)** | El cálculo del promedio de calificaciones es responsabilidad de `VerDetalleLibroUseCase`, no de la entidad `Libro`. La entidad no debe conocer sus propias estadísticas de reseñas. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear los Value Objects `DetalleLibro` y `ResenaResumen` en `domain/book/`.
- [ ] 2. Crear `LibroNoDisponibleException` en `domain/book/exception/`.
- [ ] 3. Definir la interface `ResenaRepository` en `domain/book/`.
- [ ] 4. Escribir `VerDetalleLibroUseCaseTest` con Mockito (debe fallar).
- [ ] 5. Implementar `VerDetalleLibroUseCase`.
- [ ] 6. Verificar que los tests pasan.
- [ ] 7. Crear el script `V7__agregar_campos_detalle_libro.sql`.
- [ ] 8. Actualizar `LibroJpaEntity` y `LibroMapper`.
- [ ] 9. Crear `ResenaRepositoryAdapterProvisional`.
- [ ] 10. Ampliar `LibroRepositoryAdapterTest` y verificar.
- [ ] 11. Crear `detalle-libro-view.fxml` y `DetalleLibroController.java`.
- [ ] 12. Verificar que `mvn verify` pasa completo.

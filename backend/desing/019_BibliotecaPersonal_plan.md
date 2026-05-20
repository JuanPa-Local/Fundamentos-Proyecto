# Plan de Implementación: Biblioteca Personal del Comprador
**US-019 | Épica 4 – Biblioteca Personal & Motor de Descargas**

---

## 1. Análisis y Modelado de Dominio

La `Biblioteca` es el conjunto de libros adquiridos por un Buyer. Se representa como una entidad con identidad propia dentro del contexto `order`. Se introduce aquí la implementación completa de `BibliotecaRepository`, que en US-015 y US-018 era provisional.

### 1.1 Entidad `Biblioteca` (Aggregate Root)

**Archivo:** `domain/order/Biblioteca.java`

**Atributos:**
- `UUID buyerId` — identificador del propietario
- `List<LibroBiblioteca> libros` — libros adquiridos

**Comportamientos:**
- `void agregar(LibroBiblioteca libro)` — agrega un libro. Lanza `LibroYaEnBibliotecaException` si ya existe.
- `boolean contiene(UUID libroId)` — retorna `true` si el libro ya está en la biblioteca.
- `boolean estaVacia()` — retorna `true` si no hay libros.

### 1.2 Value Object `LibroBiblioteca`

**Archivo:** `domain/order/LibroBiblioteca.java`
- `UUID libroId`
- `String titulo`
- `String autor`
- `String portadaUrl`
- `LocalDateTime fechaAdquisicion`

**Excepción:** `LibroYaEnBibliotecaException`

### 1.3 Interface `BibliotecaRepository` (implementación completa)

Reemplaza el adaptador provisional creado en US-015 y US-018:

```java
// domain/order/BibliotecaRepository.java
public interface BibliotecaRepository {
    boolean existeEnBiblioteca(UUID buyerId, UUID libroId);
    void agregar(UUID buyerId, UUID libroId);
    Optional<Biblioteca> buscarPorBuyerId(UUID buyerId, Pageable pageable);
}
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `VerBibliotecaUseCase`

**DTO de entrada:** `VerBibliotecaQuery` (`UUID buyerId`, `String ordenarPor` — "fecha" o "titulo", `int pagina`)

**DTO de salida:** `BibliotecaResponse`
- `List<LibroBibliotecaResponse>` — cada uno con `libroId`, `titulo`, `autor`, `portadaUrl`, `fechaAdquisicion`
- `int totalPaginas`, `long totalLibros`
- `boolean vacia`

**Flujo:**
1. Verificar rol `BUYER`.
2. Recuperar los libros de la biblioteca con `BibliotecaRepository.buscarPorBuyerId()` con paginación y ordenamiento.
3. Si no hay libros, retornar `BibliotecaResponse` con `vacia = true`.
4. Mapear y retornar.

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V10__crear_tabla_biblioteca.sql`

```sql
CREATE TABLE biblioteca (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id          UUID NOT NULL REFERENCES usuario(id),
    libro_id          UUID NOT NULL REFERENCES libro(id),
    fecha_adquisicion TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (buyer_id, libro_id)
);

CREATE INDEX idx_biblioteca_buyer ON biblioteca (buyer_id);
```

### 3.2 Entidad y Repositorio JPA

- `BibliotecaJpaEntity.java` con clave compuesta `(buyer_id, libro_id)`.
- `SpringBibliotecaRepository.java` extendiendo `JpaRepository`.

Métodos necesarios:
```java
boolean existsByBuyerIdAndLibroId(UUID buyerId, UUID libroId);

@Query("""
    SELECT new com.openlib.backend.infrastructure.persistence.jpa.LibroBibliotecaProjection(
        b.libroId, l.titulo, l.autor, l.portadaUrl, b.fechaAdquisicion
    )
    FROM BibliotecaJpaEntity b JOIN LibroJpaEntity l ON b.libroId = l.id
    WHERE b.buyerId = :buyerId
    ORDER BY :ordenarPor
""")
Page<LibroBibliotecaProjection> buscarPorBuyerIdOrdenado(
    @Param("buyerId") UUID buyerId,
    @Param("ordenarPor") String ordenarPor,
    Pageable pageable);
```

- `BibliotecaRepositoryAdapter.java` implementando `BibliotecaRepository` — reemplaza el adaptador provisional.

### 3.3 Reemplazo del Adaptador Provisional

Eliminar `BibliotecaRepositoryAdapterProvisional` creado en US-015 y US-018. Spring inyectará automáticamente `BibliotecaRepositoryAdapter` donde se usa la interface `BibliotecaRepository`.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/biblioteca-view.fxml`
- Grid de libros con portada (placeholder si no hay imagen), título, autor y fecha de adquisición.
- Selector de ordenamiento: "Más reciente" / "Más antiguo" / "Título A-Z" / "Título Z-A".
- Paginación en la parte inferior.
- Mensaje informativo si la biblioteca está vacía.
- Al hacer clic en un libro: navega a la vista de detalle del libro con el botón "Descargar" visible.

**Controlador:** `UI/controllers/BibliotecaController.java`
- Al cargar: invoca `VerBibliotecaUseCase`.
- Al cambiar el ordenamiento: reinvoca el caso de uso con el nuevo parámetro.
- Al cambiar de página: reinvoca con la nueva página.

---

## 5. Plan de Pruebas (TDD)

### `BibliotecaTest` (dominio)
- `agregar_conLibroNuevo_debeAgregarAlListado()`
- `agregar_conLibroDuplicado_debeLanzarLibroYaEnBibliotecaException()`
- `contiene_conLibroExistente_debeRetornarTrue()`
- `estaVacia_sinLibros_debeRetornarTrue()`

### `VerBibliotecaUseCaseTest` (con Mockito)
- `ejecutar_conLibrosAdquiridos_debeRetornarBibliotecaResponse()`
- `ejecutar_sinLibros_debeRetornarBibliotecaVacia()`
- `ejecutar_ordenandoPorFecha_debeRetornarOrdenados()`

### `BibliotecaRepositoryAdapterTest` (`@DataJpaTest`)
- `agregar_debeGuardarRelacionEnBD()`
- `existeEnBiblioteca_despuesDeAgregar_debeRetornarTrue()`
- `buscarPorBuyerId_debeRetornarLibrosConDatosDelLibro()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `VerBibliotecaUseCase` no conoce JPA ni PostgreSQL. `BibliotecaRepositoryAdapter` es el adaptador que conecta el dominio con la base de datos. |
| **DDD** | `Biblioteca` es un Aggregate Root del contexto `order`. `LibroBiblioteca` es un Value Object con los datos del libro al momento de la adquisición (desnormalizado). |
| **TDD** | Escribir `BibliotecaTest` antes de implementar la entidad. El adaptador provisional debe ser reemplazado, no reparado. |
| **SOLID (SRP)** | `BibliotecaRepositoryAdapter` reemplaza completamente el adaptador provisional. No hay lógica de negocio en el adaptador. |
| **SOLID (OCP)** | Al reemplazar el adaptador provisional por el real, el caso de uso `ConfirmarOrdenUseCase` (US-018) no necesita modificación. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear `LibroBiblioteca` (Value Object), `Biblioteca` (entidad) y `LibroYaEnBibliotecaException`.
- [ ] 2. Escribir `BibliotecaTest` (debe fallar).
- [ ] 3. Implementar la entidad `Biblioteca` y verificar tests.
- [ ] 4. Actualizar la interface `BibliotecaRepository` con todos los métodos necesarios.
- [ ] 5. Escribir `VerBibliotecaUseCaseTest` con Mockito (debe fallar).
- [ ] 6. Implementar `VerBibliotecaUseCase` y verificar.
- [ ] 7. Crear el script `V10__crear_tabla_biblioteca.sql`.
- [ ] 8. Crear `BibliotecaJpaEntity`, `SpringBibliotecaRepository` y `BibliotecaRepositoryAdapter`.
- [ ] 9. Eliminar los adaptadores provisionales de `BibliotecaRepository`.
- [ ] 10. Escribir y verificar `BibliotecaRepositoryAdapterTest`.
- [ ] 11. Crear `biblioteca-view.fxml` y `BibliotecaController.java`.
- [ ] 12. Verificar que `mvn verify` pasa completo.

# Plan de Implementación: Lista de Favoritos
**US-021 | Épica 5 – Favoritos, Historial & Recomendaciones**

---

## 1. Análisis y Modelado de Dominio

La lista de favoritos es una colección personal del Buyer que marca libros de interés sin implicar adquisición. Se introduce como una entidad propia del contexto `order` para no contaminar el dominio de `book` con conceptos del usuario.

### 1.1 Entidad `ListaFavoritos` (Aggregate Root)

**Archivo:** `domain/order/ListaFavoritos.java`

**Atributos:**
- `UUID buyerId`
- `List<ItemFavorito> items`

**Comportamientos:**
- `void agregar(ItemFavorito item)` — lanza `LibroYaEnFavoritosException` si ya existe.
- `void eliminar(UUID libroId)` — lanza `ItemFavoritoNoEncontradoException` si no existe.
- `boolean contiene(UUID libroId)`
- `boolean estaVacia()`

### 1.2 Value Object `ItemFavorito`

**Archivo:** `domain/order/ItemFavorito.java`
- `UUID libroId`
- `String titulo`
- `String autor`
- `String portadaUrl`
- `LocalDateTime fechaAgregado`

**Excepciones de dominio** (en `domain/order/exception/`):
- `LibroYaEnFavoritosException`
- `ItemFavoritoNoEncontradoException`

### 1.3 Interface `FavoritosRepository` (puerto de salida)

```java
// domain/order/FavoritosRepository.java
public interface FavoritosRepository {
    void agregar(UUID buyerId, UUID libroId);
    void eliminar(UUID buyerId, UUID libroId);
    boolean existeEnFavoritos(UUID buyerId, UUID libroId);
    Optional<ListaFavoritos> buscarPorBuyerId(UUID buyerId);
}
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `AgregarAFavoritosUseCase`

**DTO de entrada:** `AgregarFavoritoCommand` (`UUID buyerId`, `UUID libroId`)

**Flujo:**
1. Verificar rol `BUYER`.
2. Verificar que el libro existe y está `APROBADO` con `LibroRepository.buscarPorId()`.
3. Verificar que el libro no está ya en favoritos con `FavoritosRepository.existeEnFavoritos()`. Si lo está, lanzar `LibroYaEnFavoritosException`.
4. Guardar con `FavoritosRepository.agregar(buyerId, libroId)`.

### `EliminarDeFavoritosUseCase`

**Flujo:**
1. Verificar rol `BUYER`.
2. Verificar que el item existe en favoritos. Si no, lanzar `ItemFavoritoNoEncontradoException`.
3. Eliminar con `FavoritosRepository.eliminar(buyerId, libroId)`.

### `VerFavoritosUseCase`

**DTO de salida:** `FavoritosResponse` (`List<ItemFavoritoResponse>`, `boolean vacia`)

**Flujo:**
1. Verificar rol `BUYER`.
2. Recuperar con `FavoritosRepository.buscarPorBuyerId()`.
3. Si no existe lista, retornar respuesta vacía.

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V12__crear_tabla_favoritos.sql`

```sql
CREATE TABLE favorito (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id        UUID NOT NULL REFERENCES usuario(id),
    libro_id        UUID NOT NULL REFERENCES libro(id),
    fecha_agregado  TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (buyer_id, libro_id)
);

CREATE INDEX idx_favorito_buyer ON favorito (buyer_id);
```

### 3.2 Entidad y Repositorio JPA

- `FavoritoJpaEntity.java` con clave compuesta `(buyer_id, libro_id)`.
- `SpringFavoritosRepository.java` con `existsByBuyerIdAndLibroId()` y `deleteByBuyerIdAndLibroId()`.
- `FavoritosRepositoryAdapter.java` implementando `FavoritosRepository`.

En `buscarPorBuyerId`, usar una `@Query` con JOIN a `libro` para obtener `titulo`, `autor` y `portadaUrl` sin pasar por el dominio de `book`:

```java
@Query("""
    SELECT new com.openlib.backend.infrastructure.persistence.jpa.FavoritoProjection(
        f.libroId, l.titulo, l.autor, l.portadaUrl, f.fechaAgregado
    )
    FROM FavoritoJpaEntity f JOIN LibroJpaEntity l ON f.libroId = l.id
    WHERE f.buyerId = :buyerId
    ORDER BY f.fechaAgregado DESC
""")
List<FavoritoProjection> buscarPorBuyerId(@Param("buyerId") UUID buyerId);
```

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/favoritos-view.fxml`
- Grid de libros favoritos con portada, título, autor y fecha en que se agregó.
- Botón "Eliminar de favoritos" por cada item.
- Mensaje informativo si la lista está vacía.

**Controlador:** `UI/controllers/FavoritosController.java`
- Al cargar: invoca `VerFavoritosUseCase`.
- Al eliminar: invoca `EliminarDeFavoritosUseCase` y recarga.

**`DetalleLibroController.java`** (actualizado):
- Agrega botón "♥ Agregar a favoritos" / "♥ En favoritos" (toggle visual).
- Al hacer clic en agregar: invoca `AgregarAFavoritosUseCase`.
- Al hacer clic en eliminar: invoca `EliminarDeFavoritosUseCase`.

---

## 5. Plan de Pruebas (TDD)

### `ListaFavoritosTest` (dominio)
- `agregar_conItemNuevo_debeAgregarAlListado()`
- `agregar_conItemDuplicado_debeLanzarLibroYaEnFavoritosException()`
- `eliminar_conItemExistente_debeRemoverDelListado()`
- `eliminar_conItemInexistente_debeLanzarItemFavoritoNoEncontradoException()`
- `estaVacia_sinItems_debeRetornarTrue()`

### `AgregarAFavoritosUseCaseTest` (con Mockito)
- `ejecutar_conLibroAprobadoYNoDuplicado_debeGuardar()`
- `ejecutar_conLibroYaEnFavoritos_debeLanzarLibroYaEnFavoritosException()`
- `ejecutar_conLibroNoAprobado_debeLanzarLibroNoDisponibleException()`

### `FavoritosRepositoryAdapterTest` (`@DataJpaTest`)
- `agregar_debeGuardarRelacionEnBD()`
- `existeEnFavoritos_despuesDeAgregar_debeRetornarTrue()`
- `eliminar_debeRemoverRelacionDeBD()`
- `buscarPorBuyerId_debeRetornarItemsConDatosDelLibro()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Los casos de uso de favoritos no importan nada de JPA ni JavaFX. Dependen solo de `FavoritosRepository` y `LibroRepository` (interfaces). |
| **DDD** | `ListaFavoritos` es un Aggregate Root del contexto `order`. Los datos del libro (`titulo`, `autor`, `portadaUrl`) se desnormalizan en `ItemFavorito` para no cruzar contextos. |
| **TDD** | Escribir `ListaFavoritosTest` antes de implementar la entidad. Los tests del repositorio con `@DataJpaTest` verifican la restricción `UNIQUE`. |
| **SOLID (SRP)** | Tres casos de uso separados: agregar, eliminar y ver. Cada uno con una sola responsabilidad. |
| **SOLID (ISP)** | `FavoritosRepository` tiene solo los métodos que necesitan los casos de uso de esta historia. No sobrecargar con métodos de otras funcionalidades. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear `ItemFavorito` (Value Object) y las excepciones en `domain/order/`.
- [ ] 2. Escribir `ListaFavoritosTest` (debe fallar).
- [ ] 3. Crear la entidad `ListaFavoritos` y verificar tests.
- [ ] 4. Definir la interface `FavoritosRepository` en `domain/order/`.
- [ ] 5. Escribir los tests de los tres casos de uso con Mockito (deben fallar).
- [ ] 6. Implementar `AgregarAFavoritosUseCase`, `EliminarDeFavoritosUseCase` y `VerFavoritosUseCase`.
- [ ] 7. Verificar que los tests pasan.
- [ ] 8. Crear el script `V12__crear_tabla_favoritos.sql`.
- [ ] 9. Crear `FavoritoJpaEntity`, `SpringFavoritosRepository` y `FavoritosRepositoryAdapter`.
- [ ] 10. Escribir y verificar `FavoritosRepositoryAdapterTest`.
- [ ] 11. Crear `favoritos-view.fxml` y `FavoritosController.java`.
- [ ] 12. Actualizar `DetalleLibroController.java` con el toggle de favoritos.
- [ ] 13. Verificar que `mvn verify` pasa completo.

# Plan de Implementación: Recomendaciones Personalizadas de Libros
**US-023 | Épica 5 – Favoritos, Historial & Recomendaciones**

---

## 1. Análisis y Modelado de Dominio

El motor de recomendaciones opera con base en las categorías y etiquetas de los libros adquiridos por el Buyer. Es un caso de uso de solo lectura que agrega información de dos contextos (`order` y `book`) sin romper los límites entre ellos.

### 1.1 Value Object `Recomendacion`

**Archivo:** `domain/order/Recomendacion.java`
- `UUID libroId`
- `String titulo`
- `String autor`
- `String categoria`
- `String portadaUrl`
- `double calificacionPromedio`

### 1.2 Interface `RecomendacionRepository` (puerto de salida)

```java
// domain/order/RecomendacionRepository.java
public interface RecomendacionRepository {
    /**
     * Retorna hasta maxResultados libros aprobados que pertenezcan a alguna de
     * las categorías indicadas, excluyendo los libros ya adquiridos por el buyer.
     */
    List<Recomendacion> buscarPorCategorias(
        List<String> categorias,
        List<UUID> librosExcluidos,
        int maxResultados
    );

    /**
     * Retorna los libros más descargados (populares) cuando no hay historial.
     */
    List<Recomendacion> buscarMasPopulares(int maxResultados);
}
```

---

## 2. Capa de Aplicación (Caso de Uso)

### `ObtenerRecomendacionesUseCase`

**DTO de entrada:** `RecomendacionesQuery` (`UUID buyerId`)
**DTO de salida:** `List<RecomendacionResponse>` (máximo 10 elementos)

**Flujo:**
1. Verificar rol `BUYER`.
2. Recuperar las órdenes del buyer con `OrdenRepository.buscarPorBuyerIdOrdenadas()` (sin paginación, solo para extraer los libros adquiridos).
3. Si el buyer **no tiene historial**: delegar a `RecomendacionRepository.buscarMasPopulares(10)` y retornar.
4. Si **tiene historial**:
   a. Extraer la lista de `libroId` adquiridos (para excluirlos).
   b. Extraer las categorías de los libros adquiridos (con `LibroRepository.buscarCategoriasPorIds(librosAdquiridos)`).
   c. Llamar `RecomendacionRepository.buscarPorCategorias(categorias, librosAdquiridos, 10)`.
5. Retornar la lista mapeada a `RecomendacionResponse`.

### Nuevo método en `LibroRepository`

```java
List<String> buscarCategoriasPorIds(List<UUID> libroIds);
```

---

## 3. Capa de Infraestructura

### 3.1 Implementación de `RecomendacionRepository`

**`PostgresRecomendacionRepository.java`** (en `infrastructure/persistence/`):
- Implementa `RecomendacionRepository`.
- Usa `SpringLibroRepository` con queries nativas para:
  - Buscar por categorías excluyendo IDs:

```java
@Query("""
    SELECT l FROM LibroJpaEntity l
    WHERE l.estado = 'APROBADO'
    AND l.categoria IN :categorias
    AND l.id NOT IN :librosExcluidos
    ORDER BY RANDOM()
""")
List<LibroJpaEntity> buscarPorCategoriasExcluyendo(
    @Param("categorias") List<String> categorias,
    @Param("librosExcluidos") List<UUID> librosExcluidos,
    Pageable pageable);
```

  - Buscar más populares (ordenados por número de descargas):

```java
@Query("""
    SELECT l FROM LibroJpaEntity l
    LEFT JOIN RegistroDescargaJpaEntity rd ON rd.libroId = l.id
    WHERE l.estado = 'APROBADO'
    GROUP BY l.id
    ORDER BY COUNT(rd.id) DESC
""")
List<LibroJpaEntity> buscarMasPopulares(Pageable pageable);
```

### 3.2 Caché de recomendaciones

Las recomendaciones son costosas de calcular. Aplicar caché en Redis con TTL de 10 minutos por `buyerId`:
- Clave: `recomendaciones:{buyerId}`.
- Invalidar cuando el buyer confirma una nueva orden (en `ConfirmarOrdenUseCase`).

Extender `CatalogoCache` o crear una nueva interface `RecomendacionCache`:
```java
public interface RecomendacionCache {
    Optional<List<Recomendacion>> obtener(UUID buyerId);
    void guardar(UUID buyerId, List<Recomendacion> recomendaciones, int ttlMinutos);
    void invalidar(UUID buyerId);
}
```

---

## 4. Capa de UI (JavaFX)

**`buyer-view.fxml`** (actualizado — nueva sección "Para ti"):
- Sección horizontal de libros recomendados con portada, título y categoría.
- Máximo 10 tarjetas visibles; scroll horizontal si hay más de las que caben en pantalla.
- Si la lista está vacía, no se muestra la sección.

**`BuyerController.java`** (actualizado):
- Al cargar la vista de inicio: invoca `ObtenerRecomendacionesUseCase` en un hilo separado para no bloquear la UI.
- Al hacer clic en una recomendación: navega a `DetalleLibroController`.

---

## 5. Plan de Pruebas (TDD)

### `ObtenerRecomendacionesUseCaseTest` (con Mockito)
- `ejecutar_sinHistorial_debeRetornarLibrosMasPopulares()`
- `ejecutar_conHistorial_debeRetornarRecomendacionesPorCategoria()`
- `ejecutar_conHistorial_debeExcluirLibrosYaAdquiridos()`
- `ejecutar_conResultadoEnCache_debeRetornarSinConsultarRepo()`
- `ejecutar_sinCache_debeGuardarResultadoEnCache()`

### `PostgresRecomendacionRepositoryTest` (`@DataJpaTest`)
- `buscarPorCategorias_debeRetornarLibrosDeEsasCategoriasExcluyendoAdquiridos()`
- `buscarMasPopulares_debeRetornarOrdenadosPorDescargas()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `ObtenerRecomendacionesUseCase` agrega datos de `OrdenRepository` y `RecomendacionRepository`, ambas interfaces. No cruza contextos directamente; usa los repositorios como puentes. |
| **DDD** | `Recomendacion` es un Value Object de lectura del contexto `order`. Los datos del libro se obtienen a través de `RecomendacionRepository`, no cruzando directamente al dominio `book`. |
| **TDD** | Escribir todos los tests del caso de uso antes de implementarlo. La lógica de "sin historial → populares" debe verificarse explícitamente. |
| **SOLID (SRP)** | `ObtenerRecomendacionesUseCase` no decide el algoritmo de recomendación; lo delega a `RecomendacionRepository`. Si el algoritmo cambia, solo cambia el repositorio. |
| **SOLID (OCP)** | Si en el futuro el algoritmo cambia (ej. también considerar etiquetas), se puede extender `RecomendacionRepository` con un nuevo método sin modificar el caso de uso. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear el Value Object `Recomendacion` en `domain/order/`.
- [ ] 2. Definir las interfaces `RecomendacionRepository` y `RecomendacionCache` en `domain/order/`.
- [ ] 3. Agregar `buscarCategoriasPorIds()` a `LibroRepository`.
- [ ] 4. Escribir `ObtenerRecomendacionesUseCaseTest` con Mockito (debe fallar).
- [ ] 5. Implementar `ObtenerRecomendacionesUseCase` y verificar.
- [ ] 6. Implementar `PostgresRecomendacionRepository` con las queries necesarias.
- [ ] 7. Implementar `RedisRecomendacionCache`.
- [ ] 8. Escribir y verificar `PostgresRecomendacionRepositoryTest`.
- [ ] 9. Actualizar `ConfirmarOrdenUseCase` para invalidar el caché al confirmar una orden.
- [ ] 10. Actualizar `buyer-view.fxml` con la sección de recomendaciones.
- [ ] 11. Actualizar `BuyerController.java` para cargar recomendaciones asíncronamente.
- [ ] 12. Verificar que `mvn verify` pasa completo.

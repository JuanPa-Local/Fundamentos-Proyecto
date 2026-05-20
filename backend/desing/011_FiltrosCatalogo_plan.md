# Plan de Implementación: Filtros Avanzados del Catálogo
**US-011 | Épica 2 – Catálogo de Libros**

---

## 1. Análisis y Modelado de Dominio

Esta historia extiende `FiltroCatalogo` (creado en US-010) con campos adicionales para categoría y etiquetas. La lógica de filtrado combinado vive en la capa de infraestructura mediante `Specification` de JPA, mientras que el dominio solo conoce los parámetros de búsqueda como Value Objects.

### 1.1 Extensión de `FiltroCatalogo`

**Archivo:** `domain/book/FiltroCatalogo.java` (actualizado)
- `String termino` — búsqueda por título, autor, ISBN (ya existía)
- `String categoria` — filtro exacto por categoría (nuevo)
- `List<String> etiquetas` — filtro por al menos una etiqueta coincidente (nuevo)
- `int pagina`, `int tamano` — paginación (ya existían)

El Value Object sigue siendo inmutable. Se construye con un Builder para facilitar su creación con combinaciones parciales de filtros.

---

## 2. Capa de Aplicación (Caso de Uso)

`BuscarCatalogoUseCase` (creado en US-010) **no necesita modificación** en su lógica principal. Solo se le pasa un `FiltroCatalogo` más completo. El caso de uso delega a `LibroRepository.buscarAprobados()` la responsabilidad de interpretar todos los filtros.

El único cambio es en la **construcción de la clave de caché**, que ahora debe incluir los campos `categoria` y `etiquetas` para diferenciar entre búsquedas distintas:
- Clave: `catalogo:{termino}:{categoria}:{etiquetas_hash}:{pagina}:{tamano}`

---

## 3. Capa de Infraestructura

### 3.1 Búsqueda dinámica con Specification

**`LibroSpecification.java`** (en `infrastructure/persistence/jpa/`):

```java
public class LibroSpecification {

    public static Specification<LibroJpaEntity> soloAprobados() {
        return (root, q, cb) -> cb.equal(root.get("estado"), "APROBADO");
    }

    public static Specification<LibroJpaEntity> conTermino(String termino) {
        return (root, q, cb) -> cb.or(
            cb.like(cb.lower(root.get("titulo")), "%" + termino.toLowerCase() + "%"),
            cb.like(cb.lower(root.get("autor")), "%" + termino.toLowerCase() + "%"),
            cb.like(root.get("isbn"), "%" + termino + "%")
        );
    }

    public static Specification<LibroJpaEntity> conCategoria(String categoria) {
        return (root, q, cb) -> cb.equal(root.get("categoria"), categoria);
    }

    public static Specification<LibroJpaEntity> conEtiquetas(List<String> etiquetas) {
        // Usa función ANY de PostgreSQL para arrays
        return (root, q, cb) -> root.get("etiquetas").in(etiquetas);
    }
}
```

**`LibroRepositoryAdapter`** (actualizado):
- Compone las especificaciones dinámicamente según los campos no nulos de `FiltroCatalogo`.

```java
Specification<LibroJpaEntity> spec = LibroSpecification.soloAprobados();
if (filtro.getTermino() != null)   spec = spec.and(conTermino(filtro.getTermino()));
if (filtro.getCategoria() != null) spec = spec.and(conCategoria(filtro.getCategoria()));
if (!filtro.getEtiquetas().isEmpty()) spec = spec.and(conEtiquetas(filtro.getEtiquetas()));
```

### 3.2 Invalidación del caché al modificar filtros

La interface `CatalogoCache.invalidar(patron)` (ya definida en US-010) se llama con `"catalogo:*"` cada vez que se aprueba, rechaza o modifica un libro (US-014), garantizando que los filtros no devuelvan datos obsoletos.

---

## 4. Capa de UI (JavaFX)

**`buyer-view.fxml`** (actualizado):
- Panel lateral o superior con filtros adicionales:
  - `ComboBox<String>` para categoría (cargado desde un servicio).
  - `CheckBox` por cada etiqueta disponible (o `ListView` con selección múltiple).
  - Botón "Aplicar filtros" y botón "Limpiar filtros".

**`BuyerController.java`** (actualizado):
- Al aplicar filtros: construye un `FiltroCatalogo` con los valores seleccionados e invoca `BuscarCatalogoUseCase`.
- Al limpiar filtros: reinicia todos los controles y recarga el catálogo con `FiltroCatalogo` vacío.

---

## 5. Plan de Pruebas (TDD)

### `FiltroCatalogoTest` (dominio)
- `construir_conSoloCategoria_debeCrearFiltroConCamposNulos()`
- `construir_conCategoriaYEtiquetas_debeIncluirAmbosParametros()`

### `LibroRepositoryAdapterTest` (ampliado — `@DataJpaTest`)
- `buscarAprobados_filtrandoPorCategoria_debeRetornarSoloEsaCategoria()`
- `buscarAprobados_filtrandoPorEtiqueta_debeRetornarLibrosConEsaEtiqueta()`
- `buscarAprobados_combinandoCategoriaYTermino_debeAplicarAmbosParametros()`
- `buscarAprobados_sinFiltros_debeRetornarTodosLosAprobados()`

### `BuscarCatalogoUseCaseTest` (ampliado — con Mockito)
- `ejecutar_conFiltroCompleto_debeConstruirClaveCorrectaYConsultarRepo()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `LibroSpecification` vive en `infrastructure/persistence/jpa/`. El dominio solo conoce `FiltroCatalogo`; nunca `Specification` ni `CriteriaBuilder`. |
| **DDD** | `FiltroCatalogo` usa el patrón Builder para facilitar la construcción incremental. El Builder es parte del Value Object en el dominio. |
| **TDD** | Los tests de `LibroRepositoryAdapter` con filtros combinados deben escribirse antes de implementar `LibroSpecification`. |
| **SOLID (OCP)** | `LibroSpecification` está abierta a extensión: agregar un nuevo filtro (ej. rango de fechas) significa agregar un nuevo método estático sin modificar los existentes. |
| **SOLID (SRP)** | `LibroSpecification` es responsable exclusivamente de construir criterios de búsqueda JPA. `LibroRepositoryAdapter` es responsable de componerlos y ejecutar la consulta. |

---

## 📋 Tareas de Implementación

- [ ] 1. `FiltroCatalogo` con Builder no creado *(filtros implementados como parámetros simples)*.
- [ ] 2. `FiltroCatalogoTest` no creado; tests de repositorio con filtros pendientes.
- [ ] 3. `LibroSpecification.java` no creado *(filtros implementados con queries JPQL directas en el repositorio)*.
- [x] 4. `BookRepository.searchByTerminoAndCategory()` y `findByCategoryAndStatus()` implementados; `BookService.searchCatalogWithFilters()` compone los filtros dinámicamente.
- [ ] 5. Tests de repositorio pendientes.
- [ ] 6. Test de `BuscarCatalogoUseCase` pendiente.
- [x] 7. `buyer-view.fxml` existe con campo de búsqueda; panel de filtros de categoría pendiente de UI.
- [x] 8. `GET /api/books/catalog/filter?q=&category=` implementado en `BookController`.
- [ ] 9. `mvn verify` pendiente.
- [ ] 9. Verificar que `mvn verify` pasa completo.

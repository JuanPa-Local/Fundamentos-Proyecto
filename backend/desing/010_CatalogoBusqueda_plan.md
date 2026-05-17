# Plan de Implementación: Exploración del Catálogo con Búsqueda
**US-010 | Épica 2 – Catálogo de Libros**

---

## 1. Análisis y Modelado de Dominio

El catálogo expone únicamente libros en estado `APROBADO`. La búsqueda opera sobre título, autor e ISBN de forma simultánea. El rendimiento es un requisito no funcional crítico: la respuesta debe ser inferior a 1.5 segundos, lo que exige el uso de caché en Redis.

### 1.1 Value Object `FiltroCatalogo`

**Archivo:** `domain/book/FiltroCatalogo.java`
- `String termino` — texto libre para buscar en título, autor o ISBN (nullable)
- `int pagina` — número de página (default 0)
- `int tamano` — elementos por página (default 20)

Encapsula los parámetros de búsqueda sin exponer detalles de JPA al dominio.

### 1.2 Value Object `ResultadoCatalogo`

**Archivo:** `domain/book/ResultadoCatalogo.java`
- `List<LibroResumen> libros`
- `int totalPaginas`
- `long totalElementos`
- `int paginaActual`

### 1.3 Record `LibroResumen`

**Archivo:** `domain/book/LibroResumen.java`
- `UUID id`, `String titulo`, `String autor`, `String isbn`, `String categoria`, `String estado`

Proyección ligera del `Libro` completo para el listado del catálogo.

---

## 2. Capa de Aplicación (Caso de Uso)

### `BuscarCatalogoUseCase`

**DTO de entrada:** `FiltroCatalogo`
**DTO de salida:** `ResultadoCatalogo`

**Flujo:**
1. Construir una clave de caché a partir del `FiltroCatalogo` serializado.
2. Consultar en `CatalogoCache.obtener(clave)`. Si hay un resultado en caché, retornarlo directamente.
3. Si no hay caché, ejecutar `LibroRepository.buscarAprobados(filtro, pageable)`.
4. Guardar el resultado en caché con `CatalogoCache.guardar(clave, resultado, ttlSegundos)`.
5. Retornar el `ResultadoCatalogo`.

### Interface `CatalogoCache` (puerto de salida)

```java
// domain/book/CatalogoCache.java
public interface CatalogoCache {
    Optional<ResultadoCatalogo> obtener(String clave);
    void guardar(String clave, ResultadoCatalogo resultado, int ttlSegundos);
    void invalidar(String patron); // llamado al aprobar/modificar un libro
}
```

---

## 3. Capa de Infraestructura

### 3.1 Búsqueda en PostgreSQL

**`SpringLibroRepository`** (actualizado):
```java
@Query("""
    SELECT l FROM LibroJpaEntity l
    WHERE l.estado = 'APROBADO'
    AND (
        LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termino, '%'))
        OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :termino, '%'))
        OR l.isbn LIKE CONCAT('%', :termino, '%')
    )
""")
Page<LibroJpaEntity> buscarAprobadosPorTermino(
    @Param("termino") String termino, Pageable pageable);
```

Agregar índices en la migración para optimizar la búsqueda:

**Archivo nuevo:** `V6__indices_catalogo.sql`
```sql
CREATE INDEX idx_libro_titulo  ON libro (titulo);
CREATE INDEX idx_libro_autor   ON libro (autor);
CREATE INDEX idx_libro_isbn    ON libro (isbn);
CREATE INDEX idx_libro_estado  ON libro (estado);
```

### 3.2 Implementación de `CatalogoCache` con Redis

**`RedisCatalogoCache.java`** (en `infrastructure/cache/`):
- Usa `RedisTemplate<String, ResultadoCatalogo>`.
- La clave sigue el patrón: `catalogo:{termino}:{pagina}:{tamano}`.
- TTL configurable en `application.yml`:

```yaml
openlib:
  cache:
    catalogo-ttl-segundos: 300  # 5 minutos
```

---

## 4. Capa de UI (JavaFX)

**Archivo FXML:** `resources/views/buyer-view.fxml` (actualizado)
- Campo de búsqueda en la parte superior.
- Tabla/grid de libros con: portada (placeholder si no hay imagen), título, autor, ISBN.
- Paginación en la parte inferior: "Anterior", número de página actual, "Siguiente".
- El catálogo carga automáticamente al abrir la vista con todos los libros aprobados.

**Controlador:** `BuyerController.java` (actualizado)
- Al abrir: invoca `BuscarCatalogoUseCase` con `FiltroCatalogo` vacío.
- Al escribir en el campo de búsqueda: debounce de 400ms antes de invocar el caso de uso.
- Al cambiar de página: invoca el caso de uso con el nuevo número de página.

---

## 5. Plan de Pruebas (TDD)

### `BuscarCatalogoUseCaseTest` (con Mockito)
- `ejecutar_sinTermino_debeRetornarTodosLosAprobados()`
- `ejecutar_conTermino_debeRetornarLibrosQueCoinciden()`
- `ejecutar_conResultadoEnCache_debeRetornarSinConsultarBD()`
- `ejecutar_sinCache_debeConsultarBDYGuardarEnCache()`

### `RedisCatalogoCacheTest` (integración con TestContainers Redis)
- `guardar_debeAlmacenarResultadoConTTL()`
- `obtener_conClaveExistente_debeRetornarResultado()`
- `obtener_conClaveVencida_debeRetornarEmpty()`
- `invalidar_debeEliminarClavesQueCoinciden()`

### `LibroRepositoryAdapterTest` (ampliado)
- `buscarAprobados_sinTermino_debeRetornarSoloAprobados()`
- `buscarAprobados_conTerminoPorTitulo_debeRetornarCoincidencias()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `BuscarCatalogoUseCase` no depende de Redis ni de JPA. Depende de las interfaces `LibroRepository` y `CatalogoCache`. |
| **DDD** | `FiltroCatalogo` y `ResultadoCatalogo` son Value Objects del dominio `book`. No exponer entidades JPA ni tipos de Redis en el dominio. |
| **TDD** | Escribir `BuscarCatalogoUseCaseTest` antes de implementar el caso de uso. El test de caché debe estar aislado del test de repositorio. |
| **SOLID (SRP)** | `RedisCatalogoCache` solo gestiona el caché. `LibroRepositoryAdapter` solo persiste datos. No mezclar responsabilidades. |
| **SOLID (OCP)** | `CatalogoCache` es una interface. Si en el futuro se reemplaza Redis por Memcached, solo se crea un nuevo adaptador sin modificar el caso de uso. |
| **SOLID (DIP)** | `BuscarCatalogoUseCase` recibe `CatalogoCache` y `LibroRepository` por inyección de dependencias. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear los Value Objects `FiltroCatalogo`, `ResultadoCatalogo` y el record `LibroResumen` en `domain/book/`.
- [ ] 2. Definir la interface `CatalogoCache` en `domain/book/`.
- [ ] 3. Agregar `buscarAprobados()` a la interface `LibroRepository`.
- [ ] 4. Escribir `BuscarCatalogoUseCaseTest` con Mockito (debe fallar).
- [ ] 5. Implementar `BuscarCatalogoUseCase`.
- [ ] 6. Verificar que los tests pasan.
- [ ] 7. Crear el script `V6__indices_catalogo.sql`.
- [ ] 8. Actualizar `SpringLibroRepository` con la query de búsqueda por término.
- [ ] 9. Implementar `RedisCatalogoCache` en `infrastructure/cache/`.
- [ ] 10. Escribir y verificar `RedisCatalogoCacheTest` con TestContainers.
- [ ] 11. Ampliar `LibroRepositoryAdapterTest` y verificar.
- [ ] 12. Actualizar `buyer-view.fxml` y `BuyerController.java` con la búsqueda y paginación.
- [ ] 13. Verificar que `mvn verify` pasa completo con tiempo de respuesta menor a 1.5s en prueba de carga.

# Plan de Implementación: Panel de Libros Más Descargados
**US-026 | Épica 7 – Dashboard de Métricas (Admin)**

---

## 1. Análisis y Modelado de Dominio

Las métricas son un caso de uso de solo lectura que agrega datos del `RegistroDescarga` (creado en US-020). No se introduce lógica de negocio nueva; se crea una capa de consulta (query side) que sirve los datos al dashboard del administrador.

### 1.1 Enumeración `PeriodoMetrica`

**Archivo:** `domain/metrics/PeriodoMetrica.java`
- Valores: `SIETE_DIAS`, `TREINTA_DIAS`, `HISTORICO`

### 1.2 Value Object `LibroDescargado`

**Archivo:** `domain/metrics/LibroDescargado.java`
- `UUID libroId`
- `String titulo`
- `String categoria`
- `long totalDescargas`
- `int posicion` — ranking (1 al 10)

### 1.3 Interface `MetricasDescargaRepository` (puerto de salida)

```java
// domain/metrics/MetricasDescargaRepository.java
public interface MetricasDescargaRepository {
    List<LibroDescargado> buscarMasDescargados(PeriodoMetrica periodo, int maxResultados);
}
```

---

## 2. Capa de Aplicación (Caso de Uso)

### `ObtenerRankingDescargasUseCase`

**DTO de entrada:** `RankingDescargasQuery` (`UUID adminId`, `PeriodoMetrica periodo`)
**DTO de salida:** `RankingDescargasResponse`
- `List<LibroDescargadoResponse>` — máximo 10 elementos
- `PeriodoMetrica periodo`
- `boolean sinDatos` — `true` si no hay registros para el período

**Flujo:**
1. Verificar rol `ADMIN`.
2. Llamar `MetricasDescargaRepository.buscarMasDescargados(periodo, 10)`.
3. Si la lista está vacía, retornar con `sinDatos = true`.
4. Asignar `posicion` a cada elemento (1 al N según el orden retornado).
5. Retornar `RankingDescargasResponse`.

---

## 3. Capa de Infraestructura

### 3.1 Implementación de `MetricasDescargaRepository`

**`PostgresMetricasDescargaRepository.java`** (en `infrastructure/persistence/`):

```java
@Query(value = """
    SELECT
        l.id           AS libro_id,
        l.titulo       AS titulo,
        l.categoria    AS categoria,
        COUNT(rd.id)   AS total_descargas
    FROM registro_descarga rd
    JOIN libro l ON rd.libro_id = l.id
    WHERE (:fechaCorte IS NULL OR rd.timestamp >= :fechaCorte)
    GROUP BY l.id, l.titulo, l.categoria
    ORDER BY total_descargas DESC
    LIMIT :limite
""", nativeQuery = true)
List<Object[]> buscarMasDescargados(
    @Param("fechaCorte") LocalDateTime fechaCorte,
    @Param("limite") int limite);
```

La lógica para convertir `PeriodoMetrica` en `fechaCorte`:
```
SIETE_DIAS    → LocalDateTime.now().minusDays(7)
TREINTA_DIAS  → LocalDateTime.now().minusDays(30)
HISTORICO     → null (sin filtro de fecha)
```

### 3.2 Caché de métricas

Las métricas de descarga son costosas de calcular con grandes volúmenes de datos. Aplicar caché en Redis:
- Clave: `metricas:ranking-descargas:{periodo}`.
- TTL: 30 minutos.

```java
// domain/metrics/MetricasCache.java
public interface MetricasCache {
    Optional<RankingDescargasResponse> obtenerRanking(PeriodoMetrica periodo);
    void guardarRanking(PeriodoMetrica periodo, RankingDescargasResponse respuesta, int ttlMinutos);
}
```

---

## 4. Capa de UI (JavaFX)

**`admin-view.fxml`** (nueva sección "Dashboard — Descargas"):
- Selector de período: radio buttons "Últimos 7 días", "Últimos 30 días", "Histórico".
- Tabla de ranking con columnas: Posición, Título, Categoría, Total de descargas.
- Mensaje "Sin datos para el período seleccionado" cuando la lista está vacía.
- Botón "Actualizar" para forzar recarga (invalida caché y recarga).

**`AdminController.java`** (actualizado):
- Al cargar la sección de métricas: invoca `ObtenerRankingDescargasUseCase` con período `SIETE_DIAS` por defecto.
- Al cambiar el período: reinvoca el caso de uso.
- Al hacer clic en "Actualizar": invalida el caché e invoca el caso de uso.

---

## 5. Plan de Pruebas (TDD)

### `ObtenerRankingDescargasUseCaseTest` (con Mockito)
- `ejecutar_conDatosEnPeriodo_debeRetornarRankingOrdenado()`
- `ejecutar_sinDatosEnPeriodo_debeRetornarRespuestaVacia()`
- `ejecutar_conDatosEnCache_debeRetornarSinConsultarRepo()`
- `ejecutar_sinRolAdmin_debeLanzarAccesoDenegadoException()`

### `PostgresMetricasDescargaRepositoryTest` (`@DataJpaTest`)
- `buscarMasDescargados_conPeriodoSieteDias_debeRetornarSoloDescargasDelPeriodo()`
- `buscarMasDescargados_conPeriodoHistorico_debeRetornarTodas()`
- `buscarMasDescargados_debeRetornarMaximoNResultados()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `ObtenerRankingDescargasUseCase` pertenece al contexto `metrics` (nuevo paquete). No importa nada de `infrastructure/`. Depende de `MetricasDescargaRepository` y `MetricasCache` (interfaces). |
| **DDD** | Se introduce el contexto `metrics` como un contexto de solo lectura. No modifica datos; solo los agrega y presenta. `LibroDescargado` es un Value Object de este contexto. |
| **TDD** | Escribir `ObtenerRankingDescargasUseCaseTest` antes de implementar el caso de uso. El test de caché debe ser explícito. |
| **SOLID (SRP)** | `PostgresMetricasDescargaRepository` es responsable exclusivamente de calcular el ranking. La lógica de conversión de período a fecha es responsabilidad del adaptador, no del caso de uso. |
| **SOLID (OCP)** | Agregar un nuevo período (ej. `NOVENTA_DIAS`) solo requiere agregar un valor al enum `PeriodoMetrica` y un caso en el switch del adaptador, sin modificar el caso de uso. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear el paquete `domain/metrics/` con el Enum `PeriodoMetrica` y el Value Object `LibroDescargado`.
- [ ] 2. Definir las interfaces `MetricasDescargaRepository` y `MetricasCache` en `domain/metrics/`.
- [ ] 3. Escribir `ObtenerRankingDescargasUseCaseTest` con Mockito (debe fallar).
- [ ] 4. Implementar `ObtenerRankingDescargasUseCase` y verificar.
- [ ] 5. Implementar `PostgresMetricasDescargaRepository` con la query nativa.
- [ ] 6. Implementar `RedisMetricasCache` para el caché del ranking.
- [ ] 7. Escribir y verificar `PostgresMetricasDescargaRepositoryTest`.
- [ ] 8. Actualizar `admin-view.fxml` con la sección de dashboard de descargas.
- [ ] 9. Actualizar `AdminController.java` con la lógica de carga por período.
- [ ] 10. Verificar que `mvn verify` pasa completo.

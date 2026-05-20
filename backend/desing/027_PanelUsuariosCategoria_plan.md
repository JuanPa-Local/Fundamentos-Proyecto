# Plan de Implementación: Panel de Usuarios Activos y Categorías Populares
**US-027 | Épica 7 – Dashboard de Métricas (Admin)**

---

## 1. Análisis y Modelado de Dominio

Esta historia extiende el contexto `metrics` (introducido en US-026) con dos nuevas métricas: usuarios activos por mes y categorías más populares. Se reutiliza la infraestructura de caché de métricas ya creada.

### 1.1 Value Object `MetricaUsuariosActivos`

**Archivo:** `domain/metrics/MetricaUsuariosActivos.java`
- `int mesActual` — número del mes (1-12)
- `int anioActual`
- `long usuariosActivosMesActual`
- `long usuariosActivosMesAnterior`
- `double variacionPorcentual` — calculado: `((actual - anterior) / anterior) * 100`

### 1.2 Value Object `CategoriaPopular`

**Archivo:** `domain/metrics/CategoriaPopular.java`
- `String categoria`
- `long totalAdquisicionesYDescargas`
- `long totalMesAnterior`
- `double variacionPorcentual`

### 1.3 Interfaces de Repositorio (extensión de `domain/metrics/`)

```java
// domain/metrics/MetricasUsuarioRepository.java
public interface MetricasUsuarioRepository {
    long contarUsuariosActivos(LocalDate inicio, LocalDate fin);
}

// domain/metrics/MetricasCategoriaRepository.java
public interface MetricasCategoriaRepository {
    List<CategoriaPopular> buscarCategoriasPopulares(LocalDate inicio, LocalDate fin, int maxResultados);
}
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `ObtenerMetricasUsuariosUseCase`

**DTO de salida:** `MetricaUsuariosResponse`
- `long mesActual`, `long mesAnterior`, `double variacionPorcentual`
- `String mesNombre`, `int anio`

**Flujo:**
1. Verificar rol `ADMIN`.
2. Calcular el rango de fechas del mes actual y el mes anterior.
3. Consultar `MetricasUsuarioRepository.contarUsuariosActivos()` para cada rango.
4. Calcular la variación porcentual.
5. Construir y retornar `MetricaUsuariosActivos` → `MetricaUsuariosResponse`.

### `ObtenerCategoriasPopularesUseCase`

**DTO de salida:** `CategoriasPopularesResponse`
- `List<CategoriaPopularResponse>` — máximo 10, con `categoria`, `total`, `variacion`
- `boolean sinDatos`

**Flujo:**
1. Verificar rol `ADMIN`.
2. Calcular rangos de fechas para mes actual y anterior.
3. Llamar `MetricasCategoriaRepository.buscarCategoriasPopulares()` para cada rango.
4. Combinar los resultados y calcular la variación para cada categoría.
5. Retornar los primeros 10 resultados ordenados por `totalMesActual` descendente.

---

## 3. Capa de Infraestructura

### 3.1 Implementación de `MetricasUsuarioRepository`

**`PostgresMetricasUsuarioRepository.java`**:

Un usuario se considera "activo" si ha iniciado sesión o realizado alguna acción registrada en el período. Se usa la tabla `registro_descarga` como proxy de actividad, o bien se agrega una tabla `sesion_log`:

```java
@Query(value = """
    SELECT COUNT(DISTINCT buyer_id)
    FROM registro_descarga
    WHERE timestamp BETWEEN :inicio AND :fin
""", nativeQuery = true)
long contarUsuariosActivos(
    @Param("inicio") LocalDateTime inicio,
    @Param("fin") LocalDateTime fin);
```

### 3.2 Implementación de `MetricasCategoriaRepository`

**`PostgresMetricasCategoriaRepository.java`**:

```java
@Query(value = """
    SELECT
        l.categoria,
        COUNT(oi.libro_id) + COUNT(rd.id) AS total
    FROM libro l
    LEFT JOIN orden_item oi ON oi.libro_id = l.id
        AND oi.created_at BETWEEN :inicio AND :fin
    LEFT JOIN registro_descarga rd ON rd.libro_id = l.id
        AND rd.timestamp BETWEEN :inicio AND :fin
    WHERE l.estado = 'APROBADO'
    GROUP BY l.categoria
    ORDER BY total DESC
    LIMIT :limite
""", nativeQuery = true)
List<Object[]> buscarCategoriasPopulares(
    @Param("inicio") LocalDateTime inicio,
    @Param("fin") LocalDateTime fin,
    @Param("limite") int limite);
```

### 3.3 Caché de métricas

Extender `MetricasCache` (creada en US-026):
```java
Optional<MetricaUsuariosResponse> obtenerMetricaUsuarios(String mes);
void guardarMetricaUsuarios(String mes, MetricaUsuariosResponse respuesta, int ttlMinutos);
Optional<CategoriasPopularesResponse> obtenerCategoriasPopulares(String mes);
void guardarCategoriasPopulares(String mes, CategoriasPopularesResponse respuesta, int ttlMinutos);
```

Clave en Redis:
- `metricas:usuarios-activos:{YYYY-MM}`
- `metricas:categorias-populares:{YYYY-MM}`
TTL: 60 minutos (los datos mensuales no cambian frecuentemente).

---

## 4. Capa de UI (JavaFX)

**`admin-view.fxml`** (sección Dashboard ampliada):
- **Tarjeta "Usuarios activos"**: número del mes actual, flecha arriba/abajo con porcentaje de variación vs mes anterior.
- **Tabla "Categorías populares"**: columnas: Categoría, Total del mes, Variación (con color verde/rojo según tendencia).
- Ambas secciones se recargan con el botón "Actualizar" general del dashboard.

**`AdminController.java`** (actualizado):
- Al cargar el dashboard: invoca en paralelo `ObtenerMetricasUsuariosUseCase` y `ObtenerCategoriasPopularesUseCase`.
- Usa `Task<>` de JavaFX para ejecutar las consultas en hilos separados sin bloquear la UI.

---

## 5. Plan de Pruebas (TDD)

### `ObtenerMetricasUsuariosUseCaseTest` (con Mockito)
- `ejecutar_debeCalcularVariacionPorcentualCorrectamente()`
- `ejecutar_conMesAnteriorEnCero_debeRetornarVariacionCien()`
- `ejecutar_sinRolAdmin_debeLanzarAccesoDenegadoException()`
- `ejecutar_conDatosEnCache_debeRetornarSinConsultarRepo()`

### `ObtenerCategoriasPopularesUseCaseTest` (con Mockito)
- `ejecutar_debeRetornarCategoriasOrdenadasPorTotal()`
- `ejecutar_sinDatos_debeRetornarRespuestaVacia()`

### `PostgresMetricasCategoriaRepositoryTest` (`@DataJpaTest`)
- `buscarCategoriasPopulares_debeContabilizarAdquisicionesYDescargas()`
- `buscarCategoriasPopulares_debeRespetarElLimite()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Los casos de uso del contexto `metrics` solo dependen de interfaces de repositorio. No cruzan hacia los contextos `user`, `book` u `order` directamente. |
| **DDD** | `MetricaUsuariosActivos` y `CategoriaPopular` son Value Objects del contexto `metrics`. Encapsulan el cálculo de variación porcentual como comportamiento propio. |
| **TDD** | El test de variación porcentual con mes anterior en cero (división por cero) debe escribirse explícitamente y manejarse con retorno de 100% o un valor centinela. |
| **SOLID (SRP)** | `ObtenerMetricasUsuariosUseCase` y `ObtenerCategoriasPopularesUseCase` son responsabilidades separadas. Aunque se muestren en la misma pantalla del admin, son independientes. |
| **SOLID (ISP)** | `MetricasCache` se extiende con métodos para métricas de usuarios y categorías, pero se puede dividir en interfaces más pequeñas (`UsuarioMetricasCache`, `CategoriaMetricasCache`) si crece mucho. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear los Value Objects `MetricaUsuariosActivos` y `CategoriaPopular` en `domain/metrics/`.
- [ ] 2. Definir `MetricasUsuarioRepository` y `MetricasCategoriaRepository` en `domain/metrics/`.
- [ ] 3. Escribir los tests de ambos casos de uso con Mockito (deben fallar).
- [ ] 4. Implementar `ObtenerMetricasUsuariosUseCase` y `ObtenerCategoriasPopularesUseCase`.
- [ ] 5. Verificar que los tests pasan.
- [ ] 6. Implementar `PostgresMetricasUsuarioRepository` y `PostgresMetricasCategoriaRepository`.
- [ ] 7. Extender `MetricasCache` con los métodos de usuarios y categorías.
- [ ] 8. Escribir y verificar los tests de repositorio.
- [ ] 9. Actualizar `admin-view.fxml` con las tarjetas de métricas.
- [ ] 10. Actualizar `AdminController.java` con carga paralela de métricas.
- [ ] 11. Verificar que `mvn verify` pasa completo.

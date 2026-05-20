# Plan de Implementación: Rendimiento del Catálogo bajo Alta Demanda
**US-028 | Épica 8 – Atributos de Calidad**

---

## 1. Análisis y Contexto

Esta historia no agrega funcionalidad nueva; verifica que el catálogo cumple el objetivo de rendimiento establecido: respuesta menor a 1.5 segundos para el 95% de las solicitudes con 100 usuarios simultáneos. Las acciones son en su mayoría de configuración, optimización e instrumentación.

### Componentes impactados
- `BuscarCatalogoUseCase` (US-010)
- `RedisCatalogoCache` (US-010)
- `SpringLibroRepository` y los índices de la base de datos (US-010)
- Pipeline de CI (US-001)

---

## 2. Estrategia de Optimización

### 2.1 Índices de base de datos

Verificar que el script `V6__indices_catalogo.sql` (US-010) ya creó los índices necesarios. Agregar índices adicionales si el análisis de la prueba de carga lo indica:

**Archivo nuevo (si aplica):** `V14__indices_adicionales_catalogo.sql`

```sql
-- Índice compuesto para búsquedas filtradas por estado + categoría
CREATE INDEX IF NOT EXISTS idx_libro_estado_categoria
    ON libro (estado, categoria)
    WHERE estado = 'APROBADO';

-- Índice de texto completo para búsqueda por título y autor
CREATE INDEX IF NOT EXISTS idx_libro_titulo_autor_fulltext
    ON libro USING gin(to_tsvector('spanish', titulo || ' ' || autor));
```

### 2.2 Configuración del pool de conexiones

**En `application.yml`:**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 2.3 Caché ajustada

Verificar que `RedisCatalogoCache` (US-010) tiene el TTL correcto y que la invalidación no ocurre demasiado frecuentemente. Si el catálogo cambia poco, aumentar el TTL a 10 minutos.

**En `application.yml`:**
```yaml
openlib:
  cache:
    catalogo-ttl-segundos: 600  # Aumentar de 300 a 600 segundos
```

### 2.4 Paginación obligatoria

Verificar que todas las consultas al catálogo usen `Pageable` con `tamano` máximo de 50. Si el cliente envía un tamano mayor, limitarlo en el caso de uso:

```java
// En BuscarCatalogoUseCase
int tamanoEfectivo = Math.min(filtro.getTamano(), 50);
Pageable pageable = PageRequest.of(filtro.getPagina(), tamanoEfectivo);
```

---

## 3. Prueba de Carga

### 3.1 Herramienta

Usar **Apache JMeter** o **Gatling** para ejecutar la prueba de carga. Se recomienda JMeter por su integración con Maven.

**Dependencia Maven:**
```xml
<plugin>
    <groupId>com.lazerycode.jmeter</groupId>
    <artifactId>jmeter-maven-plugin</artifactId>
    <version>3.7.0</version>
</plugin>
```

### 3.2 Plan de prueba

**Archivo:** `src/test/jmeter/catalogo-load-test.jmx`

Configuración del test:
- **Usuarios simultáneos:** 100 hilos
- **Tiempo de arranque (ramp-up):** 10 segundos
- **Duración total:** 60 segundos
- **Endpoint:** `GET /api/libros?pagina=0&tamano=20`
- **Objetivo:** P95 < 1500 ms, tasa de error < 1%

### 3.3 Ejecución en el pipeline CI

**En `.github/workflows/ci.yml`** (nuevo paso opcional):

```yaml
- name: Run Load Test
  run: mvn jmeter:jmeter jmeter:results
  working-directory: backend
  continue-on-error: true  # No falla el build; solo reporta

- name: Upload JMeter Results
  uses: actions/upload-artifact@v3
  with:
    name: jmeter-results
    path: backend/target/jmeter/results/
```

---

## 4. Instrumentación (Métricas en tiempo real)

### 4.1 Spring Actuator + Micrometer

**Dependencia:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**En `application.yml`:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

Esto expone el percentil 95 de tiempo de respuesta en `/actuator/metrics/http.server.requests`.

---

## 5. Plan de Pruebas (TDD / Verificación)

### `CatalogoRendimientoTest` (integración — `@SpringBootTest`)

```java
@Test
void buscarCatalogo_debResponderEnMenosDe1500ms() {
    // Ejecutar 100 peticiones secuenciales y medir el P95
    long p95 = medirP95(100, () ->
        buscarCatalogoUseCase.ejecutar(FiltroCatalogo.vacio())
    );
    assertThat(p95).isLessThan(1500L);
}
```

> Este test se ejecuta solo en el perfil `performance` para no ralentizar el ciclo normal de CI.

### Verificación en el reporte JMeter

El reporte JMeter debe mostrar:
- **P95 ≤ 1500 ms** ✓
- **Tasa de error ≤ 1%** ✓
- **Throughput ≥ 50 req/s** ✓ (objetivo secundario)

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Las optimizaciones (índices, pool de conexiones, caché TTL) son responsabilidad exclusiva de la capa de infraestructura. No se modifica ninguna clase de dominio ni caso de uso. |
| **DDD** | No aplica cambios en el dominio. Los Value Objects y Aggregate Roots del catálogo no cambian. |
| **TDD** | El test de rendimiento `CatalogoRendimientoTest` se escribe antes de ejecutar la prueba JMeter. Debe fallar si no se han aplicado las optimizaciones. |
| **SOLID (SRP)** | La configuración de HikariCP vive en `application.yml`. La configuración de caché vive en `RedisCatalogoCache`. La prueba de carga es responsabilidad del archivo JMX. Tres responsabilidades separadas. |
| **SOLID (OCP)** | Las optimizaciones se aplican como configuración adicional (nuevos índices, ajuste de TTL) sin modificar el código existente de `BuscarCatalogoUseCase`. |

---

## 📋 Tareas de Implementación

- [ ] 1. Ejecutar `EXPLAIN ANALYZE` en las consultas del catálogo y analizar el plan de ejecución.
- [ ] 2. Crear `V14__indices_adicionales_catalogo.sql` si los índices existentes son insuficientes.
- [ ] 3. Ajustar la configuración de HikariCP en `application.yml`.
- [ ] 4. Ajustar el TTL del caché del catálogo a 600 segundos.
- [ ] 5. Agregar la validación de `tamanoEfectivo` en `BuscarCatalogoUseCase`.
- [ ] 6. Agregar la dependencia de Spring Actuator al `pom.xml`.
- [ ] 7. Configurar las métricas de percentiles en `application.yml`.
- [ ] 8. Crear el plan de prueba JMeter `catalogo-load-test.jmx`.
- [ ] 9. Escribir `CatalogoRendimientoTest` en el perfil `performance`.
- [ ] 10. Integrar el paso de prueba de carga en el pipeline CI como paso reportable.
- [ ] 11. Ejecutar la prueba y documentar el reporte de resultados en `docs/load-test-results.md`.
- [ ] 12. Verificar que el P95 es menor a 1.5 segundos y que el reporte queda adjunto al PR.

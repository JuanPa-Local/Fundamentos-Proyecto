# Plan de Implementación: Almacenamiento Temporal y Manejo de Sesiones
**US-030 | Épica 8 – Atributos de Calidad**

---

## 1. Análisis y Contexto

Esta historia consolida y verifica formalmente la arquitectura de caché y sesiones implementada de forma incremental a lo largo del proyecto. No agrega funcionalidades nuevas; refuerza la correcta implementación de Redis como sistema de almacenamiento temporal, documenta las decisiones de diseño y añade pruebas de verificación explícitas para el comportamiento del caché.

### Componentes revisados y formalizados

| Componente | Historia origen | Clave en Redis | TTL |
|---|---|---|---|
| Sesiones de usuario | US-005 | `spring:session:{sessionId}` | 1 hora |
| Carrito de compras | US-015 | `carrito:{buyerId}` | 7 días |
| Sesión de checkout | US-016 | `checkout:{buyerId}` | 30 minutos |
| Caché del catálogo | US-010 | `catalogo:{termino}:{...}` | 10 minutos |
| Caché de recomendaciones | US-023 | `recomendaciones:{buyerId}` | 10 minutos |
| Caché de métricas | US-026 | `metricas:{tipo}:{periodo}` | 60 minutos |

---

## 2. Implementación

### 2.1 Configuración centralizada de Redis

**Archivo nuevo:** `config/RedisConfig.java`

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
```

### 2.2 TTL configurable por componente

**En `application.yml`** (consolidación de toda la configuración de caché):

```yaml
openlib:
  cache:
    catalogo-ttl-segundos: 600
    recomendaciones-ttl-minutos: 10
    checkout-ttl-minutos: 30
    metricas-ttl-minutos: 60
  carrito:
    ttl-dias: 7
  session:
    ttl-horas: 1
```

Cada clase de caché (ej. `RedisCatalogoCache`) lee su TTL de estas propiedades usando `@Value` o `@ConfigurationProperties`, nunca con valores hardcodeados.

### 2.3 Invalidación correcta al modificar datos

Verificar que los siguientes eventos disparan la invalidación de caché correspondiente:

| Evento | Caché invalidada |
|---|---|
| Admin aprueba/rechaza un libro (US-014) | `catalogo:*` |
| Admin aprueba una reseña (US-025) | `libro-detalle:{libroId}` |
| Buyer confirma una orden (US-018) | `recomendaciones:{buyerId}` |
| Admin modifica categoría/etiqueta (US-013) | `catalogo:*` |

Crear una clase `CacheInvalidator` en la capa de aplicación que centraliza todas las llamadas de invalidación:

```java
// application/CacheInvalidator.java
public class CacheInvalidator {
    private final CatalogoCache catalogoCache;
    private final RecomendacionCache recomendacionCache;

    public void invalidarCatalogo() {
        catalogoCache.invalidar("catalogo:*");
    }

    public void invalidarRecomendaciones(UUID buyerId) {
        recomendacionCache.invalidar(buyerId);
    }
}
```

---

## 3. Decisión de Diseño Documentada

**Archivo nuevo:** `docs/adr/ADR-001-uso-de-redis-como-cache.md`

```markdown
# ADR-001: Uso de Redis como sistema de caché y sesiones

**Estado:** Aceptado
**Fecha:** [fecha del sprint]

## Contexto
El sistema requiere gestión de sesiones escalable y caché de consultas frecuentes
para cumplir el requisito de rendimiento < 1.5s.

## Decisión
Usar Redis como sistema único para sesiones (Spring Session) y caché de aplicación
(RedisTemplate con TTL por componente).

## Consecuencias
- **Positivo:** Sesiones desacopladas del servidor; escala horizontalmente.
- **Positivo:** Caché integrado con el mismo servidor de Redis.
- **Negativo:** Punto único de fallo si Redis no tiene réplica (mitigado con Docker Compose).

## TTLs definidos
| Componente | TTL |
|---|---|
| Sesión de usuario | 1 hora |
| Carrito | 7 días |
| Caché catálogo | 10 minutos |
```

---

## 4. Plan de Pruebas (TDD / Verificación)

### `RedisCacheIntegracionTest` (integración con TestContainers Redis)

```java
@Testcontainers
@SpringBootTest
class RedisCacheIntegracionTest {

    @Test
    void catalogoCache_debeAlmacenarYRecuperarResultado() {
        // Guardar
        var resultado = new ResultadoCatalogo(...);
        catalogoCache.guardar("catalogo:test:0:20", resultado, 600);

        // Recuperar
        var recuperado = catalogoCache.obtener("catalogo:test:0:20");
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getTotalElementos())
            .isEqualTo(resultado.getTotalElementos());
    }

    @Test
    void catalogoCache_despuesDeVencerTTL_debeRetornarVacio() throws InterruptedException {
        catalogoCache.guardar("catalogo:ttl-test:0:20", resultado, 1); // TTL 1 segundo
        Thread.sleep(2000);
        assertThat(catalogoCache.obtener("catalogo:ttl-test:0:20")).isEmpty();
    }

    @Test
    void catalogoCache_acierto_debeRetornarSinConsultarBaseDeDatos() {
        // Pre-cargar caché
        catalogoCache.guardar("catalogo:libro:0:20", resultado, 600);

        // Ejecutar caso de uso — el repositorio NO debe ser llamado
        buscarCatalogoUseCase.ejecutar(filtro);
        verify(libroRepository, never()).buscarAprobados(any(), any());
    }

    @Test
    void catalogoCache_fallo_debeConsultarBDYGuardarEnCache() {
        // Sin datos en caché
        buscarCatalogoUseCase.ejecutar(filtro);

        // El repositorio SÍ debe ser llamado
        verify(libroRepository, times(1)).buscarAprobados(any(), any());
        // Y el resultado debe guardarse en caché
        assertThat(catalogoCache.obtener(claveEsperada)).isPresent();
    }
}
```

### `SesionRedisIntegracionTest`
- `crearSesion_debeAlmacenarEnRedisConTTL()`
- `validarSesion_despuesDeExpirar_debeRetornarVacia()`
- `cerrarSesion_debeEliminarLaClaveDeRedis()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `CacheInvalidator` vive en la capa de **aplicación**, no en infraestructura ni en dominio. Coordina la invalidación sin conocer los detalles de Redis. Depende de `CatalogoCache` y `RecomendacionCache` (interfaces). |
| **DDD** | No hay cambios en el dominio. El caché es completamente invisible para los Aggregate Roots y Value Objects. |
| **TDD** | Los tests de acierto y falla de caché (`cache hit` y `cache miss`) son los más importantes de esta historia. Deben verificar el comportamiento del sistema completo end-to-end con TestContainers. |
| **SOLID (SRP)** | `RedisConfig` configura la serialización de Redis. `RedisCatalogoCache` gestiona el caché del catálogo. `SpringSesionGateway` gestiona las sesiones. `CacheInvalidator` coordina la invalidación. Cuatro responsabilidades, cuatro clases. |
| **SOLID (OCP)** | `CacheInvalidator` puede extenderse con nuevos métodos de invalidación sin modificar las clases de caché existentes. |
| **SOLID (DIP)** | `CacheInvalidator` depende de `CatalogoCache` y `RecomendacionCache` (interfaces). Las implementaciones Redis se inyectan por Spring. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear `RedisConfig.java` en `config/` con la configuración centralizada de serialización.
- [ ] 2. Consolidar todos los TTL en `application.yml` bajo `openlib.cache`.
- [ ] 3. Actualizar todas las clases de caché para leer el TTL desde `@Value` o `@ConfigurationProperties`.
- [ ] 4. Crear `CacheInvalidator.java` en la capa de aplicación.
- [ ] 5. Verificar que los eventos de invalidación (aprobación de libro, confirmación de orden, etc.) invocan `CacheInvalidator` correctamente.
- [ ] 6. Escribir `RedisCacheIntegracionTest` con TestContainers (tests de acierto, falla y expiración).
- [ ] 7. Escribir `SesionRedisIntegracionTest` con TestContainers.
- [ ] 8. Verificar que todos los tests de integración de Redis pasan.
- [ ] 9. Crear `docs/adr/ADR-001-uso-de-redis-como-cache.md` con la decisión documentada.
- [ ] 10. Verificar que `mvn verify` pasa completo incluyendo los tests de integración.
- [ ] 11. Documentar la tabla de TTLs en el `README.md` o en `docs/arquitectura-cache.md`.

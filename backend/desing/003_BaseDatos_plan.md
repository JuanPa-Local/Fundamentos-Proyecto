# Plan de Implementación: Configuración de la Base de Datos
**US-003 | Épica 0 – Infraestructura & Base Técnica**

---

## 1. Análisis de Dominio y Contexto

Esta historia configura los dos sistemas de persistencia del proyecto: PostgreSQL (datos transaccionales permanentes) y Redis (sesiones y caché). Aunque no define entidades de negocio finales, establece el esquema inicial y los mecanismos de migración que usarán todas las historias siguientes.

### Responsabilidades de cada sistema
| Sistema | Responsabilidad |
|---|---|
| **PostgreSQL** | Usuarios, Libros, Órdenes, Reseñas, Favoritos — datos permanentes |
| **Redis** | Sesiones JWT activas, caché del catálogo, carrito de compras |

---

## 2. Configuración Docker Compose

**Archivo:** `docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: openlib_dev
      POSTGRES_USER: openlib
      POSTGRES_PASSWORD: openlib_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

---

## 3. Gestión de Migraciones (Flyway)

**Dependencia a agregar en `pom.xml`:**
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**Ubicación de scripts:** `src/main/resources/db/migration/`

**Script inicial:** `V1__crear_esquema_inicial.sql`

```sql
CREATE TABLE usuario (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      VARCHAR(150) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    rol         VARCHAR(50)  NOT NULL,
    activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE libro (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo      VARCHAR(255) NOT NULL,
    autor       VARCHAR(255) NOT NULL,
    isbn        VARCHAR(20)  NOT NULL UNIQUE,
    categoria   VARCHAR(100),
    estado      VARCHAR(50)  NOT NULL DEFAULT 'PENDIENTE',
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE orden (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id    UUID NOT NULL REFERENCES usuario(id),
    estado      VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT now()
);
```

---

## 4. Configuración de Redis (Spring Session)

**Dependencia a agregar:**
```xml
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

**En `application.yml`:**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  session:
    store-type: redis
    timeout: 1h
```

---

## 5. Plan de Pruebas (TDD)

- **`DatabaseConnectionTest`** (`@SpringBootTest`, perfil `test` con H2):
  - Verificar que el contexto de JPA carga correctamente.
  - Verificar que Flyway aplica las migraciones sin errores.
  - Escribir este test **antes** de crear los scripts de migración.
- **`RedisConnectionTest`** (`@SpringBootTest`):
  - Verificar que el cliente Redis puede guardar y recuperar un valor clave-valor simple.
  - Usar `@TestContainers` con una imagen Redis si se quiere aislar el test.

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Las cadenas de conexión y configuraciones de infraestructura viven **únicamente** en `application.yml` y en clases dentro de `infrastructure/`. El dominio nunca debe conocer detalles de PostgreSQL o Redis. |
| **DDD** | El esquema SQL inicial debe reflejar el lenguaje ubicuo: tablas llamadas `usuario`, `libro`, `orden` — no `tbl_user`, `tbl_book`. Los nombres deben coincidir con los del glosario del equipo. |
| **TDD** | Los tests de conexión deben escribirse antes de que los scripts de migración existan. Deben fallar primero y volverse verdes al aplicar las migraciones. |
| **SOLID (SRP)** | `docker-compose.yml` solo orquesta infraestructura. `DataInitializer.java` (del US-002) es la única clase que inserta datos de prueba al arrancar; no mezclar esto con la configuración de Flyway. |
| **SOLID (OCP)** | Usar Flyway garantiza que el esquema sea extensible: cada nueva historia agrega un nuevo script de migración versionado sin modificar los anteriores. |

---

## 📋 Tareas de Implementación

- [ ] 1. Agregar dependencias de Flyway y Spring Session Redis al `pom.xml`.
- [ ] 2. Crear `docker-compose.yml` con los servicios de PostgreSQL y Redis.
- [ ] 3. Escribir los tests `DatabaseConnectionTest` y `RedisConnectionTest` (deben fallar inicialmente).
- [ ] 4. Crear el directorio `src/main/resources/db/migration/`.
- [ ] 5. Crear el script `V1__crear_esquema_inicial.sql` con las tablas base.
- [ ] 6. Configurar `application.yml` con los perfiles `dev` y `test`.
- [ ] 7. Verificar que ambos tests pasan tras aplicar la configuración.
- [ ] 8. Documentar el diagrama ER inicial en el `README.md` o en un archivo `docs/esquema-er.md`.
- [ ] 9. Verificar que `mvn verify` pasa con el pipeline CI activo.

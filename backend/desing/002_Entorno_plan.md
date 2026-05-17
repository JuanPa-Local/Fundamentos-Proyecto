# Plan de Implementación: Configuración del Entorno Base del Sistema
**US-002 | Épica 0 – Infraestructura & Base Técnica**

---

## 1. Análisis de Dominio y Contexto

Esta historia establece el proyecto Spring Boot como base técnica compartida. No genera entidades de negocio, pero define la estructura de paquetes que respetarán todas las historias siguientes. Es el esqueleto sobre el que crece el sistema.

### Módulos y dependencias a configurar (`pom.xml`)
| Dependencia | Propósito |
|---|---|
| `spring-boot-starter-web` | API REST y manejo de peticiones |
| `spring-boot-starter-security` | Seguridad y control de acceso |
| `spring-boot-starter-data-jpa` | Acceso a base de datos relacional |
| `spring-boot-starter-validation` | Validación de datos de entrada |
| `spring-boot-starter-data-redis` | Gestión de sesiones y caché |
| `openjfx` (javafx-controls, javafx-fxml) | Interfaz de escritorio |
| `lombok` | Reducción de código repetitivo |
| `spring-boot-starter-test` | JUnit 5 + Mockito para pruebas |
| `postgresql` (runtime) | Driver de conexión a PostgreSQL |

---

## 2. Estructura de Paquetes (Clean Architecture)

```
src/main/java/com/openlib/backend/
├── BackendApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── domain/
│   ├── user/
│   │   ├── User.java              ← Entidad de dominio
│   │   ├── UserRepository.java    ← Interface (puerto de salida)
│   │   └── UserService.java       ← Caso de uso
│   ├── book/
│   └── order/
├── infrastructure/
│   ├── persistence/
│   │   └── jpa/                   ← Implementaciones JPA
│   └── security/
└── UI/
    ├── JavaFXApp.java
    └── controllers/
```

---

## 3. Configuración de Perfiles (`application.yml`)

```yaml
spring:
  profiles:
    active: dev
---
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/openlib_dev
  jpa:
    hibernate:
      ddl-auto: validate
---
spring:
  config:
    activate:
      on-profile: test
  datasource:
    url: jdbc:h2:mem:openlib_test
```

---

## 4. Clase Principal

**Archivo:** `BackendApplication.java`
- Anotada con `@SpringBootApplication`.
- Debe implementar `Application` de JavaFX para integrar el contexto de Spring con JavaFX.
- Javadoc obligatorio explicando el propósito de la clase.

---

## 5. Plan de Pruebas (TDD)

- **`BackendApplicationTests`**: Test de contexto de Spring (`@SpringBootTest`) que verifica que el contexto carga sin errores. Este test debe escribirse **antes** de configurar los beans.
- El test inicial fallará (contexto no cargado) y debe volverse verde al finalizar la configuración correcta.

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | El paquete `domain/` no debe importar nada de `infrastructure/` ni de `UI/`. Esto se verifica desde esta historia para que no se rompa en las siguientes. |
| **DDD** | Los sub-paquetes dentro de `domain/` deben corresponder a los contextos delimitados del negocio: `user`, `book`, `order`. No mezclar conceptos entre ellos. |
| **TDD** | Escribir `BackendApplicationTests` antes de finalizar la configuración. El build de CI debe fallar si este test no pasa. |
| **SOLID (SRP)** | Cada clase de configuración tiene una única responsabilidad: `SecurityConfig` solo gestiona seguridad, `DataInitializer` solo pobla datos iniciales. No combinar configuraciones en una sola clase. |
| **SOLID (DIP)** | Las interfaces de repositorio (`UserRepository`) deben vivir en `domain/`, y sus implementaciones JPA en `infrastructure/`. Nunca al revés. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear el proyecto en [Spring Initializr](https://start.spring.io) con Java 25 y las dependencias listadas.
- [ ] 2. Configurar `pom.xml` con todas las dependencias necesarias.
- [ ] 3. Crear la estructura de paquetes base: `domain/`, `infrastructure/`, `UI/`, `config/`.
- [ ] 4. Crear `BackendApplication.java` con Javadoc.
- [ ] 5. Crear `application.yml` con perfiles `dev`, `test` y `prod`.
- [ ] 6. Escribir `BackendApplicationTests` y verificar que pasa.
- [ ] 7. Crear `SecurityConfig.java` con configuración base (permitir todo temporalmente hasta US-005).
- [ ] 8. Confirmar que `mvn verify` pasa en el pipeline CI.

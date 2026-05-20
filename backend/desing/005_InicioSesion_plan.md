# Plan de Implementación: Inicio de Sesión
**US-005 | Épica 1 – Autenticación & Gestión de Usuarios**

---

## 1. Análisis y Modelado de Dominio

El inicio de sesión valida las credenciales del usuario y emite un token de sesión gestionado por Spring Security y almacenado en Redis. La lógica de validación pertenece al dominio; la emisión y el almacenamiento del token pertenecen a la infraestructura.

### 1.1 Comportamientos nuevos en `Usuario` (Aggregate Root)

La entidad `Usuario` (creada en US-004) recibe dos nuevos comportamientos:
- `void validarCredenciales(String passwordIngresada, PasswordEncoder encoder)` — lanza `CredencialesInvalidasException` si el password no coincide o si la cuenta está inactiva.
- `boolean estaActivo()` — retorna el valor del campo `activo`.

**Excepción de dominio nueva** (en `domain/user/exception/`):
- `CredencialesInvalidasException`
- `CuentaInactivaException`

### 1.2 Value Object `SesionUsuario`

**Archivo:** `domain/user/SesionUsuario.java`
- `usuarioId` (UUID)
- `email` (String)
- `rol` (RolUsuario)
- `expiracion` (LocalDateTime)

Encapsula los datos que viajan dentro del token de sesión y que Redis almacena como referencia.

---

## 2. Capa de Aplicación (Caso de Uso)

### `IniciarSesionUseCase`

**DTO de entrada:** `IniciarSesionCommand`
- `String email`
- `String password`

**DTO de salida:** `SesionResponse`
- `String sessionId` (token emitido por Spring Session)
- `String rol`
- `String nombre`

**Flujo:**
1. Recuperar el usuario por email con `UsuarioRepository.buscarPorEmail()`. Si no existe, lanzar `CredencialesInvalidasException` (nunca revelar si el email no existe por seguridad).
2. Invocar `usuario.validarCredenciales(password, encoder)`.
3. Delegar la creación de la sesión a `SesionGateway.crearSesion(usuario)`.
4. Retornar `SesionResponse` con el token y los datos básicos del usuario.

### Interface `SesionGateway` (puerto de salida)

```java
// domain/user/SesionGateway.java
public interface SesionGateway {
    String crearSesion(Usuario usuario);
    void cerrarSesion(String sessionId);
    Optional<SesionUsuario> validarSesion(String sessionId);
}
```

---

## 3. Capa de Infraestructura

### 3.1 Implementación de Spring Security + Redis

**Archivo:** `infrastructure/security/SpringSesionGateway.java`
- Implementa `SesionGateway`.
- Usa `HttpSession` (gestionada por Spring Session + Redis) para almacenar el `SesionUsuario`.
- La sesión tiene TTL de 1 hora (configurado en `application.yml`).

**Archivo:** `infrastructure/security/OpenLibUserDetailsService.java`
- Implementa `UserDetailsService` de Spring Security.
- Busca el usuario por email usando `UsuarioRepository` para validar en el filtro de seguridad.

### 3.2 Configuración de seguridad

**Archivo:** `config/SecurityConfig.java` (actualizado desde US-002)
- Configurar `SecurityFilterChain` para:
  - Rutas públicas: `/api/auth/registro`, `/api/auth/login`, catálogo (`/api/libros/**`).
  - Rutas protegidas: todo lo demás requiere sesión activa.
- Configurar `BCryptPasswordEncoder` como bean.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML existente:** `resources/views/main-view.fxml`
- Formulario con campos `email` y `password`, botón "Ingresar".
- Enlace a la pantalla de registro.

**Controlador:** `UI/controllers/LoginController.java` (nuevo)
- Al iniciar sesión exitosamente, almacena el `SesionResponse` en `SessionManager.java`.
- Navega a la vista correspondiente según el rol: `buyer-view.fxml`, `seller-view.fxml` o `admin-view.fxml`.

**`SessionManager.java`** (actualizado):
- Guarda el `sessionId` y el `rol` del usuario activo en memoria de la sesión de JavaFX.
- Expone `getRolActual()` y `estaAutenticado()` para uso de los controladores.

---

## 5. Plan de Pruebas (TDD)

### `UsuarioTest` (dominio — ampliado)
- `validarCredenciales_conPasswordCorrecta_noDebeLanzarExcepcion()`
- `validarCredenciales_conPasswordIncorrecta_debeLanzarCredencialesInvalidasException()`
- `validarCredenciales_conCuentaInactiva_debeLanzarCuentaInactivaException()`

### `IniciarSesionUseCaseTest` (aplicación — con Mockito)
- `ejecutar_conCredencialesValidas_debeRetornarSesionResponse()`
- `ejecutar_conEmailInexistente_debeLanzarCredencialesInvalidasException()`
- `ejecutar_conCuentaInactiva_debeLanzarCuentaInactivaException()`

### `SpringSesionGatewayTest` (integración — con TestContainers Redis)
- `crearSesion_debeAlmacenarEnRedis()`
- `validarSesion_conTokenValido_debeRetornarSesionUsuario()`
- `validarSesion_conTokenExpirado_debeRetornarEmpty()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `IniciarSesionUseCase` no importa nada de Spring Security ni de Redis. Depende únicamente de las interfaces `UsuarioRepository` y `SesionGateway`. |
| **DDD** | La regla "no revelar si el email existe" es una decisión de negocio; vive en el caso de uso, no en el controlador. |
| **TDD** | Escribir todos los tests del caso de uso con Mockito antes de implementar `IniciarSesionUseCase`. |
| **SOLID (SRP)** | `SecurityConfig` configura las reglas de acceso. `SpringSesionGateway` gestiona el ciclo de vida de la sesión. `OpenLibUserDetailsService` integra con Spring Security. Tres responsabilidades, tres clases. |
| **SOLID (DIP)** | `IniciarSesionUseCase` depende de `SesionGateway` (interface). La clase `SpringSesionGateway` es inyectada por Spring en tiempo de ejecución. |
| **SOLID (LSP)** | `SpringSesionGateway` implementa todos los métodos de `SesionGateway` sin alterar el contrato esperado; puede intercambiarse por una implementación en memoria para tests. |

---

## 📋 Tareas de Implementación

- [x] 1. `UserService.login(email, password)` — valida credenciales con BCrypt.
- [x] 2. `POST /api/users/login` retorna email, fullName, role, id.
- [x] 3. `SessionManager.java` implementado con Singleton — guarda email, role, fullName, token, expone `isLoggedIn()` y `bearerHeader()`.
- [x] 4. `ControllerUI.java` — al hacer login exitoso, llama a `SessionManager.getInstance().set*()` y navega a la vista correcta según rol usando `ViewFactory`.
- [ ] PENDIENTE: `validarCredenciales()` y `estaActivo()` en entidad `User` (el chequeo de cuenta inactiva lo hace `isActive()` ya existente pero no se valida en el login aún).
- [ ] PENDIENTE: `SesionGateway` con Spring Session + Redis (actualmente se usa JWT/token simple en `SessionManager`).

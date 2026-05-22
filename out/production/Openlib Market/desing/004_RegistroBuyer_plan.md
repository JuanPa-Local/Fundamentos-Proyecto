# Plan de Implementación: Registro de Usuario Comprador
**US-004 | Épica 1 – Autenticación & Gestión de Usuarios**

---

## 1. Análisis y Modelado de Dominio

El registro de un Comprador es el primer punto de entrada al sistema. Se crea la entidad `Usuario` con rol `BUYER` y se establece la lógica de validación en el dominio para garantizar datos correctos desde el origen.

### 1.1 Enumeración necesaria

**`RolUsuario`** (en `domain/user/`):
- Valores: `BUYER`, `SELLER`, `ADMIN`

### 1.2 Entidad `Usuario` (Aggregate Root)

**Atributos:**
- `id` (UUID) — generado automáticamente
- `nombre` (String) — obligatorio
- `email` (String) — obligatorio, único, formato válido
- `password` (String) — almacenada cifrada, mínimo 8 caracteres en texto plano
- `rol` (RolUsuario) — asignado en creación
- `activo` (boolean) — `true` por defecto al registrarse
- `fechaRegistro` (LocalDateTime) — asignado automáticamente

**Comportamientos:**
- `static Usuario registrarBuyer(String nombre, String email, String passwordCifrada)` — método fábrica que valida campos y asigna el rol `BUYER`.
- `void desactivar()` — cambia `activo` a `false`.
- `void activar()` — cambia `activo` a `true`.

**Excepciones de dominio** (en `domain/user/exception/`):
- `EmailDuplicadoException`
- `PasswordDemasiadoCortaException`
- `NombreObligatorioException`

### 1.3 Interface `UsuarioRepository` (puerto de salida)

```java
// domain/user/UsuarioRepository.java
public interface UsuarioRepository {
    Usuario guardar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
    boolean existePorEmail(String email);
}
```

---

## 2. Capa de Aplicación (Caso de Uso)

### `RegistrarBuyerUseCase`

**Archivo:** `domain/user/RegistrarBuyerUseCase.java`

**DTO de entrada:** `RegistrarBuyerCommand`
- `String nombre`
- `String email`
- `String password`

**Flujo:**
1. Verificar que no exista un usuario con el mismo email usando `UsuarioRepository.existePorEmail()`. Si existe, lanzar `EmailDuplicadoException`.
2. Cifrar la contraseña usando `PasswordEncoder` (inyectado como interface, no como implementación concreta).
3. Llamar al método fábrica `Usuario.registrarBuyer(nombre, email, passwordCifrada)`.
4. Persistir usando `UsuarioRepository.guardar()`.
5. Retornar un `UsuarioResponse` con `id`, `nombre`, `email` y `rol`.

---

## 3. Capa de Infraestructura

### 3.1 Entidad JPA

**Archivo:** `infrastructure/persistence/jpa/UsuarioJpaEntity.java`
- Anotaciones: `@Entity`, `@Table(name = "usuario")`, `@Id`, `@GeneratedValue`
- Campo `password` con `@Column(nullable = false)`
- Campo `rol` con `@Enumerated(EnumType.STRING)`

### 3.2 Repositorio JPA

**Archivo:** `infrastructure/persistence/jpa/SpringUsuarioRepository.java`
- Extiende `JpaRepository<UsuarioJpaEntity, UUID>`
- Método `findByEmail(String email)`

### 3.3 Adaptador (implementación del puerto)

**Archivo:** `infrastructure/persistence/UsuarioRepositoryAdapter.java`
- Implementa `UsuarioRepository` del dominio
- Usa `UsuarioMapper` para convertir entre `Usuario` (dominio) y `UsuarioJpaEntity` (JPA)

### 3.4 Migración de base de datos

Si no existe aún, el script `V1__crear_esquema_inicial.sql` (US-003) ya incluye la tabla `usuario`. En esta historia no se requiere una nueva migración.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML:** `resources/views/register-view.fxml`
- Campos: `nombre`, `email`, `password`, botón "Registrarme".
- Validación visual: campo en rojo si el formato es inválido antes de enviar.

**Controlador:** `UI/controllers/RegisterController.java`
- Al hacer clic en "Registrarme", invoca `RegistrarBuyerUseCase`.
- Muestra un mensaje de éxito o el error correspondiente al usuario.

---

## 5. Plan de Pruebas (TDD)

**Escribir los tests antes de implementar las clases.**

### `UsuarioTest` (dominio)
- `registrarBuyer_conDatosValidos_debeCrearUsuarioConRolBuyer()`
- `registrarBuyer_sinNombre_debeLanzarNombreObligatorioException()`
- `registrarBuyer_conPasswordCorta_debeLanzarPasswordDemasiadoCortaException()`

### `RegistrarBuyerUseCaseTest` (aplicación — con Mockito)
- `ejecutar_conEmailNuevo_debeGuardarYRetornarUsuario()`
- `ejecutar_conEmailDuplicado_debeLanzarEmailDuplicadoException()`

### `UsuarioRepositoryAdapterTest` (integración — con `@DataJpaTest`)
- `guardar_debePersistitUsuarioEnPostgres()`
- `buscarPorEmail_conEmailExistente_debeRetornarUsuario()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `RegistrarBuyerUseCase` solo conoce `UsuarioRepository` (interface). Nunca importa `UsuarioJpaEntity` ni `SpringUsuarioRepository`. |
| **DDD** | La validación de negocio (email único, password mínima) vive en el dominio, no en el controlador JavaFX ni en la entidad JPA. |
| **TDD** | Escribir `UsuarioTest` y `RegistrarBuyerUseCaseTest` antes de implementar el caso de uso. El build debe fallar primero. |
| **SOLID (SRP)** | `RegistrarBuyerUseCase` tiene una única responsabilidad: orquestar el registro. El cifrado de la contraseña es responsabilidad de una clase `PasswordEncoder` separada. |
| **SOLID (DIP)** | `RegistrarBuyerUseCase` depende de la interface `UsuarioRepository` y de la interface `PasswordEncoder`, nunca de implementaciones concretas (`BCryptPasswordEncoder`, `UsuarioRepositoryAdapter`). |
| **SOLID (OCP)** | El método fábrica `Usuario.registrarBuyer()` está cerrado a modificación. Para registrar un `Seller` (US-006) se creará un método fábrica distinto `Usuario.registrarSeller()`. |

---

## 📋 Tareas de Implementación

- [x] 1. Enum `Role` existe en `User.java` con valores `BUYER`, `SELLER`, `ADMIN`.
- [ ] 2. Excepciones de dominio en `domain/user/exception/` — no creadas *(se usan `RuntimeException` directamente)*.
- [ ] 3. Tests unitarios pendientes.
- [x] 4. Entidad `User` existe con `@Builder`, campos email, fullName, passwordHash, role, active, createdAt.
- [ ] 5. Tests de dominio pendientes.
- [x] 6. `UserRepository extends JpaRepository<User, UUID>` con `findByEmail()` y `existsByEmail()`.
- [ ] 7. `RegistrarBuyerUseCaseTest` no creado *(arquitectura en capas, sin caso de uso separado)*.
- [x] 8. `UserService.register()` orquesta la creación con validación de email único y cifrado BCrypt.
- [ ] 9. Test del servicio pendiente.
- [x] 10. No existe `UsuarioJpaEntity` separada — `User.java` es directamente la entidad JPA *(arquitectura en capas)*.
- [x] 11. No existe `UsuarioMapper` — la entidad JPA es el dominio directamente.
- [ ] 12. `UsuarioRepositoryAdapterTest` no creado.
- [x] 13. `register-view.fxml` y `RegisterController.java` existen e integran el flujo de registro.
- [ ] 14. `mvn verify` pendiente.

# Plan de Implementación: Registro e Inicio de Sesión del Vendedor
**US-006 | Épica 1 – Autenticación & Gestión de Usuarios**

---

## 1. Análisis y Modelado de Dominio

El registro del Vendedor comparte la entidad `Usuario` creada en US-004, pero introduce un nuevo Value Object `PerfilSeller` que agrupa la información editorial. La distinción de roles garantiza que solo los `SELLER` accedan al panel de gestión de libros.

### 1.1 Value Object `PerfilSeller`

**Archivo:** `domain/user/PerfilSeller.java`
- `selloEditorial` (String) — obligatorio
- `descripcion` (String) — obligatorio
- `logoUrl` (String) — obligatorio

**Reglas en el constructor:**
- Los tres campos son obligatorios y no pueden estar vacíos.
- Si alguno está vacío, lanza `DatoEditorialObligatorioException`.

**Excepción nueva:** `DatoEditorialObligatorioException`

### 1.2 Comportamiento nuevo en `Usuario`

- `static Usuario registrarSeller(String nombre, String email, String passwordCifrada, PerfilSeller perfil)` — método fábrica que asigna el rol `SELLER` y vincula el `PerfilSeller`.
- `PerfilSeller getPerfilSeller()` — retorna el perfil editorial.

---

## 2. Capa de Aplicación (Caso de Uso)

### `RegistrarSellerUseCase`

**DTO de entrada:** `RegistrarSellerCommand`
- `String nombre`
- `String email`
- `String password`
- `String selloEditorial`
- `String descripcion`
- `String logoUrl`

**Flujo:**
1. Verificar que no exista un usuario con el mismo email usando `UsuarioRepository.existePorEmail()`. Si existe, lanzar `EmailDuplicadoException`.
2. Construir `PerfilSeller` (valida datos editoriales).
3. Cifrar la contraseña.
4. Llamar al método fábrica `Usuario.registrarSeller()`.
5. Persistir con `UsuarioRepository.guardar()`.
6. Retornar `UsuarioResponse` con `id`, `nombre`, `email`, `rol` y `selloEditorial`.

### Reutilización del flujo de inicio de sesión

El inicio de sesión del Seller usa exactamente el mismo `IniciarSesionUseCase` de US-005. La diferencia de comportamiento post-login (redirección a `seller-view.fxml`) es responsabilidad exclusiva de `LoginController` en la capa UI, que evalúa el rol de la sesión activa.

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V2__agregar_perfil_seller.sql`

```sql
ALTER TABLE usuario
    ADD COLUMN sello_editorial VARCHAR(200),
    ADD COLUMN descripcion_editorial TEXT,
    ADD COLUMN logo_url VARCHAR(500);
```

### 3.2 Actualización del Mapper JPA

**`UsuarioMapper`** (actualizado):
- Mapear los campos `selloEditorial`, `descripcionEditorial` y `logoUrl` entre `Usuario` (dominio) y `UsuarioJpaEntity` (infraestructura).

### 3.3 Control de acceso al panel Seller

**`SecurityConfig`** (actualizado):
- La ruta `/api/seller/**` y la vista `seller-view.fxml` solo son accesibles para usuarios con rol `SELLER`.
- Intentar acceder con rol `BUYER` retorna un error de acceso denegado.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/register-seller-view.fxml`
- Campos: `nombre`, `email`, `password`, `selloEditorial`, `descripcion`, `logoUrl`.
- Botón "Registrarme como Vendedor".

**Controlador:** `UI/controllers/RegisterSellerController.java`
- Invoca `RegistrarSellerUseCase`.
- Muestra errores de validación campo a campo.
- Al registrarse exitosamente, redirige a la pantalla de inicio de sesión.

**`LoginController`** (actualizado desde US-005):
- Tras el login exitoso, evalúa el rol en `SessionManager`:
  - `BUYER` → `buyer-view.fxml`
  - `SELLER` → `seller-view.fxml`
  - `ADMIN` → `admin-view.fxml`

---

## 5. Plan de Pruebas (TDD)

### `PerfilSellerTest` (dominio)
- `crear_conDatosValidos_debeInstanciarPerfilSeller()`
- `crear_conSelloVacio_debeLanzarDatoEditorialObligatorioException()`
- `crear_conDescripcionVacia_debeLanzarDatoEditorialObligatorioException()`
- `crear_conLogoVacio_debeLanzarDatoEditorialObligatorioException()`

### `UsuarioTest` (ampliado)
- `registrarSeller_conDatosValidos_debeCrearUsuarioConRolSeller()`
- `registrarSeller_debeAsociarPerfilSellerCorrectamente()`

### `RegistrarSellerUseCaseTest` (con Mockito)
- `ejecutar_conEmailNuevo_debeGuardarSellerYRetornarResponse()`
- `ejecutar_conEmailDuplicado_debeLanzarEmailDuplicadoException()`

### `UsuarioRepositoryAdapterTest` (ampliado — `@DataJpaTest`)
- `guardar_sellerConPerfilEditorial_debePersistirCamposEditoriales()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `RegistrarSellerUseCase` es independiente de `RegistrarBuyerUseCase`. Comparten `UsuarioRepository` pero no heredan ni se llaman entre sí. |
| **DDD** | `PerfilSeller` es un Value Object inmutable: una vez creado no puede modificarse. Para cambiar datos editoriales se crea una nueva instancia. |
| **TDD** | Escribir `PerfilSellerTest` y el test de `RegistrarSellerUseCase` antes de implementar las clases. |
| **SOLID (SRP)** | `RegistrarBuyerUseCase` y `RegistrarSellerUseCase` son clases separadas, cada una con su única responsabilidad. No combinar el registro de ambos roles en un único caso de uso. |
| **SOLID (OCP)** | La entidad `Usuario` está abierta a extensión (método fábrica `registrarSeller()`) pero cerrada a modificación de los métodos existentes (`registrarBuyer()`). |
| **SOLID (LSP)** | El mismo `UsuarioRepository` gestiona tanto Buyers como Sellers sin discriminación; el tipo es transparente para el repositorio. |

---

## 📋 Tareas de Implementación

- [ ] 1. `DatoEditorialObligatorioException` no creada *(se usa RuntimeException)*.
- [ ] 2. `PerfilSellerTest` no creado.
- [ ] 3. `PerfilSeller` Value Object no creado *(el plan usa perfil editorial; la implementación actual usa solo el rol para distinguir sellers)*.
- [ ] 4. Tests de `PerfilSeller` pendientes.
- [x] 5. `UserService.registerSeller()` implementado — crea usuario con rol `SELLER`, validación de email único y BCrypt.
- [ ] 6. Tests ampliados de `UsuarioTest` pendientes.
- [ ] 7. `RegistrarSellerUseCaseTest` no creado.
- [x] 8. `UserService.registerSeller()` cumple la función del caso de uso en arquitectura en capas.
- [ ] 9. Tests del servicio pendientes.
- [ ] 10. Script `V2__agregar_perfil_seller.sql` no existe *(Hibernate gestiona el esquema)*.
- [x] 11. No existe mapper separado — `User.java` es la entidad JPA directamente.
- [ ] 12. Test del repositorio pendiente.
- [x] 13. `SecurityConfig` permite rutas `/api/**` públicas actualmente.
- [x] 14. `register-view.fxml` y `RegisterController.java` existen *(UI comparte formulario con Buyer)*.
- [x] 15. `ControllerUI.java` redirige según rol tras login: Buyer → buyer-view, Seller → seller-view, Admin → admin-view.
- [ ] 16. `mvn verify` pendiente.

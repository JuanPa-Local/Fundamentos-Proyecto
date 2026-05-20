# Plan de Implementación: Gestión de Usuarios por el Administrador
**US-008 | Épica 1 – Autenticación & Gestión de Usuarios**

---

## 1. Análisis y Modelado de Dominio

Esta historia introduce el rol `ADMIN` como actor capaz de listar, filtrar y cambiar el estado de las cuentas. No se crean nuevas entidades; se extienden los comportamientos de `Usuario` y se añade el concepto de `AuditoriaAccion` para registrar cada cambio.

### 1.1 Value Object `AuditoriaAccion`

**Archivo:** `domain/user/AuditoriaAccion.java`
- `adminId` (UUID) — quién realizó la acción
- `accion` (String) — descripción: "ACTIVAR_CUENTA" / "DESACTIVAR_CUENTA"
- `usuarioAfectadoId` (UUID)
- `timestamp` (LocalDateTime)

### 1.2 Comportamientos ya existentes en `Usuario` (ampliados)

Los métodos `activar()` y `desactivar()` ya existen desde US-004. En esta historia se envuelve su invocación en el caso de uso que registra la `AuditoriaAccion`.

### 1.3 Interface `AuditoriaRepository` (puerto de salida)

```java
// domain/user/AuditoriaRepository.java
public interface AuditoriaRepository {
    void registrar(AuditoriaAccion accion);
}
```

### 1.4 Criterios de filtrado como Value Object

**`FiltroUsuario`** (en `domain/user/`):
- `String email` — nullable
- `RolUsuario rol` — nullable
- `Boolean activo` — nullable

Encapsula los parámetros de búsqueda sin exponer detalles de la consulta JPA al dominio.

---

## 2. Capa de Aplicación (Casos de Uso)

### `ListarUsuariosUseCase`

**DTO de entrada:** `FiltroUsuario`
**DTO de salida:** `List<UsuarioAdminResponse>` (paginado)

**Flujo:**
1. Verificar que el `usuarioId` en sesión tiene rol `ADMIN`. Si no, lanzar `AccesoDenegadoException`.
2. Delegar la búsqueda a `UsuarioRepository.buscarConFiltros(FiltroUsuario, Pageable)`.
3. Retornar la lista mapeada a `UsuarioAdminResponse`.

### `CambiarEstadoCuentaUseCase`

**DTO de entrada:** `CambiarEstadoCommand`
- `UUID adminId` (de la sesión)
- `UUID usuarioObjetivoId`
- `boolean activar`

**Flujo:**
1. Verificar que `adminId` tiene rol `ADMIN`.
2. Recuperar el `usuarioObjetivo` por id.
3. Llamar `usuario.activar()` o `usuario.desactivar()` según el comando.
4. Persistir el cambio con `UsuarioRepository.guardar()`.
5. Crear y registrar un `AuditoriaAccion` con `AuditoriaRepository.registrar()`.

### Nuevo método en `UsuarioRepository`

```java
Page<Usuario> buscarConFiltros(FiltroUsuario filtro, Pageable pageable);
```

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V4__crear_tabla_auditoria.sql`

```sql
CREATE TABLE auditoria_accion (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_id            UUID NOT NULL,
    accion              VARCHAR(100) NOT NULL,
    usuario_afectado_id UUID NOT NULL,
    timestamp           TIMESTAMP NOT NULL DEFAULT now()
);
```

### 3.2 Repositorio JPA con filtros dinámicos

**`SpringUsuarioRepository`** (actualizado):
- Usar `Specification<UsuarioJpaEntity>` (JPA Criteria API) para construir los filtros dinámicos según los campos no nulos de `FiltroUsuario`.

### 3.3 Entidad y Repositorio de Auditoría

- `AuditoriaJpaEntity.java`: entidad JPA mapeada a `auditoria_accion`.
- `SpringAuditoriaRepository.java`: extiende `JpaRepository`.
- `AuditoriaRepositoryAdapter.java`: implementa `AuditoriaRepository` del dominio.

### 3.4 Control de acceso

**`SecurityConfig`** (actualizado):
- La ruta `/api/admin/usuarios/**` solo accesible para `ADMIN`.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML existente:** `resources/views/admin-view.fxml` (actualizado)
- Tabla con columnas: `nombre`, `email`, `rol`, `estado`.
- Filtros en la parte superior: campo de texto `email`, selector de `rol`, selector de `estado`.
- Botones "Activar" / "Desactivar" para el usuario seleccionado en la tabla.

**Controlador:** `AdminController.java` (actualizado)
- Al cargar: invoca `ListarUsuariosUseCase` con filtros vacíos.
- Al aplicar filtros: invoca `ListarUsuariosUseCase` con el `FiltroUsuario` construido desde los campos de la vista.
- Al cambiar estado: invoca `CambiarEstadoCuentaUseCase`.

---

## 5. Plan de Pruebas (TDD)

### `CambiarEstadoCuentaUseCaseTest` (con Mockito)
- `ejecutar_activar_debeActivarUsuarioYRegistrarAuditoria()`
- `ejecutar_desactivar_debeDesactivarUsuarioYRegistrarAuditoria()`
- `ejecutar_sinRolAdmin_debeLanzarAccesoDenegadoException()`

### `ListarUsuariosUseCaseTest` (con Mockito)
- `ejecutar_sinFiltros_debeRetornarTodosLosUsuarios()`
- `ejecutar_filtrandoPorRolBuyer_debeRetornarSoloBuyers()`
- `ejecutar_sinRolAdmin_debeLanzarAccesoDenegadoException()`

### `AuditoriaRepositoryAdapterTest` (`@DataJpaTest`)
- `registrar_debeGuardarAuditoriaEnBD()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `ListarUsuariosUseCase` y `CambiarEstadoCuentaUseCase` no conocen Specifications de JPA ni detalles de PostgreSQL. Los filtros dinámicos son responsabilidad del adaptador en infraestructura. |
| **DDD** | `AuditoriaAccion` es un Value Object inmutable que representa un hecho ocurrido en el sistema. Una vez registrado, no se modifica. |
| **TDD** | Escribir los tests de ambos casos de uso antes de implementarlos, usando Mockito para simular `UsuarioRepository` y `AuditoriaRepository`. |
| **SOLID (SRP)** | `CambiarEstadoCuentaUseCase` tiene una sola responsabilidad: cambiar el estado y registrar la auditoría. No mezclar con `ListarUsuariosUseCase`. |
| **SOLID (ISP)** | `AuditoriaRepository` es una interface pequeña con un solo método. No añadir métodos de consulta si esta historia no los necesita; extender en historias futuras si se requiere. |

---

## 📋 Tareas de Implementación

- [ ] 1. `AuditoriaAccion` Value Object no creado.
- [ ] 2. `FiltroUsuario` Value Object no creado *(filtros implementados directamente como parámetros de método)*.
- [ ] 3. `AuditoriaRepository` interface no creada.
- [x] 4. `UserRepository` tiene `findByRole()`, `findByActive()`, `findByRoleAndActive()` para filtros dinámicos.
- [ ] 5. Tests de `CambiarEstadoCuentaUseCase` y `ListarUsuariosUseCase` pendientes.
- [x] 6. `UserService.activateUser()` y `deactivateUser()` implementados; `getUsersByRole()` implementado.
- [ ] 7. Script `V4__crear_tabla_auditoria.sql` no existe *(Hibernate gestiona el esquema)*.
- [ ] 8. `AuditoriaJpaEntity` y adaptadores no creados.
- [x] 9. `UserRepository` usa queries derivadas de Spring Data en lugar de `Specification` JPA.
- [ ] 10. Tests de auditoría pendientes.
- [x] 11. `SecurityConfig` existe *(rutas admin aún públicas — pendiente proteger con roles)*.
- [x] 12. `admin-view.fxml` y `AdminController.java` existen con listado, activación y desactivación de usuarios.
- [ ] 13. `mvn verify` pendiente.
- [ ] 13. Verificar que `mvn verify` pasa completo.

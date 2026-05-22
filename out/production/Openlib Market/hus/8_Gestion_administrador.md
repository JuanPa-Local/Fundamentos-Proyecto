# Historia de Usuario: Gestión de Usuarios por el Administrador

**Épica 1 – Autenticación & Gestión de Usuarios**

**Como** administrador del sistema, **quiero** poder listar, buscar, activar y desactivar usuarios, **para** mantener la seguridad y la calidad de la plataforma.

---

## Criterios de Aceptación

**AC-001 – Listado de usuarios con filtros**
Dado que soy administrador y estoy en el panel de gestión de usuarios, cuando busco por correo electrónico, rol o estado de la cuenta, entonces el sistema me muestra los usuarios que coinciden con los filtros aplicados.

**AC-002 – Activar o desactivar una cuenta**
Dado que soy administrador, cuando selecciono un usuario y cambio su estado (activo o inactivo), entonces el sistema aplica el cambio y el usuario no puede iniciar sesión mientras su cuenta esté desactivada.

**AC-003 – Solo el administrador tiene este acceso**
Dado que soy un usuario con rol Comprador o Vendedor, cuando intento acceder a la sección de gestión de usuarios, entonces el sistema me niega el acceso.

**AC-004 – Registro de acciones del administrador**
Dado que el administrador activa o desactiva una cuenta, cuando el sistema aplica el cambio, entonces queda registrada la acción (quién la realizó, qué cambió y cuándo).

---

## Reglas de Negocio

- **RN-ADMIN-001:** Solo los usuarios con rol ADMINISTRADOR pueden acceder a la sección de gestión de usuarios.
- **RN-ADMIN-002:** Un usuario desactivado pierde el acceso inmediato a la plataforma y no puede iniciar sesión hasta que su cuenta sea reactivada.
- **RN-ADMIN-003:** Toda acción de activación o desactivación de cuentas queda registrada en el historial de auditoría con fecha, hora y administrador responsable.
- **RN-ADMIN-004:** El administrador puede filtrar usuarios por correo electrónico, rol (Comprador, Vendedor) y estado (activo, inactivo).

---

## Criterios de Terminación

- [ ] El administrador puede listar y filtrar usuarios por correo, rol y estado
- [ ] El administrador puede activar y desactivar cuentas de usuario
- [ ] Los usuarios desactivados no pueden iniciar sesión
- [ ] Solo el administrador puede acceder a esta sección
- [ ] Cada acción del administrador queda registrada en el historial
- [ ] Las pruebas del módulo de gestión de usuarios están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

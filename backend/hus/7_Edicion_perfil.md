# Historia de Usuario: Edición del Perfil de Usuario

**Épica 1 – Autenticación & Gestión de Usuarios**

**Como** usuario autenticado, **quiero** actualizar la información de mi perfil, **para** tener mis datos correctos disponibles durante el proceso de compra.

---

## Criterios de Aceptación

**AC-001 – Ver mi información de perfil**
Dado que he iniciado sesión, cuando ingreso a la sección de mi perfil, entonces puedo ver mi nombre, foto de perfil y dirección de facturación actuales.

**AC-002 – Editar campos del perfil**
Dado que estoy en la sección de mi perfil, cuando modifico uno o más campos permitidos y guardo los cambios, entonces el sistema actualiza la información y me muestra un mensaje de confirmación.

**AC-003 – Correo electrónico no editable directamente**
Dado que estoy editando mi perfil, cuando intento cambiar mi correo electrónico, entonces el sistema me indica que este campo requiere un proceso de confirmación adicional y no permite cambiarlo directamente.

**AC-004 – Validación de campos al guardar**
Dado que estoy editando mi perfil, cuando ingreso información con un formato inválido en algún campo, entonces el sistema me muestra el error correspondiente antes de guardar.

---

## Reglas de Negocio

- **RN-PROF-001:** Los campos editables del perfil son: nombre, foto de perfil y dirección de facturación.
- **RN-PROF-002:** El correo electrónico no puede modificarse directamente desde el perfil; requiere un proceso de confirmación por correo.
- **RN-PROF-003:** La dirección de facturación se usa como valor predeterminado en el proceso de compra.

---

## Criterios de Terminación

- [ ] El usuario puede ver todos los campos de su perfil
- [ ] El usuario puede editar nombre, foto y dirección de facturación
- [ ] El correo electrónico no puede cambiarse sin confirmación adicional
- [ ] Las validaciones de cada campo muestran mensajes claros
- [ ] Las pruebas del módulo de perfil están escritas y pasan exitosamente
- [ ] El flujo de edición de perfil está documentado
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

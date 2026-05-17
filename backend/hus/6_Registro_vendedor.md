# Historia de Usuario: Registro e Inicio de Sesión del Vendedor

**Épica 1 – Autenticación & Gestión de Usuarios**

**Como** editor o autor, **quiero** registrarme como Vendedor en OpenLib Market, **para** poder publicar y gestionar mis libros digitales en la plataforma.

---

## Criterios de Aceptación

**AC-001 – Registro como Vendedor exitoso**
Dado que soy un editor o autor que desea publicar libros, cuando completo el formulario de registro como Vendedor con mis datos y los del sello editorial, entonces el sistema crea mi cuenta con el perfil de Vendedor correctamente.

**AC-002 – Perfil de Vendedor con información editorial**
Dado que me he registrado como Vendedor, cuando el sistema procesa mi registro, entonces mi perfil incluye el nombre del sello editorial, una descripción y el logo de la editorial.

**AC-003 – Acceso exclusivo al panel de Vendedor**
Dado que he iniciado sesión como Vendedor, cuando intento acceder al panel de administración de mis libros, entonces el sistema me permite entrar sin inconvenientes.

**AC-004 – Comprador no puede acceder al panel de Vendedor**
Dado que he iniciado sesión como Comprador, cuando intento acceder al panel de administración de Vendedores, entonces el sistema me niega el acceso y me muestra un mensaje indicando que no tengo permiso.

---

## Reglas de Negocio

- **RN-AUTH-009:** Al registrarse como Vendedor, el sistema asigna automáticamente el rol de VENDEDOR a la cuenta.
- **RN-AUTH-010:** El perfil del Vendedor debe incluir nombre del sello editorial, descripción y logo.
- **RN-AUTH-011:** Solo los usuarios con rol VENDEDOR pueden acceder a la sección de gestión de publicaciones.
- **RN-AUTH-012:** Los roles de Comprador y Vendedor son independientes; un Comprador no puede operar como Vendedor a menos que se registre con ese rol.

---

## Criterios de Terminación

- [ ] El registro de Vendedor crea correctamente la cuenta con el rol correspondiente
- [ ] El perfil del Vendedor incluye nombre editorial, descripción y logo
- [ ] Solo los usuarios con rol Vendedor pueden acceder al panel de gestión
- [ ] Los Compradores son bloqueados al intentar acceder al panel de Vendedor
- [ ] Las pruebas del módulo de registro Vendedor están escritas y pasan exitosamente
- [ ] Los roles y permisos están documentados
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

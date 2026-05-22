# Historia de Usuario: Aprobación y Rechazo de Libros por el Administrador

**Épica 2 – Catálogo de Libros**

**Como** administrador, **quiero** revisar y aprobar o rechazar los libros enviados por los Vendedores, **para** garantizar la calidad y la legalidad del contenido publicado en la plataforma.

---

## Criterios de Aceptación

**AC-001 – Revisar libros pendientes**
Dado que soy administrador, cuando ingreso al panel de revisión de contenido, entonces puedo ver todos los libros que están en estado "Pendiente de aprobación" con su información completa.

**AC-002 – Aprobar un libro**
Dado que soy administrador y he revisado un libro, cuando lo apruebo, entonces el sistema cambia su estado a "Aprobado" y el libro aparece visible en el catálogo público.

**AC-003 – Rechazar un libro con motivo**
Dado que soy administrador y he revisado un libro, cuando lo rechazo e ingreso el motivo del rechazo, entonces el sistema cambia su estado a "Rechazado" y el libro no aparece en el catálogo público.

**AC-004 – Notificación al Vendedor**
Dado que el administrador aprueba o rechaza un libro, cuando el sistema registra el cambio de estado, entonces el Vendedor recibe una notificación informándole la decisión y, en caso de rechazo, el motivo.

**AC-005 – Solo el administrador puede aprobar o rechazar**
Dado que soy un usuario con rol Comprador o Vendedor, cuando intento acceder al panel de revisión de libros, entonces el sistema me niega el acceso.

---

## Reglas de Negocio

- **RN-ADMIN-009:** Solo los usuarios con rol ADMINISTRADOR pueden aprobar o rechazar libros.
- **RN-ADMIN-010:** Un libro solo es visible en el catálogo público si tiene estado "Aprobado".
- **RN-ADMIN-011:** El rechazo de un libro requiere obligatoriamente un motivo escrito que se comunica al Vendedor.
- **RN-ADMIN-012:** Cada acción de aprobación o rechazo queda registrada en el historial de auditoría.

---

## Criterios de Terminación

- [ ] El administrador puede ver todos los libros pendientes de aprobación
- [ ] El administrador puede aprobar libros, los cuales quedan visibles en el catálogo
- [ ] El administrador puede rechazar libros con motivo, los cuales no aparecen en el catálogo
- [ ] El Vendedor recibe una notificación al cambiar el estado de su libro
- [ ] Solo el administrador puede acceder al panel de revisión
- [ ] Cada acción queda registrada en el historial de auditoría
- [ ] Las pruebas del módulo de aprobación están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

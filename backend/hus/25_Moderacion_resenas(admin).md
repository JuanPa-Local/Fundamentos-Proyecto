# Historia de Usuario: Moderación de Reseñas por el Administrador

**Épica 6 – Reviews & Calificaciones**

**Como** administrador, **quiero** aprobar, rechazar u ocultar reseñas inapropiadas, **para** mantener la calidad y el respeto en el contenido generado por los usuarios de la plataforma.

---

## Criterios de Aceptación

**AC-001 – Ver reseñas pendientes de moderación**
Dado que soy administrador, cuando accedo al panel de moderación de reseñas, entonces puedo ver todas las reseñas en estado "Pendiente" con su contenido, calificación y el usuario que la escribió.

**AC-002 – Aprobar una reseña**
Dado que soy administrador y estoy revisando una reseña, cuando la apruebo, entonces el sistema cambia su estado a "Aprobada" y queda visible públicamente en la página del libro correspondiente.

**AC-003 – Rechazar u ocultar una reseña con motivo**
Dado que soy administrador y estoy revisando una reseña inapropiada, cuando la rechazo u oculto e ingreso el motivo, entonces el sistema cambia su estado y la reseña deja de ser visible públicamente.

**AC-004 – Solo reseñas aprobadas son públicas**
Dado que existen reseñas en distintos estados, cuando un visitante o Comprador consulta la página de un libro, entonces solo puede ver las reseñas que han sido aprobadas por el administrador.

**AC-005 – Solo el administrador puede moderar**
Dado que soy un usuario con rol Comprador o Vendedor, cuando intento acceder al panel de moderación de reseñas, entonces el sistema me niega el acceso.

---

## Reglas de Negocio

- **RN-REV-005:** Solo los usuarios con rol ADMINISTRADOR pueden aprobar, rechazar u ocultar reseñas.
- **RN-REV-006:** Únicamente las reseñas con estado "Aprobada" son visibles en el catálogo público.
- **RN-REV-007:** El rechazo u ocultamiento de una reseña requiere un motivo registrado en el sistema.
- **RN-REV-008:** Cada acción de moderación queda registrada en el historial de auditoría con fecha, administrador y motivo.

---

## Criterios de Terminación

- [ ] El administrador puede ver todas las reseñas pendientes de moderación
- [ ] El administrador puede aprobar reseñas, las cuales quedan visibles públicamente
- [ ] El administrador puede rechazar u ocultar reseñas con motivo registrado
- [ ] Solo las reseñas aprobadas aparecen en el catálogo
- [ ] Solo el administrador puede acceder al panel de moderación
- [ ] Cada acción de moderación queda registrada en el historial de auditoría
- [ ] Las pruebas del módulo de moderación están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

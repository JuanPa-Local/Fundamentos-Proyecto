# Historia de Usuario: Historial de Órdenes

**Épica 5 – Favoritos, Historial & Recomendaciones**

**Como** Comprador, **quiero** consultar el historial de todas mis órdenes con sus fechas y detalles, **para** llevar un registro completo del material que he adquirido en la plataforma.

---

## Criterios de Aceptación

**AC-001 – Ver el historial de órdenes**
Dado que soy un Comprador autenticado con al menos una orden registrada, cuando accedo a la sección de historial de compras, entonces puedo ver un listado de todas mis órdenes ordenadas de la más reciente a la más antigua.

**AC-002 – Ver el detalle de cada orden**
Dado que estoy en mi historial de compras, cuando selecciono una orden específica, entonces puedo ver los libros incluidos en esa orden, la fecha de adquisición, el estado de la orden y el método de pago utilizado.

**AC-003 – Historial paginado**
Dado que tengo muchas órdenes registradas, cuando las visualizo en el historial, entonces el sistema las muestra de forma paginada para facilitar la navegación.

**AC-004 – Historial vacío**
Dado que soy un Comprador autenticado pero no he realizado ninguna compra, cuando accedo al historial, entonces el sistema me muestra un mensaje indicando que aún no tengo órdenes registradas.

---

## Reglas de Negocio

- **RN-FAV-005:** El historial de órdenes es exclusivo de cada Comprador y no es visible para otros usuarios.
- **RN-FAV-006:** El historial muestra todas las órdenes en estado "Completada" ordenadas por fecha, de la más reciente a la más antigua.
- **RN-FAV-007:** Cada registro del historial incluye: número de orden, fecha, libros adquiridos, estado y método de pago.

---

## Criterios de Terminación

- [ ] El Comprador puede ver su historial completo de órdenes
- [ ] Cada orden muestra sus detalles: libros, fecha, estado y método de pago
- [ ] El historial se presenta de forma paginada
- [ ] El sistema muestra un mensaje claro cuando el historial está vacío
- [ ] Las pruebas del módulo de historial están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

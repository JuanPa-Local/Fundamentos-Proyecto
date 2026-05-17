# Historia de Usuario: Carrito de Compras Persistente

**Épica 3 – Carrito de Compras & Checkout**

**Como** Comprador autenticado, **quiero** agregar libros a un carrito que se mantenga guardado entre sesiones, **para** poder retomar mi selección sin perderla si cierro el navegador o salgo de la plataforma.

---

## Criterios de Aceptación

**AC-001 – Agregar un libro al carrito**
Dado que soy un Comprador autenticado y estoy viendo el detalle de un libro, cuando hago clic en "Agregar al carrito", entonces el libro se añade a mi carrito y puedo verlo reflejado inmediatamente.

**AC-002 – Carrito disponible al volver a la plataforma**
Dado que tenía libros en el carrito y cerré sesión o el navegador, cuando vuelvo a iniciar sesión dentro de los próximos 7 días, entonces mi carrito todavía muestra los libros que había seleccionado.

**AC-003 – No se puede agregar un libro ya adquirido**
Dado que ya tengo un libro en mi biblioteca personal, cuando intento agregarlo al carrito nuevamente, entonces el sistema me informa que ya cuento con ese libro y no lo añade.

**AC-004 – No se puede agregar el mismo libro dos veces**
Dado que ya tengo un libro en el carrito, cuando intento agregarlo de nuevo, entonces el sistema me informa que ese libro ya está en mi carrito.

**AC-005 – Ver el contenido del carrito**
Dado que soy un Comprador autenticado, cuando accedo a mi carrito, entonces puedo ver todos los libros que he agregado con su título e información básica.

---

## Reglas de Negocio

- **RN-CART-001:** El carrito de compras se mantiene guardado por un máximo de 7 días. Pasado ese tiempo, se vacía automáticamente.
- **RN-CART-002:** No se puede agregar al carrito un libro que el Comprador ya tiene en su biblioteca personal.
- **RN-CART-003:** Cada libro puede aparecer solo una vez en el carrito; no se permiten duplicados.
- **RN-CART-004:** Solo los usuarios autenticados con rol Comprador pueden tener y usar un carrito de compras.

---

## Criterios de Terminación

- [ ] El Comprador puede agregar libros al carrito
- [ ] El carrito persiste entre sesiones durante un máximo de 7 días
- [ ] El sistema impide agregar libros ya adquiridos o duplicados
- [ ] El Comprador puede ver el contenido actualizado de su carrito
- [ ] Las pruebas del módulo de carrito están escritas y pasan exitosamente
- [ ] El tiempo de expiración del carrito está documentado
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

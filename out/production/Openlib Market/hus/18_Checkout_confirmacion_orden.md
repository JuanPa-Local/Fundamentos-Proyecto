# Historia de Usuario: Confirmación de Orden de Compra

**Épica 3 – Carrito de Compras & Checkout**

**Como** Comprador, **quiero** confirmar mi orden y recibir un número de confirmación, **para** tener un registro de mi adquisición y poder acceder a mis libros inmediatamente.

---

## Criterios de Aceptación

**AC-001 – Confirmación exitosa de la orden**
Dado que soy un Comprador con un carrito activo y he completado los pasos anteriores del proceso de compra, cuando confirmo la orden, entonces el sistema la registra exitosamente y me muestra el número de confirmación junto con un resumen de lo adquirido.

**AC-002 – Acceso a los libros tras la confirmación**
Dado que he confirmado mi orden exitosamente, cuando el sistema la registra, entonces los libros adquiridos quedan disponibles de inmediato en mi biblioteca personal.

**AC-003 – Carrito vacío no puede confirmarse**
Dado que tengo el carrito vacío, cuando intento confirmar la orden, entonces el sistema me informa que no hay libros en el carrito y no permite continuar.

**AC-004 – Resumen de la orden visible**
Dado que he confirmado mi orden, cuando el sistema la procesa, entonces me muestra un resumen que incluye el número de la orden, los libros adquiridos y la fecha de la transacción.

---

## Reglas de Negocio

- **RN-CART-013:** Solo se puede confirmar una orden si el carrito tiene al menos un libro.
- **RN-CART-014:** Al confirmar la orden, esta queda registrada con estado "Completada" en el sistema.
- **RN-CART-015:** Una vez confirmada la orden, los libros incluidos pasan automáticamente a la biblioteca personal del Comprador.
- **RN-CART-016:** El número de confirmación de la orden es único y sirve como identificador de la transacción.
- **RN-CART-017:** Cada orden queda registrada en la base de datos con fecha, usuario, libros adquiridos y método de pago seleccionado.

---

## Criterios de Terminación

- [ ] El Comprador puede confirmar su orden cuando el carrito tiene libros y los pasos anteriores están completos
- [ ] El sistema genera y muestra un número de confirmación único
- [ ] Los libros de la orden pasan inmediatamente a la biblioteca personal
- [ ] El sistema impide confirmar órdenes con el carrito vacío
- [ ] El resumen de la orden es visible para el Comprador
- [ ] La orden queda registrada en la base de datos
- [ ] Las pruebas del flujo de confirmación están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

# Historia de Usuario: Selección del Método de Pago

**Épica 3 – Carrito de Compras & Checkout**

**Como** Comprador, **quiero** seleccionar un método de pago durante el proceso de compra, **para** completar el flujo de adquisición de libros como en cualquier plataforma de comercio en línea.

---

## Criterios de Aceptación

**AC-001 – Ver las opciones de pago disponibles**
Dado que soy un Comprador y estoy en el paso de método de pago, cuando llego a esta pantalla, entonces el sistema me muestra las opciones disponibles: tarjeta, transferencia y donación.

**AC-002 – Seleccionar un método de pago**
Dado que estoy en el paso de selección de método de pago, cuando elijo una de las opciones disponibles y continúo, entonces el sistema guarda mi selección y me permite avanzar al siguiente paso.

**AC-003 – Método inválido rechazado**
Dado que estoy en el paso de selección de método de pago, cuando intento continuar sin haber seleccionado ninguna opción, entonces el sistema me indica que debo elegir un método de pago para continuar.

**AC-004 – Monto de la transacción**
Dado que estoy en el paso de método de pago, cuando veo el resumen de mi orden, entonces el monto de la transacción aparece como $0.00 o un valor simbólico, ya que los libros de la plataforma son de acceso gratuito o de costo mínimo.

---

## Reglas de Negocio

- **RN-CART-009:** Los métodos de pago disponibles son: tarjeta, transferencia y donación.
- **RN-CART-010:** La selección del método de pago es obligatoria para continuar con el proceso de compra.
- **RN-CART-011:** El monto de la transacción es siempre $0.00 o simbólico; el objetivo de este paso es registrar el flujo completo de adquisición.
- **RN-CART-012:** La selección del método de pago se almacena temporalmente en la sesión activa del usuario hasta que se confirme la orden.

---

## Criterios de Terminación

- [ ] El Comprador puede ver y seleccionar entre los métodos de pago disponibles
- [ ] El sistema impide avanzar sin seleccionar un método de pago
- [ ] El monto de la transacción se muestra correctamente como $0.00 o valor simbólico
- [ ] La selección del método de pago queda guardada en la sesión
- [ ] El flujo de este paso está documentado con un diagrama de secuencia
- [ ] Las pruebas de selección de método de pago están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

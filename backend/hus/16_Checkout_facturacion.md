# Historia de Usuario: Dirección de Facturación en el Proceso de Compra

**Épica 3 – Carrito de Compras & Checkout**

**Como** Comprador, **quiero** ingresar o confirmar mi dirección de facturación durante el proceso de compra, **para** que quede registrada la información de mi transacción de forma completa.

---

## Criterios de Aceptación

**AC-001 – Solicitud de dirección durante el proceso de compra**
Dado que soy un Comprador y he iniciado el proceso de compra, cuando llego al paso de dirección de facturación, entonces el sistema me solicita que confirme o ingrese mi dirección.

**AC-002 – Dirección predeterminada del perfil**
Dado que tengo una dirección registrada en mi perfil, cuando llego al paso de dirección de facturación, entonces el sistema muestra mi dirección del perfil como valor predeterminado, permitiéndome modificarla si lo deseo.

**AC-003 – Validación de campos obligatorios**
Dado que estoy en el paso de dirección de facturación, cuando intento continuar sin completar todos los campos obligatorios, entonces el sistema me indica cuál o cuáles campos faltan.

**AC-004 – Dirección guardada correctamente**
Dado que he ingresado o confirmado mi dirección de facturación, cuando continúo al siguiente paso del proceso de compra, entonces el sistema guarda la dirección y la asocia a mi orden.

---

## Reglas de Negocio

- **RN-CART-005:** La dirección de facturación es obligatoria para completar el proceso de compra.
- **RN-CART-006:** Si el Comprador tiene una dirección guardada en su perfil, esta se usa como valor predeterminado en el formulario de facturación.
- **RN-CART-007:** La información de la dirección de facturación se almacena de forma segura y protegida.
- **RN-CART-008:** Los campos obligatorios de la dirección de facturación son: calle, ciudad, departamento y país.

---

## Criterios de Terminación

- [ ] El paso de dirección de facturación solicita la información correctamente
- [ ] La dirección del perfil se carga automáticamente como valor predeterminado
- [ ] El sistema bloquea el avance si hay campos obligatorios vacíos
- [ ] La dirección queda guardada y asociada a la orden
- [ ] La información de facturación se almacena de forma segura
- [ ] Las pruebas de este paso del proceso de compra están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

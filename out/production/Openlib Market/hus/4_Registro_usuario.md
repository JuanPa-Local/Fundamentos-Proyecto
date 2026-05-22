# Historia de Usuario: Registro de Usuario Comprador

**Épica 1 – Autenticación & Gestión de Usuarios**

**Como** visitante de OpenLib Market, **quiero** registrarme con mi correo electrónico y una contraseña, **para** poder acceder al catálogo y adquirir libros digitales.

---

## Criterios de Aceptación

**AC-001 – Formulario de registro válido**
Dado que soy un visitante en la página de registro, cuando diligencio el formulario con un correo electrónico que no ha sido registrado previamente, una contraseña de al menos 8 caracteres y mi nombre completo, entonces el sistema crea mi cuenta exitosamente.

**AC-002 – Correo duplicado no permitido**
Dado que intento registrarme con un correo que ya existe en el sistema, cuando envío el formulario, entonces el sistema me informa que ese correo ya está en uso y me impide continuar.

**AC-003 – Contraseña insegura rechazada**
Dado que estoy en el formulario de registro, cuando ingreso una contraseña con menos de 8 caracteres, entonces el sistema me indica que la contraseña debe tener mínimo 8 caracteres antes de permitir el registro.

**AC-004 – Confirmación de registro enviada**
Dado que he completado el registro exitosamente, cuando el sistema procesa mi solicitud, entonces recibo un correo electrónico de confirmación de mi nueva cuenta.

**AC-005 – Nombre obligatorio**
Dado que estoy en el formulario de registro, cuando intento enviarlo sin haber ingresado mi nombre, entonces el sistema me indica que el nombre es un campo obligatorio.

---

## Reglas de Negocio

- **RN-AUTH-001:** El correo electrónico debe ser único en la plataforma; no pueden existir dos cuentas con el mismo correo.
- **RN-AUTH-002:** La contraseña debe tener mínimo 8 caracteres. La contraseña nunca se guarda en texto visible; el sistema la almacena de forma protegida.
- **RN-AUTH-003:** El nombre completo es obligatorio para completar el registro.
- **RN-AUTH-004:** El correo electrónico tiene un formato válido (debe contener @ y un dominio).

---

## Criterios de Terminación

- [ ] El formulario de registro valida correctamente correo, contraseña y nombre
- [ ] No es posible registrar dos cuentas con el mismo correo
- [ ] La contraseña se almacena de forma segura, nunca en texto visible
- [ ] El correo de confirmación se envía al registrarse exitosamente
- [ ] Las pruebas del módulo de registro están escritas y pasan exitosamente
- [ ] El flujo de registro está documentado
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

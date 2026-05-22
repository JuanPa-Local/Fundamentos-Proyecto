# Historia de Usuario: Inicio de Sesión

**Épica 1 – Autenticación & Gestión de Usuarios**

**Como** usuario registrado, **quiero** iniciar sesión en OpenLib Market, **para** acceder a las funcionalidades protegidas de la plataforma sin tener que volver a identificarme en cada acción.

---

## Criterios de Aceptación

**AC-001 – Inicio de sesión exitoso**
Dado que soy un usuario registrado, cuando ingreso mi correo y contraseña correctos, entonces el sistema me autentica y me permite acceder a la plataforma.

**AC-002 – Credenciales incorrectas rechazadas**
Dado que intento iniciar sesión, cuando ingreso un correo o contraseña incorrectos, entonces el sistema me muestra un mensaje de error y no me permite acceder.

**AC-003 – Sesión activa mientras navego**
Dado que he iniciado sesión exitosamente, cuando navego por las diferentes secciones de la plataforma, entonces el sistema recuerda que estoy autenticado sin pedirme que ingrese mis datos nuevamente.

**AC-004 – Sesión con tiempo límite**
Dado que he iniciado sesión, cuando pasan más de 7 días sin actividad o más de 1 hora desde el inicio de sesión sin renovación, entonces el sistema cierra mi sesión automáticamente por seguridad.

**AC-005 – Acceso denegado sin sesión activa**
Dado que intento acceder a una sección protegida de la plataforma sin haber iniciado sesión, cuando el sistema detecta que no tengo una sesión válida, entonces me redirige a la página de inicio de sesión.

---

## Reglas de Negocio

- **RN-AUTH-005:** La sesión activa de un usuario tiene una duración máxima de 1 hora; pasado ese tiempo, debe renovarse o volver a iniciar sesión.
- **RN-AUTH-006:** El sistema puede recordar la sesión del usuario hasta por 7 días mediante un mecanismo de renovación automática.
- **RN-AUTH-007:** Las secciones protegidas de la plataforma solo son accesibles para usuarios con sesión activa válida.
- **RN-AUTH-008:** El sistema registra las sesiones activas para poder cerrarlas de forma centralizada si es necesario.

---

## Criterios de Terminación

- [ ] El inicio de sesión funciona correctamente con credenciales válidas
- [ ] Las credenciales incorrectas son rechazadas con un mensaje claro
- [ ] La sesión se mantiene activa durante la navegación dentro del tiempo establecido
- [ ] Las secciones protegidas son inaccesibles sin sesión válida
- [ ] Las pruebas del módulo de autenticación están escritas y pasan exitosamente
- [ ] El flujo de inicio de sesión está documentado
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

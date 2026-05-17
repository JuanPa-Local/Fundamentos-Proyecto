# Historia de Usuario: Descarga Segura de Libros

**Épica 4 – Biblioteca Personal & Motor de Descargas**

**Como** Comprador, **quiero** descargar mis libros mediante un enlace seguro y exclusivo para mí, **para** que el archivo no pueda ser compartido o utilizado por otras personas sin autorización.

---

## Criterios de Aceptación

**AC-001 – Enlace de descarga generado**
Dado que soy un Comprador con un libro en mi biblioteca, cuando solicito descargarlo, entonces el sistema genera un enlace de descarga único y temporal que solo funciona para mí.

**AC-002 – Enlace con tiempo límite**
Dado que se ha generado un enlace de descarga, cuando han pasado más de 15 minutos desde que se generó, entonces el enlace deja de funcionar y el sistema me informa que ha expirado. Puedo solicitar uno nuevo desde mi biblioteca.

**AC-003 – Enlace de uso único**
Dado que he utilizado el enlace de descarga para descargar el archivo, cuando intento usar ese mismo enlace nuevamente, entonces el sistema lo rechaza y me informa que ya fue utilizado.

**AC-004 – Enlace no transferible**
Dado que se generó un enlace de descarga para un Comprador específico, cuando otra persona intenta usar ese mismo enlace, entonces el sistema lo rechaza y no permite la descarga.

**AC-005 – Registro de cada descarga**
Dado que un Comprador descarga un libro, cuando el sistema procesa la descarga, entonces queda un registro que incluye quién descargó, qué libro, cuándo y desde qué lugar de conexión.

---

## Reglas de Negocio

- **RN-LIB-005:** El enlace de descarga es único por solicitud, tiene una duración máxima de 15 minutos y es de un solo uso.
- **RN-LIB-006:** Solo el Comprador que adquirió el libro puede generar y usar enlaces de descarga para ese libro.
- **RN-LIB-007:** Cada descarga queda registrada en el sistema con fecha, hora, usuario y origen de conexión.
- **RN-LIB-008:** Un Comprador puede solicitar un nuevo enlace de descarga desde su biblioteca cuantas veces lo necesite, pero cada enlace generado caduca según las reglas anteriores.

---

## Criterios de Terminación

- [ ] El sistema genera un enlace de descarga único y temporal al solicitarlo
- [ ] El enlace expira automáticamente a los 15 minutos de haberse generado
- [ ] El enlace solo puede usarse una vez; un segundo uso es rechazado
- [ ] Solo el Comprador propietario puede usar su enlace de descarga
- [ ] Cada descarga queda registrada con los datos del usuario, libro, fecha y origen
- [ ] Las pruebas del módulo de descarga están escritas y pasan exitosamente
- [ ] El registro de descargas está documentado
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

# Historia de Usuario: Reseña de un Libro Adquirido

**Épica 6 – Reviews & Calificaciones**

**Como** Comprador que ha adquirido un libro, **quiero** dejar una reseña con calificación y comentario, **para** compartir mi experiencia con otros usuarios de la plataforma.

---

## Criterios de Aceptación

**AC-001 – Dejar una reseña exitosamente**
Dado que soy un Comprador con un libro en mi biblioteca, cuando accedo a la opción de reseñar ese libro, ingreso una calificación de 1 a 5 estrellas y escribo un comentario, entonces el sistema guarda mi reseña y queda en espera de aprobación.

**AC-002 – Solo se pueden reseñar libros adquiridos**
Dado que soy un Comprador, cuando intento reseñar un libro que no tengo en mi biblioteca personal, entonces el sistema me impide hacerlo y me informa que solo se pueden reseñar libros adquiridos.

**AC-003 – Una sola reseña por libro**
Dado que ya he dejado una reseña para un libro, cuando intento reseñarlo nuevamente, entonces el sistema me informa que ya existe una reseña mía para ese libro.

**AC-004 – Calificación obligatoria**
Dado que estoy escribiendo una reseña, cuando intento enviarla sin seleccionar una calificación, entonces el sistema me indica que la calificación es obligatoria.

**AC-005 – Reseña pendiente de aprobación**
Dado que he enviado una reseña, cuando el sistema la registra, entonces queda en estado "Pendiente" hasta que el administrador la revise y apruebe.

---

## Reglas de Negocio

- **RN-REV-001:** Solo los Compradores que tienen el libro en su biblioteca personal pueden dejar una reseña para ese libro.
- **RN-REV-002:** Cada Comprador puede dejar únicamente una reseña por libro.
- **RN-REV-003:** La calificación debe ser un valor entero entre 1 y 5 estrellas y es obligatoria.
- **RN-REV-004:** Las reseñas enviadas inician en estado "Pendiente" y deben ser aprobadas por el administrador para ser visibles públicamente.

---

## Criterios de Terminación

- [ ] El Comprador puede dejar una reseña con calificación y comentario para un libro adquirido
- [ ] El sistema impide reseñar libros no adquiridos
- [ ] El sistema impide dejar más de una reseña por libro
- [ ] La calificación es obligatoria y debe estar entre 1 y 5 estrellas
- [ ] Las reseñas quedan en estado "Pendiente" hasta ser aprobadas
- [ ] Las pruebas del módulo de reseñas están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

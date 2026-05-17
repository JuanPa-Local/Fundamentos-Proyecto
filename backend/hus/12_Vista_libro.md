# Historia de Usuario: Vista de Detalle de un Libro

**Épica 2 – Catálogo de Libros**

**Como** Comprador, **quiero** ver la página de detalle de un libro con toda su información, calificación y reseñas, **para** tomar una decisión informada antes de adquirirlo.

---

## Criterios de Aceptación

**AC-001 – Información completa del libro visible**
Dado que estoy en el catálogo y hago clic en un libro, cuando se carga la página de detalle, entonces puedo ver el título, autor, ISBN, descripción, portada, categoría y etiquetas del libro.

**AC-002 – Calificación promedio visible**
Dado que estoy en la página de detalle de un libro, cuando el libro tiene reseñas registradas, entonces veo la calificación promedio expresada en estrellas.

**AC-003 – Reseñas de otros usuarios visibles**
Dado que estoy en la página de detalle de un libro, cuando el libro tiene reseñas de otros compradores, entonces puedo leerlas de forma paginada.

**AC-004 – Libro no encontrado**
Dado que intento acceder al detalle de un libro que no existe o fue eliminado, cuando el sistema procesa la solicitud, entonces me muestra un mensaje indicando que el libro no está disponible.

---

## Reglas de Negocio

- **RN-CAT-012:** Solo se pueden consultar los detalles de libros aprobados y visibles en el catálogo público.
- **RN-CAT-013:** La calificación promedio se calcula a partir de todas las reseñas aprobadas que tiene el libro.
- **RN-CAT-014:** Las reseñas visibles en el detalle del libro son únicamente las que han sido aprobadas por el administrador.
- **RN-CAT-015:** Las reseñas se muestran de forma paginada para facilitar la lectura cuando hay muchas.

---

## Criterios de Terminación

- [ ] La página de detalle muestra toda la información del libro
- [ ] La calificación promedio se muestra correctamente cuando hay reseñas
- [ ] Las reseñas aprobadas son visibles y se presentan de forma paginada
- [ ] El sistema muestra un mensaje claro cuando el libro no existe
- [ ] Las pruebas del módulo de detalle de libro están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

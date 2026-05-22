# Historia de Usuario: Exploración del Catálogo con Búsqueda

**Épica 2 – Catálogo de Libros**

**Como** visitante o Comprador, **quiero** ver el catálogo de libros disponibles y buscar por título, autor o ISBN, **para** encontrar rápidamente el material que necesito.

---

## Criterios de Aceptación

**AC-001 – Catálogo visible sin necesidad de cuenta**
Dado que soy un visitante en la plataforma, cuando ingreso a la sección del catálogo, entonces puedo ver los libros disponibles sin necesidad de haber iniciado sesión.

**AC-002 – Búsqueda por título, autor o ISBN**
Dado que estoy en el catálogo, cuando escribo un término de búsqueda en el buscador, entonces el sistema me muestra los libros que coinciden con ese título, autor o ISBN.

**AC-003 – Resultados paginados**
Dado que la búsqueda arroja múltiples resultados, cuando el sistema los muestra, entonces los presenta de forma paginada para facilitar la navegación.

**AC-004 – Búsqueda sin resultados**
Dado que realizo una búsqueda con un término que no corresponde a ningún libro, cuando el sistema procesa la búsqueda, entonces me informa que no se encontraron resultados para ese término.

**AC-005 – Respuesta rápida del catálogo**
Dado que hay muchos libros registrados en la plataforma, cuando realizo una búsqueda, entonces el sistema me muestra los resultados en menos de 1.5 segundos.

---

## Reglas de Negocio

- **RN-CAT-005:** Solo los libros aprobados por el administrador son visibles en el catálogo público.
- **RN-CAT-006:** La búsqueda funciona sobre los campos de título, autor e ISBN de manera simultánea.
- **RN-CAT-007:** Los resultados del catálogo se presentan en páginas para no sobrecargar la vista.
- **RN-CAT-008:** El catálogo debe responder en menos de 1.5 segundos incluso con grandes volúmenes de libros registrados.

---

## Criterios de Terminación

- [ ] El catálogo es visible para visitantes sin cuenta registrada
- [ ] La búsqueda funciona correctamente por título, autor e ISBN
- [ ] Los resultados se muestran de forma paginada
- [ ] El sistema informa cuando no hay resultados para una búsqueda
- [ ] El catálogo responde en menos de 1.5 segundos bajo condiciones de carga normal
- [ ] Las pruebas de búsqueda están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

# Historia de Usuario: Publicación de un Libro por el Vendedor

**Épica 2 – Catálogo de Libros**

**Como** Vendedor, **quiero** publicar un libro digital con su información completa y el archivo correspondiente, **para** que esté disponible en el catálogo de OpenLib Market y los compradores puedan acceder a él.

---

## Criterios de Aceptación

**AC-001 – Publicación exitosa de un libro**
Dado que soy un Vendedor autenticado, cuando completo el formulario de publicación con título, autor, ISBN, categoría, etiquetas y el archivo del libro, entonces el sistema registra el libro y me confirma que fue enviado para revisión.

**AC-002 – ISBN único en la plataforma**
Dado que intento publicar un libro, cuando ingreso un ISBN que ya existe en el catálogo, entonces el sistema me muestra un mensaje indicando que ese ISBN ya está registrado y me impide continuar.

**AC-003 – Restricción de tamaño del archivo**
Dado que estoy cargando el archivo del libro, cuando el archivo supera los 100 MB de tamaño, entonces el sistema rechaza la carga y me informa el límite permitido.

**AC-004 – Formato de archivo válido**
Dado que estoy cargando el archivo del libro, cuando el archivo no es PDF ni EPUB, entonces el sistema lo rechaza e indica los formatos aceptados.

**AC-005 – Libro en espera de aprobación**
Dado que he publicado un libro exitosamente, cuando consulto el estado de mi publicación, entonces aparece como "En revisión" hasta que el administrador lo apruebe o rechace.

---

## Reglas de Negocio

- **RN-CAT-001:** El ISBN debe ser único en toda la plataforma; no pueden existir dos libros con el mismo ISBN.
- **RN-CAT-002:** El archivo del libro debe estar en formato PDF o EPUB y no puede superar los 100 MB.
- **RN-CAT-003:** Todo libro publicado por un Vendedor inicia en estado "Pendiente de aprobación" y no es visible en el catálogo público hasta que el administrador lo apruebe.
- **RN-CAT-004:** Los campos obligatorios para publicar un libro son: título, autor, ISBN, categoría y archivo.

---

## Criterios de Terminación

- [ ] El Vendedor puede publicar un libro con todos sus campos obligatorios
- [ ] El sistema rechaza ISBNs duplicados
- [ ] El sistema rechaza archivos que superen 100 MB o tengan formato distinto a PDF/EPUB
- [ ] El libro queda en estado "Pendiente de aprobación" tras ser publicado
- [ ] Las pruebas del módulo de publicación están escritas y pasan exitosamente
- [ ] El almacenamiento de archivos está configurado y funciona correctamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

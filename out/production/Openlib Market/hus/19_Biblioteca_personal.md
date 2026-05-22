# Historia de Usuario: Biblioteca Personal del Comprador

**Épica 4 – Biblioteca Personal & Motor de Descargas**

**Como** Comprador, **quiero** ver todos los libros que he adquirido en mi biblioteca personal, **para** acceder fácilmente a ellos en cualquier momento.

---

## Criterios de Aceptación

**AC-001 – Ver libros adquiridos en la biblioteca**
Dado que soy un Comprador autenticado y he realizado al menos una adquisición, cuando ingreso a mi biblioteca personal, entonces puedo ver todos los libros que he adquirido con su portada, título, autor y fecha de adquisición.

**AC-002 – Biblioteca vacía**
Dado que soy un Comprador autenticado pero aún no he adquirido ningún libro, cuando ingreso a mi biblioteca personal, entonces el sistema me muestra un mensaje indicando que mi biblioteca está vacía.

**AC-003 – Ordenar los libros de la biblioteca**
Dado que estoy en mi biblioteca personal, cuando elijo ordenar mis libros por fecha de adquisición o por título, entonces el sistema reorganiza la lista según el criterio seleccionado.

**AC-004 – Navegación paginada**
Dado que tengo muchos libros en mi biblioteca, cuando los visualizo, entonces el sistema los presenta de forma paginada para facilitar la navegación.

---

## Reglas de Negocio

- **RN-LIB-001:** Solo el Comprador propietario puede ver el contenido de su biblioteca personal; no es visible para otros usuarios.
- **RN-LIB-002:** Los libros de la biblioteca son los que han sido adquiridos mediante una orden confirmada.
- **RN-LIB-003:** La biblioteca puede ordenarse por fecha de adquisición (más reciente o más antigua) o por título (A-Z o Z-A).
- **RN-LIB-004:** Los libros de la biblioteca se muestran con paginación cuando hay más de cierta cantidad.

---

## Criterios de Terminación

- [ ] El Comprador puede ver todos sus libros adquiridos en la biblioteca
- [ ] La biblioteca muestra fecha de adquisición, portada, título y autor de cada libro
- [ ] El sistema informa cuando la biblioteca está vacía
- [ ] El Comprador puede ordenar sus libros por fecha o por título
- [ ] Los libros se muestran de forma paginada
- [ ] Las pruebas del módulo de biblioteca están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

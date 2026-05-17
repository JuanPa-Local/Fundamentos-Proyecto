# Historia de Usuario: Gestión de Categorías y Etiquetas

**Épica 2 – Catálogo de Libros**

**Como** administrador, **quiero** crear, editar y eliminar categorías y etiquetas del catálogo, **para** mantener la organización del contenido de la plataforma de forma coherente.

---

## Criterios de Aceptación

**AC-001 – Crear una nueva categoría o etiqueta**
Dado que soy administrador y estoy en el panel de gestión, cuando creo una nueva categoría o etiqueta con un nombre único, entonces el sistema la registra y queda disponible para asociar a libros.

**AC-002 – Editar el nombre de una categoría o etiqueta**
Dado que soy administrador, cuando modifico el nombre de una categoría o etiqueta existente, entonces el sistema actualiza el nombre y lo refleja en todos los libros que la tienen asociada.

**AC-003 – Eliminar una categoría sin libros asociados**
Dado que soy administrador, cuando intento eliminar una categoría que no tiene libros asociados, entonces el sistema la elimina exitosamente.

**AC-004 – No se puede eliminar una categoría con libros**
Dado que soy administrador, cuando intento eliminar una categoría que tiene libros asociados, entonces el sistema me lo impide y me informa que primero debo reasignar esos libros a otra categoría.

**AC-005 – Solo el administrador puede gestionar categorías**
Dado que soy un usuario con rol Comprador o Vendedor, cuando intento acceder a la sección de gestión de categorías, entonces el sistema me niega el acceso.

---

## Reglas de Negocio

- **RN-ADMIN-005:** Solo los usuarios con rol ADMINISTRADOR pueden crear, editar o eliminar categorías y etiquetas.
- **RN-ADMIN-006:** No se puede eliminar una categoría que tenga libros asociados; primero deben reasignarse esos libros.
- **RN-ADMIN-007:** Los nombres de categorías y etiquetas deben ser únicos dentro de la plataforma.
- **RN-ADMIN-008:** La eliminación de etiquetas no tiene restricción por libros asociados; se desvinculan automáticamente.

---

## Criterios de Terminación

- [ ] El administrador puede crear, editar y eliminar categorías y etiquetas
- [ ] El sistema impide eliminar categorías que tienen libros asociados
- [ ] Las etiquetas eliminadas se desvinculan automáticamente de los libros
- [ ] Solo el administrador puede acceder a esta sección
- [ ] Las pruebas del módulo de categorías y etiquetas están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

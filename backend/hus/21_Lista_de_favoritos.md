# Historia de Usuario: Lista de Favoritos

**Épica 5 – Favoritos, Historial & Recomendaciones**

**Como** Comprador, **quiero** marcar libros como favoritos, **para** guardarlos y recordar fácilmente el material que me interesa sin necesidad de adquirirlo de inmediato.

---

## Criterios de Aceptación

**AC-001 – Agregar un libro a favoritos**
Dado que soy un Comprador autenticado y estoy viendo el detalle o el listado del catálogo, cuando marco un libro como favorito, entonces el sistema lo agrega a mi lista de favoritos y me muestra una confirmación visual.

**AC-002 – Ver mi lista de favoritos**
Dado que tengo libros marcados como favoritos, cuando accedo a la sección de favoritos, entonces puedo ver todos los libros que he marcado con su información básica.

**AC-003 – Eliminar un libro de favoritos**
Dado que tengo un libro en mi lista de favoritos, cuando lo desmarco o lo elimino de la lista, entonces el sistema lo retira de mis favoritos y ya no aparece en esa sección.

**AC-004 – No se puede agregar el mismo libro dos veces**
Dado que ya tengo un libro en mi lista de favoritos, cuando intento marcarlo como favorito nuevamente, entonces el sistema me informa que ese libro ya está en mi lista de favoritos.

**AC-005 – Lista de favoritos vacía**
Dado que soy un Comprador autenticado pero no he marcado ningún libro, cuando accedo a la sección de favoritos, entonces el sistema me indica que aún no tengo libros favoritos.

---

## Reglas de Negocio

- **RN-FAV-001:** Solo los usuarios autenticados con rol Comprador pueden usar la lista de favoritos.
- **RN-FAV-002:** Un libro solo puede aparecer una vez en la lista de favoritos de un mismo Comprador.
- **RN-FAV-003:** Los favoritos no implican adquisición del libro; son solo un marcador de interés.
- **RN-FAV-004:** La lista de favoritos es privada y solo visible para el Comprador propietario.

---

## Criterios de Terminación

- [ ] El Comprador puede agregar libros a su lista de favoritos
- [ ] El Comprador puede ver todos sus libros marcados como favoritos
- [ ] El Comprador puede eliminar libros de su lista de favoritos
- [ ] El sistema impide agregar un libro duplicado a favoritos
- [ ] El sistema muestra un mensaje cuando la lista de favoritos está vacía
- [ ] Las pruebas del módulo de favoritos están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

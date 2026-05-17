# Historia de Usuario: Filtros Avanzados del Catálogo

**Épica 2 – Catálogo de Libros**

**Como** Comprador, **quiero** filtrar los libros del catálogo por categoría, etiquetas, autor o ISBN, **para** refinar mi búsqueda y encontrar el contenido que me interesa de forma más rápida.

---

## Criterios de Aceptación

**AC-001 – Filtrar por categoría**
Dado que estoy en el catálogo, cuando selecciono una categoría específica, entonces el sistema muestra únicamente los libros que pertenecen a esa categoría.

**AC-002 – Filtrar por etiquetas**
Dado que estoy en el catálogo, cuando selecciono una o varias etiquetas, entonces el sistema muestra los libros que tienen al menos una de esas etiquetas asociadas.

**AC-003 – Combinar múltiples filtros**
Dado que estoy en el catálogo, cuando aplico más de un filtro al mismo tiempo (por ejemplo, categoría y etiqueta), entonces el sistema combina los criterios y muestra solo los libros que cumplen todos los filtros seleccionados.

**AC-004 – Sin resultados con filtros combinados**
Dado que aplico una combinación de filtros que no corresponde a ningún libro disponible, cuando el sistema procesa la búsqueda, entonces me informa que no hay libros que coincidan con esos criterios.

**AC-005 – Limpiar filtros**
Dado que tengo filtros aplicados, cuando hago clic en la opción de limpiar filtros, entonces el catálogo vuelve a mostrar todos los libros disponibles.

---

## Reglas de Negocio

- **RN-CAT-009:** Los filtros pueden aplicarse de forma simultánea y combinarse libremente.
- **RN-CAT-010:** Solo se pueden filtrar libros que hayan sido aprobados por el administrador y estén visibles en el catálogo público.
- **RN-CAT-011:** Los filtros disponibles son: categoría, etiquetas, autor e ISBN.

---

## Criterios de Terminación

- [ ] El Comprador puede filtrar libros por categoría, etiquetas, autor e ISBN
- [ ] Los filtros se pueden combinar y funcionan de forma simultánea
- [ ] El sistema muestra un mensaje claro cuando no hay resultados con los filtros aplicados
- [ ] Es posible limpiar los filtros y volver al catálogo completo
- [ ] Las pruebas de filtrado están escritas y pasan exitosamente
- [ ] Los parámetros de búsqueda están documentados
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

# Historia de Usuario: Panel de Libros Más Descargados

**Épica 7 – Dashboard de Métricas (Admin)**

**Como** administrador, **quiero** ver un ranking de los libros más descargados con la posibilidad de filtrar por período de tiempo, **para** tomar decisiones de curaduría y promoción de contenido basadas en datos reales.

---

## Criterios de Aceptación

**AC-001 – Ver el ranking de libros más descargados**
Dado que soy administrador y accedo al panel de métricas, cuando selecciono la sección de descargas, entonces el sistema me muestra los 10 libros más descargados de la plataforma.

**AC-002 – Filtrar por período de tiempo**
Dado que estoy en el panel de libros más descargados, cuando selecciono un período (últimos 7 días, últimos 30 días o historial completo), entonces el ranking se actualiza mostrando los libros más descargados en ese período.

**AC-003 – Información completa de cada libro en el ranking**
Dado que estoy viendo el ranking de libros más descargados, cuando reviso la lista, entonces cada libro muestra su título, número total de descargas en el período seleccionado y su categoría.

**AC-004 – Panel sin datos**
Dado que soy administrador y accedo al panel de métricas en un período donde no hay descargas registradas, cuando el sistema procesa la solicitud, entonces me informa que no hay datos disponibles para ese período.

**AC-005 – Solo el administrador tiene acceso**
Dado que soy un usuario con rol Comprador o Vendedor, cuando intento acceder al panel de métricas, entonces el sistema me niega el acceso.

---

## Reglas de Negocio

- **RN-MET-001:** Solo los usuarios con rol ADMINISTRADOR pueden acceder al panel de métricas.
- **RN-MET-002:** El ranking muestra como máximo los 10 libros más descargados del período seleccionado.
- **RN-MET-003:** Los períodos disponibles para filtrar son: últimos 7 días, últimos 30 días y el historial completo.
- **RN-MET-004:** El conteo de descargas se obtiene del registro de auditoría del motor de descargas.

---

## Criterios de Terminación

- [ ] El administrador puede ver el ranking de los 10 libros más descargados
- [ ] El ranking puede filtrarse por período (7 días, 30 días, historial completo)
- [ ] Cada libro del ranking muestra título, número de descargas y categoría
- [ ] El sistema informa cuando no hay datos para el período seleccionado
- [ ] Solo el administrador puede acceder al panel
- [ ] Las pruebas del módulo de métricas de descargas están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

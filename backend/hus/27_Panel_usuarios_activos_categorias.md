# Historia de Usuario: Panel de Usuarios Activos y Categorías Populares

**Épica 7 – Dashboard de Métricas (Admin)**

**Como** administrador, **quiero** ver métricas sobre los usuarios activos de la plataforma y las categorías más consultadas, **para** entender el comportamiento de los usuarios y planear mejor la oferta de contenido.

---

## Criterios de Aceptación

**AC-001 – Ver usuarios activos del último mes**
Dado que soy administrador y accedo al panel de métricas, cuando consulto la sección de actividad de usuarios, entonces el sistema me muestra el número de usuarios activos del último mes.

**AC-002 – Comparativa con el mes anterior**
Dado que estoy en la sección de actividad de usuarios, cuando el sistema presenta los datos, entonces también muestra la comparación con el mes inmediatamente anterior para identificar si hubo crecimiento o disminución.

**AC-003 – Categorías más populares**
Dado que soy administrador, cuando accedo a la sección de categorías populares, entonces el sistema me muestra un listado de las categorías con más adquisiciones y descargas del último mes.

**AC-004 – Comparativa de categorías con el mes anterior**
Dado que estoy en la sección de categorías populares, cuando el sistema presenta los datos, entonces también incluye la comparación con el mes anterior para cada categoría.

**AC-005 – Panel sin datos para el período consultado**
Dado que accedo a una sección del panel de métricas para un período sin actividad registrada, cuando el sistema procesa la solicitud, entonces me informa que no hay datos disponibles para ese período.

---

## Reglas de Negocio

- **RN-MET-005:** Solo los usuarios con rol ADMINISTRADOR pueden acceder a este panel de métricas.
- **RN-MET-006:** Las métricas de usuarios activos y categorías populares corresponden siempre al último mes calendario.
- **RN-MET-007:** Se incluye automáticamente la comparativa con el mes inmediatamente anterior para facilitar el análisis de tendencias.
- **RN-MET-008:** Un usuario se considera "activo" si ha iniciado sesión o realizado al menos una acción en la plataforma durante el período consultado.

---

## Criterios de Terminación

- [ ] El administrador puede ver el número de usuarios activos del último mes
- [ ] El panel muestra la comparativa con el mes anterior para usuarios y categorías
- [ ] El administrador puede ver las categorías más populares con sus métricas
- [ ] El sistema informa cuando no hay datos disponibles para el período
- [ ] Solo el administrador puede acceder al panel
- [ ] Las pruebas del módulo de métricas de usuarios y categorías están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

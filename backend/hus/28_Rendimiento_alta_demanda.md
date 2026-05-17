# Historia de Usuario: Rendimiento del Catálogo bajo Alta Demanda

**Épica 8 – Atributos de Calidad**

**Como** equipo de calidad, **quiero** verificar que el catálogo de libros responde rápidamente incluso cuando muchos usuarios lo consultan al mismo tiempo, **para** garantizar que la plataforma cumple con el nivel de rendimiento esperado.

---

## Criterios de Aceptación

**AC-001 – Tiempo de respuesta aceptable bajo carga**
Dado que 100 usuarios están consultando el catálogo de manera simultánea, cuando el sistema procesa todas las solicitudes, entonces el 95% de las respuestas llega al usuario en menos de 1.5 segundos.

**AC-002 – Búsquedas frecuentes almacenadas temporalmente**
Dado que el catálogo recibe muchas solicitudes de búsqueda similares, cuando el sistema detecta una consulta repetida, entonces entrega la respuesta desde su memoria temporal en lugar de consultar la base de datos nuevamente, reduciendo el tiempo de espera.

**AC-003 – Prueba de rendimiento documentada**
Dado que se realiza la prueba de rendimiento del catálogo, cuando se completa, entonces el resultado queda registrado como parte de la documentación del proyecto.

**AC-004 – Resultado de prueba adjunto al proceso de integración**
Dado que se ha aprobado una integración de código relacionada con el catálogo, cuando el reporte de la prueba de rendimiento existe, entonces este se adjunta como parte del proceso de revisión.

---

## Reglas de Negocio

- **RN-NFR-001:** El catálogo debe responder en menos de 1.5 segundos para el 95% de las solicitudes cuando hay 100 usuarios simultáneos.
- **RN-NFR-002:** Las búsquedas frecuentes del catálogo deben ser almacenadas temporalmente para mejorar el rendimiento.
- **RN-NFR-003:** La memoria temporal del catálogo debe actualizarse cuando se aprueba o modifica un libro, para evitar mostrar información desactualizada.
- **RN-NFR-004:** Los campos de búsqueda del catálogo (título, autor, ISBN) deben estar optimizados para consultas rápidas en la base de datos.

---

## Criterios de Terminación

- [ ] El catálogo responde en menos de 1.5 segundos para el 95% de las solicitudes con 100 usuarios simultáneos
- [ ] Las búsquedas frecuentes se almacenan temporalmente y reducen el tiempo de respuesta
- [ ] La memoria temporal del catálogo se actualiza al modificar o aprobar un libro
- [ ] El resultado de la prueba de rendimiento está documentado y adjunto al proceso de integración
- [ ] Las pruebas de rendimiento están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

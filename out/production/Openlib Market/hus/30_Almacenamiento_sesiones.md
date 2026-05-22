# Historia de Usuario: Almacenamiento Temporal y Manejo de Sesiones

**Épica 8 – Atributos de Calidad**

**Como** equipo responsable de la arquitectura del sistema, **quiero** que las sesiones de los usuarios y las consultas más frecuentes estén guardadas en un sistema de acceso rápido, **para** que la plataforma pueda atender picos de usuarios sin que el rendimiento se vea afectado.

---

## Criterios de Aceptación

**AC-001 – Sesiones guardadas en almacenamiento rápido**
Dado que un usuario inicia sesión en la plataforma, cuando el sistema registra su sesión, entonces la guarda en el sistema de acceso rápido para recuperarla eficientemente en cada solicitud.

**AC-002 – Catálogo con respuesta desde memoria**
Dado que el catálogo recibe consultas repetidas con frecuencia, cuando el sistema detecta una consulta ya procesada recientemente, entonces responde desde su memoria temporal sin volver a consultar la base de datos.

**AC-003 – Memoria del catálogo se actualiza al cambiar un libro**
Dado que el administrador aprueba, rechaza o modifica la información de un libro, cuando el sistema registra ese cambio, entonces actualiza automáticamente la memoria temporal del catálogo para que los usuarios vean información al día.

**AC-004 – Tiempo de vigencia de la memoria temporal configurable**
Dado que el equipo necesita ajustar el comportamiento del sistema, cuando se modifica el tiempo de vigencia de la memoria temporal del catálogo, entonces el sistema aplica el nuevo valor sin necesidad de cambios adicionales.

**AC-005 – Prueba de memoria temporal verificada**
Dado que el sistema de memoria temporal está activo, cuando se ejecutan las pruebas, entonces se verifica que las consultas repetidas obtienen respuesta desde la memoria (acierto) y que las consultas nuevas van a la base de datos (falla de caché).

---

## Reglas de Negocio

- **RN-NFR-009:** Las sesiones activas de los usuarios se almacenan en el sistema de acceso rápido para garantizar una verificación eficiente en cada solicitud.
- **RN-NFR-010:** Las consultas frecuentes al catálogo se guardan en memoria temporal con un tiempo de vigencia configurable.
- **RN-NFR-011:** Cuando un libro es aprobado, rechazado o modificado, la memoria temporal del catálogo se invalida automáticamente para mostrar información actualizada.
- **RN-NFR-012:** El tiempo de vigencia de la memoria temporal del catálogo debe estar documentado como una decisión de diseño del equipo.

---

## Criterios de Terminación

- [ ] Las sesiones de usuario se guardan en el sistema de acceso rápido
- [ ] Las consultas frecuentes al catálogo responden desde la memoria temporal
- [ ] La memoria temporal del catálogo se actualiza automáticamente al modificar un libro
- [ ] El tiempo de vigencia de la memoria temporal es configurable y está documentado
- [ ] Las pruebas de acierto y falla de la memoria temporal están escritas y pasan exitosamente
- [ ] La decisión de diseño sobre la memoria temporal está registrada en la documentación del proyecto
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

# Historia de Usuario: Configuración del Repositorio y Flujo de Trabajo

**Épica 0 – Infraestructura & Base Técnica**

**Como** equipo de desarrollo, **quiero** organizar el repositorio de código con un flujo de trabajo claro y ordenado, **para** que cada funcionalidad se desarrolle de forma separada y el proyecto se pueda entregar de manera ordenada al final de cada ciclo de trabajo.

---

## Criterios de Aceptación

**AC-001 – Repositorio creado y organizado**
Dado que el proyecto está iniciando, cuando el equipo accede al repositorio, entonces puede ver las ramas principales (main y develop) correctamente configuradas y listas para usarse.

**AC-002 – Reglas para integrar cambios**
Dado que un integrante del equipo quiere incorporar su trabajo al proyecto, cuando solicita unir su rama al repositorio principal, entonces el sistema exige que al menos otro compañero revise y apruebe los cambios antes de integrarlos.

**AC-003 – Verificación automática del proyecto**
Dado que se realiza una solicitud de integración de código, cuando el sistema la recibe, entonces ejecuta automáticamente una verificación para comprobar que el proyecto compila correctamente y no tiene errores básicos.

**AC-004 – Guía de instalación disponible**
Dado que un integrante nuevo necesita configurar el proyecto en su computador, cuando consulta el archivo README del repositorio, entonces encuentra instrucciones claras y completas sobre cómo hacerlo.

---

## Reglas de Negocio

- **RN-INF-001:** Ningún cambio puede integrarse directamente a las ramas principales sin pasar por revisión de otro integrante del equipo.
- **RN-INF-002:** Cada nueva funcionalidad debe desarrollarse en su propia rama de trabajo, separada del código principal.
- **RN-INF-003:** El proyecto debe poder compilar y construirse sin errores antes de aceptar cualquier integración de código.
- **RN-INF-004:** El archivo de instrucciones (README) debe mantenerse actualizado con los pasos necesarios para que cualquier persona pueda configurar el entorno de desarrollo.

---

## Criterios de Terminación

- [ ] El repositorio está creado con las ramas principales (main y develop) correctamente configuradas
- [ ] Las reglas de integración están activas y exigen revisión por parte de otro integrante
- [ ] La verificación automática del proyecto se ejecuta cada vez que se solicita integrar cambios
- [ ] El archivo README contiene instrucciones claras de configuración
- [ ] La documentación básica del proyecto ha sido creada

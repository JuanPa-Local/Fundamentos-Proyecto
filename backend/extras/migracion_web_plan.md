# Plan de Migración de JavaFX a Frontend Web (HTML/CSS/JS)

Este documento detalla el paso a paso seguro para migrar la interfaz de OpenLib Market de JavaFX a una arquitectura web estándar, manteniendo intacta la lógica de negocio (Clean Architecture).

## 1. Limpieza y Configuración del Backend

Actualmente el proyecto mezcla lógica de UI de escritorio con un servidor web embebido. Debemos purgar JavaFX.

### Tareas:
- [ ] Eliminar dependencias de `JavaFX` en `pom.xml` (`javafx-controls`, `javafx-fxml`, `javafx-maven-plugin`).
- [ ] Eliminar la clase principal de JavaFX (`JavaFXApp.java`) y asegurar que `BackendApplication.java` sea el único punto de entrada de Spring Boot.
- [ ] Eliminar la carpeta `src/main/resources/views` (archivos `.fxml`).
- [ ] Eliminar el gestor de UI local (`ViewFactory.java`, `SessionManager.java`, `ControllerUI.java`).

## 2. Exposición de la Capa de Aplicación (REST API)

Ya existen controladores REST para `User`, `Book`, `Review`, `Order`, y `Favorite`. Sin embargo, las funcionalidades recientes implementadas en Facades (Carrito, Checkout, Descarga) solo eran accesibles vía JavaFX.

### Tareas:
- [ ] Crear `CarritoRestController.java`: Exponer endpoints POST, GET, DELETE que llamen a `CarritoFacade`.
- [ ] Crear `CheckoutRestController.java`: Exponer endpoints POST para dirección, pago y confirmación que llamen a `CheckoutFacade`.
- [ ] Crear `DescargaRestController.java`: Exponer endpoint GET/POST para obtener el token que llame a `DescargaFacade`.
- [ ] Modificar `SecurityConfig.java` para configurar CORS (permitir llamadas desde el frontend) y proteger las rutas `/api/**` utilizando **Tokens JWT** o **Sesiones basadas en Cookies** en lugar de Basic Auth o estado en memoria local.

## 3. Construcción del Frontend Web (Vanilla HTML/CSS/JS)

Crearemos una estructura separada para el cliente web. Al no requerir Frameworks (React/Vue/Angular), garantizamos simplicidad máxima.

### Estructura Propuesta:
```text
frontend/
├── index.html          (Landing page)
├── login.html
├── register.html
├── dashboard-buyer.html
├── dashboard-seller.html
├── dashboard-admin.html
├── css/
│   ├── main.css        (Estilos base y sistema de diseño)
│   └── components.css
└── js/
    ├── api.js          (Gestión de fetch() centralizada y manejo del JWT/Session)
    ├── auth.js         (Lógica de login/registro)
    ├── catalog.js      (Renderizado de libros)
    └── cart.js         (Lógica de carrito y checkout)
```

### Tareas:
- [ ] Implementar sistema de Autenticación en `api.js` (interceptar peticiones y enviar cabeceras de autorización).
- [ ] Crear la maqueta HTML/CSS (UI moderna, responsive, limpia).
- [ ] Conectar los formularios de login/registro con `/api/users`.
- [ ] Conectar el catálogo de libros con `/api/books`.
- [ ] Implementar el flujo de carrito y checkout interactuando con los nuevos endpoints.

## 4. Pruebas e Integración

### Tareas:
- [ ] Probar todos los endpoints de la API con Swagger o Postman.
- [ ] Probar el flujo completo desde el navegador (Registro -> Login -> Catálogo -> Carrito -> Checkout -> Descarga).
- [ ] Configurar un servidor de desarrollo simple para el frontend (ej. `npx serve`) o servir los archivos estáticos desde `src/main/resources/static` en Spring Boot.

---

### Riesgos y Consideraciones
* **CORS:** Es el error más común al separar Frontend de Backend. Será lo primero a configurar en `SecurityConfig`.
* **Estado de la Sesión:** Al eliminar `SessionManager` (que guardaba estado en memoria RAM del cliente JavaFX), el Frontend deberá almacenar el identificador del usuario (JWT o UUID) en `localStorage` o `sessionStorage` para enviarlo en cada petición HTTP.

¿Estás de acuerdo con el plan? Si lo apruebas, empezaremos por el **Paso 1: Limpieza del backend**.

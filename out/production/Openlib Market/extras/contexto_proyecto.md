# Contexto del Proyecto OpenLib Market

## Tecnologías y Herramientas Principales
- **Lenguaje:** Java 25
- **Framework Base:** Spring Boot 4.0.5
- **Interfaz de Usuario:** JavaFX (versión 23) integrado directamente en el proyecto, usando archivos FXML para el diseño visual.
- **Persistencia de Datos:** PostgreSQL con Spring Data JPA.
- **Gestión de Sesiones/Caché:** Redis (Spring Session Data Redis).
- **Seguridad:** Spring Security.
- **Gestor de dependencias:** Maven.
- **Otras herramientas:** Lombok (para reducir código repetitivo) y Spring Validation (para validar datos).

## Arquitectura y Estructura del Proyecto
El proyecto está estructurado como una aplicación unificada que incluye tanto el backend de negocio y persistencia (Spring Boot) como la interfaz de escritorio del cliente (JavaFX). 
Sigue un enfoque arquitectónico orientado al negocio, organizando los paquetes principales por **Dominios** (Domain-Driven Design).

### Árbol de Directorios y Archivos
La estructura base del proyecto se concentra en la carpeta `backend/`:

- **`pom.xml`**: Configuración fundamental de Maven con todas las dependencias listadas anteriormente.
- **`docker-compose.yml`**: Orquestación de infraestructura mediante contenedores Docker (ideal para inicializar PostgreSQL y Redis localmente).
- **`src/main/resources/`**:
  - `application.yml`: Archivo de configuración global de Spring Boot (cadenas de conexión a BD, variables de entorno, etc.).
  - `views/`: Contiene los archivos `.fxml` que estructuran la UI de JavaFX (`main-view.fxml`, `admin-view.fxml`, `buyer-view.fxml`, `seller-view.fxml`, `register-view.fxml`).
  - `styles/`: Hojas de estilo CSS (`global.css`) para aplicar temas a la interfaz de JavaFX.
- **`src/main/java/com/openlib/backend/`**: Paquete raíz del código fuente.
  - `BackendApplication.java`: Clase principal con el método `main` para ejecutar la aplicación.
  - **`config/`**: Archivos de configuración técnica de la app.
    - `SecurityConfig.java`: Reglas de seguridad, filtros y autenticación (Spring Security).
    - `DataInitializer.java`: Clase encargada de poblar la base de datos con información inicial al iniciar la aplicación.
  - **`domain/`**: Núcleo de la lógica de negocio, separada por entidades/dominios.
    - `book/`: Entidades, repositorios y servicios correspondientes al dominio de Libros.
    - `order/`: Entidades, repositorios y servicios del dominio de Órdenes/Compras.
    - `user/`: Entidades, repositorios y servicios del dominio de Usuarios.
  - **`UI/`**: Capa de presentación - Controladores e inicializadores de JavaFX.
    - `JavaFXApp.java`: Configuración de arranque de la ventana y el contexto de JavaFX.
    - `AdminController.java`, `BuyerController.java`, `SellerController.java`, `RegisterController.java`: Controladores enlazados a sus respectivos archivos `.fxml`.
    - `SessionManager.java`: Clase auxiliar para manejar el estado de la sesión del usuario que ha iniciado sesión a nivel de UI.

## Patrones y Buenas Prácticas Aplicadas
1. **Empaquetado por Dominio (Package by Feature):** El uso de la carpeta `domain/` agrupando por `book`, `order`, y `user` evita el clásico antipatrón de empaquetar por capas técnicas (donde tendrías una sola carpeta inmensa de "servicios"). Esto fomenta la alta cohesión de cada característica.
2. **Seguridad y Estado (Redis + Spring Security):** Extraer la sesión a Redis es un gran paso hacia la escalabilidad, ya que desacopla la sesión en memoria del servidor web, siendo una práctica recomendada en arquitecturas modernas.
3. **Manejo Centralizado de UI:** La inclusión de un `SessionManager` y controladores segregados por rol demuestran una separación clara entre las vistas y las responsabilidades de los distintos actores en la aplicación.

---
*Este archivo sirve como base de contexto para proporcionar información estructurada a los modelos de lenguaje (LLMs) sobre la arquitectura y el propósito de OpenLib Market.*

# OpenLib Market

Plataforma unificada (Spring Boot + JavaFX) para compra y publicación de libros.

## Prerrequisitos
- Java 25
- Maven
- Docker Desktop

## Pasos para clonar y ejecutar localmente
1. Clona este repositorio.
2. Levanta los contenedores de base de datos y redis: `docker-compose up -d` dentro de la carpeta `backend/`.
3. Ejecuta la aplicación: `mvn spring-boot:run` desde la carpeta `backend/`.

## Convención de ramas
- `main`: producción
- `develop`: integración continua
- `feature/US-XXX`: para historias de usuario

## Pruebas
- Corre las pruebas con `mvn test` desde `backend/`.

## Glosario (DDD)
- **Buyer**: Comprador de libros.
- **Seller**: Vendedor o publicador de libros.
- **Admin**: Administrador de la plataforma.
- **Book (Libro)**: Entidad que representa un libro digital en venta.
- **Order (Orden)**: Registro de compra o adquisición de un libro.

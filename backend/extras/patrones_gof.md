# 5 Patrones de Diseño GoF Recomendados para OpenLib Market

En este documento se proponen 5 patrones de diseño de la *Gang of Four* (GoF) que encajan perfectamente en la arquitectura combinada de **Spring Boot + JavaFX** que posee el proyecto OpenLib Market.

## 1. Builder (Patrón Creacional)
**¿Por qué usarlo en OpenLib Market?**
El dominio de una tienda de libros suele tener entidades con muchos parámetros opcionales. Al instanciar un libro (`Book`) o crear un carrito de compras (`Order`), usar el constructor tradicional se vuelve confuso si hay 5 o más atributos (el antipatrón *Telescoping Constructor*).
**Facilidad de Implementación:** Si estás usando la dependencia de **Lombok** en tu proyecto, el costo de implementación es literalmente cero. Basta con añadir la anotación `@Builder` encima de la clase del dominio o DTO. Si lo haces manual, es simplemente crear una clase anidada que devuelva `this` tras establecer cada campo para poder encadenarlos: `Book.builder().title("X").author("Y").build()`.

## 2. Strategy (Patrón de Comportamiento)
**¿Por qué usarlo en OpenLib Market?**
Con el crecimiento de la tienda (paquete `order`), se agregarán distintas reglas de negocio dinámicas, como por ejemplo: "Cálculo de Descuentos" (cupones, descuentos de Black Friday, beneficios para clientes VIP). El patrón Strategy permite abstraer estas reglas matemáticas en clases individuales.
**Facilidad de Implementación:** Creas una interfaz común `DiscountStrategy` con un método `calculate()`. Luego creas clases pequeñas como `VipDiscount` o `SeasonalDiscount` que la implementen. Cuando un comprador vaya a pagar, la orden ejecuta el cálculo usando la estrategia inyectada, evitando que tu servicio de órdenes se llene de decenas de condiciones `if-else` o `switch`.

## 3. Observer (Patrón de Comportamiento)
**¿Por qué usarlo en OpenLib Market?**
Al tener una interfaz de usuario integrada con JavaFX (carpeta `UI/`), el patrón Observer es crítico para que la vista reaccione a los cambios de estado sin congelarse. Además, a nivel del backend de Spring Boot, permite desacoplar los paquetes `domain`. Por ejemplo: Si se finaliza una compra en `order/`, debes notificar a `book/` para reducir el inventario y a `user/` para mandar un email. 
**Facilidad de Implementación:** 
- En el backend, Spring Boot lo proporciona nativamente a través del modelo de Eventos. Solo debes usar `applicationEventPublisher.publishEvent()` y escuchar ese evento en otra clase con la anotación `@EventListener`.
- En JavaFX, viene integrado por defecto mediante la API de Propiedades (`Properties` y `Listeners`).

## 4. Singleton (Patrón Creacional)
**¿Por qué usarlo en OpenLib Market?**
Posees una clase `SessionManager` en tu interfaz. Este gestor de sesiones en el lado del cliente (JavaFX) debe tener una **única instancia** que sobreviva todo el tiempo de ejecución para que el `AdminController` y el `BuyerController` puedan preguntar en cualquier momento "¿quién es el usuario activo?".
**Facilidad de Implementación:** Si tu `SessionManager` no está gestionado por el inyector de dependencias de Spring (es decir, no es un `@Component`), simplemente declaras su constructor como privado, creas una variable estática para almacenar su propia instancia, y provees el clásico método estático `getInstance()` para recuperarlo de forma global y segura.

## 5. Factory Method (Patrón Creacional)
**¿Por qué usarlo en OpenLib Market?**
En tu capa de presentación, existen diferentes roles de usuario (`Buyer`, `Seller`, `Admin`), cada uno con su propia vista FXML y Controlador (`buyer-view.fxml`, `admin-view.fxml`, etc.). Cuando el proceso de login en `RegisterController` es exitoso, necesitas instanciar y cargar la nueva ventana.
**Facilidad de Implementación:** Puedes centralizar esta lógica mediante una clase "fábrica" (`ViewFactory`). Esta clase tendría un método como `getView(Role rol)` que, a partir del rol del usuario, decide automáticamente cuál vista cargar y qué controlador adjuntarle. Esto evita ensuciar tus controladores con lógica técnica de inicialización de interfaces.

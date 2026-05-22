# Plan de Implementación: Selección de Método de Pago y Confirmación de Orden
**US-017 & US-018 | Épica 3 – Carrito de Compras & Checkout**

> Estos dos planes se presentan juntos porque comparten el contexto del checkout y la entidad `Orden`, que se crea al confirmar. Cada caso de uso sigue siendo independiente.

---

## 1. Análisis y Modelado de Dominio

### 1.1 Enumeración `MetodoPago`

**Archivo:** `domain/order/MetodoPago.java`
- Valores: `TARJETA`, `TRANSFERENCIA`, `DONACION`

### 1.2 Entidad `Orden` (Aggregate Root)

**Archivo:** `domain/order/Orden.java`

**Atributos:**
- `UUID id`
- `UUID buyerId`
- `List<ItemOrden> items` — copia inmutable de los items del carrito al momento de confirmar
- `DireccionFacturacion direccionFacturacion`
- `MetodoPago metodoPago`
- `EstadoOrden estado` — siempre `COMPLETADA` al crear (flujo simplificado con monto $0)
- `BigDecimal monto` — siempre `BigDecimal.ZERO`
- `LocalDateTime fechaCreacion`

**Método fábrica:**
- `static Orden confirmar(UUID buyerId, List<ItemCarrito> items, DireccionFacturacion direccion, MetodoPago metodoPago)` — verifica que `items` no esté vacío (lanza `CarritoVacioException`), crea la `Orden` con estado `COMPLETADA`.

### 1.3 Enumeración `EstadoOrden`

**Archivo:** `domain/order/EstadoOrden.java`
- Valores: `COMPLETADA`

### 1.4 Value Object `ItemOrden`

**Archivo:** `domain/order/ItemOrden.java`
- `UUID libroId`
- `String titulo`

### 1.5 Interface `OrdenRepository` (puerto de salida)

```java
// domain/order/OrdenRepository.java
public interface OrdenRepository {
    Orden guardar(Orden orden);
    Page<Orden> buscarPorBuyerId(UUID buyerId, Pageable pageable);
    Optional<Orden> buscarPorId(UUID ordenId);
}
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `GuardarMetodoPagoUseCase` (US-017)

**DTO de entrada:** `GuardarMetodoPagoCommand` (`UUID buyerId`, `String metodoPago`)

**Flujo:**
1. Verificar rol `BUYER`.
2. Convertir `String` a `MetodoPago` enum. Si el valor no es válido, lanzar `MetodoPagoInvalidoException`.
3. Recuperar la `SesionCheckout` del buyer con `CheckoutSessionGateway`. Si no existe, lanzar `SesionCheckoutExpiradaException`.
4. Asignar el `metodoPago` a la sesión.
5. Persistir la sesión con el mismo TTL.

**Excepciones nuevas:** `MetodoPagoInvalidoException`, `SesionCheckoutExpiradaException`

### `ConfirmarOrdenUseCase` (US-018)

**DTO de salida:** `OrdenResponse` (`UUID ordenId`, `String numeroOrden`, `List<String> titulosLibros`, `LocalDateTime fecha`)

**Flujo:**
1. Verificar rol `BUYER`.
2. Recuperar `SesionCheckout` con `CheckoutSessionGateway`. Verificar que tenga `direccion` y `metodoPago` completos. Si falta algo, lanzar `SesionCheckoutIncompleta`.
3. Recuperar el `Carrito` activo con `CarritoRepository`. Si está vacío, lanzar `CarritoVacioException`.
4. Crear la `Orden` con `Orden.confirmar(buyerId, items, direccion, metodoPago)`.
5. Persistir la `Orden` con `OrdenRepository.guardar()`.
6. Agregar cada libro de la orden a la biblioteca del buyer con `BibliotecaRepository.agregar(buyerId, libroId)`.
7. Vaciar el carrito con `CarritoRepository.eliminar(buyerId)`.
8. Eliminar la sesión de checkout con `CheckoutSessionGateway.eliminar(buyerId)`.
9. Retornar `OrdenResponse`.

**Excepción nueva:** `SesionCheckoutIncompletaException`

Nuevo método en `BibliotecaRepository`:
```java
void agregar(UUID buyerId, UUID libroId);
```

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V9__crear_tabla_orden.sql`

```sql
CREATE TABLE orden (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id            UUID NOT NULL REFERENCES usuario(id),
    metodo_pago         VARCHAR(50) NOT NULL,
    estado              VARCHAR(50) NOT NULL DEFAULT 'COMPLETADA',
    monto               NUMERIC(10,2) NOT NULL DEFAULT 0.00,
    calle               VARCHAR(300),
    ciudad              VARCHAR(150),
    departamento        VARCHAR(150),
    pais                VARCHAR(100),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE orden_item (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id   UUID NOT NULL REFERENCES orden(id),
    libro_id   UUID NOT NULL REFERENCES libro(id),
    titulo     VARCHAR(255) NOT NULL
);
```

### 3.2 Entidades y Repositorios JPA

- `OrdenJpaEntity.java` con relación `@OneToMany` a `OrdenItemJpaEntity`.
- `SpringOrdenRepository.java`.
- `OrdenRepositoryAdapter.java` implementando `OrdenRepository`.
- `OrdenMapper.java`.

---

## 4. Capa de UI (JavaFX)

**`checkout-pago-view.fxml`** (nuevo — paso 2):
- Radio buttons para los métodos de pago: Tarjeta, Transferencia, Donación.
- Resumen del carrito a la derecha.
- Botón "Confirmar y adquirir" (avanza al paso 3 — confirmación).
- Botón "Volver" (regresa a la dirección).

**`checkout-confirmacion-view.fxml`** (nuevo — paso 3):
- Número de confirmación de la orden.
- Lista de libros adquiridos.
- Fecha y método de pago utilizado.
- Botón "Ir a mi biblioteca".

**Controladores:** `CheckoutPagoController.java` y `CheckoutConfirmacionController.java`.

---

## 5. Plan de Pruebas (TDD)

### `OrdenTest` (dominio)
- `confirmar_conDatosCompletos_debeCrearOrdenConEstadoCompletada()`
- `confirmar_conCarritoVacio_debeLanzarCarritoVacioException()`
- `confirmar_debeAsignarMontoEnCero()`

### `GuardarMetodoPagoUseCaseTest` (con Mockito)
- `ejecutar_conMetodoValido_debeActualizarSesionCheckout()`
- `ejecutar_conMetodoInvalido_debeLanzarMetodoPagoInvalidoException()`
- `ejecutar_conSesionExpirada_debeLanzarSesionCheckoutExpiradaException()`

### `ConfirmarOrdenUseCaseTest` (con Mockito)
- `ejecutar_conSesionCompletaYCarritoConItems_debeCrearOrdenYVaciarCarrito()`
- `ejecutar_debeAgregarLibrosABiblioteca()`
- `ejecutar_conCarritoVacio_debeLanzarCarritoVacioException()`
- `ejecutar_conSesionSinDireccion_debeLanzarSesionCheckoutIncompletaException()`

### `OrdenRepositoryAdapterTest` (`@DataJpaTest`)
- `guardar_debePersistitOrdenConItems()`
- `buscarPorBuyerId_debeRetornarOrdenesDelBuyer()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `ConfirmarOrdenUseCase` orquesta múltiples repositorios (Carrito, Orden, Biblioteca, Checkout) todos a través de interfaces. No conoce Redis, PostgreSQL ni JavaFX. |
| **DDD** | `Orden` es el Aggregate Root del contexto `order`. Toda la lógica de creación vive en su método fábrica `confirmar()`. La lógica de negocio no está dispersa en el caso de uso. |
| **TDD** | Escribir `OrdenTest` antes de implementar la entidad. Luego los tests de los dos casos de uso antes de implementarlos. |
| **SOLID (SRP)** | `GuardarMetodoPagoUseCase` (paso 2) y `ConfirmarOrdenUseCase` (paso 3) son responsabilidades separadas e independientes. |
| **SOLID (DIP)** | `ConfirmarOrdenUseCase` recibe todas las interfaces por inyección de dependencias. Cada implementación concreta puede cambiarse sin tocar el caso de uso. |

---

## 📋 Tareas de Implementación (US-017)

- [ ] 1. `MetodoPago` Enum y excepciones no creados.
- [ ] 2. `GuardarMetodoPagoUseCaseTest` no creado.
- [ ] 3. `GuardarMetodoPagoUseCase` no implementado.
- [ ] 4. `checkout-pago-view.fxml` y `CheckoutPagoController.java` no creados.

## 📋 Tareas de Implementación (US-018)

- [ ] 5. `ItemOrden`, `EstadoOrden` y `SesionCheckoutIncompletaException` no creados en `domain/order/`.
- [ ] 6. `OrdenTest` no creado.
- [x] 7. Entidad `Order.java` existe con `@Builder`, relaciones a `User` y `Book`, campo `status` (default `COMPLETED`), `totalPrice`, `orderedAt`.
- [x] 8. `Order` construible mediante Builder de Lombok.
- [x] 9. `OrderRepository extends JpaRepository<Order, UUID>` con `findByUser()`.
- [ ] 10. `ConfirmarOrdenUseCaseTest` no creado.
- [x] 11. `OrderService.createOrder(userId, bookId)` crea la orden, aplica `DiscountStrategy` (patrón Strategy) y dispara `OrderCompletedEvent` (patrón Observer).
- [ ] 12. Script `V9__crear_tabla_orden.sql` no existe *(Hibernate gestiona el esquema)*.
- [x] 13. No existe `OrdenJpaEntity` separada — `Order.java` es directamente la entidad JPA.
- [ ] 14. Tests de repositorio pendientes.
- [ ] 15. `checkout-confirmacion-view.fxml` y `CheckoutConfirmacionController.java` no creados.
- [ ] 16. `mvn verify` pendiente.
- [ ] 16. Verificar que `mvn verify` pasa completo.

# Plan de Implementación: Dirección de Facturación en el Proceso de Compra
**US-016 | Épica 3 – Carrito de Compras & Checkout**

---

## 1. Análisis y Modelado de Dominio

La dirección de facturación es un Value Object que se captura en el primer paso del checkout. Se almacena temporalmente en la sesión del proceso de compra (Redis) y se asocia de forma permanente a la `Orden` al confirmarla en US-018.

### 1.1 Value Object `DireccionFacturacion`

**Archivo:** `domain/order/DireccionFacturacion.java`

**Atributos (todos obligatorios):**
- `String calle`
- `String ciudad`
- `String departamento`
- `String pais`

**Reglas en el constructor:** Todos los campos son obligatorios. Lanza `CampoDireccionObligatorioException` si alguno está vacío.

**Excepción:** `CampoDireccionObligatorioException`

### 1.2 Value Object `SesionCheckout`

**Archivo:** `domain/order/SesionCheckout.java`

Agrupa el estado parcial del proceso de compra entre pasos:
- `UUID buyerId`
- `DireccionFacturacion direccion` — nulo hasta completar el paso 1
- `String metodoPago` — nulo hasta completar el paso 2 (US-017)
- `LocalDateTime inicioCheckout`

### 1.3 Interface `CheckoutSessionGateway` (puerto de salida)

```java
// domain/order/CheckoutSessionGateway.java
public interface CheckoutSessionGateway {
    void guardar(SesionCheckout sesion, int ttlMinutos);
    Optional<SesionCheckout> buscarPorBuyerId(UUID buyerId);
    void eliminar(UUID buyerId);
}
```

---

## 2. Capa de Aplicación (Caso de Uso)

### `GuardarDireccionCheckoutUseCase`

**DTO de entrada:** `GuardarDireccionCommand`
- `UUID buyerId`
- `String calle`, `String ciudad`, `String departamento`, `String pais`

**DTO de salida:** `SesionCheckoutResponse` (confirma que el paso fue guardado)

**Flujo:**
1. Verificar que el `buyerId` tiene rol `BUYER`.
2. Verificar que el carrito del buyer no está vacío con `CarritoRepository.buscarPorBuyerId()`. Si está vacío, lanzar `CarritoVacioException`.
3. Construir `DireccionFacturacion` (valida campos obligatorios).
4. Recuperar o crear la `SesionCheckout` del buyer con `CheckoutSessionGateway.buscarPorBuyerId()`.
5. Asignar la dirección a la `SesionCheckout`.
6. Persistir la sesión con `CheckoutSessionGateway.guardar(sesion, 30)` (TTL de 30 minutos).
7. Retornar `SesionCheckoutResponse`.

**Excepción nueva:** `CarritoVacioException`

### `ObtenerDireccionPrerrellenaUseCase`

**Flujo:**
1. Recuperar el `PerfilUsuario` del buyer con `UsuarioRepository.buscarPorId()`.
2. Si tiene `direccionFacturacion` en el perfil, retornarla como `DireccionFacturacion` prerrellena.
3. Si no, retornar campos vacíos.

---

## 3. Capa de Infraestructura

### 3.1 Implementación de `CheckoutSessionGateway` con Redis

**`RedisCheckoutSessionGateway.java`** (en `infrastructure/cache/`):
- Usa `RedisTemplate<String, SesionCheckout>`.
- Clave: `checkout:{buyerId}`.
- TTL: 30 minutos, renovado en cada paso del checkout.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/checkout-direccion-view.fxml`
- Campos: `calle`, `ciudad`, `departamento`, `pais`.
- Mensaje informativo: "Estos datos son solo para registro de tu adquisición."
- Botón "Continuar" (avanza al paso 2, US-017).
- Botón "Cancelar" (regresa al carrito).

**Controlador:** `UI/controllers/CheckoutDireccionController.java`
- Al cargar: invoca `ObtenerDireccionPrerrellenaUseCase` y rellena los campos si hay datos del perfil.
- Al hacer clic en "Continuar": invoca `GuardarDireccionCheckoutUseCase`. Si pasa, navega a `checkout-pago-view.fxml`.
- Muestra errores de validación campo a campo.

---

## 5. Plan de Pruebas (TDD)

### `DireccionFacturacionTest` (dominio)
- `crear_conTodosLosCampos_debeInstanciar()`
- `crear_sinCalle_debeLanzarCampoDireccionObligatorioException()`
- `crear_sinCiudad_debeLanzarCampoDireccionObligatorioException()`
- `crear_sinDepartamento_debeLanzarCampoDireccionObligatorioException()`
- `crear_sinPais_debeLanzarCampoDireccionObligatorioException()`

### `GuardarDireccionCheckoutUseCaseTest` (con Mockito)
- `ejecutar_conCarritoActivoYDireccionValida_debeGuardarEnSesion()`
- `ejecutar_conCarritoVacio_debeLanzarCarritoVacioException()`
- `ejecutar_conCampoVacio_debeLanzarCampoDireccionObligatorioException()`

### `RedisCheckoutSessionGatewayTest` (integración)
- `guardar_debeAlmacenarSesionConTTL()`
- `buscarPorBuyerId_debeRetornarSesionConDireccion()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `GuardarDireccionCheckoutUseCase` depende de `CheckoutSessionGateway` y `CarritoRepository` (interfaces). No sabe que el almacenamiento es Redis. |
| **DDD** | `DireccionFacturacion` encapsula sus propias reglas de validación. `SesionCheckout` agrupa el estado del proceso de compra entre pasos, como un Value Object de sesión. |
| **TDD** | Escribir `DireccionFacturacionTest` antes de crear el Value Object. |
| **SOLID (SRP)** | `ObtenerDireccionPrerrellenaUseCase` y `GuardarDireccionCheckoutUseCase` son responsabilidades separadas. Un caso de uso lee, el otro escribe. |
| **SOLID (DIP)** | `GuardarDireccionCheckoutUseCase` depende de `CheckoutSessionGateway` (interface). `RedisCheckoutSessionGateway` se inyecta en tiempo de ejecución. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear `DireccionFacturacion`, `SesionCheckout` y `CampoDireccionObligatorioException` en `domain/order/`.
- [ ] 2. Escribir `DireccionFacturacionTest` (debe fallar).
- [ ] 3. Implementar el Value Object y verificar tests.
- [ ] 4. Definir la interface `CheckoutSessionGateway` en `domain/order/`.
- [ ] 5. Escribir los tests de los casos de uso (deben fallar).
- [ ] 6. Implementar `GuardarDireccionCheckoutUseCase` y `ObtenerDireccionPrerrellenaUseCase`.
- [ ] 7. Verificar que los tests pasan.
- [ ] 8. Implementar `RedisCheckoutSessionGateway`.
- [ ] 9. Escribir y verificar `RedisCheckoutSessionGatewayTest`.
- [ ] 10. Crear `checkout-direccion-view.fxml` y `CheckoutDireccionController.java`.
- [ ] 11. Verificar que `mvn verify` pasa completo.

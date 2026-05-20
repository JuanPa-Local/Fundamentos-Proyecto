# Plan de Implementación: Carrito de Compras Persistente
**US-015 | Épica 3 – Carrito de Compras & Checkout**

---

## 1. Análisis y Modelado de Dominio

El `Carrito` es el Aggregate Root del contexto `order`. Es persistido en Redis para garantizar la rapidez y la expiración automática a los 7 días. Solo los usuarios con rol `BUYER` pueden tener un carrito.

### 1.1 Entidad `Carrito` (Aggregate Root)

**Archivo:** `domain/order/Carrito.java`

**Atributos:**
- `UUID buyerId` — identificador del comprador propietario
- `List<ItemCarrito> items` — libros agregados
- `LocalDateTime fechaCreacion`
- `LocalDateTime fechaUltimaModificacion`

**Comportamientos:**
- `void agregarItem(UUID libroId, String titulo)` — verifica que el libro no esté ya en el carrito (lanza `LibroYaEnCarritoException`). Agrega el item.
- `void eliminarItem(UUID libroId)` — elimina el item correspondiente. Lanza `ItemNoEncontradoException` si no existe.
- `void vaciar()` — elimina todos los items.
- `boolean contieneLibro(UUID libroId)` — retorna `true` si el libro ya está en el carrito.
- `boolean estaVacio()` — retorna `true` si no hay items.

### 1.2 Value Object `ItemCarrito`

**Archivo:** `domain/order/ItemCarrito.java`
- `UUID libroId`
- `String titulo` — desnormalizado para mostrar sin consultar al dominio de libros

**Excepciones de dominio** (en `domain/order/exception/`):
- `LibroYaEnCarritoException`
- `ItemNoEncontradoException`
- `LibroYaAdquiridoException`

### 1.3 Interface `CarritoRepository` (puerto de salida)

```java
// domain/order/CarritoRepository.java
public interface CarritoRepository {
    void guardar(Carrito carrito, int ttlDias);
    Optional<Carrito> buscarPorBuyerId(UUID buyerId);
    void eliminar(UUID buyerId);
}
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `AgregarItemAlCarritoUseCase`

**DTO de entrada:** `AgregarItemCommand` (`UUID buyerId`, `UUID libroId`)

**Flujo:**
1. Verificar que `buyerId` tiene rol `BUYER`.
2. Verificar que el libro existe y está `APROBADO` con `LibroRepository.buscarPorId()`. Si no, lanzar `LibroNoDisponibleException`.
3. Verificar que el Buyer no tiene ya ese libro en su biblioteca con `BibliotecaRepository.existeEnBiblioteca(buyerId, libroId)`. Si lo tiene, lanzar `LibroYaAdquiridoException`.
4. Recuperar o crear el carrito del buyer con `CarritoRepository.buscarPorBuyerId()`.
5. Invocar `carrito.agregarItem(libroId, titulo)`.
6. Persistir con `CarritoRepository.guardar(carrito, 7)` (TTL de 7 días).

### `VerCarritoUseCase`

**DTO de salida:** `CarritoResponse` (`UUID buyerId`, `List<ItemCarritoResponse>`, `int totalItems`)

**Flujo:**
1. Recuperar el carrito con `CarritoRepository.buscarPorBuyerId()`.
2. Si no existe, retornar un `CarritoResponse` vacío.

### `EliminarItemDelCarritoUseCase`

**Flujo:**
1. Recuperar el carrito.
2. Invocar `carrito.eliminarItem(libroId)`.
3. Persistir con `CarritoRepository.guardar()`.

### Interface `BibliotecaRepository` (preliminar)

```java
// domain/order/BibliotecaRepository.java
public interface BibliotecaRepository {
    boolean existeEnBiblioteca(UUID buyerId, UUID libroId);
}
```

> Se implementará completamente en US-019. Por ahora, adaptador provisional que retorna `false`.

---

## 3. Capa de Infraestructura

### 3.1 Implementación de `CarritoRepository` con Redis

**`RedisCarritoRepository.java`** (en `infrastructure/cache/`):
- Usa `RedisTemplate<String, Carrito>` (serialización JSON con Jackson).
- Clave: `carrito:{buyerId}`.
- El TTL de 7 días se configura al guardar: `redisTemplate.expire(clave, 7, TimeUnit.DAYS)`.

### 3.2 Serialización del `Carrito`

Configurar `Jackson2JsonRedisSerializer` o `GenericJackson2JsonRedisSerializer` para serializar correctamente `Carrito` (con sus `UUID` y `LocalDateTime`).

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/carrito-view.fxml`
- Lista de items con: título del libro y botón "Eliminar".
- Contador de items en la parte superior.
- Botón "Proceder al pago" (lleva al flujo de checkout, US-016).
- Mensaje informativo si el carrito está vacío.

**Controlador:** `UI/controllers/CarritoController.java`
- Al abrir: invoca `VerCarritoUseCase`.
- Al eliminar item: invoca `EliminarItemDelCarritoUseCase` y recarga la vista.
- El botón "Agregar al carrito" en `DetalleLibroController` invoca `AgregarItemAlCarritoUseCase`.

---

## 5. Plan de Pruebas (TDD)

### `CarritoTest` (dominio)
- `agregarItem_conLibroNuevo_debeAgregarAlListado()`
- `agregarItem_conLibroDuplicado_debeLanzarLibroYaEnCarritoException()`
- `eliminarItem_conItemExistente_debeRemoverDelListado()`
- `eliminarItem_conItemInexistente_debeLanzarItemNoEncontradoException()`
- `estaVacio_conListaVacia_debeRetornarTrue()`

### `AgregarItemAlCarritoUseCaseTest` (con Mockito)
- `ejecutar_conLibroAprobadoYNoAdquirido_debeAgregarYGuardar()`
- `ejecutar_conLibroYaEnCarrito_debeLanzarLibroYaEnCarritoException()`
- `ejecutar_conLibroYaAdquirido_debeLanzarLibroYaAdquiridoException()`
- `ejecutar_conLibroNoAprobado_debeLanzarLibroNoDisponibleException()`

### `RedisCarritoRepositoryTest` (integración con TestContainers Redis)
- `guardar_debeAlmacenarCarritoConTTL()`
- `buscarPorBuyerId_debeRetornarCarritoGuardado()`
- `guardar_despuesDeVencerTTL_debeRetornarEmpty()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `AgregarItemAlCarritoUseCase` no conoce Redis directamente. Depende de `CarritoRepository` (interface). `RedisCarritoRepository` es el adaptador en infraestructura. |
| **DDD** | `Carrito` encapsula toda la lógica de validación de items. El caso de uso solo orquesta; no verifica duplicados directamente. |
| **TDD** | Escribir `CarritoTest` antes de implementar la entidad. Luego escribir el test del caso de uso con Mockito antes de implementarlo. |
| **SOLID (SRP)** | `RedisCarritoRepository` gestiona persistencia en Redis. La lógica de negocio (duplicados, límites) está en la entidad `Carrito`. |
| **SOLID (DIP)** | Los casos de uso dependen de `CarritoRepository` y `BibliotecaRepository` (interfaces). Las implementaciones concretas se inyectan en tiempo de ejecución. |

---

## 📋 Tareas de Implementación

- [ ] 1. `ItemCarrito` Value Object no creado.
- [ ] 2. `CarritoTest` no creado.
- [ ] 3. Entidad `Carrito` no creada *(flujo actual va directo de libro a orden sin carrito)*.
- [ ] 4. Tests pendientes.
- [ ] 5. `CarritoRepository` y `BibliotecaRepository` interfaces no creadas.
- [ ] 6. Tests de casos de uso pendientes.
- [ ] 7. Casos de uso de carrito no implementados.
- [ ] 8. `RedisCarritoRepository` no implementado.
- [ ] 9. Tests de integración pendientes.
- [ ] 10. `BibliotecaRepository` provisional no creado.
- [ ] 11. `carrito-view.fxml` y `CarritoController.java` no creados.
- [ ] 12. `DetalleLibroController` no existe aún.
- [ ] 13. `mvn verify` pendiente.
- [ ] 13. Verificar que `mvn verify` pasa completo.

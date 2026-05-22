# Plan de Implementación: Historial de Órdenes
**US-022 | Épica 5 – Favoritos, Historial & Recomendaciones**

---

## 1. Análisis y Modelado de Dominio

El historial de órdenes expone las `Orden` completadas por el Buyer (creadas en US-018) de forma paginada y ordenada por fecha descendente. No se crea nueva lógica de dominio; se extiende el repositorio existente y se expone la información al usuario.

### 1.1 Value Object `ResumenOrden`

**Archivo:** `domain/order/ResumenOrden.java`

Proyección de la `Orden` para el listado del historial:
- `UUID ordenId`
- `String numeroOrden` — representación legible del UUID (primeros 8 caracteres)
- `LocalDateTime fecha`
- `EstadoOrden estado`
- `MetodoPago metodoPago`
- `List<String> titulosLibros` — lista de títulos de los items

### 1.2 Extensión de `OrdenRepository`

Nuevo método a agregar:
```java
Page<Orden> buscarPorBuyerIdOrdenadas(UUID buyerId, Pageable pageable);
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `VerHistorialOrdenesUseCase`

**DTO de entrada:** `HistorialOrdenesQuery`
- `UUID buyerId`
- `int pagina` (default 0)
- `int tamano` (default 10)

**DTO de salida:** `HistorialOrdenesResponse`
- `List<ResumenOrdenResponse>`
- `int totalPaginas`
- `long totalOrdenes`
- `boolean vacio`

**Flujo:**
1. Verificar rol `BUYER`.
2. Recuperar las órdenes del buyer con `OrdenRepository.buscarPorBuyerIdOrdenadas()`, orden descendente por `fechaCreacion`.
3. Si no hay órdenes, retornar respuesta con `vacio = true`.
4. Mapear cada `Orden` a `ResumenOrden` y luego a `ResumenOrdenResponse`.

### `VerDetalleOrdenUseCase`

**DTO de entrada:** `UUID ordenId`, `UUID buyerId`
**DTO de salida:** `DetalleOrdenResponse`
- `String numeroOrden`, `LocalDateTime fecha`, `EstadoOrden estado`
- `MetodoPago metodoPago`
- `DireccionFacturacion direccion`
- `List<ItemOrdenResponse>` (libroId, titulo)

**Flujo:**
1. Verificar rol `BUYER`.
2. Recuperar la orden con `OrdenRepository.buscarPorId(ordenId)`. Si no existe, lanzar `OrdenNoEncontradaException`.
3. Verificar que `orden.getBuyerId().equals(buyerId)`. Si no, lanzar `AccesoDenegadoException` (una orden pertenece solo a su comprador).
4. Mapear y retornar.

**Excepción nueva:** `OrdenNoEncontradaException`

---

## 3. Capa de Infraestructura

No se requieren migraciones nuevas; la tabla `orden` y `orden_item` ya existen desde US-018 (`V9`).

### 3.1 Actualización del Repositorio JPA

**`SpringOrdenRepository`** (actualizado):
```java
Page<OrdenJpaEntity> findByBuyerIdOrderByFechaCreacionDesc(
    UUID buyerId, Pageable pageable);

Optional<OrdenJpaEntity> findByIdAndBuyerId(UUID id, UUID buyerId);
```

**`OrdenRepositoryAdapter`** (actualizado):
- Implementar `buscarPorBuyerIdOrdenadas()` delegando al nuevo método JPA.
- Implementar `buscarPorId()` ya existe desde US-018; verificar que incluye los items.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/historial-view.fxml`
- Lista de órdenes con: número de orden, fecha, lista resumida de títulos (máx. 2 + "y N más"), método de pago y estado.
- Paginación en la parte inferior.
- Mensaje informativo si el historial está vacío.
- Al hacer clic en una orden: abre un diálogo de detalle con toda la información.

**Controlador:** `UI/controllers/HistorialController.java`
- Al cargar: invoca `VerHistorialOrdenesUseCase`.
- Al seleccionar una orden: invoca `VerDetalleOrdenUseCase` y muestra el diálogo de detalle.
- Al cambiar de página: reinvoca el caso de uso con la nueva página.

---

## 5. Plan de Pruebas (TDD)

### `VerHistorialOrdenesUseCaseTest` (con Mockito)
- `ejecutar_conOrdenesExistentes_debeRetornarListaPaginada()`
- `ejecutar_sinOrdenes_debeRetornarHistorialVacio()`
- `ejecutar_debeOrdenarPorFechaDescendente()`

### `VerDetalleOrdenUseCaseTest` (con Mockito)
- `ejecutar_conOrdenDelBuyer_debeRetornarDetalle()`
- `ejecutar_conOrdenDeOtroBuyer_debeLanzarAccesoDenegadoException()`
- `ejecutar_conOrdenInexistente_debeLanzarOrdenNoEncontradaException()`

### `OrdenRepositoryAdapterTest` (ampliado — `@DataJpaTest`)
- `buscarPorBuyerIdOrdenadas_debeRetornarOrdenesDescendentes()`
- `buscarPorBuyerIdOrdenadas_conBuyerSinOrdenes_debeRetornarVacio()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `VerHistorialOrdenesUseCase` y `VerDetalleOrdenUseCase` no conocen JPA. Extienden el repositorio ya definido sin acoplar el dominio a Spring Data. |
| **DDD** | `ResumenOrden` es un Value Object de lectura (proyección). No es la entidad `Orden`; es una vista ligera para el listado. |
| **TDD** | Escribir los tests de ambos casos de uso antes de implementarlos. La verificación de propiedad de la orden (`buyerId`) debe testearse explícitamente. |
| **SOLID (SRP)** | `VerHistorialOrdenesUseCase` lista. `VerDetalleOrdenUseCase` muestra detalle. Dos responsabilidades, dos clases. |
| **SOLID (OCP)** | Se extiende `OrdenRepository` con un nuevo método sin modificar los existentes ni los casos de uso de US-018. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear el Value Object `ResumenOrden` en `domain/order/`.
- [ ] 2. Crear `OrdenNoEncontradaException` en `domain/order/exception/`.
- [ ] 3. Agregar `buscarPorBuyerIdOrdenadas()` a la interface `OrdenRepository`.
- [ ] 4. Escribir los tests de `VerHistorialOrdenesUseCase` y `VerDetalleOrdenUseCase` con Mockito (deben fallar).
- [ ] 5. Implementar ambos casos de uso y verificar.
- [ ] 6. Actualizar `SpringOrdenRepository` con los nuevos métodos JPA.
- [ ] 7. Actualizar `OrdenRepositoryAdapter` para implementar el nuevo método.
- [ ] 8. Ampliar `OrdenRepositoryAdapterTest` y verificar.
- [ ] 9. Crear `historial-view.fxml` y `HistorialController.java`.
- [ ] 10. Verificar que `mvn verify` pasa completo.

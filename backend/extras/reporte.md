# Reporte de Implementación — Carpeta `desing`

> Auditoría completa: cada plan fue cruzado con el código real del proyecto. Cada plan tiene su lista de tareas actualizada con `[x]` (hecho) o `[ ]` (pendiente/no aplica).

---

## Resumen de Estado por Plan

| Plan | Implementado en código | Pendiente / Difiere del plan |
|---|---|---|
| **001** Configuración | CI yml + README creados | Política de PR y verificación del pipeline (requieren GitHub) |
| **002** Entorno | Proyecto, dependencias, paquetes, SecurityConfig ✅ | Tests de contexto, perfil `test` separado |
| **003** Base de datos | docker-compose con PostgreSQL + Redis ✅ | Flyway (no usado — Hibernate gestiona esquema), tests de conexión |
| **004** Registro Buyer | `User` entity + `UserService.register()` + `RegisterController` ✅ | Excepciones tipadas, tests unitarios, mapper separado |
| **005** Inicio de Sesión | `UserService.login()` + `SessionManager` Singleton + redirección por rol ✅ | Validación de cuenta inactiva en login, SesionGateway con Redis |
| **006** Registro Seller | `UserService.registerSeller()` + `POST /register-seller` ✅ | `PerfilSeller` VO, excepciones tipadas, vista separada de registro |
| **007** Edición Perfil | `UserRepository.findById()` disponible por JPA ✅ | `PerfilUsuario` VO, casos de uso, vista `perfil-view.fxml` (todo pendiente) |
| **008** Gestión Admin | `activate()`/`deactivate()` en User + endpoints admin + filtros por rol ✅ | Tabla auditoría, `AuditoriaRepository`, `Specification` JPA |
| **009** Publicación Libro | `status`, `sellerEmail`, `rejectionReason` en `Book` + `existsByIsbn()` ✅ | `ArchivoGateway`, excepciones tipadas, tests unitarios |
| **010** Catálogo Búsqueda | Query JPQL multi-campo + `BookService.searchCatalog()` + endpoint ✅ | `FiltroCatalogo` VO, `CatalogoCache`, `RedisCatalogoCache`, tests |
| **011** Filtros Catálogo | Filtro por categoría + término combinado + endpoint filter ✅ | `LibroSpecification`, `FiltroCatalogoTest`, panel UI de filtros |
| **012** Detalle Libro | `description` y `coverUrl` en `Book` + `GET /api/books/{id}` ✅ | `DetalleLibro` VO, `ResenaRepository`, vista `detalle-libro-view.fxml` |
| **013** Categorías | Categoría como `String category` en `Book` ✅ | Entidades `Categoria`/`Etiqueta` independientes, sus repositorios y casos de uso |
| **014** Aprobación Libros | `Book.approve()` + `Book.reject()` + endpoints + `findByStatus()` ✅ | Excepciones tipadas, `NotificacionGateway`, tests de dominio |
| **015** Carrito | ❌ No implementado | Todo: `Carrito`, `CarritoRepository`, `RedisCarritoRepository`, vistas |
| **016** Checkout Dirección | ❌ No implementado | Todo: `DireccionFacturacion`, `CheckoutSessionGateway`, vistas |
| **017-018** Checkout Orden | `Order` entity + `OrderService.createOrder()` + Strategy + Observer ✅ | `MetodoPago` enum, checkout multi-paso, tests de dominio |

---

## Patrones GoF — Estado en Código

| Patrón | Clase(s) activas | Archivo(s) |
|---|---|---|
| **Singleton** | `SessionManager.getInstance()` | `UI/SessionManager.java` |
| **Factory Method** | `ViewFactory.loadView()` | `UI/ViewFactory.java` |
| **Builder** | `@Builder` en `User`, `Book`, `Order` | `domain/user/User.java`, `domain/book/Book.java`, `domain/order/Order.java` |
| **Strategy** | `DiscountStrategy` → `NoDiscount`, `VipDiscount`, `SeasonalDiscount` | `domain/order/strategy/` |
| **Observer** | `OrderCompletedEvent` + `BookObserver` | `domain/order/events/`, `domain/book/BookObserver.java` |

---

## Qué queda pendiente (próximos sprints)

1. **Tests unitarios** de todos los servicios y entidades (ningún plan tiene tests creados aún).
2. **Carrito de compras** (US-015): toda la funcionalidad es nueva.
3. **Checkout multi-paso** (US-016 + US-017): `DireccionFacturacion`, sesión de checkout en Redis, vistas FXML.
4. **Perfil de usuario** (US-007): `PerfilUsuario` VO, casos de uso, `perfil-view.fxml`.
5. **Excepciones de dominio tipadas**: reemplazar `RuntimeException` por excepciones propias en `domain/*/exception/`.
6. **Seguridad por roles**: proteger rutas `/api/admin/**` y `/api/seller/**` en `SecurityConfig`.

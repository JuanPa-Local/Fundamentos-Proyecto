# Plan de Implementación: Publicación de un Libro por el Vendedor
**US-009 | Épica 2 – Catálogo de Libros**

---

## 1. Análisis y Modelado de Dominio

El `Libro` es el Aggregate Root del contexto `book`. Su publicación es iniciada por un `Seller` y el libro nace en estado `PENDIENTE` hasta que el administrador lo apruebe. El archivo digital se gestiona de forma separada a través de un servicio de almacenamiento.

### 1.1 Enumeraciones necesarias

**`EstadoLibro`** (en `domain/book/`):
- Valores: `PENDIENTE`, `APROBADO`, `RECHAZADO`

### 1.2 Entidad `Libro` (Aggregate Root)

**Archivo:** `domain/book/Libro.java`

**Atributos:**
- `id` (UUID)
- `titulo` (String) — obligatorio
- `autor` (String) — obligatorio
- `isbn` (String) — obligatorio, único
- `categoria` (String) — obligatorio
- `etiquetas` (List<String>) — opcional
- `estado` (EstadoLibro) — `PENDIENTE` al crear
- `sellerId` (UUID) — referencia al Seller propietario
- `archivoUrl` (String) — URL del archivo almacenado
- `motivoRechazo` (String) — nulo hasta que el admin rechace
- `fechaPublicacion` (LocalDateTime)

**Método fábrica:**
- `static Libro publicar(String titulo, String autor, String isbn, String categoria, List<String> etiquetas, UUID sellerId, String archivoUrl)` — valida campos obligatorios y asigna `estado = PENDIENTE`.

**Excepciones de dominio** (en `domain/book/exception/`):
- `CampoObligatorioLibroException`
- `IsbnDuplicadoException`

### 1.3 Interface `LibroRepository` (puerto de salida)

```java
// domain/book/LibroRepository.java
public interface LibroRepository {
    Libro guardar(Libro libro);
    boolean existePorIsbn(String isbn);
    Optional<Libro> buscarPorId(UUID id);
    Page<Libro> buscarAprobados(FiltroCatalogo filtro, Pageable pageable);
}
```

### 1.4 Interface `ArchivoGateway` (puerto de salida)

```java
// domain/book/ArchivoGateway.java
public interface ArchivoGateway {
    String almacenar(byte[] contenido, String nombreArchivo, String tipoMime);
    void eliminar(String archivoUrl);
}
```

---

## 2. Capa de Aplicación (Caso de Uso)

### `PublicarLibroUseCase`

**DTO de entrada:** `PublicarLibroCommand`
- `UUID sellerId`
- `String titulo`, `autor`, `isbn`, `categoria`
- `List<String> etiquetas`
- `byte[] archivoContenido`
- `String nombreArchivo`
- `String tipoMime`
- `long tamanoBytes`

**Flujo:**
1. Verificar que el `sellerId` tiene rol `SELLER`.
2. Verificar que el archivo no supere 100 MB. Si supera, lanzar `ArchivoDemasiandoGrandeException`.
3. Verificar que el `tipoMime` sea `application/pdf` o `application/epub+zip`. Si no, lanzar `FormatoArchivoInvalidoException`.
4. Verificar unicidad del ISBN con `LibroRepository.existePorIsbn()`. Si existe, lanzar `IsbnDuplicadoException`.
5. Almacenar el archivo con `ArchivoGateway.almacenar()`.
6. Crear el `Libro` con el método fábrica `Libro.publicar()`.
7. Persistir con `LibroRepository.guardar()`.
8. Retornar `LibroResponse` con `id`, `titulo`, `estado`.

**Excepciones nuevas en `domain/book/exception/`:**
- `ArchivoDemasiandoGrandeException`
- `FormatoArchivoInvalidoException`

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V5__crear_tabla_libro.sql`

```sql
CREATE TABLE libro (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo           VARCHAR(255) NOT NULL,
    autor            VARCHAR(255) NOT NULL,
    isbn             VARCHAR(20)  NOT NULL UNIQUE,
    categoria        VARCHAR(100) NOT NULL,
    etiquetas        TEXT[],
    estado           VARCHAR(50)  NOT NULL DEFAULT 'PENDIENTE',
    seller_id        UUID NOT NULL REFERENCES usuario(id),
    archivo_url      VARCHAR(500) NOT NULL,
    motivo_rechazo   TEXT,
    fecha_publicacion TIMESTAMP NOT NULL DEFAULT now()
);
```

### 3.2 Entidad y Repositorio JPA

- `LibroJpaEntity.java` con anotaciones `@Entity`, `@Table(name = "libro")`.
- `SpringLibroRepository.java` extendiendo `JpaRepository`.
- `LibroRepositoryAdapter.java` implementando `LibroRepository`.
- `LibroMapper.java` para conversiones entre dominio e infraestructura.

### 3.3 Implementación de `ArchivoGateway`

**`LocalArchivoGateway.java`** (en `infrastructure/storage/`):
- Guarda el archivo en un directorio local configurado en `application.yml`.
- La URL retornada es la ruta relativa al directorio de almacenamiento.

```yaml
openlib:
  storage:
    path: ./uploads
    max-size-mb: 100
```

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/publicar-libro-view.fxml`
- Campos: `titulo`, `autor`, `isbn`, `categoria`, selector de `etiquetas`.
- Botón "Seleccionar archivo" que abre un `FileChooser` filtrado a PDF/EPUB.
- Indicador del nombre del archivo seleccionado y su tamaño.
- Botón "Publicar".

**Controlador:** `UI/controllers/PublicarLibroController.java`
- Usa `FileChooser` para leer el archivo desde el sistema de archivos local.
- Invoca `PublicarLibroUseCase`.
- Muestra el estado resultante al Seller.

---

## 5. Plan de Pruebas (TDD)

### `LibroTest` (dominio)
- `publicar_conDatosValidos_debeCrearLibroConEstadoPendiente()`
- `publicar_sinTitulo_debeLanzarCampoObligatorioLibroException()`
- `publicar_sinIsbn_debeLanzarCampoObligatorioLibroException()`

### `PublicarLibroUseCaseTest` (con Mockito)
- `ejecutar_conArchivoValido_debeGuardarLibroYRetornarResponse()`
- `ejecutar_conArchivoMayor100MB_debeLanzarArchivoDemasiandoGrandeException()`
- `ejecutar_conFormatoInvalido_debeLanzarFormatoArchivoInvalidoException()`
- `ejecutar_conIsbnDuplicado_debeLanzarIsbnDuplicadoException()`

### `LibroRepositoryAdapterTest` (`@DataJpaTest`)
- `guardar_debePersistitLibroConEstadoPendiente()`
- `existePorIsbn_conIsbnExistente_debeRetornarTrue()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `PublicarLibroUseCase` no conoce el sistema de archivos local ni JPA. Depende de `LibroRepository` y `ArchivoGateway` (ambas interfaces). |
| **DDD** | `Libro` encapsula sus propias reglas de creación en el método fábrica `publicar()`. El caso de uso no duplica esas validaciones; las delega al dominio. |
| **TDD** | Escribir `LibroTest` y `PublicarLibroUseCaseTest` antes de implementar las clases de dominio y aplicación. |
| **SOLID (SRP)** | `LocalArchivoGateway` tiene la única responsabilidad de gestionar el almacenamiento de archivos. `LibroRepositoryAdapter` gestiona la persistencia de la entidad `Libro`. |
| **SOLID (DIP)** | `PublicarLibroUseCase` depende de `ArchivoGateway` (interface). En tests se inyecta un mock; en producción se inyecta `LocalArchivoGateway`. |

---

## 📋 Tareas de Implementación

- [x] 1. Estado del libro implementado como `String status` (valores: `PENDIENTE`, `APROBADO`, `RECHAZADO`) en `Book.java`.
- [ ] 2. Excepciones de dominio en `domain/book/exception/` no creadas *(se usa `IllegalArgumentException`/`IllegalStateException`)*.
- [ ] 3. `LibroTest` no creado.
- [x] 4. Entidad `Book` existe con `@Builder`, campo `status` (default `"PENDIENTE"`), `sellerEmail`, `rejectionReason`.
- [ ] 5. Tests pendientes.
- [x] 6. `BookRepository extends JpaRepository<Book, UUID>` con `existsByIsbn()`.
- [ ] 7. `PublicarLibroUseCaseTest` no creado.
- [x] 8. `BookService.createBook()` cumple la función del caso de uso; `existsByIsbn()` previene ISBN duplicados.
- [ ] 9. Tests del servicio pendientes.
- [ ] 10. Script `V5__crear_tabla_libro.sql` no existe *(Hibernate gestiona el esquema)*.
- [x] 11. No existe `LibroJpaEntity` separada — `Book.java` es directamente la entidad JPA.
- [ ] 12. `ArchivoGateway` / `LocalArchivoGateway` no implementados *(archivos gestionados como `filePath` String)*.
- [ ] 13. Tests de repositorio pendientes.
- [x] 14. `seller-view.fxml` y `SellerController.java` existen con formulario de publicación de libros.
- [ ] 15. `mvn verify` pendiente.
- [ ] 15. Verificar que `mvn verify` pasa completo.

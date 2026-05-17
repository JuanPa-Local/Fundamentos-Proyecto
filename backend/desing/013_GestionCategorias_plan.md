# Plan de Implementación: Gestión de Categorías y Etiquetas
**US-013 | Épica 2 – Catálogo de Libros**

---

## 1. Análisis y Modelado de Dominio

Las categorías y etiquetas son entidades independientes que se asocian a los libros. Se introducen como Aggregate Roots propios dado que tienen identidad, ciclo de vida independiente y reglas de negocio específicas (no eliminar categorías con libros asociados).

### 1.1 Entidad `Categoria` (Aggregate Root)

**Archivo:** `domain/book/Categoria.java`
- `UUID id`
- `String nombre` — único, obligatorio

**Método fábrica:** `static Categoria crear(String nombre)` — valida que el nombre no esté vacío.

**Excepción:** `NombreCategoriaObligatorioException`

### 1.2 Entidad `Etiqueta` (Aggregate Root)

**Archivo:** `domain/book/Etiqueta.java`
- `UUID id`
- `String nombre` — único, obligatorio

**Método fábrica:** `static Etiqueta crear(String nombre)` — valida que el nombre no esté vacío.

**Excepción:** `NombreEtiquetaObligatorioException`

### 1.3 Interfaces de Repositorio

```java
// domain/book/CategoriaRepository.java
public interface CategoriaRepository {
    Categoria guardar(Categoria categoria);
    Optional<Categoria> buscarPorId(UUID id);
    Optional<Categoria> buscarPorNombre(String nombre);
    List<Categoria> listarTodas();
    boolean existePorNombre(String nombre);
    boolean tieneLibrosAsociados(UUID categoriaId);
    void eliminar(UUID categoriaId);
}

// domain/book/EtiquetaRepository.java
public interface EtiquetaRepository {
    Etiqueta guardar(Etiqueta etiqueta);
    Optional<Etiqueta> buscarPorId(UUID id);
    boolean existePorNombre(String nombre);
    List<Etiqueta> listarTodas();
    void eliminar(UUID etiquetaId);
}
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `GestionarCategoriaUseCase`

Agrupa las operaciones CRUD sobre `Categoria`:
- `Categoria crear(String nombre, UUID adminId)` — valida rol ADMIN, unicidad del nombre.
- `Categoria editar(UUID id, String nuevoNombre, UUID adminId)` — valida rol ADMIN, unicidad.
- `void eliminar(UUID id, UUID adminId)` — valida rol ADMIN, verifica que no haya libros asociados. Lanza `CategoriaConLibrosException` si los hay.

### `GestionarEtiquetaUseCase`

Agrupa las operaciones CRUD sobre `Etiqueta`:
- `Etiqueta crear(String nombre, UUID adminId)` — valida rol ADMIN, unicidad.
- `Etiqueta editar(UUID id, String nuevoNombre, UUID adminId)` — valida rol ADMIN.
- `void eliminar(UUID id, UUID adminId)` — valida rol ADMIN. Las etiquetas se desvinculan automáticamente de los libros en la base de datos.

**Excepciones nuevas** (en `domain/book/exception/`):
- `CategoriaConLibrosException`
- `NombreDuplicadoException`

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V8__crear_tablas_categoria_etiqueta.sql`

```sql
CREATE TABLE categoria (
    id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE etiqueta (
    id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- Relación Libro-Etiqueta (tabla de unión)
CREATE TABLE libro_etiqueta (
    libro_id   UUID NOT NULL REFERENCES libro(id) ON DELETE CASCADE,
    etiqueta_id UUID NOT NULL REFERENCES etiqueta(id) ON DELETE CASCADE,
    PRIMARY KEY (libro_id, etiqueta_id)
);

-- Referencia de libro a categoría
ALTER TABLE libro
    ADD COLUMN categoria_id UUID REFERENCES categoria(id);
```

### 3.2 Entidades y Repositorios JPA

- `CategoriaJpaEntity.java`, `SpringCategoriaRepository.java`, `CategoriaRepositoryAdapter.java`
- `EtiquetaJpaEntity.java`, `SpringEtiquetaRepository.java`, `EtiquetaRepositoryAdapter.java`

En `SpringCategoriaRepository`:
```java
@Query("SELECT COUNT(l) > 0 FROM LibroJpaEntity l WHERE l.categoria.id = :categoriaId")
boolean tieneLibrosAsociados(@Param("categoriaId") UUID categoriaId);
```

### 3.3 Invalidación del caché

Al crear, editar o eliminar una categoría o etiqueta, invocar `CatalogoCache.invalidar("catalogo:*")` para que los filtros del catálogo reflejen los cambios.

---

## 4. Capa de UI (JavaFX)

**`admin-view.fxml`** (actualizado — nueva pestaña o sección):
- Tabla de categorías: `nombre`, botones "Editar" y "Eliminar".
- Botón "Nueva categoría" que abre un diálogo de texto.
- Tabla de etiquetas con misma estructura.

**`AdminController.java`** (actualizado):
- Invoca `GestionarCategoriaUseCase` y `GestionarEtiquetaUseCase` según la acción del usuario.
- Muestra un diálogo de confirmación antes de eliminar.
- Muestra un error descriptivo si se intenta eliminar una categoría con libros.

---

## 5. Plan de Pruebas (TDD)

### `CategoriaTest` y `EtiquetaTest` (dominio)
- `crear_conNombreValido_debeInstanciar()`
- `crear_sinNombre_debeLanzarExcepcion()`

### `GestionarCategoriaUseCaseTest` (con Mockito)
- `crear_conNombreUnico_debeGuardarYRetornar()`
- `crear_conNombreDuplicado_debeLanzarNombreDuplicadoException()`
- `eliminar_sinLibrosAsociados_debeEliminar()`
- `eliminar_conLibrosAsociados_debeLanzarCategoriaConLibrosException()`
- `eliminar_sinRolAdmin_debeLanzarAccesoDenegadoException()`

### `GestionarEtiquetaUseCaseTest` (con Mockito)
- `crear_conNombreUnico_debeGuardarYRetornar()`
- `eliminar_debeEliminarSinRestriccion()`

### Tests de repositorio (`@DataJpaTest`)
- `CategoriaRepositoryAdapterTest`: guardar, buscar, verificar libros asociados.
- `EtiquetaRepositoryAdapterTest`: guardar, eliminar.

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Los casos de uso `GestionarCategoriaUseCase` y `GestionarEtiquetaUseCase` no dependen de JPA ni de JavaFX. |
| **DDD** | `Categoria` y `Etiqueta` son Aggregate Roots independientes, no Value Objects del `Libro`. Tienen repositorios propios. |
| **TDD** | Escribir los tests de ambos casos de uso antes de implementarlos. |
| **SOLID (SRP)** | Un caso de uso por concepto: `GestionarCategoriaUseCase` y `GestionarEtiquetaUseCase` separados, aunque tengan estructura similar. |
| **SOLID (OCP)** | La eliminación en cascada de etiquetas de libros está gestionada a nivel de base de datos (FK con `ON DELETE CASCADE`), sin modificar la lógica de dominio. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear entidades `Categoria` y `Etiqueta` con sus métodos fábrica y excepciones.
- [ ] 2. Escribir `CategoriaTest` y `EtiquetaTest` (deben fallar).
- [ ] 3. Verificar que los tests de dominio pasan.
- [ ] 4. Definir las interfaces `CategoriaRepository` y `EtiquetaRepository`.
- [ ] 5. Escribir los tests de `GestionarCategoriaUseCase` y `GestionarEtiquetaUseCase`.
- [ ] 6. Implementar ambos casos de uso y verificar.
- [ ] 7. Crear el script `V8__crear_tablas_categoria_etiqueta.sql`.
- [ ] 8. Crear las entidades JPA, repositorios Spring y adaptadores.
- [ ] 9. Escribir y verificar los tests de repositorio.
- [ ] 10. Actualizar `admin-view.fxml` y `AdminController.java`.
- [ ] 11. Verificar que `mvn verify` pasa completo.

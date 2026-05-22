# Plan de Implementación: Descarga Segura de Libros
**US-020 | Épica 4 – Biblioteca Personal & Motor de Descargas**

---

## 1. Análisis y Modelado de Dominio

El motor de descargas genera enlaces únicos, temporales y de un solo uso para cada solicitud. La lógica de validación del enlace pertenece al dominio; la generación del token y el almacenamiento pertenecen a la infraestructura. Cada descarga queda registrada en auditoría.

### 1.1 Entidad `EnlaceDescarga` (Aggregate Root)

**Archivo:** `domain/order/EnlaceDescarga.java`

**Atributos:**
- `UUID id`
- `UUID buyerId`
- `UUID libroId`
- `String token` — cadena única generada aleatoriamente
- `LocalDateTime expiracion` — `ahora + 15 minutos`
- `boolean usado` — `false` al crear

**Comportamientos:**
- `static EnlaceDescarga generar(UUID buyerId, UUID libroId, String token)` — método fábrica, establece expiración a 15 minutos desde ahora, `usado = false`.
- `void marcarComoUsado()` — cambia `usado` a `true`. Lanza `EnlaceYaUsadoException` si ya fue usado.
- `boolean estaVigente()` — retorna `true` si `LocalDateTime.now().isBefore(expiracion) && !usado`.
- `void validar()` — lanza `EnlaceExpiradoException` si `!estaVigente()` o `EnlaceYaUsadoException` si `usado`.

**Excepciones de dominio** (en `domain/order/exception/`):
- `EnlaceExpiradoException`
- `EnlaceYaUsadoException`
- `LibroNoEnBibliotecaException`

### 1.2 Value Object `RegistroDescarga`

**Archivo:** `domain/order/RegistroDescarga.java`
- `UUID buyerId`
- `UUID libroId`
- `String ipOrigen`
- `LocalDateTime timestamp`

### 1.3 Interfaces de Repositorio

```java
// domain/order/EnlaceDescargaRepository.java
public interface EnlaceDescargaRepository {
    EnlaceDescarga guardar(EnlaceDescarga enlace);
    Optional<EnlaceDescarga> buscarPorToken(String token);
    void guardarRegistro(RegistroDescarga registro);
}
```

### 1.4 Interface `TokenGateway` (puerto de salida)

```java
// domain/order/TokenGateway.java
public interface TokenGateway {
    String generar(); // genera un UUID aleatorio seguro
}
```

---

## 2. Capa de Aplicación (Casos de Uso)

### `GenerarEnlaceDescargaUseCase`

**DTO de entrada:** `GenerarEnlaceCommand` (`UUID buyerId`, `UUID libroId`)
**DTO de salida:** `EnlaceDescargaResponse` (`String token`, `LocalDateTime expiracion`)

**Flujo:**
1. Verificar que el `buyerId` tiene rol `BUYER`.
2. Verificar que el libro está en la biblioteca del buyer con `BibliotecaRepository.existeEnBiblioteca()`. Si no, lanzar `LibroNoEnBibliotecaException`.
3. Generar un token único con `TokenGateway.generar()`.
4. Crear el `EnlaceDescarga` con `EnlaceDescarga.generar(buyerId, libroId, token)`.
5. Persistir con `EnlaceDescargaRepository.guardar()`.
6. Retornar el `EnlaceDescargaResponse`.

### `EjecutarDescargaUseCase`

**DTO de entrada:** `DescargaCommand` (`String token`, `String ipOrigen`)
**DTO de salida:** `byte[]` (contenido del archivo) o URL de descarga local

**Flujo:**
1. Recuperar el enlace con `EnlaceDescargaRepository.buscarPorToken(token)`. Si no existe, lanzar `EnlaceExpiradoException`.
2. Invocar `enlace.validar()` — lanza excepción si está expirado o ya fue usado.
3. Invocar `enlace.marcarComoUsado()`.
4. Persistir el enlace actualizado.
5. Registrar la descarga con `EnlaceDescargaRepository.guardarRegistro(new RegistroDescarga(...))`.
6. Recuperar el archivo con `ArchivoGateway` y retornarlo.

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V11__crear_tabla_enlace_descarga.sql`

```sql
CREATE TABLE enlace_descarga (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id    UUID NOT NULL REFERENCES usuario(id),
    libro_id    UUID NOT NULL REFERENCES libro(id),
    token       VARCHAR(255) NOT NULL UNIQUE,
    expiracion  TIMESTAMP NOT NULL,
    usado       BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE registro_descarga (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id    UUID NOT NULL,
    libro_id    UUID NOT NULL,
    ip_origen   VARCHAR(45),
    timestamp   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_enlace_token ON enlace_descarga (token);
```

### 3.2 Entidades y Repositorios JPA

- `EnlaceDescargaJpaEntity.java`, `SpringEnlaceDescargaRepository.java`, `EnlaceDescargaRepositoryAdapter.java`.
- `RegistroDescargaJpaEntity.java`.

### 3.3 Implementación de `TokenGateway`

**`UuidTokenGateway.java`** (en `infrastructure/security/`):
```java
@Component
public class UuidTokenGateway implements TokenGateway {
    public String generar() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
```

---

## 4. Capa de UI (JavaFX)

**`biblioteca-view.fxml`** (actualizado):
- Al seleccionar un libro, mostrar botón "Descargar".

**`BibliotecaController.java`** (actualizado):
- Al hacer clic en "Descargar":
  1. Invoca `GenerarEnlaceDescargaUseCase` para obtener el token.
  2. Invoca `EjecutarDescargaUseCase` con el token.
  3. Abre un `FileChooser` para que el usuario elija dónde guardar el archivo.
  4. Guarda el archivo en la ruta seleccionada.
  5. Muestra un mensaje de éxito o error.

---

## 5. Plan de Pruebas (TDD)

### `EnlaceDescargaTest` (dominio)
- `generar_debeCrearEnlaceConExpiracionA15Minutos()`
- `generar_debeCrearEnlaceNoUsado()`
- `marcarComoUsado_debeSetearUsadoTrue()`
- `marcarComoUsado_conEnlaceYaUsado_debeLanzarEnlaceYaUsadoException()`
- `estaVigente_conEnlaceNuevo_debeRetornarTrue()`
- `estaVigente_conEnlaceExpirado_debeRetornarFalse()`
- `validar_conEnlaceUsado_debeLanzarEnlaceYaUsadoException()`
- `validar_conEnlaceExpirado_debeLanzarEnlaceExpiradoException()`

### `GenerarEnlaceDescargaUseCaseTest` (con Mockito)
- `ejecutar_conLibroEnBiblioteca_debeGenerarEnlaceYPersistir()`
- `ejecutar_conLibroFueraDeBiblioteca_debeLanzarLibroNoEnBibliotecaException()`

### `EjecutarDescargaUseCaseTest` (con Mockito)
- `ejecutar_conTokenValido_debeMarcarUsadoYRegistrarDescarga()`
- `ejecutar_conTokenInexistente_debeLanzarEnlaceExpiradoException()`
- `ejecutar_conTokenYaUsado_debeLanzarEnlaceYaUsadoException()`

### `EnlaceDescargaRepositoryAdapterTest` (`@DataJpaTest`)
- `guardar_debePersistirEnlace()`
- `buscarPorToken_conTokenExistente_debeRetornarEnlace()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `GenerarEnlaceDescargaUseCase` y `EjecutarDescargaUseCase` no conocen JPA, PostgreSQL ni el sistema de archivos. Dependen de interfaces. |
| **DDD** | `EnlaceDescarga` encapsula toda la lógica de validación. El caso de uso no verifica si está expirado o usado; llama a `enlace.validar()`. |
| **TDD** | Escribir `EnlaceDescargaTest` antes de implementar la entidad. Es la clase con más lógica de dominio de esta historia. |
| **SOLID (SRP)** | `GenerarEnlaceDescargaUseCase` solo genera el enlace. `EjecutarDescargaUseCase` solo procesa la descarga. Son responsabilidades distintas. |
| **SOLID (DIP)** | `EjecutarDescargaUseCase` depende de `ArchivoGateway` (interface de US-009). La implementación local se inyecta en tiempo de ejecución. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear `RegistroDescarga` (Value Object) y las excepciones en `domain/order/`.
- [ ] 2. Escribir `EnlaceDescargaTest` (debe fallar).
- [ ] 3. Crear la entidad `EnlaceDescarga` con todos sus comportamientos.
- [ ] 4. Verificar que `EnlaceDescargaTest` pasa.
- [ ] 5. Definir interfaces `EnlaceDescargaRepository` y `TokenGateway` en `domain/order/`.
- [ ] 6. Escribir los tests de `GenerarEnlaceDescargaUseCase` y `EjecutarDescargaUseCase`.
- [ ] 7. Implementar ambos casos de uso y verificar.
- [ ] 8. Crear el script `V11__crear_tabla_enlace_descarga.sql`.
- [ ] 9. Crear `EnlaceDescargaJpaEntity`, `SpringEnlaceDescargaRepository` y `EnlaceDescargaRepositoryAdapter`.
- [ ] 10. Implementar `UuidTokenGateway`.
- [ ] 11. Escribir y verificar `EnlaceDescargaRepositoryAdapterTest`.
- [ ] 12. Actualizar `biblioteca-view.fxml` y `BibliotecaController.java`.
- [ ] 13. Verificar que `mvn verify` pasa completo.

# Plan de Implementación: Edición del Perfil de Usuario
**US-007 | Épica 1 – Autenticación & Gestión de Usuarios**

---

## 1. Análisis y Modelado de Dominio

El perfil del usuario agrupa los datos complementarios editables: nombre, foto de perfil y dirección de facturación. Es un Value Object dentro del Aggregate Root `Usuario`. El email permanece inmutable desde el perfil; su cambio requiere un flujo especial fuera del alcance de esta historia.

### 1.1 Value Object `PerfilUsuario`

**Archivo:** `domain/user/PerfilUsuario.java`

**Atributos:**
- `nombre` (String) — obligatorio
- `fotoUrl` (String) — opcional, puede ser null
- `direccionFacturacion` (String) — opcional en el perfil, obligatorio al momento de comprar (ver US-016)

**Reglas en el constructor:**
- `nombre` no puede estar vacío. Lanza `NombreObligatorioException` (ya existe desde US-004).

### 1.2 Comportamientos nuevos en `Usuario`

- `void actualizarPerfil(PerfilUsuario nuevoPerfil)` — reemplaza el Value Object de perfil actual con uno nuevo.
- `PerfilUsuario getPerfil()` — retorna el perfil actual.

---

## 2. Capa de Aplicación (Casos de Uso)

### `VerPerfilUseCase`

**DTO de salida:** `PerfilResponse`
- `String nombre`
- `String email` (solo lectura, no editable)
- `String fotoUrl`
- `String direccionFacturacion`
- `String rol`

**Flujo:**
1. Recuperar el usuario por el `usuarioId` de la sesión activa.
2. Mapear `Usuario` y su `PerfilUsuario` a un `PerfilResponse`.
3. Retornar.

### `ActualizarPerfilUseCase`

**DTO de entrada:** `ActualizarPerfilCommand`
- `UUID usuarioId` (extraído de la sesión activa en el controlador)
- `String nombre`
- `String fotoUrl`
- `String direccionFacturacion`

**Flujo:**
1. Recuperar el usuario con `UsuarioRepository.buscarPorId()`.
2. Crear un nuevo `PerfilUsuario` con los datos recibidos (valida reglas).
3. Invocar `usuario.actualizarPerfil(nuevoPerfil)`.
4. Persistir con `UsuarioRepository.guardar()`.
5. Retornar el `PerfilResponse` actualizado.

### Nuevo método en `UsuarioRepository`

```java
Optional<Usuario> buscarPorId(UUID id);
```

---

## 3. Capa de Infraestructura

### 3.1 Migración de base de datos

**Archivo nuevo:** `V3__agregar_perfil_usuario.sql`

```sql
ALTER TABLE usuario
    ADD COLUMN foto_url           VARCHAR(500),
    ADD COLUMN direccion_facturacion VARCHAR(300);

-- El campo 'nombre' ya existe desde V1
```

### 3.2 Actualización del Mapper JPA

**`UsuarioMapper`** (actualizado):
- Mapear `fotoUrl` y `direccionFacturacion` entre dominio e infraestructura.
- El método `toDomain()` debe construir el `PerfilUsuario` si los campos existen.

---

## 4. Capa de UI (JavaFX)

**Archivo FXML nuevo:** `resources/views/perfil-view.fxml`
- Sección "Ver perfil": muestra nombre, email (solo lectura), foto y dirección.
- Sección "Editar perfil": campos editables para nombre, foto y dirección.
- Botón "Guardar cambios".

**Controlador:** `UI/controllers/PerfilController.java`
- Al cargar la vista, invoca `VerPerfilUseCase` y rellena los campos.
- Al hacer clic en "Guardar cambios", invoca `ActualizarPerfilUseCase`.
- Obtiene el `usuarioId` activo desde `SessionManager`.

---

## 5. Plan de Pruebas (TDD)

### `PerfilUsuarioTest` (dominio)
- `crear_conNombreValido_debeInstanciarPerfil()`
- `crear_sinNombre_debeLanzarNombreObligatorioException()`
- `crear_conFotoNula_debePermitirlo()`

### `UsuarioTest` (ampliado)
- `actualizarPerfil_conPerfilValido_debeReemplazarPerfilAnterior()`

### `ActualizarPerfilUseCaseTest` (con Mockito)
- `ejecutar_conDatosValidos_debeActualizarYRetornarPerfilResponse()`
- `ejecutar_conUsuarioInexistente_debeLanzarUsuarioNoEncontradoException()`

### `VerPerfilUseCaseTest` (con Mockito)
- `ejecutar_conUsuarioExistente_debeRetornarPerfilResponse()`

### `UsuarioRepositoryAdapterTest` (ampliado)
- `guardar_usuarioConPerfil_debePersistirCamposDePerfil()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Los casos de uso `VerPerfilUseCase` y `ActualizarPerfilUseCase` no dependen de JavaFX ni de JPA. Solo dependen de `UsuarioRepository` (interface). |
| **DDD** | `PerfilUsuario` es un Value Object inmutable. `actualizarPerfil()` no modifica el objeto existente; reemplaza el Value Object completo por uno nuevo, respetando la inmutabilidad. |
| **TDD** | Escribir `PerfilUsuarioTest` y los tests de los casos de uso antes de implementarlos. |
| **SOLID (SRP)** | `VerPerfilUseCase` solo lee datos. `ActualizarPerfilUseCase` solo actualiza. No combinar lectura y escritura en un único caso de uso. |
| **SOLID (ISP)** | Considerar si `UsuarioRepository` necesita ser dividido: `UsuarioReadRepository` (solo lectura) y `UsuarioWriteRepository` (escritura) si el número de métodos crece significativamente. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear el Value Object `PerfilUsuario` en `domain/user/`.
- [ ] 2. Escribir `PerfilUsuarioTest` (debe fallar).
- [ ] 3. Verificar que `PerfilUsuarioTest` pasa.
- [ ] 4. Agregar `actualizarPerfil()` y `getPerfil()` a la entidad `Usuario`.
- [ ] 5. Ampliar `UsuarioTest` y verificar.
- [ ] 6. Agregar `buscarPorId()` a la interface `UsuarioRepository`.
- [ ] 7. Escribir `VerPerfilUseCaseTest` y `ActualizarPerfilUseCaseTest` con Mockito.
- [ ] 8. Implementar ambos casos de uso y verificar tests.
- [ ] 9. Crear el script `V3__agregar_perfil_usuario.sql`.
- [ ] 10. Actualizar `UsuarioJpaEntity` y `UsuarioMapper`.
- [ ] 11. Ampliar `UsuarioRepositoryAdapterTest` y verificar.
- [ ] 12. Crear `perfil-view.fxml` y `PerfilController.java`.
- [ ] 13. Verificar que `mvn verify` pasa completo.

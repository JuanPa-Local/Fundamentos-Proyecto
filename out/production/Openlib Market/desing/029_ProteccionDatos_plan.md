# Plan de Implementación: Protección de Datos Sensibles de los Usuarios
**US-029 | Épica 8 – Atributos de Calidad**

---

## 1. Análisis y Contexto

Esta historia garantiza que los datos sensibles estén protegidos tanto en tránsito (HTTPS) como en reposo (cifrado en base de datos). No introduce entidades de dominio nuevas; refuerza la infraestructura de seguridad existente y añade pruebas de verificación explícitas.

### Áreas de impacto
- `SecurityConfig.java` (US-005)
- `UsuarioJpaEntity.java` — cifrado de `direccionFacturacion`
- Configuración de la aplicación (`application.yml`)
- `UsuarioRepositoryAdapter` — verificación de que `password` nunca se almacena en texto plano
- Pipeline CI — lista de verificación de seguridad

---

## 2. Implementación por Capa

### 2.1 Comunicación segura (HTTPS)

**Para el entorno de producción**, configurar el certificado SSL en `application.yml`:

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: openlib

  # Redirigir HTTP a HTTPS
  port: 8443
```

**Para desarrollo local**, usar HTTP. La configuración SSL se activa solo en el perfil `prod`.

**En `SecurityConfig.java`** (actualizado):
```java
// Solo en perfil 'prod': forzar HTTPS
http.requiresChannel(channel ->
    channel.anyRequest().requiresSecure()
);
```

### 2.2 Cifrado de datos sensibles en reposo

La dirección de facturación dentro de `OrdenJpaEntity` se cifra usando un `AttributeConverter` de JPA:

**Archivo nuevo:** `infrastructure/security/CamposCifradosConverter.java`

```java
@Converter
public class CamposCifradosConverter implements AttributeConverter<String, String> {

    private final AESCipher aesCipher; // inyectado desde Spring

    @Override
    public String convertToDatabaseColumn(String atributo) {
        return atributo != null ? aesCipher.cifrar(atributo) : null;
    }

    @Override
    public String convertToEntityAttribute(String columna) {
        return columna != null ? aesCipher.descifrar(columna) : null;
    }
}
```

**Archivo nuevo:** `infrastructure/security/AESCipher.java`
- Usa `AES/GCM/NoPadding` con clave de 256 bits.
- La clave se obtiene de una variable de entorno, nunca hardcodeada.

```yaml
openlib:
  security:
    aes-key: ${OPENLIB_AES_KEY}  # Variable de entorno en producción
    aes-key: base64EncodedTestKey  # Valor de prueba (solo en perfil 'test')
```

**En `OrdenJpaEntity.java`** (actualizado):
```java
@Convert(converter = CamposCifradosConverter.class)
@Column(name = "calle")
private String calle;

@Convert(converter = CamposCifradosConverter.class)
@Column(name = "ciudad")
private String ciudad;
// ... demás campos de dirección
```

### 2.3 Contraseñas en formato irreversible

Verificar que `RegistrarBuyerUseCase` (US-004) y `RegistrarSellerUseCase` (US-006) usan `BCryptPasswordEncoder` antes de guardar. Esto ya está implementado; esta historia agrega la **prueba de verificación explícita**.

---

## 3. Lista de Verificación de Seguridad

**Archivo nuevo:** `docs/security-checklist.md`

```markdown
## Lista de Verificación de Seguridad — OpenLib Market

- [x] Contraseñas almacenadas con BCrypt (factor de costo: 12)
- [x] Tokens de sesión almacenados en Redis con TTL de 1 hora
- [x] Dirección de facturación cifrada con AES-256-GCM en base de datos
- [x] Comunicación protegida con TLS en el perfil de producción
- [x] Variables sensibles (claves AES, contraseñas DB) en variables de entorno
- [x] Prueba unitaria que verifica que la contraseña no se almacena en texto plano
- [x] Rutas de administración protegidas por rol ADMIN
- [x] Headers de seguridad configurados (HSTS, X-Content-Type-Options)
```

---

## 4. Plan de Pruebas (TDD)

### `AESCipherTest` (infraestructura)
- `cifrar_debeProducirTextoDistintoAlOriginal()`
- `descifrar_despuesDeCifrar_debeRecuperarElTextoOriginal()`
- `cifrar_conTextoNulo_debeRetornarNulo()`

### `PasswordSeguridadTest` (integración — `@DataJpaTest`)
- `guardar_usuario_laPasswordNuncaDebeAlmacenarseEnTextoPlanotexto()`:

```java
@Test
void laPasswordNuncaDebeAlmacenarseEnTextoPlano() {
    // Crear y guardar un usuario con password "MiPassword123"
    // Consultar directamente la columna 'password' de la BD con JdbcTemplate
    String passwordEnBD = jdbcTemplate.queryForObject(
        "SELECT password FROM usuario WHERE email = ?",
        String.class, "test@openlib.com"
    );
    // Verificar que NO empieza con "MiPassword123" y SÍ empieza con "$2a$" (BCrypt)
    assertThat(passwordEnBD).doesNotContain("MiPassword123");
    assertThat(passwordEnBD).startsWith("$2a$");
}
```

### `CamposCifradosConverterTest` (infraestructura — `@DataJpaTest`)
- `guardar_ordenConDireccion_laCalleDebeEstarCifradaEnBD()`
- `buscar_orden_laCallDebeEstarDescifradaAlLeer()`

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | `AESCipher` y `CamposCifradosConverter` viven en `infrastructure/security/`. El dominio no conoce que los datos están cifrados. `DireccionFacturacion` (dominio) siempre trabaja con texto plano. |
| **DDD** | No hay cambios en el dominio. El cifrado es un detalle de infraestructura invisible para los Aggregate Roots y Value Objects. |
| **TDD** | Los tests de seguridad se escriben antes de implementar `AESCipher`. El test de contraseña en texto plano debe existir desde US-004; en esta historia se formaliza y documenta. |
| **SOLID (SRP)** | `AESCipher` es responsable exclusivamente del cifrado/descifrado. `CamposCifradosConverter` es responsable de integrar `AESCipher` con JPA. `SecurityConfig` gestiona las reglas de acceso HTTP. |
| **SOLID (DIP)** | `CamposCifradosConverter` depende de `AESCipher` inyectado por Spring, no de una implementación estática. Permite intercambiar el algoritmo en el futuro. |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear `AESCipher.java` en `infrastructure/security/` con AES-256-GCM.
- [ ] 2. Escribir `AESCipherTest` (debe fallar).
- [ ] 3. Verificar que `AESCipherTest` pasa.
- [ ] 4. Crear `CamposCifradosConverter.java`.
- [ ] 5. Escribir `CamposCifradosConverterTest` (debe fallar).
- [ ] 6. Aplicar `@Convert` a los campos de dirección en `OrdenJpaEntity.java`.
- [ ] 7. Verificar que `CamposCifradosConverterTest` pasa.
- [ ] 8. Escribir `PasswordSeguridadTest` y verificar que pasa.
- [ ] 9. Agregar la configuración SSL al perfil `prod` en `application.yml`.
- [ ] 10. Actualizar `SecurityConfig` con redirección HTTPS para el perfil `prod`.
- [ ] 11. Configurar los headers de seguridad HTTP (HSTS, X-Content-Type-Options) en `SecurityConfig`.
- [ ] 12. Crear `docs/security-checklist.md` con todos los ítems completados.
- [ ] 13. Verificar que `mvn verify` pasa completo.

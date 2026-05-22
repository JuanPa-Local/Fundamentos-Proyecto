# Historia de Usuario: Protección de Datos Sensibles de los Usuarios

**Épica 8 – Atributos de Calidad**

**Como** equipo responsable de la seguridad del proyecto, **quiero** que los datos sensibles de los usuarios estén protegidos tanto al almacenarse como al transmitirse, **para** garantizar la privacidad de la información y el cumplimiento de las buenas prácticas de seguridad.

---

## Criterios de Aceptación

**AC-001 – Comunicación segura con la plataforma**
Dado que un usuario accede a la plataforma desde su navegador, cuando el sistema establece la comunicación, entonces esta ocurre siempre a través de una conexión segura y cifrada.

**AC-002 – Datos sensibles protegidos en la base de datos**
Dado que el sistema almacena información sensible de los usuarios como direcciones de facturación, cuando guarda esa información en la base de datos, entonces la almacena de forma cifrada para que no sea legible sin autorización.

**AC-003 – Contraseñas nunca almacenadas en texto visible**
Dado que un usuario se registra o cambia su contraseña, cuando el sistema la recibe, entonces la transforma en un formato irreversible antes de guardarla, de modo que nunca quede almacenada como texto visible.

**AC-004 – Verificación de seguridad documentada**
Dado que se han implementado las medidas de seguridad, cuando el equipo realiza la revisión final, entonces existe un documento que certifica que los controles de seguridad están aplicados correctamente.

---

## Reglas de Negocio

- **RN-NFR-005:** Toda comunicación entre el usuario y la plataforma debe realizarse a través de una conexión cifrada (protocolo seguro).
- **RN-NFR-006:** Los datos sensibles como la dirección de facturación deben almacenarse cifrados en la base de datos.
- **RN-NFR-007:** Las contraseñas nunca se almacenan en texto visible; siempre se transforman a un formato irreversible antes de guardarse.
- **RN-NFR-008:** La plataforma debe contar con una lista de verificación de seguridad completada y documentada como parte de los criterios de entrega.

---

## Criterios de Terminación

- [ ] La comunicación entre el usuario y la plataforma es siempre segura y cifrada
- [ ] Los datos sensibles están cifrados en la base de datos
- [ ] Las contraseñas se almacenan en formato irreversible, nunca como texto visible
- [ ] Existe una prueba que verifica que las contraseñas no se guardan en texto plano
- [ ] La lista de verificación de seguridad está completada y documentada
- [ ] Las pruebas de seguridad están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

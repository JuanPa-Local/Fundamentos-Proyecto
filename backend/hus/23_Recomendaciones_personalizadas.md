# Historia de Usuario: Recomendaciones Personalizadas de Libros

**Épica 5 – Favoritos, Historial & Recomendaciones**

**Como** Comprador, **quiero** recibir recomendaciones de libros basadas en mis compras anteriores, **para** descubrir material relevante sin tener que buscarlo activamente.

---

## Criterios de Aceptación

**AC-001 – Recomendaciones basadas en el historial**
Dado que soy un Comprador con historial de compras, cuando accedo a la sección de recomendaciones, entonces el sistema me muestra hasta 10 libros sugeridos basándose en las categorías y etiquetas de los libros que he adquirido.

**AC-002 – Libros ya adquiridos excluidos**
Dado que el sistema está generando mis recomendaciones, cuando prepara la lista de sugerencias, entonces no incluye libros que ya tengo en mi biblioteca personal.

**AC-003 – Recomendaciones para Compradores sin historial**
Dado que soy un Comprador nuevo sin compras previas, cuando accedo a la sección de recomendaciones, entonces el sistema me muestra los libros más populares de la plataforma en lugar de personalizadas.

**AC-004 – Número máximo de recomendaciones**
Dado que el sistema genera recomendaciones, cuando las presenta, entonces muestra como máximo 10 sugerencias por consulta.

---

## Reglas de Negocio

- **RN-FAV-008:** Las recomendaciones se generan a partir de las categorías y etiquetas de los libros adquiridos por el Comprador.
- **RN-FAV-009:** Nunca se recomienda un libro que el Comprador ya tiene en su biblioteca personal.
- **RN-FAV-010:** Si el Comprador no tiene historial de compras, se muestran los libros más populares de la plataforma.
- **RN-FAV-011:** El número máximo de recomendaciones por consulta es 10.

---

## Criterios de Terminación

- [ ] El sistema genera recomendaciones basadas en las categorías y etiquetas del historial del Comprador
- [ ] Las recomendaciones excluyen libros ya adquiridos
- [ ] Los Compradores sin historial reciben recomendaciones de los libros más populares
- [ ] El sistema muestra máximo 10 recomendaciones por consulta
- [ ] La lógica de recomendación está documentada
- [ ] Las pruebas del módulo de recomendaciones están escritas y pasan exitosamente
- [ ] El código ha sido revisado y aprobado por otro integrante del equipo

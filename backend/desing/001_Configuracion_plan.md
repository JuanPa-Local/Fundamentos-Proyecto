# Plan de Implementación: Configuración del Repositorio y Flujo de Trabajo
**US-001 | Épica 0 – Infraestructura & Base Técnica**

---

## 1. Análisis de Dominio y Contexto

Esta historia no genera clases de dominio propias; su objetivo es establecer los cimientos del repositorio sobre los cuales trabajarán todas las demás historias. El entregable principal es la estructura de ramas Git y la configuración del pipeline de integración continua.

### Decisiones de diseño
- **Estrategia de ramas:** `main` (producción estable), `develop` (integración continua), `feature/US-XXX` (una rama por historia de usuario), `release/*` y `hotfix/*`.
- **Política de merge:** Pull Request obligatorio hacia `develop`; se requiere al menos una aprobación de otro integrante del equipo antes de hacer merge.
- **Pipeline CI:** GitHub Actions o equivalente que ejecute `mvn verify` en cada PR abierto contra `develop`.

---

## 2. Estructura del Repositorio

```
openlib-market/
├── backend/
│   ├── pom.xml
│   ├── docker-compose.yml
│   └── src/
│       ├── main/
│       │   ├── java/com/openlib/backend/
│       │   └── resources/
│       └── test/
├── .github/
│   └── workflows/
│       └── ci.yml          ← Pipeline de integración continua
├── .gitignore
└── README.md
```

---

## 3. Configuración del Pipeline CI (GitHub Actions)

**Archivo:** `.github/workflows/ci.yml`

```yaml
name: CI Pipeline
on:
  pull_request:
    branches: [develop, main]
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '25'
          distribution: 'temurin'
      - name: Build & Test
        run: mvn verify --no-transfer-progress
        working-directory: backend
```

---

## 4. Contenido del README.md

Debe incluir obligatoriamente:
1. Descripción del proyecto (una línea).
2. Prerrequisitos: Java 25, Maven, Docker Desktop.
3. Pasos para clonar y ejecutar localmente (`docker-compose up`, `mvn spring-boot:run`).
4. Convención de ramas y flujo de trabajo del equipo.
5. Cómo correr las pruebas (`mvn test`).

---

## 5. Plan de Pruebas (TDD)

En esta historia la verificación es estructural, no de código de producción:
- **Prueba manual:** Crear una rama `feature/US-001`, hacer un commit vacío, abrir un PR hacia `develop` y verificar que el pipeline CI se ejecuta y reporta estado.
- **Verificación del README:** Que otro integrante del equipo siga las instrucciones desde cero y pueda ejecutar el proyecto sin asistencia.

---

## ✅ Prerrequisitos: Clean Architecture / DDD / TDD / SOLID

| Principio | Acción requerida en esta US |
|---|---|
| **Clean Architecture** | Definir desde ya la separación de paquetes: `domain/`, `application/`, `infrastructure/`, `UI/`. El `pom.xml` debe estar limpio sin dependencias mezcladas entre capas. |
| **DDD** | Establecer el glosario del proyecto en el README (lenguaje ubicuo): `Buyer`, `Seller`, `Libro`, `Orden`, `Perfil`. Este vocabulario debe usarse en todos los nombres de clases. |
| **TDD** | Configurar el plugin `maven-surefire-plugin` en `pom.xml` para que `mvn verify` ejecute los tests y falle el build si alguno no pasa. |
| **SOLID** | El `pom.xml` no debe incluir dependencias que acoplen capas (ej. importar librerías de UI en el módulo de dominio). |

---

## 📋 Tareas de Implementación

- [ ] 1. Crear el repositorio en GitHub con ramas `main` y `develop`.
- [ ] 2. Configurar la política de PR: revisión obligatoria de al menos 1 integrante.
- [ ] 3. Crear el archivo `.github/workflows/ci.yml` con el pipeline básico.
- [ ] 4. Crear el `.gitignore` para Java/Maven/IntelliJ.
- [ ] 5. Redactar el `README.md` con instrucciones completas de setup.
- [ ] 6. Verificar que el pipeline CI se activa y pasa en un PR de prueba.
- [ ] 7. Comunicar al equipo la convención de nombres de ramas (`feature/US-XXX`).

<div align="center">

# Mewtwo-Code — Microservicio de Estadísticas y Analítica (M12)

### *"Métricas en tiempo real para cada estudiante de PATRIC.IA"*

---

### Stack Tecnológico

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)

### Infraestructura & Calidad

![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.7-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

### Arquitectura

![Hexagonal](https://img.shields.io/badge/Architecture-Hexagonal-blueviolet?style=for-the-badge)
![Clean Architecture](https://img.shields.io/badge/Clean-Architecture-blue?style=for-the-badge)
![REST API](https://img.shields.io/badge/REST-API-009688?style=for-the-badge)

</div>

---

## Tabla de Contenidos

1. [Integrantes](#1-integrantes)
2. [Objetivo del Microservicio](#2-objetivo-del-microservicio)
3. [Funcionalidades Principales](#3-funcionalidades-principales)
4. [Estrategia de Versionamiento y Branches](#4-estrategia-de-versionamiento-y-branches)
    - [4.1 Convenciones para crear ramas](#41-convenciones-para-crear-ramas)
    - [4.2 Convenciones para crear commits](#42-convenciones-para-crear-commits)
5. [Tecnologías Utilizadas](#5-tecnologías-utilizadas)
6. [Funcionalidad](#6-funcionalidad)
7. [Diagramas](#7-diagramas)
8. [Manejo de Errores](#8-manejo-de-errores)
9. [Evidencia de Pruebas y Ejecución](#9-evidencia-de-pruebas-y-ejecución)
10. [Scaffolding](#10-scaffolding-del-microservicio)
11. [Ejecución del Proyecto](#11-ejecución-del-proyecto)
12. [CI/CD y Despliegue](#12-cicd-y-despliegue)
13. [Contribuciones](#13-contribuciones)

---

## 1. Integrantes

- Juan Esteban Rodriguez
- Fabian Andrade
- Diego Rozo
- Juan David Gomez
- Adrian Ducuara

---

## 2. Objetivo del Microservicio

El microservicio de **Estadísticas y Analítica** tiene por función principal proveer visibilidad cuantitativa sobre la actividad del campus de la plataforma PATRIC.IA. El módulo expone un dashboard personalizado para el estudiante con métricas de participación, nivel de actividad y actividad semanal. Si el estudiante no tiene métricas registradas, el sistema retorna un snapshot vacío con todos los días de la semana en cero y nivel de participación `NUEVO`. Este microservicio corre sobre el puerto `8084`, se encuentra integrado con Apache Kafka para ingesta de eventos en tiempo cuasi-real y PostgreSQL como base de datos principal.

---

## 3. Funcionalidades Principales

<div align="center">

<table>
  <thead>
    <tr>
      <th>Funcionalidad</th>
      <th>Descripción</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Dashboard del Estudiante</strong></td>
      <td>Retorna métricas personales del estudiante autenticado: parches asistidos, categoría favorita, actividad semanal por día y nivel de participación calculado (NUEVO, ACTIVO, CONECTOR, EMBAJADOR). Si no existen métricas, retorna un snapshot vacío.</td>
    </tr>
  </tbody>
</table>

</div>

---

## 4. Estrategia de Versionamiento y Branches

### Estrategia de Ramas (Git Flow)

#### `main`
- **Propósito:** Rama estable con la versión final lista para demo/producción.
- **Reglas:** Solo recibe merges desde `release/*` y `hotfix/*`. Cada merge crea un tag SemVer (`vX.Y.Z`). Rama protegida con PR obligatorio y checks de CI en verde.

#### `develop`
- **Propósito:** Integración continua de trabajo; base de nuevas funcionalidades.
- **Reglas:** Recibe merges desde `feature/*` y `release/*`. Rama protegida.

#### `feature/*`
- **Propósito:** Desarrollo de una funcionalidad, refactor o spike.
- **Base:** `develop`. Se fusiona a `develop` mediante PR.

---

### 4.1 Convenciones para crear ramas

```
feature/[nombre-funcionalidad]
```

Ejemplos:

- `feature/dashboard`
- `feature/panel-admin`
- `feature/exportacion-csv`

**Reglas:** Descripción clara, máximo 50 caracteres.

### 4.2 Convenciones para crear commits

```
[tipo]: [descripción específica de la acción]
```

**Tipos de commit:**
- `feat`: Nueva funcionalidad
- `fix`: Corrección de errores
- `docs`: Cambios en documentación

---

## 5. Tecnologías Utilizadas

| **Tecnología / Herramienta** | **Uso principal en el proyecto** |
|---|---|
| **Java 21 (OpenJDK)** | Lenguaje base del módulo con soporte para Spring Boot. |
| **Spring Boot 3.3.0** | Framework principal que agrupa JPA, Security y Swagger en un solo ecosistema. |
| **Spring Web** | Exposición del endpoint REST `GET /api/v1/analytics/dashboard`. |
| **Spring Security + JWT** | Protección del endpoint mediante tokens JWT como OAuth2 Resource Server. |
| **Spring Data JPA** | Acceso a PostgreSQL con mapeo objeto-relacional para `StudentDashboardMetricEntity`. |
| **PostgreSQL** | Base de datos relacional principal para métricas, snapshots y solicitudes de reportes. |
| **Apache Kafka 3.7.x** | Bus de eventos para ingesta de métricas en tiempo cuasi-real. |
| **Apache Maven** | Gestión de dependencias y automatización de builds en el pipeline CI/CD. |
| **Lombok** | Reducción de boilerplate con `@Getter`, `@Builder`, `@RequiredArgsConstructor`. |
| **H2** | Base de datos en memoria para pruebas unitarias sin PostgreSQL real. |
| **JUnit 5** | Framework de pruebas unitarias para validar servicios y lógica de dominio. |
| **Mockito** | Simulación de puertos y repositorios en pruebas unitarias. |
| **JaCoCo** | Medición de cobertura de pruebas integrada al pipeline. |
| **SpringDoc OpenAPI 2.5.0** | Generación automática de Swagger UI desde anotaciones `@Operation` en los controladores. |
| **Postman** | Validación manual de endpoints. |
| **Docker** | Contenedorización del microservicio para garantizar consistencia entre ambientes. |
| **GitHub Actions** | Pipeline de CI que compila, ejecuta tests y construye la imagen Docker en cada push. |

---

## 6. Funcionalidad

---

### Dashboard del Estudiante

Retorna las métricas personales del estudiante autenticado. Si el estudiante no tiene métricas registradas, el sistema construye un snapshot vacío con `patchesAttended = 0`, actividad semanal en cero para cada día y nivel de participación `NUEVO`.

**Endpoint principal:** `GET /api/v1/analytics/dashboard`

---

### Estructura de la Respuesta (Response)

<div align="center">

| Campo | Tipo | Descripción |
|---|---|---|
| userId | UUID | Identificador del estudiante. |
| patchesAttended | Integer | Total de parches a los que se ha unido. |
| topCategory | Enum | Categoría con mayor participación: STUDY, SPORTS, CULTURE, GAMING, FOOD, OTHER. |
| weeklyActivity | Map\<DayOfWeek, Integer\> | Actividad agrupada por día de la semana (MONDAY–SUNDAY). |
| participationLevel | Enum | Nivel calculado: NUEVO, ACTIVO, CONECTOR, EMBAJADOR. |
| computedAt | LocalDateTime | Timestamp del último cálculo. |

</div>

**Niveles de participación:**

<div align="center">

| Nivel | Condición |
|---|---|
| NUEVO | 0 – 2 parches asistidos |
| ACTIVO | 3 – 9 parches asistidos |
| CONECTOR | 10 – 19 parches asistidos |
| EMBAJADOR | 20 o más parches asistidos |

</div>

---

### Happy Path (Ejemplo de Uso Exitoso)

1. El estudiante autenticado accede al dashboard enviando su JWT.
2. El sistema consulta `student_dashboard_metrics` para obtener el snapshot más reciente.
3. Si no existe snapshot, se construye uno vacío con todos los días de la semana en cero.
4. Se calcula el `participationLevel` según los parches asistidos.
5. Se retorna `200 OK` con las métricas del estudiante.

**Request:**
```
GET /api/v1/analytics/dashboard
Authorization: Bearer <token>
```

**Response:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440001",
  "patchesAttended": 12,
  "topCategory": "STUDY",
  "weeklyActivity": {
    "MONDAY": 3,
    "TUESDAY": 0,
    "WEDNESDAY": 5,
    "THURSDAY": 1,
    "FRIDAY": 3,
    "SATURDAY": 0,
    "SUNDAY": 0
  },
  "participationLevel": "CONECTOR",
  "computedAt": "2026-05-08T10:45:00"
}
```

---

### Tipos de errores manejados

<div align="center">

| **Código HTTP** | **Escenario** | **Mensaje de Error** |
|:---:|---|---|
| ![401](https://img.shields.io/badge/401-Unauthorized-red?style=flat) | JWT inválido o ausente | `"JWT inválido o ausente"` |
| ![503](https://img.shields.io/badge/503-Service_Unavailable-critical?style=flat) | Falla en conexión con PostgreSQL | `"SERVICE_UNAVAILABLE"` |

</div>

---

## 7. Diagramas

### Diagrama de Componentes General de PATRIC.IA

<div align="center">
<img src="docs/ComponentesGeneral_PATRICIA.jpg" alt="Diagrama de Componentes General" width="700"/>
</div>

---

### Diagrama de Clases del Dominio

<div align="center">
<img src="docs/M12_Clases.jpg" alt="Diagrama de Clases" width="600"/>
</div>

**Resumen del diseño de dominio:**

- **`StudentDashboardMetric`** — entidad central con `userId`, `period`, `patchesAttended`, `topCategory`, `weeklyActivity` y `computedAt`. Incluye `isStale(): boolean` que verifica si el desfase supera 5 minutos, y `getParticipationLevel(): ParticipationLevel` que calcula el nivel según parches asistidos.
- **`AdminAnalyticsSnapshot`** — snapshot diario del sistema con `snapshotDate`, `totalPatches`, `activeUsers`, `topCategories`, `peakHours`, `retentionRate` y `generatedAt`.
- **`CategoryStat`** — clase de valor con `category`, `count` y `percentage`.
- **`ReportRequest`** — gestiona solicitudes de exportación CSV con `id`, `requestedBy`, `dateFrom`, `dateTo`, `filters`, `status` y `fileUrl`.
- **`ReportFilters`** — clase de valor inmutable con `category`, `campusZone`, `dateFrom` y `dateTo`.
- **`MetricEvent`** — DTO Kafka sin persistencia JPA con `eventId`, `sourceModule`, `eventType`, `payload` y `emittedAt`.

Enumeraciones: `ReportStatus` (PENDING, READY, FAILED), `MetricEventType` (JOIN, LEAVE, VIEW, CREATE, DELETE), `ParticipationLevel` (NUEVO, ACTIVO, CONECTOR, EMBAJADOR), `PatchCategory` (STUDY, SPORTS, CULTURE, GAMING, FOOD, OTHER), `CampusZone` (BIBLIOTECA, CAFETERIA, CANCHA, SALON, PARQUEADERO, EXTERNO).

---

### Diagrama de Entidad-Relación

<div align="center">
<img src="docs/M12_Entidad.jpg" alt="Diagrama Entidad-Relación" width="600"/>
</div>

#### Tabla: `student_dashboard_metrics`

<div align="center">

| Campo | Tipo | Descripción | Restricciones |
|---|---|---|---|
| **id** | `UUID` | Identificador único | PK |
| **user_id** | `UUID` | ID del estudiante | NOT NULL |
| **period** | `DATE` | Período de las métricas | NOT NULL |
| **patches_attended** | `INT` | Total de parches asistidos | NOT NULL |
| **top_category** | `ENUM` | Categoría con mayor participación | Opcional |
| **weekly_activity** | `JSON` | Actividad por día de semana | Opcional |
| **computed_at** | `DATETIME` | Timestamp del último cálculo | NOT NULL |

</div>

#### Tabla: `admin_analytics_snapshot`

<div align="center">

| Campo | Tipo | Descripción | Restricciones |
|---|---|---|---|
| **id** | `UUID` | Identificador único | PK |
| **snapshot_date** | `DATE` | Fecha del snapshot | NOT NULL, UNIQUE |
| **total_patches** | `INT` | Total de parches en el sistema | NOT NULL |
| **active_users** | `INT` | Usuarios activos | NOT NULL |
| **top_categories** | `JSON` | Categorías más populares | Opcional |
| **retention_rate** | `FLOAT` | Tasa de retención | NOT NULL |
| **generated_at** | `DATETIME` | Timestamp de generación | NOT NULL |

</div>

#### Tabla: `report_requests`

<div align="center">

| Campo | Tipo | Descripción | Restricciones |
|---|---|---|---|
| **id** | `UUID` | Identificador único | PK |
| **requested_by** | `UUID` | ID del solicitante | NOT NULL |
| **date_from** | `DATE` | Inicio del rango | NOT NULL |
| **date_to** | `DATE` | Fin del rango | NOT NULL |
| **filters** | `JSON` | Filtros aplicados | Opcional |
| **status** | `ENUM` | PENDING, READY, FAILED | NOT NULL |
| **file_url** | `VARCHAR` | URL del CSV generado | Opcional |
| **created_at** | `DATETIME` | Fecha de creación | NOT NULL |

</div>

---

## 8. Manejo de Errores

El microservicio implementa un `GlobalExceptionHandler` con `@RestControllerAdvice` que centraliza todas las excepciones y retorna siempre el mismo formato JSON estandarizado:

```json
{
  "error": "TIPO_ERROR",
  "message": "descripción legible del problema",
  "status": "4xx"
}
```

### Excepciones de dominio manejadas

<div align="center">

| **Excepción** | **HTTP** | **Error Code** | **Escenario** |
|---|:---:|---|---|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` | Validación de campos fallida |
| `MetricNotFoundException` | 404 | `METRIC_NOT_FOUND` | No se encontraron métricas para el usuario |
| `ReportNotFoundException` | 404 | `REPORT_NOT_FOUND` | El reporte solicitado no existe |
| `InvalidReportFiltersException` | 422 | `BUSINESS_RULE_VIOLATION` | dateFrom posterior a dateTo |
| `AccessDeniedException` | 403 | `FORBIDDEN` | Usuario sin rol ADMIN intenta acceder al panel de administrador |
| `ServiceUnavailableException` | 503 | `SERVICE_UNAVAILABLE` | Falla en PostgreSQL o Kafka consumer |
| `Exception` | 500 | `INTERNAL_ERROR` | Error inesperado del servidor |

</div>

---

## 9. Evidencia de Pruebas y Ejecución

### Tipos de pruebas implementadas

<div align="center">

| **Tipo de Prueba** | **Descripción** | **Herramientas** |
|---|---|---|
| **Pruebas Unitarias** | Validan `DashboardService` con mocks del repositorio. Cubren retorno de métrica existente, snapshot vacío, cálculo de nivel de participación e `isStale()`. | JUnit 5, Mockito |
| **Cobertura de Código** | JaCoCo genera reporte HTML con métricas de cobertura por clase y método. | JaCoCo |

</div>

### Cómo ejecutar las pruebas

```bash
# Pruebas unitarias
./mvnw test

# Todas las pruebas + reporte JaCoCo
./mvnw verify

# Reporte de cobertura JaCoCo
./mvnw clean test jacoco:report
# Reporte en: target/site/jacoco/index.html

# Prueba específica
./mvnw test -Dtest=DashboardServiceTest
```

### Clases de prueba implementadas

```
src/test/java/edu/eci/patriciaM12/
└── DashboardServiceTest.java
    ├── retornaMetricaExistenteCuandoRepositorioLaEncuentra
    ├── retornaSnapshotVacioCuandoNoExistenMetricas
    ├── snapshotVacioTieneTodosLosDiasDeLaSemanaEnCero
    ├── retornaNivelNuevoCuandoTieneCeroParches
    ├── retornaNivelActivoCuandoTieneTresParches
    ├── retornaNivelConectorCuandoTieneDiezParches
    ├── retornaNivelEmbajadorCuandoTieneVeinteParches
    ├── isStaleRetornaFalseCuandoComputedAtEsReciente
    └── isStaleRetornaTrueCuandoDesfaseSuperaCincoMinutos
```

### Criterios de aceptación de pruebas

- Todas las pruebas en estado PASSED
- Cero errores de compilación
- Casos felices y de error implementados por caso de uso
- Ports mockeados correctamente sin acceso a infraestructura real

---

## 10. Scaffolding del Microservicio

```
mewtwocode-statistics-analytics/
│
├── src/
│   ├── main/
│   │   ├── java/edu/eci/patriciaM12/
│   │   │   │
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   │   └── response/
│   │   │   │   │       └── StudentDashboardResponse.java
│   │   │   │   └── service/
│   │   │   │       └── DashboardService.java
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── exceptions/
│   │   │   │   │   ├── InvalidReportFiltersException.java
│   │   │   │   │   ├── MetricNotFoundException.java
│   │   │   │   │   └── ReportNotFoundException.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── AdminAnalyticsSnapshot.java
│   │   │   │   │   ├── CategoryStat.java
│   │   │   │   │   ├── MetricEvent.java
│   │   │   │   │   ├── ReportFilters.java
│   │   │   │   │   ├── ReportRequest.java
│   │   │   │   │   ├── StudentDashboardMetric.java
│   │   │   │   │   └── enums/
│   │   │   │   │       ├── CampusZone.java
│   │   │   │   │       ├── MetricEventType.java
│   │   │   │   │       ├── ParticipationLevel.java
│   │   │   │   │       ├── PatchCategory.java
│   │   │   │   │       └── ReportStatus.java
│   │   │   │   └── ports/
│   │   │   │       ├── in/
│   │   │   │       │   ├── GetAdminAnalyticsUseCase.java
│   │   │   │       │   ├── GetStudentDashboardUseCase.java
│   │   │   │       │   ├── ProcessMetricEventUseCase.java
│   │   │   │       │   └── RequestReportUseCase.java
│   │   │   │       └── out/
│   │   │   │           ├── AdminSnapshotRepositoryPort.java
│   │   │   │           ├── ReportRequestRepositoryPort.java
│   │   │   │           └── StudentMetricsRepositoryPort.java
│   │   │   │
│   │   │   ├── entrypoints/
│   │   │   │   └── rest/controller/
│   │   │   │       └── DashboardController.java        (GET /api/v1/analytics/dashboard)
│   │   │   │
│   │   │   ├── infrastructure/
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── adapter/
│   │   │   │   │   │   └── StudentMetricsRepositoryAdapter.java
│   │   │   │   │   └── persistence/
│   │   │   │   │       ├── entity/
│   │   │   │   │       │   ├── AdminAnalyticsSnapshotEntity.java
│   │   │   │   │       │   ├── ReportRequestEntity.java
│   │   │   │   │       │   └── StudentDashboardMetricEntity.java
│   │   │   │   │       ├── mapper/
│   │   │   │   │       │   └── StudentMetricsMapper.java
│   │   │   │   │       └── repository/
│   │   │   │   │           └── StudentMetricsJpaRepository.java
│   │   │   │   └── config/
│   │   │   │       ├── SecurityConfig.java
│   │   │   │       └── SwaggerConfig.java
│   │   │   │
│   │   │   └── PatriciaM12Application.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/edu/eci/patriciaM12/
│           └── DashboardServiceTest.java
│
├── docs/
│   ├── ComponentesGeneral_PATRICIA.jpg
│   ├── M12_Clases.jpg
│   └── M12_Entidad.jpg
│
├── .github/workflows/
│   └── ci.yml
│
├── .dockerignore
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

### Arquitectura Hexagonal Implementada

<div align="center">

| **Capa** | **Responsabilidad** | **Dependencias** |
|---|---|---|
| **Domain** | Modelos (`StudentDashboardMetric`, `AdminAnalyticsSnapshot`, `ReportRequest`, `CategoryStat`, `MetricEvent`), enums, excepciones y puertos | Ninguna (independiente) |
| **Application** | `DashboardService` — implementa `GetStudentDashboardUseCase` | Solo `Domain` |
| **Entrypoints** | `DashboardController` — `GET /api/v1/analytics/dashboard` | `Domain` + `Application` |
| **Infrastructure** | `StudentMetricsRepositoryAdapter`, entidades JPA, `StudentMetricsMapper`, `StudentMetricsJpaRepository`, `SecurityConfig`, `SwaggerConfig` | `Domain` + `Application` |

</div>

**Flujo de dependencias:** `Entrypoints / Infrastructure → Application → Domain`

---

## 11. Ejecución del Proyecto

### Prerrequisitos

- Java 21
- Maven 3.9+
- Docker & Docker Compose

### Opción 1: Ejecución Local (Maven)

```bash
# 1. Clonar repositorio
git clone https://github.com/<org>/mewtwocode-statistics-analytics.git

# 2. Levantar base de datos y Kafka
docker compose up -d

# 3. Ejecutar la aplicación
./mvnw spring-boot:run
```

**URL Local:** `http://localhost:8084`
**Swagger UI:** `http://localhost:8084/swagger-ui.html`
**OpenAPI Docs:** `http://localhost:8084/v3/api-docs`

### Opción 2: Ejecución con Docker Compose

```bash
docker compose up --build
```

### Variables de Entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/m12_analytics` | URL de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `patricia` | Usuario de PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | `patricia` | Contraseña de PostgreSQL |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Broker de Kafka |
| `PORT` | `8084` | Puerto del servidor |

---

## 12. CI/CD y Despliegue

### Pipeline de Automatización (GitHub Actions)

El flujo en `.github/workflows/ci.yml` se ejecuta en cada push a `main`, `develop` o `feature/**` y en cada PR a `main` o `develop`:

1. **Checkout** — Clona el repositorio con `actions/checkout@v4`.
2. **Java 21** — Configura el JDK con `actions/setup-java@v4` (distribución Temurin).
3. **Cache Maven** — Restaura dependencias cacheadas para acelerar el build.
4. **Dar permisos** — `chmod +x mvnw`.
5. **Compilar** — `./mvnw compile -q` verifica que el código sea válido.
6. **Tests y JaCoCo** — `./mvnw verify` ejecuta pruebas y genera reporte de cobertura.
7. **Publicar reporte** — Sube el reporte JaCoCo como artifact de la ejecución.
8. **Docker Build** — Construye la imagen `m12-statistics-analytics:{sha}`.

### Infraestructura

<div align="center">

| **Componente** | **Descripción** |
|---|---|
| PostgreSQL | Base de datos relacional — tablas `student_dashboard_metrics`, `admin_analytics_snapshot`, `report_requests` |
| Apache Kafka | Bus de eventos para ingesta de métricas en tiempo cuasi-real |
| GitHub Actions | Pipeline CI de compilación, pruebas y Docker build |
| Swagger UI | Documentación interactiva en `/swagger-ui.html` |

</div>


---

## 13. Contribuciones y Metodología

El equipo **Mewtwo-Code** aplicó la metodología **Scrum** con sprints semanales para garantizar una entrega incremental y mejora continua.

### Equipo Scrum

| Rol | Responsabilidad |
|---|---|
| **Product Owner** | Priorización del Backlog y maximización de valor. |
| **Scrum Master** | Facilitador del proceso y eliminación de impedimentos. |
| **Developers** | Diseño, implementación y pruebas de funcionalidades. |

### Eventos y Artefactos

- **Sprints Semanales**: Ciclos cortos de desarrollo.
- **Daily Scrum**: Sincronización diaria (15 min).
- **Sprint Review & Retrospective**: Demostración de incrementos y mejora de procesos.

---

<div align="center">

### Equipo **Mewtwo-Code**

![Team](https://img.shields.io/badge/Team-Mewtwo--Code-blueviolet?style=for-the-badge&logo=github&logoColor=white)
![Module](https://img.shields.io/badge/Module-M12_Estad%C3%ADsticas_%26_Anal%C3%ADtica-orange?style=for-the-badge)
![Course](https://img.shields.io/badge/Course-DOSW-orange?style=for-the-badge)
![Year](https://img.shields.io/badge/Year-2026--1-blue?style=for-the-badge)

> **PATRIC.IA Statistics & Analytics Service** es el punto central de visibilidad de métricas del campus, diseñado para retornar dashboards personalizados con niveles de participación calculados en tiempo real.

**Escuela Colombiana de Ingeniería Julio Garavito**

</div>
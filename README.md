<div align="center">

# Mewtwo-Code — Microservicio de Estadísticas y Analítica

### *"Métricas en tiempo real para cada estudiante de PATRIC.IA"*

---

### Stack Tecnológico

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white)

### Infraestructura & Calidad

![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

### Arquitectura

![Hexagonal](https://img.shields.io/badge/Architecture-Hexagonal-blueviolet?style=for-the-badge)
![Adapter Pattern](https://img.shields.io/badge/Pattern-Adapter-blue?style=for-the-badge)
![REST API](https://img.shields.io/badge/REST-API-009688?style=for-the-badge)

</div>

---

## Tabla de Contenidos

1. [Nombre del Microservicio](#1-nombre-del-microservicio)
2. [Integrantes](#2-integrantes)
3. [Tecnologías Utilizadas](#3-tecnologías-utilizadas)
4. [Descripción del Módulo](#4-descripción-del-módulo)
5. [Cómo Funciona el Módulo](#5-cómo-funciona-el-módulo)
6. [Diagramas de Datos](#6-diagramas-de-datos)
7. [Diagramas de Clases](#7-diagramas-de-clases)
8. [Diagrama de Componentes](#8-diagrama-de-componentes)
9. [Funcionalidades y Endpoints](#9-funcionalidades-y-endpoints)
10. [Colas de Mensajería](#10-colas-de-mensajería)
11. [Evidencia de Pruebas Unitarias](#11-evidencia-de-pruebas-unitarias)
12. [Análisis de Cobertura](#12-análisis-de-cobertura)
13. [Cómo Ejecutar el Proyecto](#13-cómo-ejecutar-el-proyecto)
14. [Evidencia del Despliegue CI/CD](#14-evidencia-del-despliegue-cicd)
15. [Scaffolding y Código Documentado](#15-scaffolding-y-código-documentado)
16. [Pipeline de Desarrollo](#16-pipeline-de-desarrollo)
17. [Pipeline de PROD](#17-pipeline-de-prod)

---

## 1. Nombre del Microservicio

**m12-statistics-analytics**
Puerto: `8084` · Base de datos: `m12_analytics` · Paquete base: `edu.eci.patriciaM12`

---

## 2. Integrantes

- Juan Esteban Rodríguez
- Diego Alejandro Rozo
- Cristian Adrián Ducuara
- Juan David Gómez
- Diego Fabian Andrade

---

## 3. Tecnologías Utilizadas

| **Tecnología**                           | **Versión** | **Uso en el proyecto**                                                |
|------------------------------------------|:-----------:|-----------------------------------------------------------------------|
| Java                                     |     21      | Lenguaje principal. Usado en build stage y runtime del contenedor Docker. |
| Spring Boot                              |    3.3.0    | Framework principal. Orquesta web, seguridad, persistencia y mensajería. |
| Spring Web                               |      —      | Exposición de controladores REST del módulo.                          |
| Spring Security + OAuth2 Resource Server |      —      | Validación de JWT emitidos por el módulo de identidad.                |
| Spring Data JPA                          |      —      | Acceso a PostgreSQL para las  entidades del módulo.                   |
| Spring Kafka                             |      —      | Consumer del topic `metric_events`. Consumer group: `m12-analytics-group`. |
| PostgreSQL                               |     16      | Base de datos propia del módulo                                       |
| Apache Kafka                             |    7.6.0    | Bus de eventos. Topic: `metric_events`. Puerto: `9092`.               |
| OpenCSV                                  |     5.9     | Generación de archivos CSV                                            |
| Lombok                                   |      —      | Reducción de boilerplate                                              |
| Jackson Datatype                         |      —      | Serialización de fechas y dias de la semana  en JSON.                 |
| SpringDoc OpenAPI                        |    2.5.0    | Swagger UI automático                                                 |
| H2                                       |      —      | BD en memoria para perfil. Modo PostgreSQL para compatibilidad.       |
| JUnit 5                                  |      —      | Framework de pruebas unitarias.                                       |
| Mockito                                  |      —      | Simulación de puertos en pruebas unitarias.                           |
| AssertJ                                  |      —      | Aserciones fluidas en pruebas unitarias.                              |
| JaCoCo                                   |   0.8.12    | Cobertura de código integrada al pipeline CI.                         |
| Maven                                    |   3.9.14    | Gestión de dependencias y ciclo de vida del build.                    |
| Docker                                   |      -      | Contenedor independiente del servicio                                 
| Docker Compose                           |      —      | Orquestación local                                                    |
| Sonar                                    |      —      | Annalisis estático de codigos                                         |
| GitHub Actions                           |      —      | Pipeline CI disparado en push a `main`, `develop` y `feature/**`.     |

---

## 4. Descripción del Módulo

El módulo de **Estadísticas y Analítica** provee visibilidad cuantitativa sobre la actividad del campus dentro del sistema PATRIC.IA. Opera como microservicio completamente independiente con su propia base de datos, su propio pipeline de despliegue y su propio ciclo de vida. Un fallo en este módulo nunca interrumpe el núcleo del sistema ni los demás módulos.

<div align="center">

| Campo | Descripción |
|---|---|
| **Nombre** | Estadísticas y Analítica |
| **Sistema** | PATRIC.IA — EciBuddy |
| **Equipo** | Mewtwo Code |
| **Puerto** | `8084` |
| **Base de datos** | `m12_analytics` (PostgreSQL 16) |
| **Actores** | Estudiante autenticado · Administrador del sistema |

</div>

**Funcionalidades cubiertas:**

<div align="center">

| Funcionalidad | Actor | Descripción |
|---|---|---|
| **Dashboard del Estudiante** | Estudiante autenticado | Métricas personales: parches asistidos, categoría favorita, actividad semanal por día y nivel de participación calculado en dominio. Si no hay datos retorna snapshot vacío con todos los días en 0. |
| **Panel de Analítica** | Administrador | Métricas globales del sistema filtradas por rango de fechas y tipo de métrica. Semestre activo calculado automáticamente como rango por defecto. |
| **Reportes CSV** | Administrador / Estudiante | Generación asíncrona de reportes CSV filtrados por fechas, categoría y zona. Ciclo de vida: `PENDING → READY / FAILED`. Verificación de ownership en descarga. |

</div>

---

## 5. Cómo Funciona el Módulo

### Qué otros módulos lo usan

El módulo es consumido directamente por el **frontend de PATRIC.IA** a través de sus tres endpoints REST. Los demás módulos del sistema (Parches, Matching, Identidad) no llaman a este módulo directamente, en cambio, **publican eventos en el topic Kafka `metric_events`**, que el módulo consume para construir sus propias proyecciones de datos. El **Módulo de Identidad** emite los JWT que este módulo valida como OAuth2 Resource Server contra el issuer `http://localhost:8080`.

### Qué patrones de diseño utiliza

El módulo implementa el **patrón Adapter (Estructural)** como patrón de diseño principal. El dominio define puertos de salida como interfaces puras (`StudentMetricsRepositoryPort`, `AdminSnapshotRepositoryPort`, `ReportRequestRepositoryPort`, `CsvGeneratorPort`). Los adaptadores de infraestructura implementan estos puertos traduciendo entre objetos de dominio y tecnologías concretas (JPA, OpenCSV), sin que el dominio sepa nada de ellas.


### Estilo de arquitectura detallado

El módulo sigue una **arquitectura hexagonal** organizada en cuatro capas con dependencias estrictamente unidireccionales:

```
Entrypoints / Infrastructure → Application → Domain
```

<div align="center">

| Capa | Responsabilidad | Dependencias |
|---|---|---|
| **Entrypoints** | Controladores REST + SecurityFilter. Traduce HTTP a llamadas de dominio. Extrae `userId` del JWT. | Application |
| **Application** | Servicios que implementan los casos de uso. Lógica de coordinación: semestre activo, validación de rangos, generación `@Async`. | Domain |
| **Domain** | Modelos, enumeraciones, excepciones y puertos (in/out). Calcula `participationLevel` directamente en el modelo. | Ninguna |
| **Infrastructure** | Adaptadores JPA, `CsvGeneratorAdapter`, mappers, `SecurityConfig`, `SwaggerConfig`. | Domain |

</div>

La generación de CSV ocurre en un **hilo separado** con `@Async` en `ReportService.generateAsync()`, permitiendo retornar `202 Accepted` inmediatamente sin bloquear el hilo del request HTTP. Si la generación falla, el estado del reporte se persiste como `FAILED` — el error no propaga al cliente porque el `202` ya fue enviado.

---

## 6. Diagramas de Datos


<div align="center">
<img src="docs/M12_Entidad.jpg" alt="Diagrama Entidad-Relación" width="700"/>
</div>

El módulo persiste tres tablas independientes en `m12_analytics`. No existen claves foráneas entre ellas ni hacia bases de datos externas. Las referencias a entidades de otros módulos (como `userId`) se almacenan como `UUID` sin constraint de integridad referencial, ya que el módulo de identidad tiene su propia base de datos.

### Tabla: `student_dashboard_metrics`

<div align="center">

| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK | Identificador único del registro |
| `user_id` | `UUID` | NOT NULL | Referencia externa al estudiante (sin FK) |
| `period` | `DATE` | NOT NULL | Período al que corresponde la métrica |
| `patches_attended` | `INTEGER` | NOT NULL | Total de parches asistidos en el período |
| `top_category` | `VARCHAR` | NULLABLE | Categoría de parche con mayor participación |
| `weekly_activity` | `JSONB` | NULLABLE | Actividad por día de la semana (MON–SUN → Integer) |
| `computed_at` | `TIMESTAMP` | NOT NULL | Momento de cálculo de la métrica |

</div>

### Tabla: `admin_analytics_snapshot`

<div align="center">

| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK | Identificador único del snapshot |
| `snapshot_date` | `DATE` | NOT NULL, UNIQUE | Fecha del snapshot (un registro por día) |
| `total_patches` | `INTEGER` | NOT NULL | Total de parches realizados en esa fecha |
| `active_users` | `INTEGER` | NOT NULL | Usuarios activos en esa fecha |
| `top_categories` | `TEXT` | NULLABLE | Lista de categorías con conteos (JSON serializado) |
| `retention_rate` | `FLOAT` | NOT NULL | Tasa de retención de usuarios (0.0 – 1.0) |
| `generated_at` | `TIMESTAMP` | NOT NULL | Momento de generación del snapshot |

</div>

### Tabla: `report_requests`

<div align="center">

| Campo | Tipo | Restricción | Descripción |
|---|---|---|---|
| `id` | `UUID` | PK | Identificador único de la solicitud |
| `requested_by` | `UUID` | NOT NULL | Usuario que solicitó el reporte (ownership) |
| `date_from` | `DATE` | NOT NULL | Inicio del rango de fechas del reporte |
| `date_to` | `DATE` | NOT NULL | Fin del rango de fechas del reporte |
| `filters` | `TEXT` | NULLABLE | Filtros aplicados (JSON: categoría, zona, includeAdmin) |
| `status` | `VARCHAR` | NOT NULL | Estado actual: `PENDING` / `READY` / `FAILED` |
| `file_url` | `VARCHAR` | NULLABLE | Ruta del CSV generado. `null` mientras es `PENDING` |
| `created_at` | `TIMESTAMP` | NOT NULL | Momento de creación de la solicitud |

</div>

---

## 7. Diagramas de Clases


<div align="center">
<img src="docs/M12_Clases.jpg" alt="Diagrama de Clases" width="700"/>
</div>

### Patrón de diseño: Adapter (Estructural)

El módulo aplica el patrón **Adapter** para resolver la incompatibilidad entre lo que el dominio espera (objetos de dominio con métodos semánticamente nombrados) y lo que la infraestructura ofrece (entidades JPA, APIs de Spring Data, librerías externas). Los adaptadores implementan las interfaces de puerto del dominio y por dentro delegan en la tecnología concreta correspondiente, sin que el dominio ni los servicios de aplicación conozcan esa tecnología.

<div align="center">

| Rol  | Clase en el módulo |
|---|---|
| **Target** (interfaz esperada) | `StudentMetricsRepositoryPort`, `AdminSnapshotRepositoryPort`, `ReportRequestRepositoryPort`, `CsvGeneratorPort` |
| **Adapter** | `StudentMetricsRepositoryAdapter`, `AdminSnapshotRepositoryAdapter`, `ReportRequestRepositoryAdapter`, `CsvGeneratorAdapter` |
| **Adaptee** (interfaz incompatible) | `StudentMetricsJpaRepository`, `AdminSnapshotJpaRepository`, `ReportRequestJpaRepository`, `CSVWriter` (OpenCSV) |
| **Client** | `DashboardService`, `AdminAnalyticsService`, `ReportService` |

</div>

**Modelos del dominio:**

- **`StudentDashboardMetric`** — calcula `getParticipationLevel()` e `isStale()` directamente en el modelo.
- **`AdminAnalyticsSnapshot`** — snapshot diario con restricción `UNIQUE` en `snapshotDate`.
- **`ReportRequest`** — ciclo de vida `PENDING → READY / FAILED`. Verifica ownership con `requestedBy`.
- **`ReportFilters`** — clase de valor inmutable con los filtros del reporte.
- **`CategoryStat`** — clase de valor con `category`, `count` y `percentage`.
- **`MetricEvent`** — DTO Kafka sin persistencia JPA.

Enumeraciones: `ReportStatus` · `ParticipationLevel` · `PatchCategory` · `CampusZone` · `MetricType` · `MetricEventType`

---

## 8. Diagrama de Componentes

> **Insertar aquí:** imagen del diagrama de componentes (`docs/diagrama_componentes_m12.png`)  
> El archivo editable está en `docs/diagrama_componentes_m12.drawio` — abrir en [draw.io](https://app.diagrams.net) con **File → Open from → Device**.

<div align="center">
<img src="docs/diagrama_componentes_m12.png" alt="Diagrama de Componentes" width="900"/>
</div>

El diagrama muestra la arquitectura hexagonal completa con los cuatro grupos de componentes del módulo y sus conexiones con los sistemas externos:

- **Entrypoints:** `DashboardController`, `AdminAnalyticsController`, `ReportController`, `SecurityFilter`
- **Aplicación:** `DashboardService`, `AdminAnalyticsService`, `ReportService` (con hilo `@Async`)
- **Dominio:** puertos de entrada y salida, modelos, enumeraciones, excepciones
- **Infraestructura:** `StudentMetricsRepositoryAdapter`, `AdminSnapshotRepositoryAdapter`, `ReportRequestRepositoryAdapter`, `CsvGeneratorAdapter` + mappers JPA
- **Sistemas externos:** PostgreSQL 16, Apache Kafka 7.6 (KRaft), Filesystem `/tmp/reports`, Spring Security OAuth2, Módulo de Identidad, Frontend

---

## 9. Funcionalidades y Endpoints

### Resumen de Endpoints

<div align="center">

| **Método** | **Endpoint** | **Descripción** | **Rol requerido** |
|:---:|---|---|:---:|
| `GET` | `/api/v1/analytics/dashboard` | Dashboard personal del estudiante autenticado | JWT válido |
| `GET` | `/api/v1/analytics/admin` | Panel de analítica global del sistema | `ADMINISTRADOR` |
| `POST` | `/api/analytics/reports` | Solicitar generación asíncrona de reporte CSV | JWT válido |
| `GET` | `/api/analytics/reports/{id}/download` | Consultar estado y descargar reporte CSV | JWT válido (owner) |

</div>

---

### Endpoint 1 — Dashboard del Estudiante

**Request:**
```
GET /api/v1/analytics/dashboard
Authorization: Bearer <token>
```

El `userId` se extrae del JWT — el cliente nunca lo envía directamente.

**Response — 200 OK:**
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

<div align="center">

| Campo | Tipo | Descripción |
|---|---|---|
| `userId` | `UUID` | Identificador del estudiante |
| `patchesAttended` | `int` | Total de parches asistidos |
| `topCategory` | `PatchCategory` | Categoría con mayor participación. `null` si no hay datos |
| `weeklyActivity` | `Map<DayOfWeek, Integer>` | Actividad por día. Todos en `0` si no hay datos |
| `participationLevel` | `ParticipationLevel` | Nivel calculado en dominio |
| `computedAt` | `LocalDateTime` | Momento del cálculo |

</div>

---

### Endpoint 2 — Panel de Analítica para Administrador

**Request:**
```
GET /api/v1/analytics/admin?startDate=2025-02-01&endDate=2025-06-30&metricType=USERS
Authorization: Bearer <token>  (role: ADMINISTRADOR)
```

<div align="center">

| Query Param | Tipo | Obligatorio | Descripción |
|---|---|:---:|---|
| `startDate` | `yyyy-MM-dd` | No | Inicio del rango. Por defecto: inicio del semestre activo |
| `endDate` | `yyyy-MM-dd` | No | Fin del rango. Por defecto: fin del semestre activo |
| `metricType` | `MetricType` | No | `USERS` / `PARCHES` / `EVENTS` / `MATCHES`. Sin valor retorna todas |

</div>

**Response — 200 OK:**
```json
{
  "activeUsers": {
    "timeSeries": { "2025-04-01": 45, "2025-04-02": 52 },
    "total": 97
  },
  "parcheStats": {
    "timeSeries": { "2025-04-01": 18, "2025-04-02": 24 },
    "total": 42,
    "categories": [
      { "category": "STUDY", "count": 20 },
      { "category": "SPORTS", "count": 12 }
    ]
  },
  "matchSuccessRate": 0.78
}
```

Los campos no solicitados son `null` gracias a `@JsonInclude(NON_NULL)`.

---

### Endpoint 3 — Solicitar Reporte CSV

**Request:**
```
POST /api/analytics/reports
Authorization: Bearer <token>
Content-Type: application/json
```
```json
{
  "dateFrom": "2025-04-01",
  "dateTo": "2025-06-30",
  "category": "STUDY",
  "campusZone": "BIBLIOTECA",
  "includeAdmin": false
}
```

<div align="center">

| Campo | Tipo | Obligatorio | Descripción |
|---|---|:---:|---|
| `dateFrom` | `yyyy-MM-dd` | Sí | Inicio del rango |
| `dateTo` | `yyyy-MM-dd` | Sí | Fin del rango. Debe ser posterior a `dateFrom` |
| `category` | `PatchCategory` | No | `STUDY` / `SPORTS` / `CULTURE` / `GAMING` / `FOOD` / `OTHER` |
| `campusZone` | `CampusZone` | No | `BIBLIOTECA` / `CAFETERIA` / `CANCHA` / `SALON` / `PARQUEADERO` / `EXTERNO` |
| `includeAdmin` | `boolean` | No | Incluye actividad administrativa. Por defecto `false` |

</div>

**Response — 202 Accepted:**
```json
{
  "id": "b7e2a1f0-...",
  "status": "PENDING",
  "fileUrl": null
}
```

---

### Endpoint 4 — Consultar y Descargar Reporte

**Request:**
```
GET /api/analytics/reports/{id}/download
Authorization: Bearer <token>
```

<div align="center">

| Código | Condición | Body |
|:---:|---|---|
| `200 OK` | `status = READY` | URL del archivo CSV generado |
| `202 Accepted` | `status = PENDING` | `"The report is still being generated. Try again in a few moments."` |
| `500 Internal Server Error` | `status = FAILED` | `"Report generation failed. Try requesting it again."` |
| `404 Not Found` | ID no existe o no pertenece al usuario | `"Reporte no encontrado: {id}"` |

</div>

---

## 10. Colas de Mensajería

El módulo utiliza **Apache Kafka 7.6** en modo **KRaft** como bus de eventos para la ingesta de métricas en tiempo cuasi-real.

### Tópico consumido

<div align="center">

| Parámetro | Valor |
|---|---|
| **Tópico** | `metric_events` |
| **Consumer group** | `m12-analytics-group` |
| **Auto offset reset** | `earliest` |
| **Key deserializer** | `StringDeserializer` |
| **Value deserializer** | `StringDeserializer` |
| **Bootstrap server (dev)** | `localhost:9092` |
| **Bootstrap server (docker)** | `kafka:9092` |

</div>

### Estructura del evento consumido

Los eventos publicados por los demás módulos de PATRIC.IA en el topic `metric_events` son procesados por el `ProcessMetricEventUseCase`:

<div align="center">

| Campo | Tipo | Descripción |
|---|---|---|
| `eventId` | `UUID` | Identificador único del evento |
| `sourceModule` | `String` | Módulo que originó el evento (ej. `Parches`, `Matching`) |
| `eventType` | `MetricEventType` | Tipo de acción: `JOIN` / `LEAVE` / `VIEW` / `CREATE` / `DELETE` |
| `payload` | `String` | JSON con el contexto: userId, patchId, zona, categoría |
| `emittedAt` | `LocalDateTime` | Timestamp de emisión del evento |

</div>

El módulo **no publica** eventos — es exclusivamente consumidor. La retención de mensajes en Kafka garantiza que si el módulo cae, los eventos se procesan al recuperarse gracias al `auto-offset-reset=earliest`.

---

## 11. Evidencia de Pruebas Unitarias

> **Insertar aquí:** screenshot del output de `./mvnw test` con todos los tests en verde.

El proyecto cuenta con **tres clases de prueba** que cubren los tres servicios principales:

### `DashboardServiceTest` — 9 casos

```
✔ retornaMetricaExistenteCuandoRepositorioLaEncuentra
✔ retornaSnapshotVacioCuandoNoExistenMetricas
✔ snapshotVacioTieneTodosLosDiasDeLaSemanaEnCero
✔ retornaNivelNuevoCuandoTieneCeroParches
✔ retornaNivelActivoCuandoTieneTresParches
✔ retornaNivelConectorCuandoTieneDiezParches
✔ retornaNivelEmbajadorCuandoTieneVeinteParches
✔ isStaleRetornaFalseCuandoComputedAtEsReciente
✔ isStaleRetornaTrueCuandoDesfaseSuperaCincoMinutos
```

### `AdminAnalyticsServiceTest` — 3 casos

```
✔ retornaTodasLasMetricasCuandoMetricTypeEsNull
✔ retornaSoloUsuariosCuandoMetricTypeEsUsers
✔ rechazaEndDateCuandoNoEsPosteriorAStartDate
```

### `ReportServiceTest` — 6 casos

```
✔ createReport_conFiltrosValidos_retornaPending
✔ createReport_cuandoDateFromEsPosteriorADateTo_lanzaInvalidReportFiltersException
✔ findById_cuandoExisteYEsDelUsuario_retornaReporte
✔ findById_cuandoNoExiste_lanzaReportNotFoundException
✔ findById_cuandoPerteneceAOtroUsuario_lanzaReportNotFoundException
✔ reportConVolumenGrande_sinDatosSensibles_generaCSVCorrectamente
```

**Criterios de aceptación:**
- Todos los tests en estado `PASSED`
- Puertos mockeados con Mockito — sin acceso a infraestructura real
- Casos felices y de error cubiertos por cada caso de uso
- Aserciones fluidas con AssertJ

**Cómo ejecutar:**
```bash
./mvnw test
```

---

## 12. Análisis de Cobertura

> **Insertar aquí:** screenshot del reporte JaCoCo (`target/site/jacoco/index.html`).

**Cómo generar el reporte:**
```bash
# Genera el reporte HTML de cobertura
./mvnw clean test jacoco:report
# Reporte disponible en: target/site/jacoco/index.html

# O con verify (incluye todos los checks del pipeline)
./mvnw verify
```

El reporte JaCoCo se genera automáticamente en cada ejecución del pipeline CI y se publica como artifact en GitHub Actions bajo el nombre `jacoco-report`.

---

## 13. Cómo Ejecutar el Proyecto

### Prerrequisitos

- Java 21
- Maven 3.9+
- Docker & Docker Compose

### Opción 1 — Ejecución local con perfil `dev`

El perfil `dev` usa H2 en memoria y no requiere Docker para la base de datos ni Kafka.

```bash
# 1. Clonar el repositorio
git clone https://github.com/<org>/mewtwocode-statistics-analytics.git
cd mewtwocode-statistics-analytics

# 2. Ejecutar con perfil dev
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

- **URL:** `http://localhost:8084`
- **Swagger UI:** `http://localhost:8084/swagger-ui.html`
- **H2 Console:** `http://localhost:8084/h2-console`

### Opción 2 — Ejecución con Docker Compose (perfil `docker`)

```bash
# Levanta backend + PostgreSQL 16 + Kafka KRaft
docker compose up --build
```

Los servicios se levantan con healthchecks: el backend espera a que PostgreSQL y Kafka estén saludables antes de arrancar (`depends_on: condition: service_healthy`).

### Variables de entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Perfil activo: `dev` o `docker` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/m12_analytics` | URL de PostgreSQL (perfil docker) |
| `SPRING_DATASOURCE_USERNAME` | `patricia` | Usuario de PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | `patricia` | Contraseña de PostgreSQL |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` | Broker de Kafka (perfil docker) |
| `SERVER_PORT` | `8084` | Puerto del servidor |

### Pruebas

```bash
# Pruebas unitarias
./mvnw test

# Pruebas + reporte de cobertura JaCoCo
./mvnw verify

# Prueba de una clase específica
./mvnw test -Dtest=DashboardServiceTest
```

---

## 14. Evidencia del Despliegue CI/CD

> **Insertar aquí:** screenshot del pipeline de GitHub Actions en verde (Actions → CI — M12 Estadísticas y Analítica → última ejecución).

El pipeline se define en `.github/workflows/ci.yml` y se ejecuta en cada push a `main`, `develop` o `feature/**` y en cada PR a `main` o `develop`.

| Paso | Acción | Descripción |
|---|---|---|
| 1 | `actions/checkout@v4` | Clona el repositorio |
| 2 | `actions/setup-java@v4` | Configura JDK 21 Temurin |
| 3 | `actions/cache@v4` | Restaura caché de dependencias Maven |
| 4 | `chmod +x mvnw` | Permisos al Maven Wrapper |
| 5 | `./mvnw compile -q` | Verifica compilación |
| 6 | `./mvnw verify` | Ejecuta tests + genera reporte JaCoCo |
| 7 | `actions/upload-artifact@v4` | Publica reporte JaCoCo como artifact |
| 8 | `docker build` | Construye imagen `m12-statistics-analytics:{sha}` |

---

## 15. Scaffolding y Código Documentado

```
mewtwocode-statistics-analytics/
│
├── src/
│   ├── main/
│   │   ├── java/edu/eci/patriciaM12/
│   │   │   │
│   │   │   ├── application/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   └── ReportFiltersRequest.java         # DTO entrada para solicitud de reporte
│   │   │   │   │   └── response/
│   │   │   │   │       ├── AdminAnalyticsResponse.java       # Response panel admin (@JsonInclude NON_NULL)
│   │   │   │   │       ├── AnalyticsDTO.java                 # Serie temporal + total + categorías
│   │   │   │   │       ├── EventAnalyticsDTO.java            # Top eventos del campus
│   │   │   │   │       ├── HeatmapDTO.java                   # Heatmap de actividad por zona
│   │   │   │   │       ├── ReportRequestResponse.java        # Response ciclo de vida del reporte
│   │   │   │   │       └── StudentDashboardResponse.java     # Response dashboard del estudiante
│   │   │   │   └── service/
│   │   │   │       ├── AdminAnalyticsService.java            # Implementa GetAdminAnalyticsUseCase
│   │   │   │       ├── DashboardService.java                 # Implementa GetStudentDashboardUseCase
│   │   │   │       └── ReportService.java                    # Implementa RequestReportUseCase + @Async
│   │   │   │
│   │   │   ├── domain/
│   │   │   │   ├── exceptions/
│   │   │   │   │   ├── CsvGenerationException.java           # Fallo en generación del CSV
│   │   │   │   │   ├── InvalidReportFiltersException.java    # dateFrom > dateTo (@ResponseStatus 400)
│   │   │   │   │   ├── MetricNotFoundException.java          # Métrica de dominio no encontrada
│   │   │   │   │   └── ReportNotFoundException.java          # Reporte no existe o no es del usuario
│   │   │   │   ├── model/
│   │   │   │   │   ├── AdminAnalyticsSnapshot.java           # Snapshot diario (UNIQUE por fecha)
│   │   │   │   │   ├── CategoryStat.java                     # Valor: category + count + percentage
│   │   │   │   │   ├── MetricEvent.java                      # DTO Kafka (sin persistencia JPA)
│   │   │   │   │   ├── ReportFilters.java                    
│   │   │   │   │   ├── ReportRequest.java                    
│   │   │   │   │   ├── StudentDashboardMetric.java           
│   │   │   │   │   └── enums/
│   │   │   │   │       ├── CampusZone.java
│   │   │   │   │       ├── MetricEventType.java
│   │   │   │   │       ├── MetricType.java
│   │   │   │   │       ├── ParticipationLevel.java
│   │   │   │   │       ├── PatchCategory.java
│   │   │   │   │       └── ReportStatus.java
│   │   │   │   └── ports/
│   │   │   │       ├── in/
│   │   │   │       │   ├── GetAdminAnalyticsUseCase.java     # Puerto entrada: panel admin
│   │   │   │       │   ├── GetStudentDashboardUseCase.java   # Puerto entrada: dashboard estudiante
│   │   │   │       │   ├── ProcessMetricEventUseCase.java    # Puerto entrada: consumer Kafka
│   │   │   │       │   └── RequestReportUseCase.java         # Puerto entrada: reportes CSV
│   │   │   │       └── out/
│   │   │   │           ├── AdminSnapshotRepositoryPort.java  # Puerto salida: repositorio snapshots
│   │   │   │           ├── CsvGeneratorPort.java             # Puerto salida: generación CSV
│   │   │   │           ├── ReportRequestRepositoryPort.java  # Puerto salida: repositorio reportes
│   │   │   │           └── StudentMetricsRepositoryPort.java # Puerto salida: repositorio métricas
│   │   │   │
│   │   │   ├── entrypoints/
│   │   │   │   └── rest/controller/
│   │   │   │       ├── AdminAnalyticsController.java         # GET /api/v1/analytics/admin
│   │   │   │       ├── DashboardController.java              # GET /api/v1/analytics/dashboard
│   │   │   │       └── ReportController.java                 # POST + GET /api/analytics/reports
│   │   │   │
│   │   │   ├── infrastructure/
│   │   │   │   ├── adapters/
│   │   │   │   │   ├── csv/
│   │   │   │   │   │   └── CsvGeneratorAdapter.java          # Implementa CsvGeneratorPort (OpenCSV)
│   │   │   │   │   └── persistence/
│   │   │   │   │       ├── AdminSnapshotRepositoryAdapter.java
│   │   │   │   │       ├── ReportRequestRepositoryAdapter.java
│   │   │   │   │       ├── StudentMetricsRepositoryAdapter.java
│   │   │   │   │       ├── entity/
│   │   │   │   │       │   ├── AdminAnalyticsSnapshotEntity.java
│   │   │   │   │       │   ├── ReportRequestEntity.java
│   │   │   │   │       │   └── StudentDashboardMetricEntity.java
│   │   │   │   │       ├── mapper/
│   │   │   │   │       │   ├── AdminAnalyticsSnapshotMapper.java
│   │   │   │   │       │   ├── ReportRequestMapper.java
│   │   │   │   │       │   └── StudentMetricsMapper.java
│   │   │   │   │       └── repository/
│   │   │   │   │           ├── AdminSnapshotJpaRepository.java
│   │   │   │   │           ├── ReportRequestJpaRepository.java
│   │   │   │   │           └── StudentMetricsJpaRepository.java
│   │   │   │   └── config/
│   │   │   │       ├── SecurityConfig.java                   # JWT converter + rutas protegidas por rol
│   │   │   │       └── SwaggerConfig.java                    # OpenAPI / Swagger UI
│   │   │   │
│   │   │   └── PatriciaM12Application.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties                        # Config base: puerto, Kafka, OAuth2, Swagger
│   │       ├── application-dev.properties                    # H2 en memoria, ddl-auto: create-drop
│   │       ├── application-docker.properties                 # PostgreSQL real, ddl-auto: update
│   │       └── data-dev.sql                                  # Seed de datos para perfil dev
│   │
│   └── test/
│       └── java/edu/eci/patriciaM12/
│           ├── AdminAnalyticsServiceTest.java                # 3 casos — panel admin y validaciones
│           ├── DashboardServiceTest.java                     # 9 casos — dashboard y participationLevel
│           └── ReportServiceTest.java                        # 6 casos — reportes CSV y ownership
│
├── docs/
│   ├── diagrama_componentes_m12.png                          # Diagrama de componentes especifico del sistema
│   ├── ComponentesGeneral_PATRICIA.jpg                       # Diagrama general del sistema
│   ├── M12_Clases.jpg                                        # Diagrama de clases
│   └── M12_Entidad.jpg                                       # Diagrama entidad-relación
│
├── .github/workflows/
│    ├── ci.yml                                               # Pipeline CI: compile → test → jacoco → docker
│    ├── cd.yml                                               # Deploy JAR -> Azure App Service
│    └── sonar.yml                                            # Análisis de calidad con SonarCloud                                                                          
├── .dockerignore 
├── Dockerfile                                                # Contenedorización
├── docker-compose.yml                                        # Backend + PostgreSQL 16 + Kafka KRaft
├── pom.xml
└── README.md
```

---

## 16. Pipeline de Desarrollo

El pipeline de desarrollo garantiza que ningún código roto llegue a la rama de integración. Se ejecuta automáticamente en cada push a `develop` y `feature/**`.

```yaml
on:
  push:
    branches: [ develop, feature/** ]
  pull_request:
    branches: [ develop ]
```

**Pasos:**
```bash
# 1. Checkout + Java 21 Temurin + caché Maven
# 2. Compilar
./mvnw compile -q

# 3. Tests + JaCoCo
./mvnw verify

# 4. Publicar reporte JaCoCo como artifact
# 5. Build imagen Docker
docker build -t m12-statistics-analytics:${GITHUB_SHA} .
```

**Estrategia de ramas (Git Flow):**

```
main          ← merges desde release/* y hotfix/* únicamente (tags vX.Y.Z)
  └── develop ← integración continua
        └── feature/dashboard
        └── feature/panel-admin
        └── feature/exportacion-csv
```

**Convenciones de ramas:**
```
feature/[nombre-funcionalidad]   # máximo 50 caracteres
```

**Convenciones de commits:**
```
feat: nueva funcionalidad
fix:  corrección de error
docs: cambio en documentación
```

---

## 17. Pipeline de PROD

El pipeline de producción se ejecuta únicamente en push a `main`, garantizando que solo código validado mediante PR y con todos los checks en verde llega a producción.

```yaml
on:
  push:
    branches: [ main ]
```

**Pasos adicionales respecto al pipeline de desarrollo:**

| Paso | Descripción |
|---|---|
| Todos los pasos del pipeline de desarrollo | Compile → Test → JaCoCo → Docker build |
| Tag de imagen con versión semántica | `m12-statistics-analytics:v{tag}` |
| Push de imagen al registro de contenedores | Publicación de la imagen Docker |
| Despliegue en EC2 | `docker compose up -d` en instancia EC2 t3.medium con perfil `docker` |

**Reglas de protección de `main`:**
- PR obligatorio con al menos 1 aprobación
- Todos los checks del pipeline de desarrollo en verde antes del merge
- Push directo a `main` bloqueado

---

<div align="center">

### Equipo **Mewtwo-Code**

![Team](https://img.shields.io/badge/Team-Mewtwo--Code-blueviolet?style=for-the-badge&logo=github&logoColor=white)
![Module](https://img.shields.io/badge/Module-M12_Estad%C3%ADsticas_%26_Anal%C3%ADtica-orange?style=for-the-badge)
![Course](https://img.shields.io/badge/Course-DOSW-orange?style=for-the-badge)
![Year](https://img.shields.io/badge/Year-2026--1-blue?style=for-the-badge)

> **PATRIC.IA Statistics & Analytics Service** — punto central de visibilidad de métricas del campus, con dashboards personalizados, analítica administrativa y exportación asíncrona de reportes CSV.

**Escuela Colombiana de Ingeniería Julio Garavito**

</div>
# AGENTS.md — Guidelines for AI Coding Agents in SLM

This document establishes behavioral rules, architectural constraints, and repository maintenance protocols for AI agents working in **Simon's Life Manager (SLM)**.

---

## 1. Core Principles & Philosophy

1. **Maintain Living Documentation**:
   - Code changes that alter behavior, schema, dependencies, or interfaces must be documented immediately in [`CHANGELOG.md`](CHANGELOG.md).
   - Non-trivial technical, domain, or architectural choices must be formalized in [`DECISIONS.md`](DECISIONS.md).
2. **Never Break Working Invariants**:
   - Do not bypass security boundaries, break polymorphic inheritance, or violate domain separation rules.
3. **Verify with Live Tests & Health Checks**:
   - Run tests (`./gradlew test -x jacocoTestReport -x compileAotTestJava`) and verify runtime health before concluding tasks.

---

## 2. Maintaining [`CHANGELOG.md`](CHANGELOG.md)

- **Standard**: Follow [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and Semantic Versioning.
- **Location**: Update under the currently active version header (e.g. `## [0.0.2f] - YYYY-MM-DD`).
- **Categories to Use**:
  - `### Added` for new features, entities, endpoints, or tests.
  - `### Changed` for changes in existing functionality, metrics, or dependencies.
  - `### Deprecated` for soon-to-be-removed features.
  - `### Removed` for now removed features.
  - `### Fixed` for any bug fix.
  - `### Security` in case of vulnerabilities (e.g. CWE-22 path traversal defense).
- **Format**: Include concise, informative bullet points highlighting what changed and why.

---

## 3. Maintaining [`DECISIONS.md`](DECISIONS.md)

- **Standard**: Every new major architectural, security, database, or infrastructural decision must be added as an Architectural Decision Record (ADR).
- **Structure for New ADRs**:
  ```markdown
  ## ADR 0XX: [Concise Decision Title]

  ### Status
  Accepted | Proposed | Superseded

  ### Context
  [Explain the problem, previous limitations, security issues, or domain requirements.]

  ### Decision
  [Explain the chosen technical approach, schemas, diagrams, or code patterns.]

  ### Consequences & Trade-offs
  [Detail the benefits, constraints, and operational implications.]
  ```
- **Sync with `doc/plans/`**:
  - Exploratory plans live in `doc/plans/`. Once an architectural plan is approved and implemented, summarize its core decisions into an ADR in `DECISIONS.md`.

---

## 4. Build, Compilation & Test Standards

### Technology Baseline
- **Language**: Java 27 (`JavaLanguageVersion.of(27)`)
- **Framework**: Spring Boot 4.2.0-M2
- **Testing**: JUnit 6 (`org.junit:junit-bom:6.1.3`) + Spring Boot Test + Testcontainers (`1.21.3`)
- **Database**: PostgreSQL 18 with PostGIS 3.6 (`postgis/postgis:18-3.6`)
- **JSON Platform**: Jackson 3 BOM (`tools.jackson:jackson-bom:3.2.3`)

### Gradle Test Execution Rule
JaCoCo does not yet support Java 27 bytecode instrumentation (major version 71).
Always execute tests with `-x jacocoTestReport -x compileAotTestJava`:
```bash
./gradlew test -x jacocoTestReport -x compileAotTestJava
```
- To run a specific test class:
  ```bash
  ./gradlew test --tests de.oberamsystems.slm.controller.SessionControllerTest -x jacocoTestReport -x compileAotTestJava
  ```

### Integration Test Guidelines
- Integration tests must be named `*IT.java` and inherit from `AbstractIntegrationTest`.
- `AbstractIntegrationTest` manages a shared singleton `postgis/postgis:18-3.6` container initialized with `src/test/resources/schema-init.sql` using Spring Boot's `@ServiceConnection`.
- Never start independent container instances per test class—reuse the shared singleton container.

---

## 5. Database & Schema Synchronization

Whenever database schemas, tables, triggers, or functions are modified, agents must update **all three** schema artifacts:

1. **`src/test/resources/schema-init.sql`**: Authoritative schema for Testcontainers integration tests.
2. **`init.sql`**: Root initialization script mounted by Docker Compose (`/docker-entrypoint-initdb.d/init.sql`).
3. **`src/main/resources/tables.sql`** & **`enums.sql`**: Baseline reference schema for local installations.
4. **`src/main/resources/migration_YYYY-MM-DD[_N].sql`**: Incremental migration script for existing local / production databases.

### Mandatory Schema Elements
- **PostGIS**: Always include `CREATE EXTENSION IF NOT EXISTS postgis;`.
- **Spatial Geometry**: `point.geom` must be `geography(Point, 4326)` with GIST index `idx_point_geom`.
- **Triggers**: Preserve geodesic air distance triggers for routes (`calculate_route_air_distance()`) and doctors (`calculate_doctor_air_distance()`).
- **Helper Functions**: Preserve `this_years_birthday(date)` required by `Human.java` `@Formula`.

---

## 6. Domain Invariants & Architecture Rules

1. **JPA Model Entities Must NOT Be Java Records (ADR 002)**:
   - Entity classes in `de.oberamsystems.slm.model` must remain mutable JavaBeans with non-private no-arg constructors.
   - Do NOT convert `@Entity` classes to `record` (breaks Hibernate proxying, joined inheritance, and `WebDataBinder`).
2. **Polymorphic Session Hierarchy (`InheritanceType.JOINED`) (ADR 001, ADR 011)**:
   - Root `Session` entity defines `id TEXT PRIMARY KEY` (UUIDv7), `starttime`, `endtime`, `breakDuration`, `duration`, `netDuration`, and ratings.
   - Subclasses (`CodingSession`, `WorkingSession`, `LearningSession`, `ReadingSession`, `GamingSession`, `SportSession`, `MeditationSession`, `SleepSession`) must extend `Session`.
3. **Four Pillars Life Balance Model (ADR 011)**:
   - **Productivity**: Coding, Working, Learning, Reading.
   - **Sport**: Dedicated physical training, cardio, workouts. *(Must remain distinct from wellness).*
   - **Wellness**: Restorative practices including Meditation and Sleep.
   - **Leisure**: Recreational gaming.
4. **Net Focused Time Purity (ADR 012)**:
   - On `/sessions` (`ALL` & `PRODUCTIVITY`), "Net Focused Time" (`totalNetDuration`) strictly aggregates cognitive **Productivity** sessions.
   - Sleep and Meditation durations must **never** be included in Net Focused Time.
5. **Path Traversal Defense for File Storage (ADR 007)**:
   - All file uploads (selfies, documents) must route through dedicated storage services (e.g. `SelfieStorageService`).
   - Must enforce canonical path boundary check: `targetPath.normalize().toAbsolutePath().startsWith(baseDir)`.
   - Store original bytes without transcoding (`.jpg`, `.jpeg`, `.heic`).
6. **Separation of Work vs. Expenses (ADR 004)**:
   - `Employer` models professional income sources and clients for `WorkingSession`.
   - `Contractor` models outgoing expense service providers (telecom, insurance, subscriptions) for `Contract`.

---

## 7. Docker & Container Orchestration (ADR 014)

- **Database Image**: `db` in `docker-compose.yml` must use `postgis/postgis:18-3.6`.
- **Healthcheck**: `db` must declare a healthcheck (`test: ["CMD-SHELL", "pg_isready -U simon -d slm"]`).
- **Service Dependency**: `app` must declare `depends_on: db: condition: service_healthy`.
- **Application Image**: `Dockerfile` must use `eclipse-temurin:27-jre` runtime base image.

---

## 8. Communication & Response Style

- Keep responses concise and focused on high-level decisions, open questions, and results.
- Create clickable GitHub-style markdown links using the `file://` scheme for all mentioned files and classes (e.g. [`SessionController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java)).
- Avoid speculative rewrites; follow existing design patterns and verified architectural decisions.

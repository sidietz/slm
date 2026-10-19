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

Whenever database schemas, tables, triggers, or functions are modified, agents must synchronize the baseline schema artifacts and provide an incremental migration script for legacy databases:

1. **`src/main/resources/tables.sql`** & **`enums.sql`**: Baseline reference schema for local installations. Always represents the latest authoritative state.
2. **`init.sql`**: Root initialization script mounted by Docker Compose (`/docker-entrypoint-initdb.d/init.sql`).
3. **`src/test/resources/schema-init.sql`**: Authoritative schema for Testcontainers integration tests.
4. **`src/main/resources/migration_YYYY-MM-DD[_N].sql`**: Incremental migration script for existing local / production databases.

### Mandatory Schema Elements
- **PostGIS**: Always include `CREATE EXTENSION IF NOT EXISTS postgis;`.
- **Spatial Geometry**: `point.geom` must be `geography(Point, 4326)` with GIST index `idx_point_geom`.
- **Triggers**: Preserve geodesic air distance triggers for routes (`calculate_route_air_distance()`) and doctors (`calculate_doctor_air_distance()`).
- **Helper Functions**: Preserve `this_years_birthday(date)` required by `Human.java` `@Formula`.

---

## 6. Database Migrations & Legacy Upgrades

- **`tables.sql` Is Always Authoritative**:
  - [`src/main/resources/tables.sql`](src/main/resources/tables.sql) (along with [`enums.sql`](src/main/resources/enums.sql)) is **always the single authoritative, most up-to-date schema** reflecting the full, current state of the application.
  - New local installations, Docker Compose bootstrap ([`init.sql`](init.sql)), and Testcontainers test suites ([`src/test/resources/schema-init.sql`](src/test/resources/schema-init.sql)) instantiate the full database directly from this complete schema.
  - Fresh setups must never run legacy migration scripts sequentially—they execute the current `tables.sql` / `init.sql`.

- **Migrations Only Affect Legacy Systems**:
  - Incremental scripts (`src/main/resources/migration_YYYY-MM-DD[_N].sql`) exist **solely to upgrade existing / legacy databases** (e.g. running developer or production instances) without data loss.
  - Migrations do not define the target architecture; they are non-destructive transitions bridging previous states to the current `tables.sql`.

- **Mandatory Transactional DDL (`BEGIN;` ... `COMMIT;`)**:
  - PostgreSQL supports transactional DDL for table alterations, constraint changes, index creation, and data transformations.
  - All incremental migration scripts (`src/main/resources/migration_YYYY-MM-DD[_N].sql`) **must start with `BEGIN;` and terminate with `COMMIT;`**.
  - Never author bare, non-transactional statements in migration scripts. This guarantees that multi-step migrations execute atomically and roll back automatically upon any error, preventing partial or corrupted schema states on developer and production databases.

- **Protocol for Schema Alterations**:
  1. **Update Baseline Files First**: Apply table modifications, indices, functions, and triggers to:
     - [`src/main/resources/tables.sql`](src/main/resources/tables.sql)
     - [`init.sql`](init.sql)
     - [`src/test/resources/schema-init.sql`](src/test/resources/schema-init.sql)
  2. **Generate Transactional Migration Delta**: Create a new file [`src/main/resources/migration_YYYY-MM-DD[_N].sql`](src/main/resources/) enclosed in `BEGIN;` ... `COMMIT;` containing only the statements needed to safely migrate legacy data and structures.
  3. **Verification**: Verify that the migration script executes cleanly on a legacy database state and results in a schema equivalent to the fresh baseline.

---

## 7. Domain Invariants & Architecture Rules

1. **JPA Model Entities Must NOT Be Java Records (ADR 002)**:
   - Entity classes in `de.oberamsystems.slm.model` must remain mutable JavaBeans with non-private no-arg constructors.
   - Do NOT convert `@Entity` classes to `record` (breaks Hibernate proxying, joined inheritance, and `WebDataBinder`).
2. **Polymorphic Session Hierarchy (`InheritanceType.JOINED`) (ADR 001, ADR 011, ADR 018)**:
   - Root `Session` entity defines `id UUID PRIMARY KEY` (native UUIDv7 via Hibernate `@UuidGenerator` and PostgreSQL 18 `DEFAULT uuidv7()`), `starttime`, `endtime`, `breakDuration`, `duration`, `netDuration`, and ratings.
   - Subclasses (`CodingSession`, `WorkingSession`, `LearningSession`, `ReadingSession`, `GamingSession`, `SportSession`, `MeditationSession`, `SleepSession`, `LectureSession`) must extend `Session`.
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

## 8. Deterministic Domain-Specific Sorting & Ordering

To ensure consistent and predictable UI views across filters, hub tabs, and API endpoints, entities and lists must always be ordered according to domain-specific criteria:

1. **Academic Courses & Semesters**:
   - Courses must be sorted by **semester descending** (most recent semester first via [`SemesterComparator.DESCENDING`](src/main/java/de/oberamsystems/slm/util/SemesterComparator.java)), then **alphabetically ascending by course title** (`title ASC` case-insensitive via [`CourseComparator`](src/main/java/de/oberamsystems/slm/util/CourseComparator.java)).
   - Distinct semester tabs and filters must display semesters in chronological descending order (most recent first).
2. **Train Lines**:
   - Train lines must be sorted using [`TrainLineComparator`](src/main/java/de/oberamsystems/slm/util/TrainLineComparator.java): alphabetically by line prefix category (e.g. `ICE`, `RE`, `S`, `U`), then numerically ascending by line number (e.g. `S1`, `S2`, `S8`, `U2`, `U3`).
3. **Temporal Logs & Activities (Appointments, Sessions, Trips, Journal Entries)**:
   - When domain-specific grouping is not applicable, temporal events and activity logs must always be sorted by **DateTime descending** (most recent first, e.g. `start DESC`).
4. **Catalog & Reference Data (Habits, Doctors, Employers, Contractors, Games, Books, Sport Types)**:
   - Static or reference catalog entities must be sorted **alphabetically ascending** by primary name or title (e.g. habit name, doctor last name, book title).

---

## 9. Docker & Container Orchestration (ADR 014)

- **Database Image**: `db` in `docker-compose.yml` must use `postgis/postgis:18-3.6`.
- **Healthcheck**: `db` must declare a healthcheck (`test: ["CMD-SHELL", "pg_isready -U simon -d slm"]`).
- **Service Dependency**: `app` must declare `depends_on: db: condition: service_healthy`.
- **Application Image**: `Dockerfile` must use `eclipse-temurin:27-jre` runtime base image.

---

## 10. Communication & Response Style

- Keep responses concise and focused on high-level decisions, open questions, and results.
- Create clickable GitHub-style markdown links using the `file://` scheme for all mentioned files and classes (e.g. [`SessionController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java)).
- Avoid speculative rewrites; follow existing design patterns and verified architectural decisions.

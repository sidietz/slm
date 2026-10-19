# Architectural Decision Records (ADR)

This document records the architectural and design decisions made for **Simon's Life Manager (SLM)**, capturing context, rationale, trade-offs, and implementation details.

---

## Table of Contents

- [ADR 001: Session Hierarchy, Duration Calculation & Identifier Strategy](#adr-001-session-hierarchy-duration-calculation--identifier-strategy)
- [ADR 002: Rejection of Java Records for JPA Model Entities](#adr-002-rejection-of-java-records-for-jpa-model-entities)
- [ADR 003: Application Version Synchronization via Spring Boot BuildInfo](#adr-003-application-version-synchronization-via-spring-boot-buildinfo)
- [ADR 004: Domain Separation of Employer (Income/Work) and Contractor (Expenses/Contracts)](#adr-004-domain-separation-of-employer-incomework-and-contractor-expensescontracts)
- [ADR 005: PostGIS Spatial Geometry & Automated Route Air Distance Calculation](#adr-005-postgis-spatial-geometry--automated-route-air-distance-calculation)
- [ADR 006: Integration of Healthcare Providers (Doctor) into Spatial Point Architecture](#adr-006-integration-of-healthcare-providers-doctor-into-spatial-point-architecture)
- [ADR 007: Defensive Path Traversal Architecture (CWE-22) & Native Media Preservation for Selfies](#adr-007-defensive-path-traversal-architecture-cwe-22--native-media-preservation-for-selfies)
- [ADR 008: Shared Singleton PostGIS Testcontainers Pattern for Integration Testing](#adr-008-shared-singleton-postgis-testcontainers-pattern-for-integration-testing)
- [ADR 009: Container Cleanup Strategy: Ryuk Watchdog vs. JVM Shutdown Hooks](#adr-009-container-cleanup-strategy-ryuk-watchdog-vs-jvm-shutdown-hooks)
- [ADR 010: Modernization to Jackson 3 BOM Platform (`tools.jackson`)](#adr-010-modernization-to-jackson-3-bom-platform-toolsjackson)
- [ADR 011: Sleep Tracking Unification into Session Architecture & Four Pillars Life Balance Model](#adr-011-sleep-tracking-unification-into-session-architecture--four-pillars-life-balance-model)
- [ADR 012: Decoupling Biological Rest from Net Focused Time & Context-Adaptive KPIs](#adr-012-decoupling-biological-rest-from-net-focused-time--context-adaptive-kpis)
- [ADR 013: Migration to JUnit 6 Testing Platform](#adr-013-migration-to-junit-6-testing-platform)
- [ADR 014: Containerized Production Deployment with PostGIS 18-3.6 & Healthcheck Orchestration](#adr-014-containerized-production-deployment-with-postgis-18-36--healthcheck-orchestration)
- [ADR 015: Unified Life Events, Anniversaries & Memorials Architecture](#adr-015-unified-life-events-anniversaries--memorials-architecture)
- [ADR 016: Spring AI Model Context Protocol (MCP) Server Integration](#adr-016-spring-ai-model-context-protocol-mcp-server-integration)
- [ADR 017: University Lectures, Academic Tracking & Native UUIDv7 Markdown Lecture Notes](#adr-017-university-lectures-academic-tracking--native-uuidv7-markdown-lecture-notes)
- [ADR 018: Migration of Polymorphic Session Hierarchy to Native PostgreSQL 18 UUIDv7](#adr-018-migration-of-polymorphic-session-hierarchy-to-native-postgresql-18-uuidv7)

---

## ADR 001: Session Hierarchy, Duration Calculation & Identifier Strategy

### Status
Accepted

### Context
In the early SLM codebase, multiple time-tracking activities existed independently (`LearningSession`, `GamingSession`, `ReadingSession`, `SportSession`, `MeditationSession`). Each duplicated core fields (`start`, `end`, `duration`, `comment`), redundant repository interfaces, copy-pasted controller endpoints, and disjoint templates.

We generalized all time-tracking activities into an extensible polymorphic hierarchy supporting work, study, leisure, sport, wellness, and sleep.

Three major architectural decisions were established:
1. **Inheritance Mapping Strategy** in JPA (`InheritanceType.JOINED` vs `@MappedSuperclass` vs `SINGLE_TABLE`).
2. **Duration Calculation Strategy** (Database-computed generated column vs Application-computed domain logic).
3. **Primary Key Identifier Strategy** (UUIDv7 vs UUIDv4 vs BIGSERIAL).

---

### Decision 1: Inheritance Strategy — Option A (`InheritanceType.JOINED`)

We adopt **`InheritanceType.JOINED`** with a base entity `Session` and dedicated subclass tables.

```mermaid
classDiagram
    class Session {
        <<abstract>>
        +String id
        +LocalDateTime start
        +LocalDateTime end
        +Duration breakDuration
        +Duration duration
        +Duration netDuration
        +String status
        +Integer focusRating
        +String comment
        +String outcomeSummary
    }
    class LearningSession {
        +LearningItem learningItem
        +Integer unitsCompleted
    }
    class GamingSession {
        +Game game
        +Device device
        +String gameMode
    }
    class CodingSession {
        +String project
        +String repository
        +String branch
        +String ticket
        +String language
    }
    class WorkingSession {
        +Employer employer
        +String project
        +Boolean billable
        +BigDecimal hourlyRate
        +String taskCategory
    }
    class ReadingSession {
        +Book book
        +Integer startPageCount
        +Integer endPageCount
        +Float readingSpeed
    }
    class MeditationSession {
    }
    class SportSession {
        +SportType type
    }
    class SleepSession {
        +Boolean isNap
        +Duration deepSleepDuration
        +Duration remSleepDuration
        +Integer awakeCount
    }

    Session <|-- LearningSession
    Session <|-- GamingSession
    Session <|-- CodingSession
    Session <|-- WorkingSession
    Session <|-- ReadingSession
    Session <|-- MeditationSession
    Session <|-- SportSession
    Session <|-- SleepSession
```

#### Rationale & Evaluation
- **Unified Base Table:** All sessions inherit from `Session`, sharing `id`, timestamps, `breakDuration`, `duration`, `netDuration`, status, and focus ratings.
- **Polymorphic Queries:** Allows holistic queries across all life activities (e.g. `SELECT s FROM Session s WHERE s.start BETWEEN :from AND :to`). This makes aggregated dashboards, daily time distributions, and unified timelines straightforward.
- **Normalized Relational Schema:** Each session type maintains its own foreign keys and attributes in separate tables (`reading_session`, `learning_session`, `coding_session`, `sleep_session`, etc.) without polluting the base table with dozens of nullable columns.

---

### Decision 2: Duration Calculation Strategy — Database Generated Columns

We adopt **Database-computed generated columns** for storing and calculating duration.

#### Database Definition (PostgreSQL)
```sql
break_duration INTERVAL NOT NULL DEFAULT '0 seconds',
duration INTERVAL GENERATED ALWAYS AS (endtime - starttime) STORED,
net_duration INTERVAL GENERATED ALWAYS AS (endtime - starttime - break_duration) STORED
```

#### Entity Mapping (JPA / Hibernate)
```java
@Type(PostgreSQLIntervalType.class)
@Column(name = "break_duration", columnDefinition = "interval")
private Duration breakDuration = Duration.ZERO;

@Type(PostgreSQLIntervalType.class)
@Column(name = "duration", columnDefinition = "interval", insertable = false, updatable = false)
@org.hibernate.annotations.Generated(event = {
    org.hibernate.generator.EventType.INSERT, 
    org.hibernate.generator.EventType.UPDATE
})
private Duration duration;

@Type(PostgreSQLIntervalType.class)
@Column(name = "net_duration", columnDefinition = "interval", insertable = false, updatable = false)
@org.hibernate.annotations.Generated(event = {
    org.hibernate.generator.EventType.INSERT, 
    org.hibernate.generator.EventType.UPDATE
})
private Duration netDuration;
```

#### Trade-offs: Database-Only vs. Application-Side

| Strategy | Pros | Cons |
| :--- | :--- | :--- |
| **Strategy 1: Database Generated Columns**<br>*(PostgreSQL `GENERATED ALWAYS AS`)* | • **Single source of truth at storage level:** Guarantees interval consistency regardless of entry point (direct SQL, migrations, batch imports).<br>• **Zero application CPU overhead:** Interval arithmetic is handled by the database engine.<br>• **Consistent with existing schema:** Matches existing tables in SLM (`appointment`, `car_trip`). | • **Hibernate post-insert state:** Without `@Generated(event = ...)`, Hibernate does not automatically populate the column into memory on `INSERT`.<br>• **Dialect specificity:** Requires PostgreSQL `INTERVAL` and generated column support. |
| **Strategy 2: Application-Side Calculation**<br>*(JPA Lifecycle Hooks)* | • Immediate in-memory consistency without re-fetching. | • Integrity risk on direct SQL or external updates unless paired with DB check constraints. |

**Mitigation:** We annotate generated fields with Hibernate's `@Generated(event = {EventType.INSERT, EventType.UPDATE})` and `@Column(insertable = false, updatable = false)` so Hibernate refreshes the column value upon flush/commit.

---

### Decision 3: Type Standards in PostgreSQL Schema

- All variable-length textual fields use PostgreSQL **`TEXT`** rather than arbitrary `VARCHAR(n)`.
- Primary keys for the session hierarchy use PostgreSQL **`TEXT`** (storing UUIDs as canonical 36-character strings), preventing sequence collisions across joined tables and enabling client-side / offline ID generation.

---

### Decision 4: Primary Key Identifier Strategy — Adopting UUIDv7 (RFC 9562)

In `InheritanceType.JOINED`, the root `session` table defines `id TEXT PRIMARY KEY`, and every child table defines:
```sql
id TEXT PRIMARY KEY REFERENCES session(id) ON DELETE CASCADE
```

We adopt **UUIDv7** (RFC 9562) over UUIDv4 and BIGSERIAL:

| Characteristic | BIGSERIAL (Integer) | UUIDv4 (Random) | UUIDv7 (Time-Ordered, RFC 9562) |
| :--- | :--- | :--- | :--- |
| **B-Tree Index Locality** | Optimal (strictly sequential append) | Poor (random writes cause page splits & cache misses) | **Optimal (monotonically increasing time prefix appends sequentially)** |
| **Collision Resistance** | Single sequence | Globally unique | **Globally unique (48-bit timestamp + 74 bits of entropy)** |
| **Distributed / Offline Generation** | No (requires DB roundtrip) | Yes | **Yes (generated client-side without DB roundtrip)** |
| **Sorting** | Numeric | Unordered | **Naturally chronological (`ORDER BY id` == `ORDER BY created_at`)** |

In Java:
```java
@Id
@Column(name = "id", columnDefinition = "TEXT")
private String id;

@PrePersist
public void prePersist() {
    if (this.id == null) {
        this.id = UUID.ofEpochMillis(System.currentTimeMillis()).toString();
    }
}
```

---

## ADR 002: Rejection of Java Records for JPA Model Entities

### Status
Accepted

### Context
Java records provide concise immutable data carriers. An architectural evaluation was conducted to determine if JPA entity classes in `de.oberamsystems.slm.model` should be refactored into Java records.

### Decision
Model entities in `de.oberamsystems.slm.model` must **not** be converted to Java `record`s.

### Rationale
1. **Jakarta Persistence Specification Constraints:** JPA and Hibernate require entity classes to have a non-private no-arg constructor and to be non-final to allow runtime proxy generation (ByteBuddy) for lazy loading, dirty checking, and relationship hydration. Java records are implicitly `final` and cannot declare parameterless constructors; annotating a record with `@Entity` throws `AnnotationException: An entity cannot be a record`.
2. **Polymorphic Class Hierarchies:** Java records implicitly extend `java.lang.Record` and cannot extend classes. The `Session` hierarchy (`CodingSession`, `GamingSession`, `SportSession`, `ReadingSession`, `MeditationSession`, `SleepSession`) relies fundamentally on `@Inheritance(strategy = InheritanceType.JOINED)`.
3. **Web Form Binding:** Spring MVC and Thymeleaf require mutable JavaBean property conventions (`get*` / `set*`) for `@ModelAttribute` binding with `WebDataBinder`.

Java records remain reserved for read-only DTOs, query projections, or immutable `@Embeddable` value objects.

---

## ADR 003: Application Version Synchronization via Spring Boot BuildInfo

### Status
Accepted

### Context
Previously, version strings were duplicated across `build.gradle`, `application.properties`, and hardcoded controller logic, leading to version drift and manual maintenance overhead.

### Decision
Make `build.gradle` the single source of truth for the application version using Spring Boot's `buildInfo()` task and `BuildProperties`.

### Implementation
1. **Gradle Build Info Configuration (`build.gradle`):**
   ```groovy
   springBoot {
       buildInfo()
   }
   ```
   Gradle automatically generates `META-INF/build-info.properties` during `bootBuildInfo`, capturing `build.version`, `build.name`, `build.group`, and `build.time`.
2. **Spring Context Auto-Configuration:**
   Spring Boot automatically registers a `BuildProperties` bean when `build-info.properties` is present.
3. **Controller Access:**
   Injected into controllers (e.g. [`ImprintController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/ImprintController.java)) via `@Autowired(required = false) BuildProperties buildProperties`.
4. **Configuration Cleanup:**
   Removed hardcoded `version=...` from `application.properties`.

---

## ADR 004: Domain Separation of Employer (Income/Work) and Contractor (Expenses/Contracts)

### Status
Accepted

### Context
Previously, `WorkingSession` referenced `Contractor` as `client` via `contractor_id`. However, in the SLM domain:
- `Contractor` models third-party service providers, utilities, telecommunications, and insurers (e.g. *Vodafone*, *Telekom*, *Allianz*, *Netflix*) associated with outgoing recurring expenses and `Contract` entries.
- `WorkingSession` models billable and salaried professional work performed for employers and consulting clients.

### Decision
1. Introduce a dedicated `Employer` (`employer`) entity and table (`employer_id` in `working_session`).
2. Add a dedicated `EmployerController` and view (`/employers`) accessible via the Spend Management dashboard alongside *Vendors* and *Contractors*.
3. Versionize database migrations using date-stamped scripts (`migration_YYYY-MM-DD.sql`).

---

## ADR 005: PostGIS Spatial Geometry & Automated Route Air Distance Calculation

### Status
Accepted (v0.0.2b)

### Context
The `point` table previously stored coordinates as raw `TEXT` strings (often in Degrees-Minutes-Seconds DMS format, e.g. `49°17'04.49"N`, `10°40'53.20"E`). On the `route` table, `air_distance` was manually entered by the user upon route creation. This resulted in manual guesswork, entry errors, and inability to perform spatial queries.

### Decision
1. Enable native **PostGIS 3.6** spatial extensions in PostgreSQL (`CREATE EXTENSION IF NOT EXISTS postgis;`).
2. Store coordinates as standard IEEE double-precision floats (`latitude`, `longitude`) in WGS 84 (`SRID 4326`).
3. Add a computed, stored PostGIS geography column and spatial index:
   ```sql
   geom geography(Point, 4326) GENERATED ALWAYS AS (
       ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
   ) STORED
   CREATE INDEX IF NOT EXISTS idx_point_geom ON point USING GIST (geom);
   ```
4. Implement database triggers (`trg_route_air_distance`, `trg_point_update_route_air_distance`) using PostGIS geodesic distance calculation:
   ```sql
   ST_Distance(start_geom, end_geom) / 1000.0
   ```
   Air distance is automatically computed in kilometers on insert or update of routes and points, completely eliminating manual entry.

---

## ADR 006: Integration of Healthcare Providers (Doctor) into Spatial Point Architecture

### Status
Accepted (v0.0.2b)

### Context
`Doctor` previously stored arbitrary text addresses and a manual `air_distance REAL` field that had to be manually entered in `doctors.html`. Analysis revealed that these distances represented the geodesic distance from the user's primary residence (`Haus Mama`) to the doctor's practice.

### Decision
1. Unify healthcare provider locations with the spatial `point` architecture by adding `point_id BIGINT REFERENCES point(id)` (practice clinic) and optional `base_point_id BIGINT REFERENCES point(id)` (defaulting to the user's home point with `is_home = TRUE`).
2. Automate doctor air distance calculation using database triggers:
   ```sql
   ST_Distance(origin_geom, practice_geom) / 1000.0
   ```
3. Remove manual distance inputs from the UI while retaining auto-calculated mileage for medical appointment travel duration records.

---

## ADR 007: Defensive Path Traversal Architecture (CWE-22) & Native Media Preservation for Selfies

### Status
Accepted (v0.0.2d)

### Context
The daily selfie upload endpoint (`SelfieController`) allowed users to upload image files. Vulnerability assessment identified risks of directory traversal (CWE-22) via manipulated multipart filenames (e.g., `../../../../etc/passwd`). Furthermore, the form only allowed `.jpg` files, rejecting modern mobile formats like `.heic`.

### Decision
1. Extract file storage and validation into a dedicated, thoroughly tested `SelfieStorageService`.
2. Enforce strict canonical path boundary checks:
   ```java
   Path destination = this.baseDir.resolve(safeFilename).normalize().toAbsolutePath();
   if (!destination.startsWith(this.baseDir)) {
       throw new SecurityException("Directory traversal attempt detected");
   }
   ```
3. Support both `.jpg` (and `.jpeg`) and `.heic` natively without transcoding: raw bytes are preserved exactly on disk.
4. Automatically clean up stale files with different extensions (e.g. replacing `2026-10-18.jpg` when re-uploading `2026-10-18.heic`).

---

## ADR 008: Shared Singleton PostGIS Testcontainers Pattern for Integration Testing

### Status
Accepted (v0.0.2e)

### Context
Unit tests mocked repositories or ran against in-memory schemas, which failed to validate PostgreSQL-specific dialect features, PostGIS spatial triggers, JPA joined inheritance polymorphism, and generated stored columns. Running separate containers per test class caused excessive Docker overhead and slow build times.

### Decision
1. Standardize on Spring Boot Testcontainers with `@ServiceConnection` running the official `postgis/postgis:18-3.6` container image.
2. Implement a **shared singleton container pattern** via an abstract base class:
   ```java
   public abstract class AbstractIntegrationTest {
       @ServiceConnection
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgis/postgis:18-3.6")
               .withDatabaseName("slm_test")
               .withUsername("test")
               .withPassword("test")
               .withInitScript("schema-init.sql");
       static {
           postgres.start();
       }
   }
   ```
3. Maintain an authoritative test initialization schema in [`src/test/resources/schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql) consolidating enums, tables, PostGIS extensions, and spatial triggers.
4. Configure Gradle's `test` task to execute both unit tests (`*Test.class`) and integration tests (`*IT.class`).

---

## ADR 009: Container Cleanup Strategy: Ryuk Watchdog vs. JVM Shutdown Hooks

### Status
Accepted (v0.0.2e)

### Context
In Testcontainers, **Ryuk** (`testcontainers/ryuk`) runs as an auxiliary watchdog container attached to `/var/run/docker.sock` to terminate test containers when the JVM exits. Architectural evaluation weighed whether to disable Ryuk in favor of JVM shutdown hooks or keep it active.

### Decision
1. **Retain Ryuk active by default** for local developer workflows. JVM shutdown hooks fail to execute if an IDE terminates a test abruptly (`SIGKILL` / red stop button) or on JVM crash, which would leak running PostgreSQL 18 containers and consume host ports.
2. **Support zero-Ryuk execution** via environment property `TESTCONTAINERS_RYUK_DISABLED=true` or `ryuk.disabled=true` for restricted corporate CI/CD or rootless Podman/Kubernetes environments where socket mounting is restricted.

---

## ADR 010: Modernization to Jackson 3 BOM Platform (`tools.jackson`)

### Status
Accepted (v0.0.2f)

### Context
Jackson 2 (`com.fasterxml.jackson`) reached end-of-life for major architectural innovations. Jackson 3 introduced modular namespace changes (`tools.jackson.core`) with improved record support, stricter typing, and higher performance.

### Decision
1. Adopt the official Jackson 3 BOM platform:
   ```groovy
   implementation enforcedPlatform('tools.jackson:jackson-bom:3.2.3')
   implementation 'tools.jackson.core:jackson-databind'
   implementation 'tools.jackson.core:jackson-core'
   ```
2. Bridge annotations using `com.fasterxml.jackson.core:jackson-annotations:2.22` for full compatibility with Spring Boot 4 and existing serialization rules.

---

## ADR 011: Sleep Tracking Unification into Session Architecture & Four Pillars Life Balance Model

### Status
Accepted (v0.0.2f)

### Context
Sleep was tracked through a legacy, standalone `SleepTime` entity (`sleeptime` table) separate from the polymorphic `Session` hierarchy. This prevented unified queries across the 24-hour day and resulted in disconnected reporting.

### Decision
1. Convert `SleepTime` into joined inheritance:
   ```java
   @Entity
   @Table(name = "sleep_session")
   public class SleepSession extends Session
   ```
2. Establish a **Four Pillars Life Balance Model**:
   - **Productivity**: Coding, Working, Learning, Reading sessions.
   - **Sport**: Dedicated physical training, running, and workouts (distinct from wellness).
   - **Wellness**: Biological restoration, meditation, and sleep sessions.
   - **Leisure**: Recreational gaming sessions.
3. Preserve full backward compatibility for legacy endpoints (`/sleep-times`, `/sleeptime-bb`, `/api/sleeptimes`) via transient compatibility getters/setters (`gotobed`, `getup`, `sleeptime`).

---

## ADR 012: Decoupling Biological Rest from Net Focused Time & Context-Adaptive KPIs

### Status
Accepted (v0.0.2f)

### Context
When viewing "All Sessions" (`type=ALL`) on `/sessions`, all logged durations were summed into `totalNetDuration`. Because sleep accounts for 7–8 hours daily (257h+ in sample datasets), "Net Focused Time" was inflated to 280h+, destroying its meaning as a metric for intentional cognitive deep work.

### Decision
1. Strictly decouple Sleep and Meditation from "Net Focused Time" and "Avg Focus Rating" on `/sessions`:
   - When viewing `ALL` or `PRODUCTIVITY`, `totalNetDuration` only aggregates intentional cognitive sessions (`CodingSession`, `WorkingSession`, `LearningSession`, `ReadingSession`).
   - Sleep quality ratings (1–5) and meditation focus are calculated independently from cognitive focus ratings.
2. Introduce context-adaptive KPI titles that dynamically reflect the active category filter:
   - **All / Productivity**: `Net Focused Time` *(Cognitive focus hours)*, `Avg Focus Rating`.
   - **Sport**: `Active Training Time`, `Avg Workout Intensity`.
   - **Wellness / Sleep**: `Rest & Recovery Time` (or `Sleep Duration`), `Awake / Disruptions`, `Avg Sleep Quality`.
   - **Leisure**: `Recreation Time`, `Avg Game Rating`.
3. Provide total logged hours under `Total Life: Xh Ym` so comprehensive time tracking is preserved without distorting cognitive focus metrics.

---

## ADR 013: Migration to JUnit 6 Testing Platform

### Status
Accepted (v0.0.2f)

### Context
JUnit 6 unified versioning across Platform, Jupiter, and Vintage modules with a native Java 17+ baseline.

### Decision
Enforce the unified JUnit 6 BOM platform across all test configurations:
```groovy
dependencyManagement {
    imports {
        mavenBom 'org.junit:junit-bom:6.1.3'
    }
}
dependencies {
    testImplementation enforcedPlatform('org.junit:junit-bom:6.1.3')
}
```
All Jupiter and Platform components resolve cleanly to version `6.1.3` (`junit-jupiter`, `junit-platform-launcher`, `junit-platform-engine`), operating seamlessly with Spring Boot 4 and Testcontainers.

---

## ADR 014: Containerized Production Deployment with PostGIS 18-3.6 & Healthcheck Orchestration

### Status
Accepted (v0.0.2f)

### Context
The default `docker-compose.yml` previously used standard `postgres`, which lacked PostGIS binaries, causing database initialization errors. Furthermore, the application container started before PostgreSQL was ready, causing connection failures.

### Decision
1. Upgrade database container to `postgis/postgis:18-3.6`.
2. Add automated healthcheck testing database readiness:
   ```yaml
   db:
     image: postgis/postgis:18-3.6
     healthcheck:
       test: ["CMD-SHELL", "pg_isready -U simon -d slm"]
       interval: 3s
       timeout: 3s
       retries: 10
   ```
3. Configure application container dependencies:
   ```yaml
   app:
     depends_on:
       db:
         condition: service_healthy
   ```
4. Update [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql) to include PostGIS extensions, modern polymorphic session tables, and spatial distance calculation triggers.
5. Modernize [`Dockerfile`](file:///home/simon/code/eclipse/slm/Dockerfile) runtime base image to `eclipse-temurin:27-jre` matching Java 27.

---

## ADR 015: Unified Life Events, Anniversaries & Memorials Architecture

### Status
Accepted (v0.0.2g)

### Context
Previously, contact date management was limited to a single `birthday: DATE` field on the `Human` entity. In reality, personal relationships encompass a wide spectrum of annual recurring milestones:
1. **Birthdays**: Age milestones for living contacts.
2. **Memorials & Death Dates**: Remembrance days for deceased friends, relatives, and historical figures, calculating years since passing and respecting deceased status.
3. **Wedding & Relationship Anniversaries**: Milestone celebrations (e.g. Silver or Golden Wedding) that link primary and secondary partner contacts.
4. **Career & Custom Anniversaries**: Workplace milestones, graduations, or personal achievements.

### Decision
1. **First-Class `HumanEvent` Entity**:
   Introduce a unified `human_event` table and entity modeling all annual life events with:
   - `human_id` (primary contact) and optional `secondary_human_id` (partner contact for couple celebrations).
   - `event_type` (`BIRTHDAY`, `MEMORIAL`, `WEDDING_ANNIVERSARY`, `RELATIONSHIP_ANNIVERSARY`, `WORK_ANNIVERSARY`, `CUSTOM`).
   - `event_date` (original historic date) and `annual_recurrence`.
2. **Deceased Contact Tracking on `Human`**:
   Extend `human` with `death_date DATE` and formula properties `is_deceased` and `age_at_death` to distinguish active living contacts from remembered ones.
3. **PostgreSQL Helper Functions**:
   Introduce `next_annual_occurrence(date)` and `days_until_annual_occurrence(date)` supporting leap years (February 29 -> February 28 on non-leap years) and rolling over past events to next year with positive day countdowns.
4. **Dedicated Events Hub (`/human-events`) & Dashboard Integration**:
   - Provide an interactive hub with filter pills, countdown badges, and milestone labels.
   - Aggregate top upcoming milestones into the Home Dashboard alert stream.
5. **Seamless Backward Compatibility & Auto-Migration**:
   Automatically migrate existing `human.birthday` values into initial `BIRTHDAY` events in `migration_2026-10-18_5.sql` and keep contact edits synchronized.

---

## ADR 016: Spring AI Model Context Protocol (MCP) Server Integration

### Status
Accepted (v0.0.2h)

### Context
Simon's Life Manager (SLM) serves as the central operational hub for personal tracking, deep work sessions, life balance, habits, medical appointments, and life milestones. Interacting with this data via external AI assistants (e.g. Claude Desktop, Gemini CLI, Cursor, Antigravity IDE) required exposing SLM's domain capabilities through a standardized protocol.

The **Model Context Protocol (MCP)** by Anthropic is the industry standard for connecting AI agents to local/remote tools and data sources. Spring AI provides native MCP server support via `spring-ai-starter-mcp-server-webmvc`.

Key architectural requirements:
1. **Transport Protocol**: Server-Sent Events (SSE) over HTTP to allow concurrent external AI client connectivity while SLM runs as a web service.
2. **Endpoint Standardization**: Rely on standard Spring AI WebMVC default endpoints (`/sse` handshake and `/mcp/message` JSON-RPC message endpoint) rather than custom non-standard paths.
3. **Security Architecture**: Secure all MCP communications against unauthorized local network access using a dedicated API key authentication filter (`X-API-KEY` header, `?apiKey=` query parameter, or `Bearer` token) while bypassing CSRF protection for JSON-RPC operations.
4. **Jackson Compatibility**: Seamlessly interoperate with SLM's Jackson 3 BOM platform (`tools.jackson:jackson-bom:3.2.3`).
5. **Domain Breadth**: Expose a cohesive suite of tools spanning Life Events, Polymorphic Sessions & Four Pillars Balance, Wellness & Habits, and Medical Healthcare Appointments.

### Decision

1. **Spring AI WebMVC Dependency**:
   Integrated `spring-ai-bom:2.0.1` and `spring-ai-starter-mcp-server-webmvc` into [`build.gradle`](file:///home/simon/code/eclipse/slm/build.gradle). This pulls `io.modelcontextprotocol.sdk:mcp-json-jackson3:2.0.0`, natively matching SLM's Jackson 3 architecture.
2. **Default SSE Transport Endpoints**:
   Maintained standard Spring AI default paths:
   - `GET /sse`: Server-Sent Events handshake and event stream.
   - `POST /mcp/message`: Inbound JSON-RPC protocol messages and tool invocation requests.
3. **Dedicated API Key Security Layer**:
   Implemented [`McpApiKeyAuthFilter`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/security/McpApiKeyAuthFilter.java) placed before `UsernamePasswordAuthenticationFilter`:
   - Validates incoming requests to `/mcp/**` and `/sse`.
   - Checks `X-API-KEY` header, `?apiKey=` URL query parameter, or `Authorization: Bearer <key>` against configurable property `slm.mcp.api-key` (default: `slm-mcp-secret-key-2026`).
   - Returns clean HTTP 401 JSON error responses for unauthenticated requests.
   - Configured [`SecurityConfig`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/SecurityConfig.java) with `.ignoringRequestMatchers("/mcp/**", "/sse", "/api/**")` to prevent CSRF interference on state-changing tool executions.
4. **Tool Suite Architecture (`de.oberamsystems.slm.mcp`)**:
   Organized tool callbacks across 4 functional domains using `@Tool`:
   - [`McpLifeEventTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpLifeEventTools.java): `getUpcomingEvents`, `getEventsForContact`, `createLifeEvent`.
   - [`McpSessionTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java): `getDailyLifeBalance` (Productivity, Sport, Wellness, Leisure hours), `getRecentSessions`, `logCodingSession`, `logSleepSession`.
   - [`McpWellnessTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpWellnessTools.java): `getHabitsStatus`, `recordHabitCompleted`, `logDailyMood`, `logGratitude`.
   - [`McpMedicalTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpMedicalTools.java): `getUpcomingAppointments`, `searchDoctors`, `scheduleAppointment`.
   - Configured [`McpServerConfig`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpServerConfig.java) providing a `ToolCallbackProvider` bean with `MethodToolCallbackProvider`.
5. **Live Integration Testing**:
   Created [`McpServerIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/McpServerIT.java) extending `AbstractIntegrationTest`:
   - Verifies HTTP 401 rejection on `/sse` and `/mcp/message` without API key.
   - Verifies successful SSE connection with valid `X-API-KEY`.
   - Validates live JSON-RPC `tools/call` invocations against the shared singleton PostGIS Testcontainer.

### Consequences & Trade-offs
- **Benefits**: Any MCP-compliant agent can query and modify SLM schedule, balance, and health state directly via SSE.
- **Security**: Direct API key authentication isolates AI agent interactions from browser session state without requiring form login or session cookies.
- **Constraints**: Standard `/sse` and `/mcp/message` paths must remain unmapped and exempt from CSRF checks.

---

## ADR 017: University Lectures, Academic Tracking & Native UUIDv7 Markdown Lecture Notes

### Status
Accepted (v0.0.2i)

### Context
Simon's Life Manager previously supported open-ended `LearningSession` entries, but lacked dedicated infrastructure for university studies and academic courses:
1. **Curricular Context**: Courses require academic attributes such as semester (e.g. `WS 2026/27`), ECTS credits, assigned lecturers (modeled via [`Human`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Human.java) with `HumanCategory.LECTURER`), default lecture halls/meeting links, syllabi, and exam schedules (`LocalDateTime examDate` with exam locations).
2. **Cognitive Focus Tracking**: Attending a lecture is an intentional cognitive deep work activity. Under the **Four Pillars Life Balance Model** ([ADR 011](DECISIONS.md#adr-011-sleep-tracking-unification-into-session-architecture--four-pillars-life-balance-model)), it belongs to the **Productivity** pillar alongside Coding, Working, Learning, and Reading sessions, and must contribute to **Net Focused Time** ([ADR 012](DECISIONS.md#adr-012-decoupling-biological-rest-from-net-focused-time--context-adaptive-kpis)).
3. **Lecture Notes & Knowledge Retention**: Lecture notes require chronological sequencing per course, rich Markdown formatting, key concepts, executive takeaway summaries, homework tracking, and exam relevance ratings (1–5).
4. **Identifier Architecture**: Notes benefit from time-ordered identifiers for sequential B-tree clustering, distributed generation, and tamper-resistant URL routing. PostgreSQL 18 introduced native `uuidv7()` and Hibernate 7 natively provides `@UuidGenerator(style = Style.VERSION_7)`.

### Decision

1. **First-Class Academic Course Entity (`course` table)**:
   - Modeled via [`Course`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Course.java) with `title`, `code`, `semester`, `ectsCredits` (`NUMERIC(3,1)`), `syllabus`, `defaultRoom`, `meetingLink`, `examDate` (`LocalDateTime` / `TIMESTAMP`), `examLocation`, `grade`, `examPassed`, and `status`.
   - Links to [`Human`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Human.java) lecturer (`category = 'LECTURER'`).
   - Managed via [`AcademicController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/AcademicController.java) at `/courses` and `/courses/{id}`.

2. **Joined Polymorphic `LectureSession` Entity (`lecture_session` table)**:
   - Extends [`Session`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java) with joined inheritance (`@DiscriminatorValue("LECTURE")`).
   - Links to [`Course`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Course.java) with `lectureNumber`, `topic`, `room`, and standard `start`, `end`, `breakDuration`, `focusRating`, and `comment`.
   - Included under the **Productivity** pillar in `/sessions` filtering, dashboard balance metrics, and Net Focused Time aggregation.

3. **First-Class `LectureNote` with Native UUIDv7 (`lecture_note` table)**:
   - Primary key is native UUIDv7:
     ```sql
     CREATE TABLE lecture_note (
         id UUID PRIMARY KEY DEFAULT uuidv7(),
         course_id BIGINT REFERENCES course(id) ON DELETE CASCADE,
         lecture_session_id TEXT REFERENCES lecture_session(id) ON DELETE SET NULL,
         title TEXT NOT NULL,
         lecture_number INTEGER,
         lecture_date DATE,
         content_markdown TEXT,
         key_concepts TEXT,
         summary TEXT,
         has_homework BOOLEAN DEFAULT FALSE,
         homework_todo TEXT,
         exam_relevance_rating INTEGER,
         created_at TIMESTAMP,
         last_updated TIMESTAMP
     );
     ```
   - In JPA:
     ```java
     @Id
     @GeneratedValue
     @UuidGenerator(style = Style.VERSION_7)
     @Column(name = "id", columnDefinition = "uuid")
     private UUID id;
     ```
   - Features a split-screen Markdown editor with live preview via `marked.js`, snippet insertion toolbar (formulas, code, callouts, tables), key concepts, and exam relevance stars.
   - Global keyword search across Markdown content, titles, and concept tags via `LectureNoteRepository.searchNotes()`.

4. **Spring AI Model Context Protocol (MCP) Expansion**:
   - Implemented [`McpAcademicTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java) providing `getCourses`, `getCourseDetails`, `logLectureSession`, `saveLectureNote`, and `searchLectureNotes`.
   - Integrated into [`McpServerConfig`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpServerConfig.java) callback provider.
   - Updated [`McpSessionTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java) to factor `LectureSession` into daily Productivity balance.

5. **Schema Synchronization & Migration**:
   - Synchronized baseline schema artifacts: [`tables.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/tables.sql), [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql), and [`schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql).
   - Generated non-destructive incremental migration [`migration_2026-10-18_6.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/migration_2026-10-18_6.sql) for upgrading existing local / production databases.

### Consequences & Trade-offs
- **Benefits**: Complete academic lifecycle tracking seamlessly unified with personal life management; instant notes retrieval; Net Focused Time remains an accurate measure of cognitive work; optimal database clustering through UUIDv7.
- **Constraints**: Requires PostgreSQL 18+ for native SQL `uuidv7()` defaults; client-side Markdown rendering requires `marked.js` in views.

---

## ADR 018: Migration of Polymorphic Session Hierarchy to Native PostgreSQL 18 UUIDv7

### Status
Accepted (v0.0.2j)

### Context
Following the implementation of `LectureNote` with native `java.util.UUID` and PostgreSQL 18 `DEFAULT uuidv7()` ([ADR 017](file:///home/simon/code/eclipse/slm/DECISIONS.md#adr-017-university-lectures-academic-tracking--native-uuidv7-markdown-lecture-notes)), an architectural inconsistency existed across the domain model:
- `Session` and all 9 polymorphic joined subclasses (`CodingSession`, `WorkingSession`, `LearningSession`, `ReadingSession`, `GamingSession`, `SportSession`, `MeditationSession`, `SleepSession`, `LectureSession`) stored UUIDv7 identifiers as canonical 36-character `TEXT` strings.
- Foreign keys targeting sessions (such as `lecture_note.lecture_session_id`) were defined as `TEXT REFERENCES lecture_session(id)`.
- Storing UUIDs as `TEXT` imposed a 36-byte storage overhead (compared to 16 bytes for native `uuid`), sub-optimal lexicographical B-tree comparison, and prevented Hibernate 7's direct `@UuidGenerator(style = Style.VERSION_7)` mapping on the root session entity.

### Decision

1. **Native Entity Identifier**:
   - Updated root [`Session`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java) entity to use `java.util.UUID` with Hibernate 7 `@UuidGenerator`:
     ```java
     @Id
     @GeneratedValue
     @UuidGenerator(style = Style.VERSION_7)
     @Column(name = "id", columnDefinition = "uuid")
     private UUID id;
     ```
   - Maintained `@PrePersist` fallback using Java 27 `UUID.ofEpochMillis(System.currentTimeMillis())` to guarantee instant version 7 UUID generation for unit tests executed outside an active persistence context.

2. **Spring Data JPA Repositories**:
   - Migrated all 10 session repositories to `JpaRepository<T, UUID>`:
     - `SessionRepository`, `CodingSessionRepository`, `WorkingSessionRepository`, `LearningSessionRepository`, `ReadingSessionRepository`, `GamingSessionRepository`, `SportSessionRepository`, `MeditationSessionRepository`, `SleepSessionRepository`, `LectureSessionRepository`.
   - Updated [`LectureNoteRepository`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LectureNoteRepository.java) to define `findByLectureSessionId(UUID lectureSessionId)`.

3. **Web Controllers & Spring AI MCP Server**:
   - Updated [`SessionController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java) `/sessions/delete/{id}` endpoint to take `@PathVariable UUID id`.
   - Updated [`AcademicController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/AcademicController.java) `/courses/{courseId}/notes/new` and `/notes` endpoints to bind `@RequestParam(required = false) UUID sessionId`.
   - Thymeleaf templates seamlessly bind and render native `UUID` instances via `UUID.toString()`.
   - MCP session logging tools ([`McpSessionTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java), [`McpAcademicTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java)) return native UUIDs serialized automatically to JSON.

4. **Authoritative Baseline Schemas & Non-Destructive Migration**:
   - Updated baseline definitions in [`tables.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/tables.sql), [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql), and [`schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql) to `id UUID PRIMARY KEY DEFAULT uuidv7()`.
   - Created incremental migration script [`src/main/resources/migration_2026-10-19_1.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/migration_2026-10-19_1.sql) wrapped in an atomic transaction (`BEGIN;` ... `COMMIT;`) to safely drop foreign keys, alter columns with `USING id::uuid`, set default to `uuidv7()`, and restore constraints with full rollback guarantees.

### Consequences & Trade-offs
- **Benefits**:
  - Consistent type safety across the entire application domain.
  - Over 55% reduction in identifier storage size (16 bytes vs 36+ bytes).
  - Faster B-Tree index traversal and cache efficiency in PostgreSQL 18.
  - Native alignment with PostgreSQL 18 `uuidv7()` and Hibernate 7 `@UuidGenerator`.
- **Constraints**:
  - Legacy databases require execution of `migration_2026-10-19_1.sql`. Any custom mock IDs in test databases must conform to standard RFC 9562 UUID format.

---

## ADR 019: Sleep Architecture Tracking, Light Sleep & Cross-Table Duration Invariant Enforcement

### Status
Accepted (v0.0.2j)

### Context
In SLM, restorative wellness practices are tracked under the Wellness pillar (ADR 011). Previously, `SleepSession` only tracked `deep_sleep_duration`, `rem_sleep_duration`, and `awake_count` (with interruptions stored in `session.break_duration`). This left three gaps:
1. **Missing Light Sleep**: Standard sleep cycles consist of Awake, Deep, REM, and Light sleep. Without `light_sleep_duration`, complete sleep architecture could not be represented.
2. **Missing Duration Auto-Calculation**: Users with untracked sleep or wearables reporting only 2 stages had no way to automatically deduce the missing duration without manual arithmetic.
3. **Lack of Sum Consistency & Validation**: No constraint existed to ensure `deep + rem + light + awake` equaled `endtime - starttime`. Users could enter contradictory durations, and the database did not enforce cross-table consistency.

### Decision
1. **Model & Schema Addition**:
   - Added `light_sleep_duration INTERVAL` to `sleep_session` table and `SleepSession.java` entity (mapped with `@Type(PostgreSQLIntervalType.class)`).
2. **Auto-Calculation & Input Validation Rules**:
   - **All 3 stages empty**: Defaults the entire net sleep duration (`endtime - starttime - break_duration`) to Light Sleep (`deep = 0`, `rem = 0`).
   - **Exactly 1 stage missing**: Automatically solves for the missing stage: $\text{missing} = \text{net\_duration} - \sum(\text{entered stages})$.
   - **All 3 stages provided**: Validates that $\text{deep} + \text{rem} + \text{light} + \text{break} = \text{endtime} - \text{starttime}$.
   - **Invalid inputs**: Rejects mismatched or ambiguous combinations with user-facing validation error flash messages (`errorMessage`).
3. **Cross-Table PostgreSQL Database Triggers**:
   - Implemented `trg_check_sleep_session_duration` (`BEFORE INSERT OR UPDATE ON sleep_session`) and `trg_check_session_sleep_duration` (`AFTER UPDATE ON session`) raising a PostgreSQL exception if sleep stages plus awake disruptions do not equal total gross session duration.
4. **Migration & Baselines**:
   - Authored atomic transactional migration `migration_2026-10-19_2.sql` (`BEGIN;` ... `COMMIT;`) backfilling legacy records (`light = duration - break - deep - rem`) and establishing triggers.
   - Synchronized `tables.sql`, `init.sql`, and `schema-init.sql`.

### Consequences & Trade-offs
- **Benefits**:
  - Full polysomnographic sleep architecture representation.
  - Frictionless UI experience: users can log simple sleep with 0 stage inputs (auto-defaults to Light Sleep) or enter 2 stages (third auto-calculates).
  - Absolute data integrity guaranteed at the PostgreSQL database level across all entry channels.
- **Constraints**:
  - Direct SQL scripts and external integrations must satisfy `deep + rem + light + break = end - start`.

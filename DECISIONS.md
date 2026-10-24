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
- [ADR 019: Cross-Table Generated Column Consistency in Sleep Tracking](#adr-019-cross-table-generated-column-consistency-in-sleep-tracking)
- [ADR 020: Full Integration Test Coverage Across WebMVC Controllers](#adr-020-full-integration-test-coverage-across-webmvc-controllers)
- [ADR 021: Universal Active Sessions (Start & Stop Later) across Polymorphic Hierarchy](#adr-021-universal-active-sessions-start--stop-later-across-polymorphic-hierarchy)
- [ADR 022: Vehicle Refuelings, Station Polymorphism, and Spritmonitor CSV Import](#adr-022-vehicle-refuelings-station-polymorphism-and-spritmonitor-csv-import)
- [ADR 023: Contract Lifecycle Semantics, Auto-Extension Modeling, and Non-Negative Days Remaining](#adr-023-contract-lifecycle-semantics-auto-extension-modeling-and-non-negative-days-remaining)
- [ADR 024: Universal Modal-First Input Architecture and In-Place Entity Editing](#adr-024-universal-modal-first-input-architecture-and-in-place-entity-editing)
- [ADR 025: Two-Phase Deprecation of Legacy SleepTime UI in Preparation for Schema Removal](#adr-025-two-phase-deprecation-of-legacy-sleeptime-ui-in-preparation-for-schema-removal)
- [ADR 026: Consistent Session Start Time Initialization & Auto-Populated Stop End Time](#adr-026-consistent-session-start-time-initialization--auto-populated-stop-end-time)
- [ADR 027: Academic Activity Classification, STEM Lab Support & Clean Domain Separation](#adr-027-academic-activity-classification-stem-lab-support--clean-domain-separation)
- [ADR 028: Car-Related Expense Tracking, Spritmonitor Taxonomy, and Lifecycle TCO & Multi-Vehicle Comparison Analytics (€/100km)](#adr-028-car-related-expense-tracking-spritmonitor-taxonomy-and-lifecycle-tco--multi-vehicle-comparison-analytics-100km)
- [ADR 029: Universal PostgreSQL TIMESTAMPTZ Migration & Daylight Saving Time (Europe/Berlin) Integrity](#adr-029-universal-postgresql-timestamptz-migration--daylight-saving-time-europeberlin-integrity)
- [ADR 030: Complete Migration to OffsetDateTime and Dynamic ZoneId Resolution for TIMESTAMPTZ Integration](#adr-030-complete-migration-to-offsetdatetime-and-dynamic-zoneid-resolution-for-timestamptz-integration)

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
Accepted (v0.0.3-rc1)

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
Accepted (v0.0.3-rc1)

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
   - **Deferred Constraint Trigger for Joined-Table Inheritance Flush Ordering (v0.0.3-rc11)**:
     - In Hibernate joined inheritance, updating an entity flushes the parent table (`session`) before the child table (`sleep_session`). An immediate `AFTER UPDATE` trigger on `session` evaluated `sleep_session` with stale stages before Hibernate had executed `UPDATE sleep_session`.
     - Converted `trg_check_session_sleep_duration` on `session` into a `CONSTRAINT TRIGGER ... AFTER UPDATE ON session DEFERRABLE INITIALLY DEFERRED`. This defers consistency checks until transaction `COMMIT` time when both tables have flushed, while `trg_check_sleep_session_duration` on `sleep_session` enforces immediate validation on child inserts/updates.
4. **Migration & Baselines**:
   - Authored atomic transactional migration `migration_2026-10-19_2.sql` (`BEGIN;` ... `COMMIT;`) backfilling legacy records (`light = duration - break - deep - rem`) and establishing triggers.
   - Authored incremental transactional migration [`migration_2026-10-24_1.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/migration_2026-10-24_1.sql) converting `trg_check_session_sleep_duration` to a deferred constraint trigger.
   - Synchronized `tables.sql`, `init.sql`, and `schema-init.sql`.

### Consequences & Trade-offs
- **Benefits**:
  - Full polysomnographic sleep architecture representation.
  - Frictionless UI experience: users can log simple sleep with 0 stage inputs (auto-defaults to Light Sleep) or enter 2 stages (third auto-calculates).
  - Absolute data integrity guaranteed at the PostgreSQL database level across all entry channels.
  - Deferring `trg_check_session_sleep_duration` until transaction `COMMIT` cleanly aligns database integrity constraints with Hibernate's joined-table entity flush lifecycle without loosening validation.
- **Constraints**:
  - Direct SQL scripts and external integrations must satisfy `deep + rem + light + break = end - start`.

---

## ADR 020: Comprehensive Controller Integration Testing Architecture & Model Alignment

### Status
Accepted (v0.0.3-rc1)

### Context
While isolated unit tests with Mockito verified individual controller methods in isolation, real runtime interactions across Spring Security, CSRF token handling, Spring MVC `DomainClassConverter` parameter binding, Hibernate joined inheritance, PostGIS geodesic distance functions, and Thymeleaf template rendering required live integration verification. Additionally, prior integration tests only covered academic workflows, leaving 31 controllers without automated full-stack verification against PostgreSQL.

### Decision

1. **Singleton Shared Testcontainers Architecture**:
   - Enforced [`AbstractIntegrationTest`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/AbstractIntegrationTest.java) base class across all integration tests (`*IT.java`).
   - Utilizes a shared singleton `postgis/postgis:18-3.6` container managed via Spring Boot `@ServiceConnection` and initialized with `src/test/resources/schema-init.sql`.
   - Enforced class-level `@Transactional` across all test classes to guarantee automatic rollback after every test, preventing database mutation pollution across test classes.

2. **Domain-Cohesive Test Suite Partitioning**:
   - Partitioned the 33 controllers into domain-specific, high-cohesion integration test classes:
     - [`DashboardAndManagementIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DashboardAndManagementIT.java): Index, Imprint, and the 6 domain management hubs.
     - [`SessionHubIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SessionHubIT.java): Unified sessions hub, polymorphic session subclasses, 4 pillars KPI aggregation, and UUIDv7 deletion.
     - [`ActivitiesAndMediaIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/ActivitiesAndMediaIT.java): Activities, sports, learning, meditation, games, and books.
     - [`DoctorControllerIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DoctorControllerIT.java): Doctors, specialities, appointments, and PostGIS air distance calculation.
     - [`WellnessAndHabitsIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java): Habits, daily mood tracking, REST APIs, and sleep times.
     - [`SocialAndPeopleIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SocialAndPeopleIT.java): Human contacts, automated birthday/event synchronization, and calendar event timelines.
     - [`PersonalJournalIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/PersonalJournalIT.java): Diary entries with date filtering and gratitude logging.
     - [`FinanceManagementIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/FinanceManagementIT.java): Employers, recurring contracts/subscriptions, and purchase logging.
     - [`TransitAndMobilityIT`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java): GPS Points, Routes with PostGIS geodesic distance, Car trips, TrainLines, TrainStations, and TrainTrips.

3. **Entity Model Alignment**:
   - Fixed [`TrainTrip2`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip2.java) entity associations: aligned `origin` and `destination` fields to [`TrainStation2`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainStation2.java), fixing foreign key violations with `trainstation2(id)`.

4. **Spring Security Route Permissibility**:
   - Hardened [`SecurityConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/SecurityConfig.java) with explicit matchers for `/doctor.html` and `/speciality.html`.

### Consequences & Trade-offs
- **Benefits**:
  - 100% of Spring MVC and REST controllers are covered by automated integration tests executing against a real PostgreSQL 18 + PostGIS database.
  - Runtime errors in template rendering, parameter converters, and database foreign keys are caught before deployment.
  - Sub-35 second test execution across the entire 131-test suite due to singleton Testcontainers container reuse.
- **Constraints**:
  - Tests must include `.with(csrf())` on state-mutating requests (`POST`, `PUT`, `DELETE`).
  - Class-level `@Transactional` must be maintained on all integration tests modifying database state.

---

## ADR 021: Universal Active Sessions (Start & Stop Later) across Polymorphic Hierarchy

### Status
Accepted (v0.0.3-rc2)

### Context
In SLM, time tracking across all Four Pillars (Productivity, Sport, Wellness, Leisure) previously required both a start datetime and an end datetime upon creation. In practice, users begin activities (e.g. client work, coding sessions, reading, gym workouts, meditation, sleep) without knowing in advance when they will finish. This created artificial friction and prevented real-time session tracking.

### Decision

1. **Nullable End Time & Generated Columns Compatibility**:
   - In PostgreSQL 18, `session.endtime` is nullable, and the stored generated columns (`duration`, `net_duration`) evaluate to `NULL` when `endtime IS NULL` without raising database constraint violations.
   - Initialized session records created without an end datetime with `status = 'IN_PROGRESS'`.
   - Added domain methods `isInProgress()` and `stop(LocalDateTime, Duration, Integer, String, String)` directly on root [`Session`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java).

2. **Polymorphic Stop Endpoint**:
   - Implemented a unified `POST /sessions/{id}/stop` endpoint in [`SessionController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java) accepting optional `end` datetime (defaulting to current time), `breakMinutes`, `focusRating` (1-5), `outcomeSummary`, and reflection `comment`.
   - When stopping a `SleepSession`, automatically calculates missing sleep stages (`lightSleepDuration = netDuration - deep - rem`) to strictly preserve the cross-table database triggers established in ADR 019.

3. **Active Session Banner & Real-Time Client Ticker**:
   - Added an **Active Session Hero Banner** in [`sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sessions.html) that appears dynamically whenever active ongoing sessions exist.
   - Displayed elapsed time using client-side JavaScript ticking every second (`00:00:00`), along with 1-click **Quick Stop** and modal **Finish with Review** actions.
   - Enhanced the sessions table with `IN_PROGRESS` badges, live timers, and inline stop action buttons.

4. **Relaxation of Input Validation**:
   - Removed `required` attributes from the `end` datetime inputs across [`sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sessions.html), [`working-sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/working-sessions.html), and [`coding-sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/coding-sessions.html).
   - Safeguarded [`IndexController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/IndexController.java) against null end datetimes when displaying recent activities.

5. **Spring AI MCP Server Tools**:
   - Added `getActiveSessions`, `startSession`, and `stopSession` to [`McpSessionTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java).

### Consequences & Trade-offs
- **Benefits**:
  - Completely frictionless session tracking: users can start an activity with one click and finish whenever done.
  - Zero schema migration required: existing `tables.sql`, `init.sql`, and `schema-init.sql` already support nullable `endtime` and `status = 'IN_PROGRESS'`.
  - Polymorphic design ensures any new session subclass automatically inherits start/stop capabilities.
- **Constraints**:
  - Aggregated KPIs (e.g. Net Focused Time) only tally completed sessions (or sessions with non-null durations), safely ignoring ongoing sessions until stopped.

---

## ADR 022: Vehicle Refuelings, Station Polymorphism, and Spritmonitor CSV Import

### Status
Accepted (v0.0.3-rc4)

### Context
SLM tracks vehicles ([`Car`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Car.java)) and vehicle journeys ([`CarTrip`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarTrip.java)). However, tracking operational fuel economy, costs per liter, total expenditure, odometer logs, and migrating long-term historical records from [Spritmonitor](https://www.spritmonitor.de/) previously required manual work outside the system.

Furthermore, fuel and energy infrastructure requires modeling both traditional combustion gas stations (with external price telemetry IDs such as TankenTanken / TankerKönig UUIDs) and EV charging stations (with individual charging point stalls, rated kW capacity, and distinct European plug standards).

### Decision

1. **Station Polymorphic Hierarchy (`InheritanceType.JOINED`) with PostgreSQL 18 `uuidv7()`**:
   - Implemented base [`Station`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Station.java) using native UUIDv7 primary keys (`DEFAULT uuidv7()`), spatial geometry links ([`Point`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Point.java)), and address metadata.
   - Subclass [`GasStation`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/GasStation.java) adds an optional `tankentanken_id UUID UNIQUE` for external fuel price API integration.
   - Subclass [`ChargingStation`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/ChargingStation.java) models EV charging infrastructure with 1:N [`ChargingPoint`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/ChargingPoint.java) stalls holding `max_power_kw NUMERIC(6, 1)` and [`PlugType`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/PlugType.java) (`CCS_TYPE2`, `TYPE_2`, `CHADEMO`, `TESLA`, `TYPE_1`, `GBT`, `OTHER`, `UNKNOWN`).

2. **Vehicle Refueling Entity with Automatic Metrics Calculation**:
   - Created [`Refueling`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Refueling.java) linked to [`Car`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Car.java) and optionally to [`GasStation`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/GasStation.java) (`ON DELETE SET NULL`).
   - Domain method `autoCalculateMetrics()` automatically derives missing `costPerLiter = totalCost / liters` and calculates consumption `(liters / tripDistance) * 100`.
   - Telemetry from vehicle on-board computers is recorded (`board_computer_consumption`, `board_computer_quantity`, `board_computer_speed`).
   - Strictly enforces ISO 3166-1 alpha-2 for country codes (`country VARCHAR(2)`) and ISO 4217 for currency (`currency VARCHAR(3)`).

3. **Spritmonitor CSV Parser (`SpritmonitorCsvService`)**:
   - Parses Spritmonitor exported CSV files (e.g. [`1676970_fuelings.csv`](file:///home/simon/code/eclipse/slm/1676970_fuelings.csv)) featuring semicolon delimiters, quotes, and German decimal commas.
   - Preserves vehicle scope: CSV imports are tied directly to an existing vehicle selected in the UI without altering the `car` table schema.
   - Auto-normalizes legacy country indicators (e.g. `"D"` $\rightarrow$ `"DE"`).
   - Automatically provisions missing gas stations in the database to prevent manual duplicate entry.

4. **Interactive Billboard.js Time-Series Analytics & Management UI**:
   - Built [`refuelings.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/refuelings.html) with vehicle filter pills, KPI overview cards (Total Spent, Total Volume, Avg €/L, Avg L/100km), and an interactive Billboard.js chart powered by `GET /api/refuelings`.
   - The chart dynamically switches between **Consumption (`L/100km`)**, **Trip Distance (`km`)**, and **Fuel Price (`€/L`)** without page reloads.
   - Built [`stations.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/stations.html) with dual tabs for gas stations and EV charging hubs.

5. **Spring AI Model Context Protocol (MCP) Server Integration**:
   - Implemented [`McpVehicleTools`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpVehicleTools.java) registered in [`McpServerConfig`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpServerConfig.java), exposing `getRefuelings`, `logRefueling`, `getStations`, and `createStation`.

### Consequences & Trade-offs
- **Benefits**:
  - Full end-to-end support for tracking vehicle efficiency, expenditures, and station telemetry.
  - Seamless migration of existing Spritmonitor historical logs without manual entry.
  - EV readiness with dedicated charging station and plug type models.
  - 100% test coverage verified with unit and Testcontainers integration tests.
- **Constraints**:
  - Deleting a station cleanly sets `station_id = NULL` on historical refuelings to preserve financial and consumption history.

---

## ADR 023: Contract Lifecycle Semantics, Auto-Extension Modeling, and Non-Negative Days Remaining

### Status
Accepted

### Context
In [`Contract.java`](src/main/java/de/oberamsystems/slm/model/Contract.java), `daysRemaining` was previously calculated via `@Formula(value = "extract(day FROM end_date - now())")`. When a contract's initial minimum commitment period (`end_date`) passed without cancellation, `daysRemaining` turned negative (e.g. `-240 d`), rendering as an alarming red danger badge in [`contracts.html`](src/main/resources/templates/contracts.html).

In modern consumer and commercial law (notably German BGB § 309 Nr. 9 b), contracts with a minimum commitment period (*Mindestvertragslaufzeit*) that are not explicitly cancelled automatically extend (indefinitely with a 1-month rolling notice period, or periodic renewal). Displaying negative days for active, auto-extended subscriptions was semantically incorrect and visually misleading. Furthermore, the label "End Date" conflated the initial commitment period with actual contract termination.

### Decision
1. **Rephrasing "End Date" to "Initial End Date" (`initial_end_date`)**:
   - Renamed column `end_date` to `initial_end_date` across baseline schemas ([`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), [`schema-init.sql`](src/test/resources/schema-init.sql)) and in the entity model.
   - Provided backward-compatible `getEndDate()` and `setEndDate()` getter/setter aliases in [`Contract.java`](src/main/java/de/oberamsystems/slm/model/Contract.java).

2. **Explicit Auto-Extension & Cancellation Domain Model**:
   - Added `auto_extension BOOL NOT NULL DEFAULT TRUE` and `auto_extension_months INT NOT NULL DEFAULT 1`.
   - Added cancellation tracking fields `is_canceled BOOL NOT NULL DEFAULT FALSE`, `cancellation_date DATE`, and `termination_date DATE`.

3. **Guaranteed Non-Negative Days Remaining & Status Classification**:
   - Replaced naive date subtraction with a CASE-based `@Formula`:
     - If contract is inactive: returns `0`.
     - If canceled and `termination_date` exists: returns positive countdown `GREATEST(0, termination_date - CURRENT_DATE)`.
     - If active within initial term: returns positive countdown `initial_end_date - CURRENT_DATE`.
     - If active past initial term with auto-extension: returns `NULL` (signaling indefinite/rolling continuation; never negative).
   - Added `ContractStatus` enum (`ACTIVE_INITIAL_TERM`, `ACTIVE_AUTO_EXTENDED`, `CANCELED_PENDING`, `TERMINATED`) and domain helper `getCalculatedStatus()`.

4. **UI Modernization ([`contracts.html`](src/main/resources/templates/contracts.html))**:
   - Table displays "Initial End Date".
   - Remaining column renders distinct status badges:
     - `Auto-Extended` badge for rolling contracts (no negative days).
     - Countdown badge (`X d`) for initial term contracts.
     - Canceled warning badge (`X d left`) for contracts pending termination.
     - `—` for terminated contracts.
   - Form controls include auto-extension toggle, extension period, and cancellation fields.

5. **Schema Synchronization & Transactional Migration**:
   - Authored transactional incremental migration [`src/main/resources/migration_2026-10-20.sql`](src/main/resources/migration_2026-10-20.sql) enclosed in `BEGIN; ... COMMIT;`.

### Consequences & Trade-offs
- **Benefits**:
  - Eliminates misleading negative day counts and false-positive danger badges.
  - Correctly represents German consumer contract law (BGB § 309 Nr. 9 b) and standard rolling subscriptions.
  - Maintains complete backwards compatibility for existing queries, controllers, and tests.
- **Constraints**:
  - Hibernate `@Formula` fields are evaluated on database fetch/refresh, requiring `entityManager.refresh(...)` when asserting formula results in JPA test transactions.

---

## ADR 024: Universal Modal-First Input Architecture and In-Place Entity Editing

### Status
Accepted

### Context
In earlier versions of Simon's Life Manager (SLM), user interface interactions for entity creation and modification were fragmented across disparate paradigms:
1. **Asymmetric 2-Column Split Views**: Many catalog and reference views (e.g. `contractors.html`, `employers.html`, `vendors.html`, `authors.html`, `publishers.html`, `studios.html`, `manufacturers.html`, `habits.html`, `activity-types.html`, `sport-types.html`) squeezed an inline form on the left (`col-md-5`) and a table on the right (`col-md-7`), causing table horizontal overflow, text clipping, and cramped layout on non-wide viewports.
2. **Top-Heavy Form Cards**: Views like `books.html`, `games.html`, `devices.html`, `activities.html`, `sports.html`, `learning-items.html`, `diary.html`, `gratitudes.html`, `moods.html`, `purchases.html`, `doctors.html`, `humans.html`, `cars.html`, `routes.html`, `points.html`, and `train-trips.html` rendered a large static form card above the data table, pushing the main data grid well below the fold.
3. **Lack of In-Place Editing**: In most entity views, rows could only be deleted or inspected; updating an entry required re-creation or was unsupported in the UI.
4. **Dedicated Termination Actions**: Certain business domain entities like contracts required distinct action flows: editing contract properties (amount, contractor, initial end date) must remain distinct from terminating the contract (setting canceled flag and calculating termination date).

### Decision
We established a project-wide **Modal-First Input Architecture** that unifies all entity listing and management views across SLM:

1. **Full-Width Table Real Estate (`col-12`)**:
   - All legacy 2-column split views and top-heavy form cards have been removed from the page flow.
   - Every entity view now showcases a full-width (`col-12`), responsive data table maximizing readability, column alignment, and horizontal information density.
   - Action buttons (e.g., `+ Add <Entity>`) are placed prominently in the header or table card controls.

2. **Dual-Purpose Bootstrap 5 Modals (Create & Edit)**:
   - Each view defines a single modal dialog serving both entity creation and in-place entity modification without full-page reloads.
   - Each modal form includes a hidden identifier input (`<input type="hidden" th:field="*{id}" id="<entity>Id" />`).
   - Tables feature a dedicated `Actions` column with an `[ Edit ]` button on every entity row.
   - Row edit buttons carry entity attributes via HTML5 `data-*` attributes (`th:data-id`, `th:data-name`, `th:data-*`).

3. **Client-Side Modal Population & Reset**:
   - Vanilla JavaScript listens to Bootstrap's `show.bs.modal` and `hidden.bs.modal` lifecycle events.
   - When opened via `[ Edit ]` (`event.relatedTarget` has `data-id`), the script updates the modal title (`Edit <Entity>`), updates the submit button label (`Update <Entity>`), and populates form fields instantly from `data-*` attributes without making AJAX round-trips.
   - When opened via `[ Add <Entity> ]`, the script resets the form, clears the hidden ID, and sets the modal title to `Add <Entity>`.

4. **Entity Model Mutability & Spring WebDataBinder Integration (ADR 002 Compliance)**:
   - Added explicit `public void setId(Long id)` (or `setId(long id)` / `setId(UUID id)`) methods across all mutable JavaBean entities (`Book`, `Author`, `Press`, `Game`, `Publisher`, `Studio`, `Device`, `Manufacturer`, `Habit`, `HabitEntry`, `Activity`, `ActivityType`, `SportType`, `DiaryEntry`, `Gratitude`, `Mood`).
   - When an edit form submits with a non-empty `id`, Spring MVC binds the ID to the `@ModelAttribute`, allowing JPA repositories to cleanly execute an SQL `UPDATE` instead of an `INSERT`.
   - In `HabitController.submitLastDoneHabitEntries`, explicitly ensured `entry.setId(null)` to preserve distinct creation of new habit completion logs when selected by habit ID.

5. **Dedicated Contract Termination Workflow**:
   - In `contracts.html` and `ContractController.java`, provided a dedicated `[ Terminate ]` button and `POST /contracts/{id}/terminate` endpoint alongside `[ Edit ]`, ensuring contract termination logic operates safely and independently from property modifications.

### Consequences & Trade-offs
- **Benefits**:
  - Consistent, modern, and uncluttered UX across all 25+ entity domains.
  - Zero full-page reload lag during edit dialog invocation.
  - Vastly improved mobile and tablet responsiveness with full-width tables.
  - Eliminates duplicate edit templates and ad-hoc controllers.
- **Constraints**:
  - Complex entity models with relational dropdowns require passing referenced entity IDs (e.g., `author.id`, `press.id`, `category.id`) through `data-*` attributes to ensure `<select>` elements select the correct option on modal opening.

---

## ADR 025: Deprecation and Disabling of Legacy SleepTime UI Prior to Schema Removal

### Status
Accepted (v0.0.3-rc9)

### Context
With the introduction of the polymorphic session hierarchy ([ADR 011](DECISIONS.md#adr-011-sleep-tracking-unification-into-session-architecture--four-pillars-life-balance-model), [ADR 018](DECISIONS.md#adr-018-migration-of-polymorphic-session-hierarchy-to-native-postgresql-18-uuidv7)), sleep tracking was unified under `SleepSession extends Session` (`sleep_session` table) featuring sleep stages (Deep, REM, Light sleep), disruptions, quality ratings, and sleep efficiency metrics.

However, the legacy `SleepTime` UI (`/sleep-times`, `sleep-times.html`) remained active and linked from `activity-management.html`. This created dual-writing risks where new entries could be inserted into the legacy `sleeptime` table without modern session attributes, and prevented dropping the legacy table.

### Decision
In preparation for removing the `sleeptime` table and model in a future major release:
1. **Activity Management Navigation**: Re-routed the primary sleep card in [`activity-management.html`](src/main/resources/templates/activity-management.html) from `/sleep-times` to `/sessions?type=SLEEP`.
2. **Read-Only Archive View with Disabled Controls**:
   - Converted [`sleep-times.html`](src/main/resources/templates/sleep-times.html) to a read-only archive view.
   - Retained the creation form card with inputs and submit button explicitly `disabled`.
   - Added a high-visibility deprecation alert banner with a call-to-action button to the Sleep Sessions Hub.
3. **Controller Interception**:
   - In [`SleepTimeController.java`](src/main/java/de/oberamsystems/slm/controller/SleepTimeController.java), intercepted `POST /sleep-times`, blocking database persistence and redirecting with flash error message to guide users to `/sessions?type=SLEEP`.
4. **Deprecation Lifecycle Annotations**:
   - Annotated [`SleepTime.java`](src/main/java/de/oberamsystems/slm/model/SleepTime.java), [`SleepTimeRepository.java`](src/main/java/de/oberamsystems/slm/model/SleepTimeRepository.java), [`SleepTimeController.java`](src/main/java/de/oberamsystems/slm/controller/SleepTimeController.java), and [`SleepTimeRestController.java`](src/main/java/de/oberamsystems/slm/controller/SleepTimeRestController.java) with `@Deprecated(since = "0.0.3-rc9", forRemoval = true)`.
5. **Schema Preservation**: Kept database table `sleeptime` and JPA entities completely intact for zero data loss until the future major release cleanup.

### Consequences & Trade-offs
- **Benefits**: Eliminates data fragmentation and ensures all new sleep logs leverage sleep stages and Four Pillars balance metrics, while preserving historical records in read-only mode.
- **Constraints**: Legacy `sleeptime` table and Java classes remain in the repository until Phase 2 removal in a future major version.

---

## ADR 026: Consistent Session Start Time Initialization & Auto-Populated Stop End Time

### Status
Accepted (v0.0.3-rc9)

### Context
In SLM, users log cognitive, sport, wellness, and leisure activities through the polymorphic `Session` hierarchy either directly upon completion or by starting an active session (`IN_PROGRESS`, `end == null`) to stop later ([ADR 011](DECISIONS.md#adr-011-sleep-tracking-unification-into-session-architecture--four-pillars-life-balance-model), [ADR 018](DECISIONS.md#adr-018-migration-of-polymorphic-session-hierarchy-to-native-postgresql-18-uuidv7)).

Previously, start and stop time initialization suffered from discrepancies:
1. Blank session models (`new CodingSession()`, etc.) had `start == null`, leaving the `<input type="datetime-local" name="start">` empty when opening "+ Log Session" in the Unified Sessions Hub ([`sessions.html`](src/main/resources/templates/sessions.html)).
2. Standalone sub-hub endpoints hardcoded arbitrary past offsets (e.g. `viewCodingSessions` defaulted to `now().minusHours(1)`, `viewWorkingSessions` defaulted to `now().minusHours(2)`).
3. When stopping an ongoing session via the finish modal (`#stopSessionModal`), the end time input was left blank with a hint "Leave blank to stamp with current time", requiring users to either leave it empty or manually enter the current time.

### Decision
1. **Entity-Level Default Initialization**:
   - Initialized `private LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);` in [`Session.java`](src/main/java/de/oberamsystems/slm/model/Session.java). All instantiated subclasses (`CodingSession`, `WorkingSession`, `LearningSession`, `GamingSession`, `ReadingSession`, `MeditationSession`, `SportSession`, `SleepSession`, `LectureSession`) consistently inherit current-time initialization for form bindings and programmatic creation.
2. **Controller Alignment**:
   - In [`SessionController.java`](src/main/java/de/oberamsystems/slm/controller/SessionController.java), replaced past hour offsets in `viewCodingSessions` and `viewWorkingSessions` with `LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)` and `end = null`.
   - Reinforced defensive fallback in `initializeSessionStatus`: `if (session.getStart() == null) session.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));`.
3. **Frontend Client-Side Pre-Filling**:
   - In [`sessions.html`](src/main/resources/templates/sessions.html), added a standard ISO datetime helper `formatLocalDateTime(date)` (`YYYY-MM-DDTHH:mm`).
   - On `#newSessionModal` `show.bs.modal`, when in create mode (`!id`), dynamically populated `input[name="start"]` with the fresh local client time across all 9 session forms and cleared `input[name="end"]`.
   - On `#stopSessionModal` `show.bs.modal` and in `setStopModalTarget(btn)`, auto-populated `#stopModalEndTime` with `formatLocalDateTime(new Date())`, updating the helper hint to clarify that the current time is pre-filled and can be fine-tuned if stopping retroactively.

### Consequences & Trade-offs
- **Benefits**:
  - Consistent user experience: opening any session creation form or stopping an active session immediately shows the current time truncated to minutes.
  - Zero cognitive friction: stopping an active session visually confirms the exact stop timestamp before submission while still allowing adjustments.
  - Full backward compatibility: Quick Stop forms submitting with null/empty `end` continue to default to `now().truncatedTo(ChronoUnit.MINUTES)`.
- **Constraints**:
  - Hibernate entity hydration immediately overwrites the field default with database column values, so persisted records are never altered.

---

## ADR 027: Academic Activity Classification, STEM Lab Support & Clean Domain Separation

### Status
Accepted (v0.0.3-rc9)

### Context
In university modules and structured educational courses, academic workloads encompass far more than passive lecture attendance. Students prepare via flipped classroom readings, complete laboratory practicals (critical in STEM disciplines like chemistry and computer engineering), solve homework problem sets, work on group term projects, and deliver oral presentations. Furthermore, lifelong learners enroll in structured adult education (massive open online courses / MOOCs on Coursera/edX or German Volkshochschule / VHS courses).

Previously:
1. `LectureSession` was strictly modeled around passive lecture listening with sequential `lectureNumber`, without categorizing whether time was spent in a lecture, lab, homework, or preparation.
2. We evaluated two architectural directions for unifying course activities with the existing `LearningItem` / `LearningSession` domain:
   - **Approach A (Intertwined Foreign Keys)**: Adding `course_id` to `learning_item` and connecting `learning_session` directly to courses.
   - **Approach B (Clean Domain Separation)**: Preserving `LearningItem` + `LearningSession` purely for self-paced, casual, or gamified skill learning (e.g. Duolingo, LeetCode, standalone vocabulary drill), while extending `Course` + `LectureSession` to model all structured, multi-session course curricula across academic universities and lifelong learning (using dedicated semester designations like `"OTHER"`, `"MOOC"`, `"VHS"`).

### Decision
We adopted **Approach B: Clean Domain Separation**:
1. **Academic Activity Type Enum (`academic_activity_type`)**:
   - Created PostgreSQL enum `academic_activity_type` with 7 values:
     - `LECTURE`: Traditional synchronous or recorded lecture.
     - `PREPARATION`: Pre-lecture reading, flipped classroom preparation, reviewing syllabus material.
     - `LEARNING`: Self-directed study, post-lecture recap, exam revision for course topics.
     - `HOMEWORK`: Problem sets, tutorial assignments, exercise sheets.
     - `LAB`: Hands-on laboratory experiments, chemistry lab practicals, hardware/software lab benches.
     - `PROJECT`: Group or individual term project work, coding milestones.
     - `PRESENTATION`: Delivering talks, seminar defenses, poster presentations.
   - Mapped in Java via [`LectureActivityType.java`](src/main/java/de/oberamsystems/slm/model/LectureActivityType.java) with human-readable labels, Bootstrap Icons, and contextual badge styles.
2. **Entity Enhancement & Backward Compatibility**:
   - Extended [`LectureSession.java`](src/main/java/de/oberamsystems/slm/model/LectureSession.java) with `activityType = LectureActivityType.LECTURE`.
   - Added column `activity_type academic_activity_type NOT NULL DEFAULT 'LECTURE'` with index `idx_lecture_session_activity_type` in [`migration_2026-10-23_1.sql`](src/main/resources/migration_2026-10-23_1.sql) and synchronized baseline schemas ([`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), [`src/test/resources/schema-init.sql`](src/test/resources/schema-init.sql)).
3. **ECTS Workload Progress Analytics**:
   - In [`Course.java`](src/main/java/de/oberamsystems/slm/model/Course.java), added `getTargetWorkloadHours()` ($ECTS \times 30\text{h}$) and `isNonAcademicCourse()`.
   - In [`AcademicController.java`](src/main/java/de/oberamsystems/slm/controller/AcademicController.java), computed total net duration, progress percentage against target workload, and breakdown of time and counts per activity type.
4. **Direct Course Activity Logging & UI Filtering**:
   - Added `POST /courses/{courseId}/sessions` endpoint to log any activity directly from the course detail view.
   - In [`course-detail.html`](src/main/resources/templates/course-detail.html), integrated an ECTS Workload Progress card, activity breakdown badges, instant client-side activity filter pills, and `#logSessionModal`.
   - In [`sessions.html`](src/main/resources/templates/sessions.html), added activity type selection to `#form-lecture` and activity badges with icons to the table row rendering.
5. **Lifelong & Non-Academic Course Support**:
   - Supported courses without fixed academic semesters or ECTS (e.g. MOOCs on Coursera/edX or Volkshochschulkurse) using semester tags `"OTHER"`, `"MOOC"`, `"VHS"`, gracefully categorized at the bottom of descending semester views via [`SemesterComparator.java`](src/main/java/de/oberamsystems/slm/util/SemesterComparator.java).
6. **MCP Tooling**:
   - Extended `McpAcademicTools.logLectureSession` with optional `activityType` parameter.

### Consequences & Trade-offs
- **Benefits**:
  - High fidelity tracking for university degrees, flipped classroom models, and laboratory STEM disciplines.
  - Zero schema pollution: `learning_item` and `lecture_session` maintain clean domain invariants without sparse nullable foreign keys.
  - Complete ECTS progress tracking gives students immediate visibility into remaining semester effort.
- **Constraints**:
  - Unstructured/self-paced apps (e.g. Duolingo) remain logged under `LearningItem` / `LearningSession` and do not appear in course workload totals.

---

## ADR 028: Car-Related Expense Tracking, Spritmonitor Taxonomy, and Lifecycle TCO & Multi-Vehicle Comparison Analytics (€/100km)

### Status
Accepted (v0.0.3-rc10)

### Context
Vehicle tracking in SLM previously captured refueling events (`Refueling`) and vehicle specifications (`Car`), but suffered from several limitations:
1. Non-fuel operating expenses (insurance, taxes, maintenance, inspections, repairs, tolls, parking, charging base fees, etc.) could not be tracked.
2. Vehicles lacked forward-looking lifecycle parameters (`targetSellDate` and `residualValue`), making real-world Total Cost of Ownership (TCO) and €/100km calculation impossible.
3. Users could not perform head-to-head comparisons between vehicles (e.g. ICE vs. EV) across ownership horizons with interactive annual mileage adjustments.

### Decision
1. **Spritmonitor.de Expense Taxonomy (`car_expense_type`)**:
   - Implemented all 23 official Spritmonitor.de categories as a PostgreSQL enum `car_expense_type` and Java enum `CarExpenseType` using clean, idiomatic English identifiers:
     `MAINTENANCE`, `REPAIR`, `TIRE_CHANGE`, `OIL_CHANGE`, `INSURANCE`, `TAX`, `INSPECTION`, `TUNING`, `ACCESSORIES`, `PURCHASE_PRICE`, `OTHER`, `CARE`, `LEASING_RATE`, `REGISTRATION`, `FINANCING_RATE`, `REFUND`, `FINE`, `PARKING_FEE`, `TOLL`, `SPARE_PARTS`, `CHARGING_BASE_FEE`, `DIGITAL_SERVICES`, `GHG_QUOTA`.
   - Categories `REFUND` (16, Erstattung) and `GHG_QUOTA` (23, THG-Prämie) are modeled with `isCredit() = true`, allowing positive amount inputs while automatically reducing net TCO in financial calculations.
2. **Database Schema & Migration**:
   - Added `target_sell_date DATE` and `residual_value NUMERIC(10, 2)` to `car`.
   - Created table `car_expense` with native UUIDv7 primary key, foreign key to `car`, `expense_type`, `expense_date`, `odometer`, `amount`, `title`, and `note`.
   - Synchronized baseline schemas (`tables.sql`, `enums.sql`, `init.sql`, `schema-init.sql`) and authored transactional migration `migration_2026-10-23_2.sql`.
3. **Triple-Tab Vehicle Expenses Hub (`/car-expenses`)**:
   - **Tab 1 (Expenses Log)**: Complete expense history with vehicle filter, category badges, "+ Add Expense" modal, and deletion.
   - **Tab 2 (Expense Analytics & €/100km)**: Computes realized and projected TCO ($\text{Depreciation} + \text{Fuel} + \text{Operating Expenses} - \text{Credits}$), price per 100 km (€/100km), component breakdowns, Billboard.js donut chart ("TCO torus"), and an inline form to adjust lifecycle parameters directly. Fixed initial render dimensions by hooking into Bootstrap `shown.bs.tab` events with explicit SVG bounding constraints.
   - **Tab 3 (Vehicle Comparison)**: Enables side-by-side multi-year cost trajectory comparison between two vehicles (Car A vs Car B) via a Billboard.js line chart ($X = \text{Years } 1..10, Y = \text{Total Cost } €$). Integrates an interactive annual mileage slider styled with **noUiSlider** (default 10,000 km) that dynamically recalculates trajectories on the client in real-time. Detects break-even year when curves intersect.
   - **Decoupled Fuel & Energy Economics**:
     - Fuel type (`ELECTRIC`, `DIESEL`, `SUPER_E10`, etc.) is inferred **strictly from recorded refuelings** (`refueling.getFuelType()`), never guessed by car or model names.
     - Decouples unit economics: $\text{Fuel Cost per km} = \frac{\text{Weighted Avg Consumption}}{100} \times \text{Weighted Avg Price per Unit}$ ($€/L$ or $€/kWh$, $L/100km$ or $kWh/100km$).
     - Renders a dedicated side-by-side fuel efficiency comparison card dynamically syncing annual fuel spend to the distance slider.
4. **Security & MCP Integration**:
   - Whitelisted `/car-expenses` and `/car-expenses/**` in `SecurityConfig`.
   - Added MCP tools in `McpVehicleTools`: `getCarExpenses`, `logCarExpense`, and `updateVehicleLifecycleParameters`.

### Consequences & Trade-offs
- **Benefits**:
  - Full transparency over total mobility costs and realistic cost per kilometer.
  - Accurately captures dramatic operational cost differences between electric vehicles and combustion engines.
  - Interactive comparison tool enables data-driven vehicle acquisition decisions (e.g. evaluating whether lower running costs of an EV offset a higher purchase price).
  - Native integration with standard Spritmonitor categories ensures future CSV import compatibility.
- **Constraints**:
  - Requires estimating `residualValue` and `targetSellDate` for prospective vehicles to yield accurate projected trajectories.

---

## ADR 029: Universal PostgreSQL TIMESTAMPTZ Migration & Daylight Saving Time (Europe/Berlin) Integrity

### Status
Accepted (v0.0.3-rc12)

### Context
SLM previously used unqualified SQL `TIMESTAMP` (`timestamp without time zone`) across 16 tables and 25 date-time columns. Unqualified timestamps present critical architectural flaws in temporal arithmetic, cross-system integrations, and seasonal transitions:
1. **Daylight Saving Time (DST) Distortion**: In `Europe/Berlin` (CET/CEST), clocks spring forward 1 hour in March (02:00 -> 03:00) and fall back 1 hour in October (03:00 -> 02:00). Unqualified `TIMESTAMP` differences (`endtime - starttime`) perform purely arithmetic wall-clock subtraction, ignoring DST jumps. A sleep or work session spanning the spring transition from 23:00 to 07:00 was incorrectly computed as 8 hours instead of the physical 7 hours, while the fall transition was computed as 8 hours instead of 9 hours.
2. **PostgreSQL Generated Columns**: Columns such as `duration`, `net_duration`, `travel_duration`, `complete_duration`, and `sleeptime` are computed at the database level using `GENERATED ALWAYS AS (endtime - starttime) STORED`. With `TIMESTAMP`, generated intervals reflected distorted naive differences.
3. **Hibernate & Multi-Tier Timezone Ambiguity**: Without explicit timezone binding, JDBC drivers interpret timestamps according to host OS timezone, creating drift across Docker containers, developer environments, and remote databases.

### Decision
1. **Universal Migration to `TIMESTAMPTZ`**:
   - Migrated all 16 tables and 25 date-time columns to `TIMESTAMPTZ` (`TIMESTAMP WITH TIME ZONE`) in [`tables.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/tables.sql), [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql), and [`schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql).
   - Tables affected: `session`, `appointment`, `calendar`, `train_trip`, `train_trip2`, `activity`, `sleeptime`, `car_trip`, `learning_item`, `habit_entry`, `course`, `lecture_note`, `refueling`, `station`, `charging_point`, and `car_expense`.
   - Updated trigger functions (e.g. `check_sleep_session_duration()`) to declare `TIMESTAMPTZ` local variables.
2. **Daylight Saving Time & Europe/Berlin Default**:
   - Enforced `Europe/Berlin` as authoritative application and database default time zone:
     - Spring configuration: `spring.jpa.properties.hibernate.jdbc.time_zone=Europe/Berlin` in `application.properties`.
     - Application initialization: `TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))` in `SlmApplication.java`.
     - Container environments: `TZ=Europe/Berlin` and `PGTZ=Europe/Berlin` on both `app` and `db` services in `docker-compose.yml`.
3. **Atomic Transactional Migration Script (`migration_2026-10-24_2.sql`)**:
   - Wrapped in atomic `BEGIN; ... COMMIT;`.
   - Safely handles PostgreSQL's restriction on altering column types used in generated column expressions by dropping dependent generated columns (`duration`, `net_duration`, `travel_duration`, `complete_duration`, `sleeptime`), altering all timestamp columns using `USING col AT TIME ZONE 'Europe/Berlin'`, and restoring generated columns.
4. **Entity Model & Java Compatibility**:
   - Maintained `LocalDateTime` on Java entity models while adding `@Column(columnDefinition = "TIMESTAMPTZ")` annotations.
   - Retaining `LocalDateTime` guarantees zero friction with HTML5 `<input type="datetime-local">`, Spring MVC `@DateTimeFormat`, and Thymeleaf forms while Hibernate's `hibernate.jdbc.time_zone` delegates timezone normalization to PostgreSQL.
5. **End-to-End Precision Verification**:
   - Created comprehensive integration test suite [`DateTimePrecisionIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DateTimePrecisionIT.java) verifying:
     - Spring DST cutover (+1h clock jump) yields exact physical 7h duration for an overnight session.
     - Fall DST cutover (-1h clock jump) yields exact physical 9h duration for an overnight session.
     - Generated column durations on `session`, `sleeptime`, and `appointment` reflect UTC-based physical duration.
     - Exact wall-clock `LocalDateTime` preservation upon round-trip persistence and cache eviction.

### Consequences & Trade-offs
- **Benefits**:
  - Physically accurate time and duration calculations guaranteed at the database level across all DST switches.
  - Consistent timezone handling across host, Docker, JVM, and database layers.
  - Zero disruption to existing web controllers and UI templates.
- **Constraints**:
  - Legacy PostgreSQL databases must apply `migration_2026-10-24_2.sql` inside a transaction.

---

## ADR 030: Complete Migration to OffsetDateTime and Dynamic ZoneId Resolution for TIMESTAMPTZ Integration

### Status
Accepted (v0.0.3-rc14)

### Context
In [ADR 029](#adr-029-universal-postgresql-timestamptz-migration--daylight-saving-time-europeberlin-integrity), the database schema was fully migrated from unqualified `TIMESTAMP` to `TIMESTAMPTZ` (`TIMESTAMP WITH TIME ZONE`). However, the Java domain model initially retained `java.time.LocalDateTime`. This hybrid state introduced several technical and architectural challenges:
1. **Type Impedance Mismatch & DTO Projection Failures**:
   - PostgreSQL `TIMESTAMPTZ` is specified in JDBC 4.2 to map natively to `java.time.OffsetDateTime` (or `java.time.Instant`), not `LocalDateTime`.
   - Native SQL queries, window functions, and Hibernate 7 `NativeQueryConstructorTransformer` projected database timestamps as JDBC `Instant` / `OffsetDateTime`, triggering runtime `ClassCastException` (`Cannot cast java.time.Instant to java.time.LocalDateTime`, as encountered on `/last-done-habit-entries`).
2. **Physical Time & DST Calculation Precision**:
   - `LocalDateTime` calculates pure wall-clock differences, ignoring daylight saving transitions. In `Europe/Berlin`, calculating `Duration.between` over the spring clock jump resulted in an inexact 8h instead of the physical 7h.
   - During the fall cutover (03:00 -> 02:00), `LocalDateTime` cannot distinguish whether an event took place in `+02:00` or `+01:00`.
3. **API & Spring AI MCP Timezone Ambiguity**:
   - Exposing `LocalDateTime` over REST APIs or Spring AI Model Context Protocol (MCP) tools transmitted naive ISO strings (e.g. `2026-10-24T14:30:00`), leaving consumers and LLM agents to speculate on the active timezone offset.
4. **Hardcoded Timezone Literal Anti-Pattern**:
   - Hardcoding `"Europe/Berlin"` string literals in business logic violated maintainability and configurable deployment principles.

### Decision
We adopt **Option A: Full Domain Migration to `OffsetDateTime` paired with dynamic `ZoneId` resolution via `TimeZoneConfig`**:

1. **Comprehensive Model & Repository Migration**:
   - Migrated all timestamp fields across all 16 domain entities and DTOs from `LocalDateTime` to `java.time.OffsetDateTime`:
     - [`Session.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java) (`start`, `end`, `stop()`)
     - [`Appointment.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Appointment.java) (`departure`, `arrival`, `start`, `end`, `arrivalHome`)
     - [`CalendarEvent.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CalendarEvent.java) (`start`, `end`)
     - [`TrainTrip.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip.java), [`TrainTrip2.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip2.java) (`departure`, `arrival`)
     - [`Activity.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Activity.java) (`start`, `end`)
     - [`SleepTime.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepTime.java), [`SleepSession.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepSession.java) (`start`, `end`)
     - [`CarTrip.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarTrip.java) (`departure`, `arrival`)
     - [`LearningItem.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LearningItem.java) (`lastPracticed`)
     - [`HabitEntry.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/HabitEntry.java), [`LastDoneHabitEntry.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LastDoneHabitEntry.java) (`lastDone`)
     - [`Course.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Course.java) (`examDate`), [`LectureNote.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LectureNote.java) (`noteDate`)
     - [`Refueling.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Refueling.java), [`Station.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Station.java), [`ChargingPoint.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/ChargingPoint.java) (`refuelingDate`, `firstUsed`, `lastUsed`)
     - [`CarExpense.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarExpense.java) (`expenseDate`)
   - Updated all repository query methods, derived queries, and `@Param` parameters across [`SessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SessionRepository.java), [`CalendarEventRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CalendarEventRepository.java), [`CourseRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CourseRepository.java), [`TrainTripRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTripRepository.java), [`TrainTrip2Repository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip2Repository.java), [`ActivityRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/ActivityRepository.java), [`AppointmentRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/AppointmentRepository.java), [`SportSessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SportSessionRepository.java), [`SleepTimeRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepTimeRepository.java), [`SleepSessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepSessionRepository.java), and [`MeditationSessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/MeditationSessionRepository.java).
   - Maintained `LocalDate` on pure dates without time components (`Human.birthday`, `Contract.startDate`, `Contract.endDate`, `Car.purchaseDate`, `Car.targetSellDate`).

2. **HTML5 `<input type="datetime-local">` Transparent Converter Layer**:
   - HTML5 datetime-local inputs submit strings in `yyyy-MM-dd'T'HH:mm` format without timezone offsets.
   - Implemented bidirectional Spring MVC converters:
     - [`StringToOffsetDateTimeConverter.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/converter/StringToOffsetDateTimeConverter.java): Parses standard ISO-8601 offset strings directly, and falls back to parsing local datetime strings by resolving the active application offset via `TimeZoneConfig.getZoneId()`.
     - [`OffsetDateTimeToStringConverter.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/converter/OffsetDateTimeToStringConverter.java): Formats `OffsetDateTime` into `yyyy-MM-dd'T'HH:mm` in the configured timezone for pre-populating HTML5 datetime-local form inputs.
     - Registered in [`ConverterConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/ConverterConfig.java) without redundant `@Component` annotations to prevent `BeanDefinitionOverrideException`.

3. **Dynamic Timezone Resolution without Hardcoded Literals**:
   - Centralized timezone binding in [`TimeZoneConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/config/TimeZoneConfig.java) reading `@Value("${spring.jpa.properties.hibernate.jdbc.time_zone:Europe/Berlin}")`.
   - All `now()` invocations across controllers, services, schedulers, and tests strictly utilize `OffsetDateTime.now(TimeZoneConfig.getZoneId())`.
   - Zero hardcoded `"Europe/Berlin"` strings exist in business logic.

4. **Spring AI Model Context Protocol (MCP) & REST Alignment**:
   - Updated all MCP tools across [`McpAcademicTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java), [`McpMedicalTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpMedicalTools.java), [`McpSessionTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java), [`McpVehicleTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpVehicleTools.java), and [`McpWellnessTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpWellnessTools.java) to accept and return `OffsetDateTime`.
   - Produces unambiguous ISO-8601 strings with offsets (e.g. `2026-10-24T14:30:00+02:00`), ensuring LLM clients and third-party integrations interpret exact physical moments without locale guesswork.

5. **Utilities & Serialization**:
   - Modernized [`Utils.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/utils/Utils.java) with `OffsetDateTimeToString(OffsetDateTime)` and an overloaded `LocalDateTimeToString(OffsetDateTime)` bridge.
   - Updated [`SpritmonitorCsvService.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/service/SpritmonitorCsvService.java) to parse German date strings directly to `OffsetDateTime` via `TimeZoneConfig.getZoneId()`.

### Consequences & Trade-offs
- **Benefits**:
  - **100% JDBC 4.2 & Hibernate Specification Harmony**: Native SQL queries and projection transformers seamlessly map timestamps without custom object casting or reflection hacks.
  - **Physical Duration Accuracy**: `Duration.between` calculates exact epoch seconds, guaranteeing accurate sleep, work, and exercise metrics across seasonal clock changes.
  - **Full Temporal Order Preservation**: Disambiguates timestamps occurring during the ambiguous 02:00-03:00 fall fallback hour.
  - **API Standardization**: Clean ISO-8601 output across MCP tools and REST endpoints.
- **Constraints**:
  - HTML5 datetime-local form inputs do not supply timezone offsets; the application must consistently supply the configured `ZoneId` through global converters.







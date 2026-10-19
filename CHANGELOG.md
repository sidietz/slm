# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.2j] - 2026-10-19

### Changed
- Migrated **Polymorphic Session Hierarchy to Native PostgreSQL 18 UUIDv7** (ADR 018):
  - Migrated root [`Session`](src/main/java/de/oberamsystems/slm/model/Session.java) and all 9 joined subclasses ([`CodingSession`](src/main/java/de/oberamsystems/slm/model/CodingSession.java), [`WorkingSession`](src/main/java/de/oberamsystems/slm/model/WorkingSession.java), [`LearningSession`](src/main/java/de/oberamsystems/slm/model/LearningSession.java), [`ReadingSession`](src/main/java/de/oberamsystems/slm/model/ReadingSession.java), [`GamingSession`](src/main/java/de/oberamsystems/slm/model/GamingSession.java), [`SportSession`](src/main/java/de/oberamsystems/slm/model/SportSession.java), [`MeditationSession`](src/main/java/de/oberamsystems/slm/model/MeditationSession.java), [`SleepSession`](src/main/java/de/oberamsystems/slm/model/SleepSession.java), [`LectureSession`](src/main/java/de/oberamsystems/slm/model/LectureSession.java)) from `String` / `TEXT` to native `java.util.UUID` / PostgreSQL `UUID`.
  - Configured Hibernate 7 `@GeneratedValue @UuidGenerator(style = Style.VERSION_7)` on `Session.id`, backed by PostgreSQL 18 `DEFAULT uuidv7()`.
  - Retained Java 27 `UUID.ofEpochMillis(...)` in `@PrePersist` to ensure version 7 ID generation in standalone unit tests without active persistence context.
  - Migrated foreign key reference [`LectureNote.lectureSession`](src/main/java/de/oberamsystems/slm/model/LectureNote.java) (`lecture_note.lecture_session_id`) from `TEXT` to native `UUID`.
  - Updated all 10 session repositories to `JpaRepository<T, UUID>` and updated [`LectureNoteRepository`](src/main/java/de/oberamsystems/slm/model/LectureNoteRepository.java) to define `findByLectureSessionId(UUID lectureSessionId)`.
  - Updated [`SessionController`](src/main/java/de/oberamsystems/slm/controller/SessionController.java) `/sessions/delete/{id}` endpoint to take `@PathVariable UUID id`.
  - Updated [`AcademicController`](src/main/java/de/oberamsystems/slm/controller/AcademicController.java) `/courses/{courseId}/notes/new` and `/notes` endpoints to bind `@RequestParam(required = false) UUID sessionId`.
  - Updated Spring AI MCP Server session tools ([`McpSessionTools`](src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java), [`McpAcademicTools`](src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java)) to handle and return native UUIDs.
  - Created non-destructive, atomic transactional migration script [`src/main/resources/migration_2026-10-19_1.sql`](src/main/resources/migration_2026-10-19_1.sql) (`BEGIN;` ... `COMMIT;`) to safely drop foreign keys, alter columns with `USING id::uuid`, set default to `uuidv7()`, and restore constraints with full rollback safety.
  - Synchronized authoritative baseline schemas [`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), and [`schema-init.sql`](src/test/resources/schema-init.sql).

### Fixed
- Resolved Thymeleaf premature HTTP response commitment during deferred CSRF token evaluation:
  - Configured `spring.thymeleaf.servlet.produce-partial-output-while-processing=false` in [`application.properties`](src/main/resources/application.properties), ensuring the entire template document is rendered in memory before flushing to the servlet response stream.
  - Removed temporary `OncePerRequestFilter` from [`SecurityConfig.java`](src/main/java/de/oberamsystems/slm/SecurityConfig.java), preserving the clean Spring Security filter chain and eliminating `IllegalStateException: Cannot create a session after the response has been committed` across all large table views.

## [0.0.2i] - 2026-10-19

### Added
- Implemented **University Lectures, Academic Tracking & Native UUIDv7 Markdown Lecture Notes** (ADR 017):
  - Created first-class [`Course`](src/main/java/de/oberamsystems/slm/model/Course.java) entity modeling academic modules, semester, ECTS credits, lecturer association ([`Human`](src/main/java/de/oberamsystems/slm/model/Human.java) with `category = 'LECTURER'`), default room, syllabus, and exam schedule (`LocalDateTime examDate` and exam location).
  - Extended polymorphic session hierarchy with joined inheritance [`LectureSession extends Session`](src/main/java/de/oberamsystems/slm/model/LectureSession.java) (`lecture_session` table) under the **Productivity** pillar, contributing to Net Focused Time.
  - Created first-class [`LectureNote`](src/main/java/de/oberamsystems/slm/model/LectureNote.java) entity using native **UUIDv7** (`java.util.UUID` with Hibernate 7 `@UuidGenerator(style = Style.VERSION_7)` and PostgreSQL 18 `DEFAULT uuidv7()`).
  - Added split-screen Markdown editor with real-time preview via `marked.js`, snippet toolbar (formulas, code, tables, callouts), key concepts tagging, homework action items, and 1–5 exam relevance ratings.
  - Implemented [`AcademicController`](src/main/java/de/oberamsystems/slm/controller/AcademicController.java) managing course catalogs, course details, notes management, and global keyword search.
  - Integrated lecture sessions and academic navigation into Sessions Hub ([`sessions.html`](src/main/resources/templates/sessions.html)) and Home Dashboard ([`index.html`](src/main/resources/templates/index.html)).
  - Added Spring AI MCP Academic Tool Suite ([`McpAcademicTools`](src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java)) exposing `getCourses`, `getCourseDetails`, `logLectureSession`, `saveLectureNote`, and `searchLectureNotes`.
  - Added comprehensive Testcontainers integration test [`AcademicIT`](src/test/java/de/oberamsystems/slm/AcademicIT.java) verifying course persistence, lecturer linking, polymorphic session duration calculation, UUIDv7 generation, and MCP tool interactions.
  - Added `testAddLecturerInHumansSection` in [`HumanEventIT`](src/test/java/de/oberamsystems/slm/HumanEventIT.java) verifying contact directory lecturer registration and category querying.
  - Created legacy migration script [`migration_2026-10-18_6.sql`](src/main/resources/migration_2026-10-18_6.sql) and synchronized authoritative baselines [`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), and [`schema-init.sql`](src/test/resources/schema-init.sql).

### Security
- Configured public access in [`SecurityConfig.java`](src/main/java/de/oberamsystems/slm/SecurityConfig.java) across all academic routes:
  - `/courses`, `/courses/**`, `/course`, `/course/**`, `/courses.html`, `/course.html`, `/course-detail`, `/course-detail.html`.
  - `/notes`, `/notes/**`, `/note`, `/note/**`, `/lecture-notes`, `/lecture-notes/**`, `/lecture-note`, `/lecture-note/**`.
  - `/lecture-sessions`, `/lecture-sessions/**`, `/lecture-session`, `/lecture-session/**`, and `/academics/**`.
  - Fixed primitive `long` binding in Thymeleaf lecturer selection avoiding `ConversionFailedException` during unauthorized redirect evaluations.
  - Added Spring Security integration tests in [`AcademicIT.java`](src/test/java/de/oberamsystems/slm/AcademicIT.java) verifying public accessibility of all academic endpoints.

## [0.0.2h] - 2026-10-18

### Added
- Integrated **Spring AI Model Context Protocol (MCP) Server** (ADR 016):
  - Added `spring-ai-starter-mcp-server-webmvc` and `spring-ai-bom:2.0.1` compatible with SLM's Jackson 3 BOM platform.
  - Configured WebMVC Server-Sent Events (SSE) transport using standard default endpoints (`GET /sse` for event streaming and `POST /mcp/message` for JSON-RPC message dispatching).
  - Implemented [`McpApiKeyAuthFilter`](src/main/java/de/oberamsystems/slm/security/McpApiKeyAuthFilter.java) enforcing API key authorization (`X-API-KEY`, query parameter `?apiKey=`, or Bearer token) on `/mcp/**` and `/sse`.
  - Implemented 4 cohesive tool groups under `de.oberamsystems.slm.mcp`:
    - [`McpLifeEventTools`](src/main/java/de/oberamsystems/slm/mcp/McpLifeEventTools.java): query upcoming events, query contact events, and schedule new life events.
    - [`McpSessionTools`](src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java): calculate Four Pillars daily balance (productivity, sport, wellness, leisure hours), fetch recent sessions, and log coding or sleep sessions.
    - [`McpWellnessTools`](src/main/java/de/oberamsystems/slm/mcp/McpWellnessTools.java): habit status overview, mark habit completed, log daily mood, and record gratitude notes.
    - [`McpMedicalTools`](src/main/java/de/oberamsystems/slm/mcp/McpMedicalTools.java): query upcoming medical appointments, search doctors by name/specialty, and schedule doctor appointments.
  - Added comprehensive Testcontainers integration suite [`McpServerIT`](src/test/java/de/oberamsystems/slm/McpServerIT.java) verifying API key rejection/acceptance and live tool execution against PostGIS 18.

### Security
- Configured CSRF exclusion for `/mcp/**` and `/sse` in [`SecurityConfig.java`](src/main/java/de/oberamsystems/slm/SecurityConfig.java) with pre-authentication enforcement via `McpApiKeyAuthFilter`.

## [0.0.2g] - 2026-10-18

### Added
- Implemented the **Life Events, Anniversaries & Memorials System** (ADR 015):
  - Created first-class `HumanEvent` entity and `EventType` enum supporting `BIRTHDAY`, `MEMORIAL`, `WEDDING_ANNIVERSARY`, `RELATIONSHIP_ANNIVERSARY`, `WORK_ANNIVERSARY`, and `CUSTOM`.
  - Added support for couple/partner anniversaries linking primary and optional secondary contacts (`secondaryHuman`).
  - Added `death_date` tracking on `Human` with derived `deceased` and `ageAtDeath` formulas.
  - Implemented SQL helper functions `next_annual_occurrence(date)` and `days_until_annual_occurrence(date)` supporting leap years and year-rollover countdowns.
  - Added dedicated **Events & Anniversaries Hub** at `/human-events` (`human-events.html`) with category filter pills, timeline countdown badges, milestone counters, and event creation modal.
  - Enhanced Contact Directory (`/humans`) with deceased indicators, death date recording, and direct links to contact events.
  - Enhanced Home Dashboard (`/`) with upcoming milestone notifications and direct navigation to events.
  - Added `src/main/resources/migration_2026-10-18_5.sql` with automatic migration of existing `human.birthday` entries into initial `BIRTHDAY` events.
  - Added unit test suite `HumanEventControllerTest` and Testcontainers integration test `HumanEventIT`.
- Added `src/main/resources/migration_2026-10-18_4.sql` providing the DST-immune, leap-year safe `this_years_birthday(date)` helper function and geodesic air distance calculation triggers for legacy databases.
- Added Section 6 "Database Migrations & Legacy Upgrades" to [`AGENTS.md`](AGENTS.md) codifying `tables.sql` authority and migration lifecycle protocols.

### Changed
- Synchronized authoritative baseline schema files across [`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), and [`schema-init.sql`](src/test/resources/schema-init.sql):
  - Fixed `this_years_birthday(date)` helper function using `make_date` to prevent 1-day shifts caused by timestamp arithmetic across Daylight Saving Time (DST) clock boundaries and safely handling February 29 leap days.
  - Added `CREATE EXTENSION IF NOT EXISTS postgis;` and geodesic air distance calculation triggers to [`tables.sql`](src/main/resources/tables.sql).
  - Deduplicated `platform` table definition in [`tables.sql`](src/main/resources/tables.sql).
  - Added `category human_category` column to `human` table across all baseline schemas.
  - Adjusted [`SecurityConfig.java`](src/main/java/de/oberamsystems/slm/SecurityConfig.java) to permit public access to `/human-events`, `/human-events/**`, `/events`, and `/events/**`.
  - Added Spring Security filter chain integration test in [`HumanEventIT.java`](src/test/java/de/oberamsystems/slm/HumanEventIT.java) verifying authorization rules for human-events endpoints.

## [0.0.2f] - 2026-10-18

### Added
- Converted `SleepTime` into joined inheritance `SleepSession extends Session` (`sleep_session` table) under the unified Sessions architecture.
- Added four pillars category balance model across Sessions Hub (`/sessions`):
  - **Productivity**: Coding, Working, Learning, Reading sessions.
  - **Sport**: Dedicated separate category for fitness, cardio, and workouts.
  - **Wellness**: Restorative practices including Meditation and Sleep sessions.
  - **Leisure**: Recreational gaming sessions.
- Added 4 interactive category balance metric cards with direct category filtering to `/sessions`.
- Added `SLEEP` session filter pill, `Sleep Chart` quick navigation, and full sleep logging form modal (bedtime, wake up, sleep type, deep sleep, REM sleep, awake count, break/awake duration, sleep quality rating, and dream notes).
- Preserved full backward compatibility for `/sleep-times`, `/sleeptime-bb`, and `/api/sleeptimes` using transient compatibility accessors (`gotobed`, `getup`, `sleeptime`).
- Added end-to-end integration tests in `SleepTimeIT` verifying `SleepSession` persistence, polymorphic session queries, and endpoint rendering against live PostgreSQL 18 Testcontainers.
- Upgraded Jackson to Jackson 3 BOM platform (`tools.jackson:jackson-bom:3.2.3`) with `jackson-annotations:2.22`.

### Changed
- Overhauled and updated [`DECISIONS.md`](DECISIONS.md) into a comprehensive Architecture Decision Record suite containing 14 formalized ADRs (ADR 001 through ADR 014) synthesized from git history, architectural evaluations, and [`doc/plans/`](doc/plans/).
- Fixed `docker-compose.yml` database service to use `postgis/postgis:18-3.6` image instead of standard `postgres`, ensuring PostGIS 3.6 spatial support for routes and doctor distances.
- Added container healthcheck to `db` service (`pg_isready`) and configured `app` to wait for healthy database readiness before launching.
- Updated root `init.sql` database initialization script to include `CREATE EXTENSION IF NOT EXISTS postgis;`, `this_years_birthday()` helper function, modern session hierarchy tables, and geospatial distance calculation triggers.
- Updated `Dockerfile` runtime base image to `eclipse-temurin:27-jre` (matching Java 27) and modernized JAR artifact handling.
- Migrated test framework to **JUnit 6** (`org.junit:junit-bom:6.1.3`), enforcing the JUnit 6 platform across all Jupiter and Platform testing dependencies and integrating with Spring Boot 4 and Testcontainers.
- Decoupled Wellness (Meditation, Sleep), Sport, and Leisure sessions from **Net Focused Time** and **Avg Focus Rating** on the Sessions Hub (`/sessions`).
- Net Focused Time now strictly aggregates intentional cognitive productivity sessions (`CodingSession`, `WorkingSession`, `LearningSession`, `ReadingSession`), preventing biological sleep hours from inflating deep focus statistics.
- Added adaptive KPI header titles across session categories (`kpiDurationTitle`, `kpiRatingTitle`, and disruption/break labels):
  - **All / Productivity**: "Net Focused Time" (`(Cognitive focus hours)`), "Avg Focus Rating", and "Total Life: Xh Ym".
  - **Sport**: "Active Training Time", "Avg Workout Intensity".
  - **Wellness / Sleep**: "Rest & Recovery Time" / "Sleep Duration", "Awake / Disruptions", "Avg Sleep Quality" / "Avg Restfulness Rating".
  - **Leisure**: "Recreation Time", "Avg Game Rating".

## [0.0.2e] - 2026-10-18

### Added
- Spring Boot Testcontainers integration test framework running PostgreSQL 18 with PostGIS 3.6 (`postgis/postgis:18-3.6`) using a shared singleton container pattern (`AbstractIntegrationTest`) and `@ServiceConnection`.
- Comprehensive integration test suite ending in `IT` (`RouteSpatialIT`, `DoctorSpatialIT`, `SessionHierarchyIT`, `EmployerContractIT`, `SelfieControllerIT`) testing real PostGIS spatial calculations, database triggers, JPA joined inheritance, and MockMvc workflows.
- Authoritative test schema initialization in `src/test/resources/schema-init.sql` consolidating `enums.sql`, `tables.sql`, and spatial triggers directly for fresh installations without legacy migration scripts.
- Test dependencies: `spring-boot-testcontainers`, `testcontainers:postgresql`, and `testcontainers:junit-jupiter`.

### Changed
- Configured Gradle `test` task to execute both unit tests (`*Test.class`) and integration tests (`*IT.class`).

## [0.0.2d] - 2026-10-18

### Added
- Dedicated `SelfieStorageService` providing path traversal defense (CWE-22) via strict canonical path boundary checks (`targetPath.startsWith(baseDir)`), regex-based extension validation, and raw byte storage integrity.
- Direct native support for `.heic` and `.jpg` (and `.jpeg`) photo uploads, preserving original files without conversion or transcoding.
- Comprehensive security test suite in `SelfieStorageServiceTest` verifying rejection of directory traversal attempts (`../../`, `..\..\`, `/etc/passwd`), invalid extensions, and format change replacement cleanup.

### Changed
- Refactored `SelfieController` to delegate file storage, validation, and error handling to `SelfieStorageService`.
- Updated `selfies.html` upload form input accept attribute and label to explicitly allow and support `.jpg` and `.heic` files without conversion.

## [0.0.2c] - 2026-10-18

### Added
- Natural alphanumeric `TrainLineComparator` utility to order rail transit lines by natural line numbers (e.g. `RE1`, `RE28`, `RE90`, `S1`, `S4`).
- Unit tests in `TrainLineComparatorTest` verifying natural alphanumeric sorting behavior.

### Changed
- Standardized all date/time log and history tables to sort by date descending (`DESC`, most recent first) across Train Trips, Car Trips, Appointments, Sessions (Sessions Hub, Coding, Working, Gaming, Reading, Meditation, Sport, Learning), Sleep Times, Contracts, Purchases, Diary Entries, Selfies, Moods, Gratitudes, Habit Entries, Activities, and Calendar Events.
- Standardized all `<select>` dropdown menus to sort options alphabetically ascending (`ASC`) across Doctors, Specialities, Practice & Base Points, Routes, Cars, Employers, Games, Devices, Books, Authors, Presses, Publishers, Studios, Vendors, Manufacturers, Sport Types, and Activity Types.
- Sorted Train Lines in both tables and dropdowns by natural alphanumeric line order (`RE1, RE28, RE90, S1, S4`).
- Sorted catalog and directory tables without dates alphabetically ascending (`ASC`) by display title or name (Doctors, Specialities, Routes, Cars, Employers, Contractors, Habits, Activity Types, Books, Games, Publishers, Studios, Vendors, Manufacturers).
- Cleaned up fully-qualified package and class hierarchies in favor of top-level imports across controllers (`LearningController`, `GameController`, `BookController`, `SessionController`) and test suites (`SessionControllerTest`, `ContractControllerTest`, `EmployerControllerTest`, `RouteControllerTest`).

## [0.0.2b] - 2026-10-18

### Added
- Integrated `Doctor` entity with `Point` (`point_id`) and home base reference (`base_point_id`, `is_home` flag on `point`).
- Automated geodesic air distance calculation for doctors using PostGIS `ST_Distance` on WGS 84 ellipsoid via database triggers and `DoctorRepository.calculateAirDistance()`.
- Versioned database migration file `src/main/resources/migration_2026-10-18_2.sql` adding `is_home` to `point`, linking existing doctors to clinic points, and adding triggers.
- PostGIS spatial integration with WGS 84 (`SRID 4326`) `geography(Point, 4326)` generated column and GIST index on `point`.
- Automatic geodesic air distance calculation for routes using PostGIS `ST_Distance` on WGS 84 ellipsoid via database triggers and `RouteRepository.calculateAirDistance()`.
- Versioned database migration file `src/main/resources/migration_2026-10-18.sql` parsing DMS/decimal strings to numeric degrees and generating spatial geography columns.

### Changed
- Converted `Doctor.airDistance` from primitive `float` to `Double`.
- Removed manual `airDistance` required number input from `doctors.html`, replacing it with practice location point selection and PostGIS auto-calculation badge.
- Added doctor air distance formatting in `appointments.html` doctor selection dropdown.
- Converted `Point` entity `latitude` and `longitude` fields from `String` to `Double` with standard numeric degree inputs.
- Removed manual `airDistance` required user input from `routes.html`, automatically calculating geodesic distances upon route creation or point coordinate updates.
- Standardized Duration formatting to `HH:mm`, removing seconds from `DurationConverter` across all views (`sessions.html`, `appointments.html`, `car-trips.html`, etc.).
- Fixed raw `PT...` ISO strings (e.g. `PT6H`) in category views (`gaming-sessions.html`, `working-sessions.html`, `coding-sessions.html`, `reading-sessions.html`, and `learning-sessions.html`) by routing net and break durations through Spring's ConversionService via double curly braces (`${{...}}`).

## [0.0.2a] - 2026-10-18

### Added
- Dedicated `Employer` entity, repository, and table (`employer`) for professional working sessions and client billing.
- Versioned database migration file `src/main/resources/migration_2026-10-17.sql` migrating `working_session.contractor_id` to `employer_id`.
- `EmployerController` and `employers.html` directory and management view.
- Added **Employers & Clients** action button to the Spend Management dashboard (`/spend-management`).

### Changed
- Standardized UI timestamp precision to minutes (`HH:mm`), removing second-level precision (`HH:mm:ss`) across form inputs and tables to align with Sessions.
- Updated `LocalDateTimeConverter` and `LocalDateTimeConverter2` formatting to `yyyy-MM-dd'T'HH:mm` for clean HTML5 `datetime-local` input bindings without activating seconds pickers.
- Removed `step="1"` attribute from `car-trips.html` inputs and aligned table timestamp formats to `dd.MM.yyyy HH:mm`.
- Formatted `gotobed` and `getup` fields in `sleeptime-bb.html` with `#temporals.format(..., 'dd.MM.yyyy HH:mm')`.
- Truncated initial controller `LocalDateTime.now()` defaults to minutes (`ChronoUnit.MINUTES`) across all entity logging forms.
- Refactored `WorkingSession` to reference `Employer employer` (`employer_id`) instead of `Contractor`.
- Disentangled `Contractor` to strictly model commercial service contracts, utilities, and insurers.
- Updated `working-sessions.html` and Sessions Hub modal to select and display employers.

## [0.0.2-SNAPSHOT] - 2026-10-17

### Added
- Generalized `session` table architecture using JPA `InheritanceType.JOINED` polymorphic hierarchy for all session types (`CodingSession`, `GamingSession`, `SportSession`, `ReadingSession`, `MeditationSession`, and `LearningSession`).
- Native Java 26+ RFC 9562 UUIDv7 (`java.util.UUID.ofEpochMillis`) primary key generation across all session entities.
- PostgreSQL `generate_uuid_v7()` function supporting native `uuidv7()` (PostgreSQL 18+) with a standards-compliant fallback using `gen_random_uuid()`.
- Session break tracking with `break_duration` column and PostgreSQL generated columns `duration = endtime - starttime` and `net_duration = endtime - starttime - break_duration`.
- Unified Sessions Hub (`/sessions`) dashboard providing aggregate metrics (Total Sessions, Net Focused Time, Total Breaks, Active Categories), tabbed category filtering, and quick-log modals for each session category.
- Consistent design system across all 57 Thymeleaf templates powered by central `slm.css` with 12 thematic color gradients, glassmorphic cards, responsive navigation fragments, and accessible tables.
- Spring Boot `BuildProperties` integration via `springBoot { buildInfo() }` in `build.gradle` and dynamic injection into `ImprintController`.
- Comprehensive architectural decision records in `DECISIONS.md`.

### Changed
- Upgraded Java toolchain and language level to Java 27.
- Migrated legacy `reading_session`, `meditation_session`, `coding_session`, `gaming_session`, `sport_session`, and `learning_session` records and tables into the unified `session` base table.
- Replaced legacy per-view navigation fragments with centralized `templates/fragments/layout.html` (`standard-head`, `header`, `footer`).
- Delegated application version reporting to `build.gradle` and Spring Boot `BuildProperties`, removing static version configuration in `application.properties`.

### Fixed
- Fixed SpEL parsing errors in Thymeleaf layout navigation fragments caused by string quotation escaping.
- Fixed SpEL null-safety evaluations in `sessions.html` for optional session attributes (`branch`, `gameMode`, etc.).
- Documented and handled JaCoCo 0.8.13 incompatibility with Java 27 classfile version 71 bytecode.

## [0.0.1a-SNAPSHOT] - 2026-08-26

### Added
- Car trips tracking module (`car-trips`).
- Duration converter utility supporting seconds-based interval conversions.
- JaCoCo test coverage reporting configuration in Gradle.
- Selfies tracking module.
- Personal diary module.

### Changed
- Updated Spring Boot, Tomcat embed, and Hibernate dependencies.
- Configured PostgreSQL datasource username to load from `SLM_USER` environment variable.
- Improved habit entry processing to reliably reflect the last completed entry.

### Fixed
- Fixed learning session entity persistence issues.
- Fixed activity sorting and presentation order.
- Fixed appointment and doctor reference handling.

## [0.0.1-SNAPSHOT] - 2025-08-27

### Added
- Initial release of Simon's Life Manager (SLM) personal management platform.
- Core tracking modules: sleep times, habits, activities, sport, meditation, books, purchases, and contracts.
- Contacts/humans management and medical doctor appointments tracking.
- Automated email notification scheduler for upcoming events and reminders.
- Spring Security authentication and role-based access control.
- Thymeleaf web interface with Bootstrap components.
- PostgreSQL database schema and Spring Data JPA repositories.

[Unreleased]: https://github.com/sidietz/slm/compare/v0.0.2i...HEAD
[0.0.2i]: https://github.com/sidietz/slm/compare/v0.0.2h...v0.0.2i
[0.0.2h]: https://github.com/sidietz/slm/compare/v0.0.2g...v0.0.2h
[0.0.2g]: https://github.com/sidietz/slm/compare/v0.0.2f...v0.0.2g
[0.0.2f]: https://github.com/sidietz/slm/compare/v0.0.2e...v0.0.2f
[0.0.2e]: https://github.com/sidietz/slm/compare/v0.0.2d...v0.0.2e
[0.0.2d]: https://github.com/sidietz/slm/compare/v0.0.2c...v0.0.2d
[0.0.2c]: https://github.com/sidietz/slm/compare/v0.0.2b...v0.0.2c
[0.0.2b]: https://github.com/sidietz/slm/compare/v0.0.2a...v0.0.2b
[0.0.2a]: https://github.com/sidietz/slm/compare/v0.0.2-SNAPSHOT...v0.0.2a
[0.0.2-SNAPSHOT]: https://github.com/sidietz/slm/compare/v0.0.1a-SNAPSHOT...v0.0.2-SNAPSHOT
[0.0.1a-SNAPSHOT]: https://github.com/sidietz/slm/compare/v0.0.1-SNAPSHOT...v0.0.1a-SNAPSHOT
[0.0.1-SNAPSHOT]: https://github.com/sidietz/slm/releases/tag/v0.0.1-SNAPSHOT


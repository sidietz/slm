# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[Unreleased]: https://github.com/sidietz/slm/compare/v0.0.2e...HEAD
[0.0.2e]: https://github.com/sidietz/slm/compare/v0.0.2d...v0.0.2e
[0.0.2d]: https://github.com/sidietz/slm/compare/v0.0.2c...v0.0.2d
[0.0.2c]: https://github.com/sidietz/slm/compare/v0.0.2b...v0.0.2c
[0.0.2b]: https://github.com/sidietz/slm/compare/v0.0.2a...v0.0.2b
[0.0.2a]: https://github.com/sidietz/slm/compare/v0.0.2-SNAPSHOT...v0.0.2a
[0.0.2-SNAPSHOT]: https://github.com/sidietz/slm/compare/v0.0.1a-SNAPSHOT...v0.0.2-SNAPSHOT
[0.0.1a-SNAPSHOT]: https://github.com/sidietz/slm/compare/v0.0.1-SNAPSHOT...v0.0.1a-SNAPSHOT
[0.0.1-SNAPSHOT]: https://github.com/sidietz/slm/releases/tag/v0.0.1-SNAPSHOT

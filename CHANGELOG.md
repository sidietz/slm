# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[Unreleased]: https://github.com/sidietz/slm/compare/v0.0.2-SNAPSHOT...HEAD
[0.0.2-SNAPSHOT]: https://github.com/sidietz/slm/compare/v0.0.1a-SNAPSHOT...v0.0.2-SNAPSHOT
[0.0.1a-SNAPSHOT]: https://github.com/sidietz/slm/compare/v0.0.1-SNAPSHOT...v0.0.1a-SNAPSHOT
[0.0.1-SNAPSHOT]: https://github.com/sidietz/slm/releases/tag/v0.0.1-SNAPSHOT

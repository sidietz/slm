# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.3-rc15] - 2026-10-25

### Added
- **UAV (Drone) & Flight Tracking System** ([ADR 031](file:///home/simon/code/eclipse/slm/DECISIONS.md#adr-031-uav--drone--flight-tracking-with-postgis-geodesics-and-offsetdatetime-interval-duration)):
  - **EU UAS Open-Category Classes (`C0`, `C1`, `C2`, `C3`, `C4`)**: Added `drone_category` PostgreSQL enum and Java enum [`DroneCategory.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/DroneCategory.java).
  - **Domain Entities & UUIDv7**: Authored [`Drone.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Drone.java) with takeoff weight in grams (`weightGrams`), buy date (`buyDate` as `LocalDate`), serial number, UAS operator e-ID (`uasOperatorId`), purchase price, notes, and relational mappings to `Manufacturer` and `Vendor`.
  - **Flight Sessions & PostGIS Geodesics**: Authored [`DroneFlight.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/DroneFlight.java) with `OffsetDateTime` takeoff (`start`) and landing (`end`), PostgreSQL generated interval `duration` (`(endtime - starttime)`), takeoff/landing WGS84 coordinates, PostGIS generated geography points (`start_geom`, `landing_geom`), database-computed straight-line geodesic `air_distance`, and flight mission notes textarea.
  - **Spring Data Repositories & Web Controller**: Created [`DroneRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/DroneRepository.java), [`DroneFlightRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/DroneFlightRepository.java), and [`DroneController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/DroneController.java) managing `/drones` and `/drone-flights`.
  - **Thymeleaf UI & Leaflet Map Telemetry**: Authored [`drones.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/drones.html) and [`drone-flights.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/drone-flights.html) featuring fleet KPI cards, EU category badges, HTML5 `<input type="datetime-local">` form binding, and Leaflet interactive coordinate maps with polyline flight vectors.
  - **Spring AI MCP Tools**: Added [`McpDroneTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpDroneTools.java) exposing tools `listDrones`, `registerDrone`, `logDroneFlight`, and `listDroneFlights` for AI agent automation.
  - **Authoritative Schemas & Incremental Migration**: Synchronized baseline schemas ([`enums.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/enums.sql), [`tables.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/tables.sql), [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql), [`schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql)), and generated transactional migration script [`migration_2026-10-25_1.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/migration_2026-10-25_1.sql) enclosed in `BEGIN; ... COMMIT;`.
  - **Integration Testing**: Created [`DroneIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DroneIT.java) verifying drone persistence across all 5 EU categories, generated interval durations, PostGIS geodesic distance calculations, web MVC endpoints, and MCP tools.

### Changed
- Configured public access rules for `/drones`, `/drones/**`, `/drone-flights`, and `/drone-flights/**` in [`SecurityConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/SecurityConfig.java).
- Promoted Drone Flights to the primary mobility activity cards on the Transit & Medical Management hub in [`misc-management.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/misc-management.html) (replacing Selfies, which lives under Human Management), harmonizing Rail (Train Trips), Road (Car Trips), and Air (Drone Flights).
- **Centralized `toOffset` Construction in Integration Testing & Utility Layer**:
  - Added static `toOffset(int year, int month, int day, int hour, int minute, ZoneId zoneId)` and `toOffset(int year, int month, int day, int hour, int minute, int second, ZoneId zoneId)` to [`Utils.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/utils/Utils.java).
  - Provided centralized helper methods in [`AbstractIntegrationTest.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/AbstractIntegrationTest.java) passing `TimeZoneConfig.getZoneId()` by default to all subclasses.
  - Eliminated duplicated private `toOffset` definitions across [`DateTimePrecisionIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DateTimePrecisionIT.java), [`CourseDetailIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/CourseDetailIT.java), [`SleepSessionUIIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SleepSessionUIIT.java), [`ActivitiesAndMediaIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/ActivitiesAndMediaIT.java), [`AcademicIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/AcademicIT.java), and [`TransitAndMobilityIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java).
  - Replaced arbitrary `ZoneOffset.UTC` reference timestamp constructors with timezone-consistent `toOffset(...)` in [`RefuelingIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/RefuelingIT.java), [`DoctorControllerIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DoctorControllerIT.java), [`WellnessAndHabitsIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java), and [`SleepTimeIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SleepTimeIT.java).
  - Added unit test coverage for `Utils.toOffset` in [`UtilsTest.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/utils/UtilsTest.java).

## [0.0.3-rc14] - 2026-10-24

### Changed
- **Full Domain Migration from `LocalDateTime` to `OffsetDateTime`** ([ADR 030](file:///home/simon/code/eclipse/slm/DECISIONS.md#adr-030-complete-migration-to-offsetdatetime-and-dynamic-zoneid-resolution-for-timestamptz-integration)):
  - **Entity Models & DTOs**: Migrated all temporal timestamp fields across all 16 domain entities and DTOs from `java.time.LocalDateTime` to `java.time.OffsetDateTime` ([`Session.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java), [`Appointment.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Appointment.java), [`CalendarEvent.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CalendarEvent.java), [`TrainTrip.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip.java), [`TrainTrip2.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip2.java), [`Activity.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Activity.java), [`SleepTime.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepTime.java), [`SleepSession.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepSession.java), [`CarTrip.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarTrip.java), [`LearningItem.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LearningItem.java), [`HabitEntry.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/HabitEntry.java), [`LastDoneHabitEntry.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LastDoneHabitEntry.java), [`Course.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Course.java), [`LectureNote.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LectureNote.java), [`Refueling.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Refueling.java), [`Station.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Station.java), [`ChargingPoint.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/ChargingPoint.java), and [`CarExpense.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarExpense.java)). Maintained `LocalDate` on pure dates without time components (`Human.birthday`, `Contract.startDate`, `Contract.endDate`, `Car.purchaseDate`, `Car.targetSellDate`).
  - **Spring Data JPA Repositories**: Updated all temporal query filters, range parameters, derived queries, and return types across [`SessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SessionRepository.java), [`CalendarEventRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CalendarEventRepository.java), [`CourseRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CourseRepository.java), [`TrainTripRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTripRepository.java), [`TrainTrip2Repository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip2Repository.java), [`ActivityRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/ActivityRepository.java), [`AppointmentRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/AppointmentRepository.java), [`SportSessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SportSessionRepository.java), [`SleepTimeRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepTimeRepository.java), [`SleepSessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepSessionRepository.java), and [`MeditationSessionRepository.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/MeditationSessionRepository.java).
  - **Dynamic Timezone Resolution without Hardcoded Literals**: Replaced all `LocalDateTime.now()` invocations across controllers, services, schedulers, and tests with `OffsetDateTime.now(TimeZoneConfig.getZoneId())`, guaranteeing zero hardcoded timezone string literals in application logic.
  - **Spring AI MCP Tools**: Standardized input parameters and tool return values on `OffsetDateTime` across [`McpAcademicTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java), [`McpMedicalTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpMedicalTools.java), [`McpSessionTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java), [`McpVehicleTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpVehicleTools.java), and [`McpWellnessTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpWellnessTools.java), producing unambiguous ISO-8601 strings with timezone offsets.

### Added
- **Global Spring MVC Form Converters for HTML5 `<input type="datetime-local">`**:
  - Authored [`StringToOffsetDateTimeConverter.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/converter/StringToOffsetDateTimeConverter.java) providing bidirectional parsing for both full ISO-8601 offset strings and HTML5 local datetime strings (`yyyy-MM-dd'T'HH:mm`) using `TimeZoneConfig.getZoneId()`.
  - Authored [`OffsetDateTimeToStringConverter.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/converter/OffsetDateTimeToStringConverter.java) formatting `OffsetDateTime` into HTML5 `datetime-local` input format.
  - Registered converters globally in [`ConverterConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/ConverterConfig.java).

## [0.0.3-rc13] - 2026-10-24

### Fixed
- Fixed `ClassCastException` / constructor instantiation mismatch (`Cannot cast java.time.Instant to java.time.LocalDateTime`) when navigating to `/last-done-habit-entries`:
  - **Dynamic Timezone Resolution from Spring Boot Property**: Created [`TimeZoneConfig.java`](src/main/java/de/oberamsystems/slm/config/TimeZoneConfig.java) in `de.oberamsystems.slm.config` reading `@Value("${spring.jpa.properties.hibernate.jdbc.time_zone:Europe/Berlin}")`, setting the JVM default time zone dynamically, and exposing static accessor `TimeZoneConfig.getZoneId()`.
  - **Single 3-Parameter Constructor in DTO**: Updated [`LastDoneHabitEntry.java`](src/main/java/de/oberamsystems/slm/model/LastDoneHabitEntry.java) with a single 3-parameter constructor `LastDoneHabitEntry(Long id, Object date, String difference)` matching Hibernate's single-constructor requirement for native query DTO transformation, dynamically converting JDBC `Instant` to `LocalDateTime` using `TimeZoneConfig.getZoneId()`.
  - **Deterministic Window Diff**: Refined [`HabitEntryRepository.java`](src/main/java/de/oberamsystems/slm/model/HabitEntryRepository.java) native query with `OVER (ORDER BY last_done ASC)` to guarantee chronological `LAG` intervals.

### Changed
- Upgraded Spring Boot from `4.2.0-M2` to `4.2.0-RC1`.
- Reduced maximum multipart upload size (`spring.servlet.multipart.max-file-size` & `max-request-size`) from 512MB to 64MB in [`application.properties`](src/main/resources/application.properties).

## [0.0.3-rc12] - 2026-10-24

### Changed
- **Universal Migration to PostgreSQL `TIMESTAMPTZ` (`TIMESTAMP WITH TIME ZONE`) & Daylight Saving Time (DST) Integrity** (ADR 029):
  - **Database Schemas & Generated Column Expressions**:
    - Migrated all 16 tables and 25 date-time columns from unqualified `TIMESTAMP` to `TIMESTAMPTZ` across [`src/main/resources/tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), and [`src/test/resources/schema-init.sql`](src/test/resources/schema-init.sql).
    - Preserved UTC-accurate generated column expressions (`endtime - starttime`) for `duration`, `net_duration`, `travel_duration`, `complete_duration`, and `sleeptime`.
    - Updated trigger functions (`check_sleep_session_duration()`) to use `TIMESTAMPTZ` local variables for stage calculation.
  - **Incremental Atomic Migration**:
    - Created [`src/main/resources/migration_2026-10-24_2.sql`](src/main/resources/migration_2026-10-24_2.sql) enclosed in `BEGIN; ... COMMIT;` to upgrade existing databases safely: drops dependent generated columns, alters all date-time columns using `USING col AT TIME ZONE 'Europe/Berlin'`, and restores generated duration columns.
  - **Timezone Standardization & Daylight Saving Time (`Europe/Berlin`)**:
    - Configured Hibernate JDBC driver time zone `spring.jpa.properties.hibernate.jdbc.time_zone=Europe/Berlin` in [`application.properties`](src/main/resources/application.properties).
    - Initialized JVM default time zone `TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))` in [`SlmApplication.java`](src/main/java/de/oberamsystems/slm/SlmApplication.java).
    - Added `TZ=Europe/Berlin` and `PGTZ=Europe/Berlin` environment variables to `app` and `db` services in [`docker-compose.yml`](docker-compose.yml).
  - **Entity Model Annotations**:
    - Updated entity mappings to `@Column(name = "...", columnDefinition = "TIMESTAMPTZ")` across [`Session.java`](src/main/java/de/oberamsystems/slm/model/Session.java), [`Appointment.java`](src/main/java/de/oberamsystems/slm/model/Appointment.java), [`CalendarEvent.java`](src/main/java/de/oberamsystems/slm/model/CalendarEvent.java), [`TrainTrip.java`](src/main/java/de/oberamsystems/slm/model/TrainTrip.java), [`TrainTrip2.java`](src/main/java/de/oberamsystems/slm/model/TrainTrip2.java), [`Activity.java`](src/main/java/de/oberamsystems/slm/model/Activity.java), [`SleepTime.java`](src/main/java/de/oberamsystems/slm/model/SleepTime.java), [`CarTrip.java`](src/main/java/de/oberamsystems/slm/model/CarTrip.java), [`LearningItem.java`](src/main/java/de/oberamsystems/slm/model/LearningItem.java), [`HabitEntry.java`](src/main/java/de/oberamsystems/slm/model/HabitEntry.java), [`Course.java`](src/main/java/de/oberamsystems/slm/model/Course.java), [`LectureNote.java`](src/main/java/de/oberamsystems/slm/model/LectureNote.java), [`Refueling.java`](src/main/java/de/oberamsystems/slm/model/Refueling.java), [`Station.java`](src/main/java/de/oberamsystems/slm/model/Station.java), [`ChargingPoint.java`](src/main/java/de/oberamsystems/slm/model/ChargingPoint.java), and [`CarExpense.java`](src/main/java/de/oberamsystems/slm/model/CarExpense.java).
    - Preserved `LocalDateTime` on Java entity properties to maintain complete compatibility with HTML5 `<input type="datetime-local">`, Spring MVC `@DateTimeFormat`, and Thymeleaf UI views.

### Added
- **Daylight Saving Time (DST) & Temporal Precision Integration Tests**:
  - Authored [`DateTimePrecisionIT.java`](src/test/java/de/oberamsystems/slm/DateTimePrecisionIT.java) verifying:
    - Spring DST clock forward jump (+1h, Sunday March 29, 2026): 23:00 to 07:00 yields exact physical 7h duration in generated `duration` and `net_duration`.
    - Fall DST clock backward jump (-1h, Sunday October 25, 2026): 23:00 to 07:00 yields exact physical 9h duration in generated `duration` and `net_duration`.
    - `SleepTime` generated column `sleeptime` accurately reflects physical 7h and 9h across DST transitions.
    - `Appointment` multi-leg travel and total durations compute accurately with `TIMESTAMPTZ`.
    - Exact wall-clock `LocalDateTime` round-trip persistence and cache eviction for `CalendarEvent`, `Course`, and `Refueling`.

## [0.0.3-rc11] - 2026-10-24

### Fixed
- Fixed unhandled PostgreSQL trigger exception (`ERROR: Sleep stages sum (00:00:00) does not equal total session duration (07:14:00)`) when logging, completing, or editing a `SleepSession` with start and end times where sleep stage inputs are omitted:
  - **Deferred Constraint Trigger on `session`**: Converted `trg_check_session_sleep_duration` on the `session` table into a PostgreSQL `CONSTRAINT TRIGGER ... AFTER UPDATE ON session DEFERRABLE INITIALLY DEFERRED`. This defers cross-table consistency validation until transaction `COMMIT` time, preventing immediate statement-boundary trigger execution while Hibernate flushes joined-table mutations in hierarchy order (parent `session` before child `sleep_session`).
  - **Light Sleep Fallback Guarantee**: Preserved automatic allocation of the entire net sleep duration (`sleepMinutes = totalMinutes - awakeMinutes`) to light sleep (`lightSleepDuration = sleepMinutes`, `deep = 0`, `rem = 0`) when sleep stage inputs are omitted.
  - **Database Migration & Baseline Sync**: Created transactional migration script [`src/main/resources/migration_2026-10-24_1.sql`](src/main/resources/migration_2026-10-24_1.sql) and synchronized authoritative baseline definitions in [`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), and [`src/test/resources/schema-init.sql`](src/test/resources/schema-init.sql).
  - **Regression Integration Tests**: Added end-to-end integration tests in [`SleepSessionUIIT.java`](src/test/java/de/oberamsystems/slm/SleepSessionUIIT.java) verifying concluding an ongoing sleep session (`status = 'IN_PROGRESS'`) with omitted stages via `POST /sessions/sleep` and editing the duration of an existing completed sleep session.

## [0.0.3-rc10] - 2026-10-23

### Added
- Implemented **Car Expense Tracking (23 Spritmonitor Categories), Lifecycle TCO Analytics (€/100km), and Multi-Vehicle Comparison** (ADR 028):
  - Database Schema & Migration:
    - Added PostgreSQL enum `car_expense_type` with 23 values in English matching Spritmonitor taxonomy (`MAINTENANCE`, `REPAIR`, `TIRE_CHANGE`, `OIL_CHANGE`, `INSURANCE`, `TAX`, `INSPECTION`, `TUNING`, `ACCESSORIES`, `PURCHASE_PRICE`, `OTHER`, `CARE`, `LEASING_RATE`, `REGISTRATION`, `FINANCING_RATE`, `REFUND`, `FINE`, `PARKING_FEE`, `TOLL`, `SPARE_PARTS`, `CHARGING_BASE_FEE`, `DIGITAL_SERVICES`, `GHG_QUOTA`) in [`migration_2026-10-23_2.sql`](src/main/resources/migration_2026-10-23_2.sql).
    - Added columns `target_sell_date DATE` and `residual_value NUMERIC(10, 2)` to `car` table.
    - Created `car_expense` table with native UUIDv7 primary key, indexes on `(car_id, expense_date DESC)` and `(expense_type)`.
    - Synchronized baseline schema artifacts in [`tables.sql`](src/main/resources/tables.sql), [`enums.sql`](src/main/resources/enums.sql), [`init.sql`](init.sql), and [`src/test/resources/schema-init.sql`](src/test/resources/schema-init.sql).
  - Domain Model & Entities:
    - Created Java enum [`CarExpenseType.java`](src/main/java/de/oberamsystems/slm/model/CarExpenseType.java) with English identifiers, Spritmonitor ID mapping, German and English labels, Bootstrap icons, badge classes, and credit flags (`REFUND` and `GHG_QUOTA` marked as credits/inflows).
    - Created entity [`CarExpense.java`](src/main/java/de/oberamsystems/slm/model/CarExpense.java) and repository [`CarExpenseRepository.java`](src/main/java/de/oberamsystems/slm/model/CarExpenseRepository.java).
    - Extended [`Car.java`](src/main/java/de/oberamsystems/slm/model/Car.java) with `targetSellDate`, `residualValue`, and accessors.
  - Controller & Analytics Engine:
    - Created [`CarExpenseController.java`](src/main/java/de/oberamsystems/slm/controller/CarExpenseController.java) managing `/car-expenses` with support for three tabs (`LOG`, `ANALYTICS`, `COMPARISON`), expense CRUD, and inline lifecycle parameter updates (`/car-expenses/cars/{carId}/parameters`).
    - Implemented comprehensive TCO model: $\text{Total Net TCO} = \text{Depreciation} + \text{Fuel} + \text{Operating Expenses} - \text{Credits}$, realized price per 100km ($\text{TCO} / \Delta km \times 100$), and subcomponent breakdown (€/100km for fuel, depreciation, operating).
    - Whitelisted `/car-expenses` and `/car-expenses/**` in [`SecurityConfig.java`](src/main/java/de/oberamsystems/slm/SecurityConfig.java).
  - User Interface & Interactive Multi-Vehicle Comparison:
    - Authored [`car-expenses.html`](src/main/resources/templates/car-expenses.html) with:
      - **Tab 1 (Expenses Log)**: Table with Spritmonitor badges, positive/negative amount styling, and "+ Add Expense" modal.
      - **Tab 2 (Expense Analytics & €/100km)**: Lifecycle parameters card, KPI summary cards, cost breakdown table, and Billboard.js donut chart.
      - **Tab 3 (Vehicle Comparison)**: Head-to-head comparison of two vehicles (Car A vs Car B) via a Billboard.js multi-line chart (Years 1..10 on X-axis, Total Cost € on Y-axis). Features an interactive annual mileage slider powered by **noUiSlider** (default **10,000 km**) with synchronized number input and quick preset buttons (5k, 10k, 15k, 20k, 30k) that smoothly update trajectories and break-even insights in real-time without full page reloads.
      - **Decoupled Fuel & Energy Efficiency Card**: Displays fuel type, unit prices ($€/L$ vs $€/kWh$), average consumption, and running fuel cost per 100km inferred strictly from recorded refuelings, with real-time annual fuel spend updates tied to the distance slider.
    - Updated [`cars.html`](src/main/resources/templates/cars.html) and [`refuelings.html`](src/main/resources/templates/refuelings.html) with navigation links, target sell date, residual value table columns, and modal fields.
  - MCP Server Tools:
    - Added `@Tool getCarExpenses`, `@Tool logCarExpense`, and `@Tool updateVehicleLifecycleParameters` in [`McpVehicleTools.java`](src/main/java/de/oberamsystems/slm/mcp/McpVehicleTools.java).
  - Automated Tests:
    - Created [`CarExpenseModelTest.java`](src/test/java/de/oberamsystems/slm/model/CarExpenseModelTest.java) for taxonomy mapping, credits, and trajectory calculations.
    - Created [`CarExpenseIT.java`](src/test/java/de/oberamsystems/slm/CarExpenseIT.java) testing the complete lifecycle: expense logging, credits, deletion, parameter updates, analytics, electric vs. combustion refueling comparisons, and multi-vehicle comparison.

### Fixed
- Fixed visual appearance bug where the TCO torus (donut chart) failed to display on the Analytics tab by connecting Billboard.js rendering to Bootstrap `shown.bs.tab` lifecycle events, enforcing explicit sizing, and rendering an empty-state message when no expenses are recorded yet.
- Fixed `MethodArgumentNotValidException` / `typeMismatch` upon submitting the vehicle modal (`POST /cars`) with empty strings for optional numeric fields (`sellPrice`, `sellKilometers`, `pricePer100km`, `purchasePrice`, `purchaseKilometers`, `residualValue`):
  - Refactored numeric fields in [`Car.java`](src/main/java/de/oberamsystems/slm/model/Car.java) from primitive types to boxed wrappers (`Double`, `Long`) with null-safe default accessors (`0.0`, `0L`) and backward-compatible primitive overloads.
  - Added `BindingResult` parameter to `submitCars` and `submitCarTrips` in [`RouteController.java`](src/main/java/de/oberamsystems/slm/controller/RouteController.java) to catch binding errors and gracefully flash user-facing error messages instead of throwing fatal unhandled exceptions.
  - Added regression integration tests in [`TransitAndMobilityIT.java`](src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java) verifying vehicle creation with empty optional fields and invalid input handling.

## [0.0.3-rc9] - 2026-10-23

### Added
- Implemented **Academic Activity Classification, STEM Lab Support & Clean Domain Separation (Approach B)** (ADR 027):
  - Database & Schema Synchronization:
    - Added PostgreSQL enum `academic_activity_type` with 7 values (`LECTURE`, `PREPARATION`, `LEARNING`, `HOMEWORK`, `LAB`, `PROJECT`, `PRESENTATION`) in transactional migration [`src/main/resources/migration_2026-10-23_1.sql`](src/main/resources/migration_2026-10-23_1.sql).
    - Added column `activity_type academic_activity_type NOT NULL DEFAULT 'LECTURE'` and index `idx_lecture_session_activity_type` to `lecture_session`.
    - Synchronized authoritative baseline schemas in [`tables.sql`](src/main/resources/tables.sql), [`enums.sql`](src/main/resources/enums.sql), [`init.sql`](init.sql), and [`src/test/resources/schema-init.sql`](src/test/resources/schema-init.sql).
  - Domain Model & Entity Extensions:
    - Created Java enum [`LectureActivityType.java`](src/main/java/de/oberamsystems/slm/model/LectureActivityType.java) with human-readable display names, Bootstrap Icons, and Bootstrap 5.3 contextual badges (`bg-*-subtle text-*`).
    - Extended [`LectureSession.java`](src/main/java/de/oberamsystems/slm/model/LectureSession.java) with `activityType = LectureActivityType.LECTURE`, constructors, accessors, and `toString`.
    - Added ECTS workload helpers in [`Course.java`](src/main/java/de/oberamsystems/slm/model/Course.java): `getTargetWorkloadHours()` (ECTS x 30h) and `isNonAcademicCourse()`.
  - Controller & Analytics:
    - In [`AcademicController.java`](src/main/java/de/oberamsystems/slm/controller/AcademicController.java), computed total net duration, target workload hours, workload progress percentage, and activity breakdown maps (`activityDurations`, `activityCounts`).
    - Added `@PostMapping("/courses/{courseId}/sessions")` endpoint allowing direct session and activity logging from the course detail view.
    - Updated [`SessionController.java`](src/main/java/de/oberamsystems/slm/controller/SessionController.java) to bind `activityType` with `LectureActivityType.LECTURE` fallback and exposed `lectureActivityTypes` to view templates.
  - User Interface:
    - In [`course-detail.html`](src/main/resources/templates/course-detail.html), added ECTS Workload Progress card with target hours vs. logged time, activity breakdown badges, instant client-side activity filter pills (`All Activities`, `Lecture`, `Preparation`, `Learning`, `Homework`, `Lab`, `Project`, `Presentation`), and `#logSessionModal`.
    - In [`sessions.html`](src/main/resources/templates/sessions.html), added Activity Type select dropdown to `#form-lecture`, activity badges with icons to table rows, and activity indicators to the active session hero banner.
    - Enhanced semester placeholders in [`courses.html`](src/main/resources/templates/courses.html) clarifying non-academic lifelong learning support (`"OTHER"`, `"MOOC"`, `"VHS"`).
  - MCP Server & Tools:
    - Extended `logLectureSession` in [`McpAcademicTools.java`](src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java) to accept an optional `activityType` parameter.
  - Testing & Verification:
    - Added unit test `testLectureSessionActivityTypeDefaults` in [`SessionModelTest.java`](src/test/java/de/oberamsystems/slm/model/SessionModelTest.java).
    - Expanded [`CourseDetailIT.java`](src/test/java/de/oberamsystems/slm/CourseDetailIT.java) with `testDirectCourseSessionLoggingAndActivityTypes` and `testNonAcademicCourseSupport` verifying all 7 activity types, direct logging, and non-ECTS courses.

### Deprecated
- Deprecated legacy `SleepTime` subsystem in preparation for database table and model removal at a future major release (ADR 025):
  - Marked [`SleepTime.java`](src/main/java/de/oberamsystems/slm/model/SleepTime.java), [`SleepTimeRepository.java`](src/main/java/de/oberamsystems/slm/model/SleepTimeRepository.java), [`SleepTimeController.java`](src/main/java/de/oberamsystems/slm/controller/SleepTimeController.java), and [`SleepTimeRestController.java`](src/main/java/de/oberamsystems/slm/controller/SleepTimeRestController.java) with `@Deprecated(since = "0.0.3-rc9", forRemoval = true)`.
  - Converted [`sleep-times.html`](src/main/resources/templates/sleep-times.html) into a read-only archive view: added a prominent deprecation alert banner linking to the Unified Sessions Hub, retained the create form card with disabled inputs and disabled submit button.
  - Intercepted and disabled `POST /sleep-times` in [`SleepTimeController.java`](src/main/java/de/oberamsystems/slm/controller/SleepTimeController.java), blocking legacy persistence and redirecting with flash error message advising users to log sleep sessions via `/sessions?type=SLEEP`.
  - Re-routed primary sleep navigation in [`activity-management.html`](src/main/resources/templates/activity-management.html) to [`/sessions?type=SLEEP`](src/main/resources/templates/sessions.html) and updated header back-link in [`sleeptime-bb.html`](src/main/resources/templates/sleeptime-bb.html).
  - Updated [`WellnessAndHabitsIT.java`](src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java) and [`SleepTimeControllerTest.java`](src/test/java/de/oberamsystems/slm/controller/SleepTimeControllerTest.java) verifying read-only presentation and write rejection.

### Changed
- Standardized session start time initialization and stop session end time auto-population across the polymorphic `Session` hierarchy and UI modals (ADR 026):
  - Initialized `private LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);` in [`Session.java`](src/main/java/de/oberamsystems/slm/model/Session.java) so all blank session models (`CodingSession`, `WorkingSession`, `LearningSession`, `GamingSession`, `ReadingSession`, `MeditationSession`, `SportSession`, `SleepSession`, `LectureSession`) consistently default to the current minute upon creation.
  - Replaced arbitrary legacy past offsets in [`SessionController.java`](src/main/java/de/oberamsystems/slm/controller/SessionController.java) (`viewCodingSessions` previously `now().minusHours(1)`, `viewWorkingSessions` previously `now().minusHours(2)`) with `now().truncatedTo(ChronoUnit.MINUTES)` and `end = null`.
  - Upgraded [`sessions.html`](src/main/resources/templates/sessions.html) modal handlers:
    - Log Session Modal: Auto-populates `input[name="start"]` with fresh local client time (`YYYY-MM-DDTHH:mm`) across all 9 session forms upon opening, ensuring accurate timestamps even after long page idle times.
    - Finish / Stop Session Modal: Auto-populates `#stopModalEndTime` with current local client time upon clicking "Stop Session", updated hint text to reflect pre-filled current time, while preserving retroactive adjustment capabilities.
  - Added unit test coverage in [`SessionModelTest.java`](src/test/java/de/oberamsystems/slm/model/SessionModelTest.java) (`testDefaultStartTimeInitializedToNow`) and [`SessionControllerTest.java`](src/test/java/de/oberamsystems/slm/controller/SessionControllerTest.java) (`testViewSessionsBlankModelsHaveDefaultStartTime`, `testStopSessionWithExplicitEndTime`, and `testStopSessionWithNullEndTimeDefaultsToNow`).

## [0.0.3-rc8] - 2026-10-21

### Added
- Implemented **Multi-Plug & Multi-Stall EV Charging Station Configuration** in [`stations.html`](src/main/resources/templates/stations.html), [`StationController.java`](src/main/java/de/oberamsystems/slm/controller/StationController.java), and [`ChargingStation.java`](src/main/java/de/oberamsystems/slm/model/ChargingStation.java):
  - Upgraded the EV charging station modal with a dynamic charging points builder allowing users to configure multiple distinct plug types, power tiers, and stall counts (e.g. 2x CCS Combo 2 @ 400 kW, 4x CCS Combo 2 @ 100 kW, 1x CHAdeMO @ 100 kW) in a single station.
  - Implemented dynamic client-side row generation with "+ Add Stalls / Plug Type" and per-row delete controls, full edit prefilling via Jackson/JSON `data-points` serialization, and pristine create modal resetting.
  - Added `ChargingPointGroup` aggregation in [`ChargingStation.java`](src/main/java/de/oberamsystems/slm/model/ChargingStation.java) providing formatted display badges and JSON serialization for roundtrip fidelity.
  - Modernized EV charging station table display with grouped badges (e.g., `2x CCS Type 2 (400.0 kW)`) and total stall count badges.
  - Hardened [`StationController.java`](src/main/java/de/oberamsystems/slm/controller/StationController.java) `@PostMapping("/stations/charging")` with station name validation, stall count capping, safe filtering, and orphan removal.
  - Expanded [`RefuelingIT.java`](src/test/java/de/oberamsystems/slm/RefuelingIT.java) (`testCreateAndUpdateMultiPlugChargingStationFlow` and `testRefuelingAndStationsCompleteLifecycle`) verifying end-to-end multi-plug provisioning, editing, and table rendering.

### Changed
- Tuned HikariCP connection pool settings in [`application.properties`](src/main/resources/application.properties) (`max-lifetime=10m`, `keepalive-time=2.5m`, `idle-timeout=5m`, `connection-timeout=20s`, `minimum-idle=5`, `maximum-pool-size=15`) to proactively ping and rotate pool connections, eliminating `Failed to validate connection ... (This connection has been closed.)` log warnings caused by remote database socket timeouts during idle periods.
- Extended HTTP servlet session timeout to 300 minutes (`server.servlet.session.timeout=300m`) in [`application.properties`](src/main/resources/application.properties) to fully support long-form activity sessions (such as 100+ minute continuous lecture note-taking) without CSRF token expiration or session loss.

### Fixed
- Fixed `MethodArgumentNotValidException` when creating new contacts (e.g. lecturers) via modal forms submitting `id=""` (`Failed to convert property value of type 'java.lang.String' to required type 'long' for property 'id'`):
  - Boxed primary key `id` from primitive `long` to `Long` in [`Human.java`](src/main/java/de/oberamsystems/slm/model/Human.java), [`Purchase.java`](src/main/java/de/oberamsystems/slm/model/Purchase.java), [`Contract.java`](src/main/java/de/oberamsystems/slm/model/Contract.java), [`Contractor.java`](src/main/java/de/oberamsystems/slm/model/Contractor.java), [`SleepTime.java`](src/main/java/de/oberamsystems/slm/model/SleepTime.java), [`SportType.java`](src/main/java/de/oberamsystems/slm/model/SportType.java), [`Address.java`](src/main/java/de/oberamsystems/slm/model/Address.java), and [`LastDoneHabitEntry.java`](src/main/java/de/oberamsystems/slm/model/LastDoneHabitEntry.java).
  - Implemented global `StringTrimmerEditor(true)` in [`GlobalBindingInitializer.java`](src/main/java/de/oberamsystems/slm/config/GlobalBindingInitializer.java) converting empty or whitespace strings in form submissions directly to `null`.
  - Added defensive non-positive ID check (`human.getId() <= 0`) in [`HumanController.java`](src/main/java/de/oberamsystems/slm/controller/HumanController.java) to reset incoming empty IDs to `null` and safely guarded `syncHumanEvents`.
  - Added dedicated integration tests `testAddLecturerViaModalFormWithEmptyIdParam` in [`SocialAndPeopleIT.java`](src/test/java/de/oberamsystems/slm/SocialAndPeopleIT.java) and updated `testAddLecturerInHumansSection` in [`HumanEventIT.java`](src/test/java/de/oberamsystems/slm/HumanEventIT.java) to submit empty string `id` parameters.
- Fixed `DataIntegrityViolationException: null value in column "line_id" of relation "train_trip2" violates not-null constraint` when creating train trips via the UI modal form:
  - Enhanced line resolution in [`TrainTripController2.java`](src/main/java/de/oberamsystems/slm/controller/TrainTripController2.java) across `lineId`, `line`, `tt.line.id`, and `id`, with automatic fallback to the first available line in `getSortedLines()`, guaranteeing `tt.getLine()` is never null.
  - Added origin and destination station fallbacks in [`TrainTripController2.java`](src/main/java/de/oberamsystems/slm/controller/TrainTripController2.java) defaulting to the first available station if null or omitted.
  - Updated [`train-trips.html`](src/main/resources/templates/train-trips.html) to remove legacy `onchange="updateLine(event)"` on `#modalSelectLine` which caused an unwanted page reload inside the modal dialog, populated origin and destination selects with all stations (`${stations != null && !#lists.isEmpty(stations) ? stations : traintrips}`), and ensured default selection in `openCreateTrainTripModal()`.
  - Added integration test `testAddTrainTripViaModalForm` in [`TransitAndMobilityIT.java`](src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java) verifying train trip creation with empty string `id`, `lineId`, and omitted line parameter fallback.
- System-wide audit and hardening of all modal-first entity creation endpoints against primary key type conversion issues (`id=""`, `id <= 0`) and `NOT NULL` foreign key / column constraint violations:
  - Universal defensive non-positive ID reset (`id <= 0` -> `null`) across all controller handlers: [`PurchaseController.java`](src/main/java/de/oberamsystems/slm/controller/PurchaseController.java), [`ActivityController.java`](src/main/java/de/oberamsystems/slm/controller/ActivityController.java), [`BookController.java`](src/main/java/de/oberamsystems/slm/controller/BookController.java), [`GameController.java`](src/main/java/de/oberamsystems/slm/controller/GameController.java), [`DoctorController.java`](src/main/java/de/oberamsystems/slm/controller/DoctorController.java), [`RouteController.java`](src/main/java/de/oberamsystems/slm/controller/RouteController.java), [`HabitController.java`](src/main/java/de/oberamsystems/slm/controller/HabitController.java), [`SportController.java`](src/main/java/de/oberamsystems/slm/controller/SportController.java), [`LearningController.java`](src/main/java/de/oberamsystems/slm/controller/LearningController.java), [`ContractController.java`](src/main/java/de/oberamsystems/slm/controller/ContractController.java), [`EmployerController.java`](src/main/java/de/oberamsystems/slm/controller/EmployerController.java), [`DiaryEntryController.java`](src/main/java/de/oberamsystems/slm/controller/DiaryEntryController.java), [`GratitudeController.java`](src/main/java/de/oberamsystems/slm/controller/GratitudeController.java), [`MoodController.java`](src/main/java/de/oberamsystems/slm/controller/MoodController.java), [`SleepTimeController.java`](src/main/java/de/oberamsystems/slm/controller/SleepTimeController.java), [`HumanEventController.java`](src/main/java/de/oberamsystems/slm/controller/HumanEventController.java), [`AcademicController.java`](src/main/java/de/oberamsystems/slm/controller/AcademicController.java), and [`TrainTripController2.java`](src/main/java/de/oberamsystems/slm/controller/TrainTripController2.java).
  - Eliminated arbitrary entity fallbacks and implemented strict validation rejection across all creation endpoints:
    - Replaced silent fallback assignment (`*.getFirst()`, synthetic placeholder ISBNs) with explicit validation rejection, aborting database persistence and communicating failure via flash attribute `errorMessage` to the client.
    - Protected endpoints and required fields:
      - [`DoctorController.java`](src/main/java/de/oberamsystems/slm/controller/DoctorController.java): rejected appointments with missing/invalid Doctor, and doctors with missing/invalid Speciality.
      - [`PurchaseController.java`](src/main/java/de/oberamsystems/slm/controller/PurchaseController.java): rejected purchases with missing/invalid Vendor.
      - [`ActivityController.java`](src/main/java/de/oberamsystems/slm/controller/ActivityController.java): rejected activities with missing/invalid ActivityType.
      - [`BookController.java`](src/main/java/de/oberamsystems/slm/controller/BookController.java): rejected books with missing/invalid Author, missing/invalid Press, or blank/missing ISBN.
      - [`GameController.java`](src/main/java/de/oberamsystems/slm/controller/GameController.java): rejected games with missing/invalid Publisher or Studio; rejected devices with missing/invalid DeviceType, Vendor, or Manufacturer.
      - [`RouteController.java`](src/main/java/de/oberamsystems/slm/controller/RouteController.java): rejected routes with missing/invalid Start or End points; rejected car trips with missing/invalid Car or Route; rejected cars with missing/invalid Manufacturer or Vendor.
      - [`HabitController.java`](src/main/java/de/oberamsystems/slm/controller/HabitController.java): rejected habit entries with missing/invalid Habit.
      - [`SportController.java`](src/main/java/de/oberamsystems/slm/controller/SportController.java): rejected sport sessions with missing/invalid SportType.
      - [`HumanEventController.java`](src/main/java/de/oberamsystems/slm/controller/HumanEventController.java): rejected human events with missing/invalid primary contact.
      - [`TrainTripController2.java`](src/main/java/de/oberamsystems/slm/controller/TrainTripController2.java): rejected train trips with missing/invalid TrainLine, Origin station, or Destination station.
  - Added dismissible Bootstrap alert banners (`<div th:if="${errorMessage}" class="alert alert-danger ...">`) across all affected Thymeleaf templates: [`doctors.html`](src/main/resources/templates/doctors.html), [`appointments.html`](src/main/resources/templates/appointments.html), [`purchases.html`](src/main/resources/templates/purchases.html), [`books.html`](src/main/resources/templates/books.html), [`games.html`](src/main/resources/templates/games.html), [`devices.html`](src/main/resources/templates/devices.html), [`routes.html`](src/main/resources/templates/routes.html), [`cars.html`](src/main/resources/templates/cars.html), [`car-trips.html`](src/main/resources/templates/car-trips.html), [`activities.html`](src/main/resources/templates/activities.html), [`habit-entries.html`](src/main/resources/templates/habit-entries.html), [`sports.html`](src/main/resources/templates/sports.html), [`human-events.html`](src/main/resources/templates/human-events.html), and [`train-trips.html`](src/main/resources/templates/train-trips.html).
  - Updated domain integration tests to assert rejection and state non-pollution when mandatory relations are omitted:
    - [`DoctorControllerIT.java`](src/test/java/de/oberamsystems/slm/DoctorControllerIT.java) (`testModalCreateDoctorAndAppointmentWithEmptyIdParam`)
    - [`FinanceManagementIT.java`](src/test/java/de/oberamsystems/slm/FinanceManagementIT.java) (`testModalCreateFlowWithEmptyIdParam`)
    - [`ActivitiesAndMediaIT.java`](src/test/java/de/oberamsystems/slm/ActivitiesAndMediaIT.java) (`testModalCreateActivitiesAndMediaWithEmptyIdParam`)
    - [`TransitAndMobilityIT.java`](src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java) (`testModalCreateTransitAndMobilityWithEmptyIdParam`, `testAddTrainTripViaModalForm`)
    - [`WellnessAndHabitsIT.java`](src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java) (`testModalCreateWellnessAndHabitsWithEmptyIdParam`)
    - [`HumanEventIT.java`](src/test/java/de/oberamsystems/slm/HumanEventIT.java) (`testModalCreateHumanEventWithEmptyIdParam`)
    - [`PersonalJournalIT.java`](src/test/java/de/oberamsystems/slm/PersonalJournalIT.java) (`testModalCreateDiaryAndGratitudeWithEmptyIdParam`)
    - [`AcademicIT.java`](src/test/java/de/oberamsystems/slm/AcademicIT.java) (`testModalCreateCourseWithEmptyIdParam`)

## [0.0.3-rc7] - 2026-10-21

### Added
- Added comprehensive in-place entity editing integration test suite covering all SLM domains (ADR 024):
  - **Spend & Commercial Partners** ([`FinanceManagementIT.java`](src/test/java/de/oberamsystems/slm/FinanceManagementIT.java)): added `testUpdateEmployerFlow`, `testUpdateContractorFlow`, `testUpdateVendorFlow`, and `testUpdatePurchaseFlow` verifying single-row edit semantics (`count` unchanged) and persistence of updated fields.
  - **Healthcare & Personal** ([`DoctorControllerIT.java`](src/test/java/de/oberamsystems/slm/DoctorControllerIT.java) & [`SocialAndPeopleIT.java`](src/test/java/de/oberamsystems/slm/SocialAndPeopleIT.java)): added `testUpdateSpecialityFlow`, `testUpdateDoctorFlow`, `testUpdateAppointmentFlow`, `testUpdateHumanFlow`, and `testUpdateHumanEventFlow`.
  - **Vehicles, Transit & Mobility** ([`TransitAndMobilityIT.java`](src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java) & [`RefuelingIT.java`](src/test/java/de/oberamsystems/slm/RefuelingIT.java)): added `testUpdatePointAndRouteFlow`, `testUpdateCarAndTripFlow`, `testUpdateTrainLineStationAndTripFlow`, `testUpdateGasStationFlow`, and `testUpdateRefuelingFlow`.
  - **Books, Media & Gaming** ([`ActivitiesAndMediaIT.java`](src/test/java/de/oberamsystems/slm/ActivitiesAndMediaIT.java)): added `testUpdateActivityAndTypeFlow`, `testUpdateBookEcosystemFlow`, and `testUpdateGameEcosystemFlow`.
  - **Habits, Routines & Well-being** ([`WellnessAndHabitsIT.java`](src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java) & [`PersonalJournalIT.java`](src/test/java/de/oberamsystems/slm/PersonalJournalIT.java)): added `testUpdateHabitFlowAndCompletionLogSafety` (validating quick log appending safety), `testUpdateMoodFlow`, `testUpdateSleepTimeFlow`, and `testUpdateDiaryAndGratitudeFlow`.
- Added end-to-end integration test `testActiveLectureSessionLifecycleWithLinkedNoteAndStop` in [`AcademicIT.java`](src/test/java/de/oberamsystems/slm/AcademicIT.java) verifying seamless interaction between ongoing polymorphic `LectureSession` (started with `end = null` / `status = 'IN_PROGRESS'`), pre-population and persistence of rich markdown `LectureNote`, and concluding the session via `POST /sessions/{id}/stop`.
- Added missing public `setId(...)` mutator methods to mutable JPA JavaBean models and DTOs: [`Contractor.java`](src/main/java/de/oberamsystems/slm/model/Contractor.java), [`Vendor.java`](src/main/java/de/oberamsystems/slm/model/Vendor.java), [`Purchase.java`](src/main/java/de/oberamsystems/slm/model/Purchase.java), [`Doctor.java`](src/main/java/de/oberamsystems/slm/model/Doctor.java), [`Speciality.java`](src/main/java/de/oberamsystems/slm/model/Speciality.java), [`Appointment.java`](src/main/java/de/oberamsystems/slm/model/Appointment.java), [`TrainLine.java`](src/main/java/de/oberamsystems/slm/model/TrainLine.java), [`TrainStation2.java`](src/main/java/de/oberamsystems/slm/model/TrainStation2.java), [`TrainStation.java`](src/main/java/de/oberamsystems/slm/model/TrainStation.java), [`TrainTrip2.java`](src/main/java/de/oberamsystems/slm/model/TrainTrip2.java), [`TrainTrip.java`](src/main/java/de/oberamsystems/slm/model/TrainTrip.java), [`Route.java`](src/main/java/de/oberamsystems/slm/model/Route.java), [`Point.java`](src/main/java/de/oberamsystems/slm/model/Point.java), [`CarTrip.java`](src/main/java/de/oberamsystems/slm/model/CarTrip.java), [`DeviceType.java`](src/main/java/de/oberamsystems/slm/model/DeviceType.java), [`SleepTime.java`](src/main/java/de/oberamsystems/slm/model/SleepTime.java), [`CalendarEvent.java`](src/main/java/de/oberamsystems/slm/model/CalendarEvent.java), and [`TrainStation2Dto.java`](src/main/java/de/oberamsystems/slm/model/TrainStation2Dto.java).

### Fixed
- Fixed PostgreSQL generated column updates (`ERROR: column ... can only be updated to DEFAULT`) on entity update operations by adding `updatable = false` and `@Generated(event = { EventType.INSERT, EventType.UPDATE })` on generated `INTERVAL` columns in [`Appointment.java`](src/main/java/de/oberamsystems/slm/model/Appointment.java), [`CarTrip.java`](src/main/java/de/oberamsystems/slm/model/CarTrip.java), [`TrainTrip2.java`](src/main/java/de/oberamsystems/slm/model/TrainTrip2.java), [`TrainTrip.java`](src/main/java/de/oberamsystems/slm/model/TrainTrip.java), [`Activity.java`](src/main/java/de/oberamsystems/slm/model/Activity.java), [`CalendarEvent.java`](src/main/java/de/oberamsystems/slm/model/CalendarEvent.java), and [`SleepTime.java`](src/main/java/de/oberamsystems/slm/model/SleepTime.java).
- Fixed train trip submission in [`TrainTripController2.java`](src/main/java/de/oberamsystems/slm/controller/TrainTripController2.java) to cleanly disambiguate `lineId` from `id` (trip primary key) and guard against optimistic locking errors on insert when trip ID is null, non-positive, or unpersisted.

## [0.0.3-rc6] - 2026-10-20

### Added
- Implemented **Universal Modal-First Input Architecture and In-Place Entity Editing** across all entity domains (ADR 024):
  - **Spend & Commercial Partners**: Migrated [`purchases.html`](src/main/resources/templates/purchases.html), [`contractors.html`](src/main/resources/templates/contractors.html), [`employers.html`](src/main/resources/templates/employers.html), and [`vendors.html`](src/main/resources/templates/vendors.html) to full-width (`col-12`) tables with dual-purpose Create & Edit modals and instant client-side prefilling via HTML5 `data-*` attributes.
  - **Healthcare & Personal**: Upgraded [`doctors.html`](src/main/resources/templates/doctors.html), [`appointments.html`](src/main/resources/templates/appointments.html), [`specialities.html`](src/main/resources/templates/specialities.html), [`humans.html`](src/main/resources/templates/humans.html), and [`human-events.html`](src/main/resources/templates/human-events.html) to full-width tables with in-place modal editing.
  - **Vehicles & Mobility**: Transformed [`refuelings.html`](src/main/resources/templates/refuelings.html), [`stations.html`](src/main/resources/templates/stations.html), [`cars.html`](src/main/resources/templates/cars.html), [`car-trips.html`](src/main/resources/templates/car-trips.html), [`train-trips.html`](src/main/resources/templates/train-trips.html), [`train-lines.html`](src/main/resources/templates/train-lines.html), [`train-stations.html`](src/main/resources/templates/train-stations.html), [`routes.html`](src/main/resources/templates/routes.html), and [`points.html`](src/main/resources/templates/points.html) into full-width layouts with modal dialogs and row `[ Edit ]` actions.
  - **Books, Media & Gaming**: Modernized [`books.html`](src/main/resources/templates/books.html), [`authors.html`](src/main/resources/templates/authors.html), [`press.html`](src/main/resources/templates/press.html), [`games.html`](src/main/resources/templates/games.html), [`publishers.html`](src/main/resources/templates/publishers.html), [`studios.html`](src/main/resources/templates/studios.html), [`devices.html`](src/main/resources/templates/devices.html), and [`manufacturers.html`](src/main/resources/templates/manufacturers.html) to responsive `col-12` tables with modal creation/editing.
  - **Habits, Routines & Well-being**: Converted [`habits.html`](src/main/resources/templates/habits.html), [`habit-entries.html`](src/main/resources/templates/habit-entries.html), [`activities.html`](src/main/resources/templates/activities.html), [`activity-types.html`](src/main/resources/templates/activity-types.html), [`sports.html`](src/main/resources/templates/sports.html), [`sport-types.html`](src/main/resources/templates/sport-types.html), [`learning-items.html`](src/main/resources/templates/learning-items.html), [`diary.html`](src/main/resources/templates/diary.html), [`gratitudes.html`](src/main/resources/templates/gratitudes.html), and [`moods.html`](src/main/resources/templates/moods.html) to full-width table views with dual-purpose modals.
  - **Academics & Polymorphic Sessions**: Added in-place editing to [`courses.html`](src/main/resources/templates/courses.html) and row-level `[ Edit ]` functionality across the 9 polymorphic session types on [`sessions.html`](src/main/resources/templates/sessions.html).
  - Added public `setId(...)` mutator methods to mutable JPA JavaBean entities ([`Book.java`](src/main/java/de/oberamsystems/slm/model/Book.java), [`Author.java`](src/main/java/de/oberamsystems/slm/model/Author.java), [`Press.java`](src/main/java/de/oberamsystems/slm/model/Press.java), [`Game.java`](src/main/java/de/oberamsystems/slm/model/Game.java), [`Publisher.java`](src/main/java/de/oberamsystems/slm/model/Publisher.java), [`Studio.java`](src/main/java/de/oberamsystems/slm/model/Studio.java), [`Device.java`](src/main/java/de/oberamsystems/slm/model/Device.java), [`Manufacturer.java`](src/main/java/de/oberamsystems/slm/model/Manufacturer.java), [`Habit.java`](src/main/java/de/oberamsystems/slm/model/Habit.java), [`HabitEntry.java`](src/main/java/de/oberamsystems/slm/model/HabitEntry.java), [`Activity.java`](src/main/java/de/oberamsystems/slm/model/Activity.java), [`ActivityType.java`](src/main/java/de/oberamsystems/slm/model/ActivityType.java), [`SportType.java`](src/main/java/de/oberamsystems/slm/model/SportType.java), [`DiaryEntry.java`](src/main/java/de/oberamsystems/slm/model/DiaryEntry.java), [`Gratitude.java`](src/main/java/de/oberamsystems/slm/model/Gratitude.java), [`Mood.java`](src/main/java/de/oberamsystems/slm/model/Mood.java)) enabling Spring WebDataBinder to bind IDs for update operations.
  - Added endpoints `GET /press` and `POST /press` to [`BookController.java`](src/main/java/de/oberamsystems/slm/controller/BookController.java) to support press catalog management.
  - Formalized **ADR 024: Universal Modal-First Input Architecture and In-Place Entity Editing** in [`DECISIONS.md`](DECISIONS.md).

### Fixed
- Fixed Spring `WebDataBinder` parameter collision in [`HabitController.java`](src/main/java/de/oberamsystems/slm/controller/HabitController.java) where `entry.id` inadvertently inherited the selected habit ID, ensuring new completion logs are always created with `entry.setId(null)`.

## [0.0.3-rc5] - 2026-10-20

### Added
- Implemented **Contract Lifecycle Semantics, Auto-Extension Modeling, and Non-Negative Days Remaining** (ADR 023):
  - Rephrased `end_date` to `initial_end_date` across database schemas ([`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), [`schema-init.sql`](src/test/resources/schema-init.sql)) and entity model [`Contract.java`](src/main/java/de/oberamsystems/slm/model/Contract.java) to clearly represent the initial minimum commitment period (*Mindestvertragslaufzeit*).
  - Added backward-compatible `getEndDate()` and `setEndDate()` getter and setter aliases in [`Contract.java`](src/main/java/de/oberamsystems/slm/model/Contract.java).
  - Added first-class domain fields for auto-extension (`auto_extension`, `auto_extension_months`) and cancellation tracking (`is_canceled`, `cancellation_date`, `termination_date`).
  - Added [`ContractStatus`](src/main/java/de/oberamsystems/slm/model/Contract.java) enum (`ACTIVE_INITIAL_TERM`, `ACTIVE_AUTO_EXTENDED`, `CANCELED_PENDING`, `TERMINATED`) and helper `getCalculatedStatus()`.
  - Replaced naive date subtraction in Hibernate `@Formula` with a CASE-based calculation guaranteeing that active, auto-extended contracts never display negative days remaining, returning `NULL` for indefinite rolling contracts and positive countdowns for pending cancellations.
  - Upgraded [`contracts.html`](src/main/resources/templates/contracts.html) to display "Initial End Date", semantic status badges (`Auto-Extended`, `Active`, `Canceled`, `Ended`), active monthly fee summaries, and form switches for auto-extension and cancellation details.
  - Added atomic transactional migration script [`migration_2026-10-20.sql`](src/main/resources/migration_2026-10-20.sql) (`BEGIN;` ... `COMMIT;`).
  - Added comprehensive lifecycle and non-negative formula test cases in [`ContractIT.java`](src/test/java/de/oberamsystems/slm/ContractIT.java).

- Implemented **Modal-First Input Architecture, In-Place Editing, and Dedicated Contract Termination Action** (ADR 023):
  - Upgraded [`contracts.html`](src/main/resources/templates/contracts.html) to full-width (`col-12`) layout with header `+ Add Contract` button and a unified dual-purpose Bootstrap 5 modal (`#contractModal`) serving both creation and editing without page reloads.
  - Added dedicated `[ Edit ]` button on each table row pre-filling modal fields via HTML5 `data-*` attributes and Vanilla JS.
  - Added dedicated `[ Terminate ]` action button next to edit on active contracts, submitting to new endpoint `POST /contracts/{id}/terminate` in [`ContractController.java`](src/main/java/de/oberamsystems/slm/controller/ContractController.java).
  - Added integration and unit tests in [`ContractIT.java`](src/test/java/de/oberamsystems/slm/ContractIT.java) and [`ContractControllerTest.java`](src/test/java/de/oberamsystems/slm/controller/ContractControllerTest.java) verifying edit persistence and termination flow.

### Changed
- Decoupled composite integration test [`EmployerContractIT.java`](src/test/java/de/oberamsystems/slm/EmployerContractIT.java) into dedicated single-domain test classes [`EmployerIT.java`](src/test/java/de/oberamsystems/slm/EmployerIT.java) and [`ContractIT.java`](src/test/java/de/oberamsystems/slm/ContractIT.java) (ADR 004 & ADR 023).

## [0.0.3-rc4] - 2026-10-19

### Added
- Implemented **Vehicle Refuelings, Station Polymorphism, and Spritmonitor CSV Import** (ADR 022):
  - Created base entity [`Station`](src/main/java/de/oberamsystems/slm/model/Station.java) using native PostgreSQL 18 UUIDv7 primary keys and `InheritanceType.JOINED`.
  - Created [`GasStation`](src/main/java/de/oberamsystems/slm/model/GasStation.java) subclass with optional `tankentanken_id UUID UNIQUE` for external fuel price telemetry.
  - Created [`ChargingStation`](src/main/java/de/oberamsystems/slm/model/ChargingStation.java) subclass and 1:N [`ChargingPoint`](src/main/java/de/oberamsystems/slm/model/ChargingPoint.java) stalls supporting power in kW and [`PlugType`](src/main/java/de/oberamsystems/slm/model/PlugType.java) enum (`CCS_TYPE2`, `TYPE_2`, `CHADEMO`, `TESLA`, `TYPE_1`, `GBT`, `OTHER`, `UNKNOWN`).
  - Created [`Refueling`](src/main/java/de/oberamsystems/slm/model/Refueling.java) entity with automatic metrics calculation (`costPerLiter`, `calculatedConsumption`), board computer telemetry (`board_computer_consumption`, `board_computer_quantity`, `board_computer_speed`), ISO 3166-1 alpha-2 country codes, and ISO 4217 currencies.
  - Implemented [`SpritmonitorCsvService`](src/main/java/de/oberamsystems/slm/service/SpritmonitorCsvService.java) parsing semicolon-delimited CSV exports with German decimal commas, normalizing country codes, and auto-provisioning missing gas stations.
  - Implemented [`RefuelingController`](src/main/java/de/oberamsystems/slm/controller/RefuelingController.java) managing `/refuelings`, `/refuelings/import`, and timeseries API endpoint `GET /api/refuelings`.
  - Implemented [`StationController`](src/main/java/de/oberamsystems/slm/controller/StationController.java) managing `/stations`, `/stations/gas`, and `/stations/charging`.
  - Created modern UI [`refuelings.html`](src/main/resources/templates/refuelings.html) with vehicle filter pills, KPI overview cards, and an interactive Billboard.js analytics chart switchable between Consumption (`L/100km`), Trip Distance (`km`), and Fuel Price (`€/L`).
  - Created UI [`stations.html`](src/main/resources/templates/stations.html) featuring dual tabs for Gas Stations and EV Charging Stations.
  - Added Spring AI Model Context Protocol (MCP) server tools `getRefuelings`, `logRefueling`, `getStations`, and `createStation` in [`McpVehicleTools.java`](src/main/java/de/oberamsystems/slm/mcp/McpVehicleTools.java).
  - Added non-destructive atomic migration script [`migration_2026-10-19_3.sql`](src/main/resources/migration_2026-10-19_3.sql) (`BEGIN;` ... `COMMIT;`) and synchronized baseline schemas [`tables.sql`](src/main/resources/tables.sql), [`enums.sql`](src/main/resources/enums.sql), [`init.sql`](init.sql), and [`schema-init.sql`](src/test/resources/schema-init.sql).
  - Added unit test [`SpritmonitorCsvServiceTest.java`](src/test/java/de/oberamsystems/slm/service/SpritmonitorCsvServiceTest.java) and Testcontainers integration test [`RefuelingIT.java`](src/test/java/de/oberamsystems/slm/RefuelingIT.java).

## [0.0.3-rc3] - 2026-10-19

### Added
- Added [`SemesterComparator`](src/main/java/de/oberamsystems/slm/util/SemesterComparator.java) parsing European and German academic semester formats (`WiSe`, `WS`, `SoSe`, `SS`, `Wintersemester`, `Sommersemester`, `Summer`) and sorting them chronologically descending (most recent first).
- Added [`CourseComparator`](src/main/java/de/oberamsystems/slm/util/CourseComparator.java) enforcing deterministic course ordering: primarily by semester descending, secondarily by course title alphabetically ascending (case-insensitive), and tertiarily by course ID.
- Added comprehensive unit tests in [`SemesterComparatorTest.java`](src/test/java/de/oberamsystems/slm/util/SemesterComparatorTest.java) and [`CourseComparatorTest.java`](src/test/java/de/oberamsystems/slm/util/CourseComparatorTest.java).
- Added integration test `testCourseSortingConsistencyBetweenAllAndSemesterFilter()` in [`AcademicIT.java`](src/test/java/de/oberamsystems/slm/AcademicIT.java) verifying exact course order preservation between "All Semesters" and single-semester views.
- Exposed Spring AI MCP server tool `getSemesters` in [`McpAcademicTools.java`](src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java) returning all distinct academic semesters sorted chronologically descending.
- Documented **Deterministic Domain-Specific Sorting & Ordering** rules in Section 8 of [`AGENTS.md`](AGENTS.md).

### Fixed
- Fixed course sorting inconsistency in [`AcademicController.java`](src/main/java/de/oberamsystems/slm/controller/AcademicController.java) where switching from "All Semesters" (`createdAt DESC`) to a specific semester tab (`title ASC`) flipped the relative ordering of courses within the same semester.
- Fixed semester tab ordering in [`AcademicController.java`](src/main/java/de/oberamsystems/slm/controller/AcademicController.java) so semester tabs always present the most recent semester first.
- Aligned MCP tool `getCourses` in [`McpAcademicTools.java`](src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java) with `CourseComparator`.

## [0.0.3-rc2] - 2026-10-19

### Added
- Implemented **Universal Active Sessions (Start & Stop Later)** across the Polymorphic Session Hierarchy (ADR 021):
  - Added support for starting ongoing sessions without an end time (`endtime = NULL`), setting `status = 'IN_PROGRESS'` across all session types (`CodingSession`, `WorkingSession`, `LearningSession`, `ReadingSession`, `GamingSession`, `SportSession`, `MeditationSession`, `SleepSession`, `LectureSession`).
  - Added domain lifecycle helper methods `isInProgress()` and `stop(LocalDateTime, Duration, Integer, String, String)` directly on root [`Session`](src/main/java/de/oberamsystems/slm/model/Session.java).
  - Added query methods `findByStatusOrderByStartDesc`, `findByEndIsNullOrderByStartDesc`, and `findFirstByEndIsNullOrderByStartDesc` to [`SessionRepository`](src/main/java/de/oberamsystems/slm/model/SessionRepository.java).
  - Implemented shared polymorphic `POST /sessions/{id}/stop` in [`SessionController`](src/main/java/de/oberamsystems/slm/controller/SessionController.java) allowing any ongoing session to be stopped at any time with optional break duration, focus rating (1-5), outcome summary, and reflections.
  - Added automated calculation of missing sleep stages upon stopping an ongoing `SleepSession` to maintain PostgreSQL cross-table invariant triggers.
  - Added **Active Session Hero Banner** in [`sessions.html`](src/main/resources/templates/sessions.html) with pulsing status badge, real-time ticking elapsed timer, 1-click **Quick Stop**, and **Finish with Review** modal.
  - Enhanced session table with live `IN_PROGRESS` badges, `live-row-timer` real-time elapsed tickers, and inline stop action buttons.
  - Removed strict `required` constraint on session end datetimes across [`sessions.html`](src/main/resources/templates/sessions.html), [`working-sessions.html`](src/main/resources/templates/working-sessions.html), and [`coding-sessions.html`](src/main/resources/templates/coding-sessions.html).
  - Safeguarded [`IndexController`](src/main/java/de/oberamsystems/slm/controller/IndexController.java) against null end datetimes when rendering recent activity messages.
  - Exposed Spring AI MCP server tools `getActiveSessions`, `startSession`, and `stopSession` in [`McpSessionTools`](src/main/java/de/oberamsystems/slm/mcp/McpSessionTools.java).
  - Added comprehensive automated integration tests in [`SessionHubIT.java`](src/test/java/de/oberamsystems/slm/SessionHubIT.java) covering start, stop, polymorphic subclasses, and UI banner rendering.

## [0.0.3-rc1] - 2026-10-19

### Added
- Implemented **Sleep Architecture Tracking, Light Sleep & Cross-Table Duration Invariant Enforcement** (ADR 019):
  - Added `light_sleep_duration INTERVAL` to [`SleepSession`](src/main/java/de/oberamsystems/slm/model/SleepSession.java) and PostgreSQL `sleep_session` table.
  - Implemented automatic stage defaulting in [`SessionController`](src/main/java/de/oberamsystems/slm/controller/SessionController.java): when Deep, REM, and Light sleep inputs are left empty, the entire net sleep duration defaults to Light Sleep.
  - Implemented single-missing-stage auto-calculation: when exactly one stage is omitted while the other two are entered, the missing duration is solved dynamically as `(endtime - starttime - break_duration) - sum(other stages)`.
  - Added input validation in controller and UI: rejects mismatched stage sums with informative flash error banners (`errorMessage`).
  - Implemented PostgreSQL 18 cross-table triggers `trg_check_sleep_session_duration` (`BEFORE INSERT OR UPDATE ON sleep_session`) and `trg_check_session_sleep_duration` (`AFTER UPDATE ON session`) guaranteeing that `deep + rem + light + break == endtime - starttime`.
  - Added real-time client-side calculation badges and live auto-calculation hints in [`sessions.html`](src/main/resources/templates/sessions.html).
  - Authored non-destructive atomic migration script [`src/main/resources/migration_2026-10-19_2.sql`](src/main/resources/migration_2026-10-19_2.sql) (`BEGIN;` ... `COMMIT;`) backfilling legacy records and installing triggers.
  - Synchronized authoritative baseline schemas [`tables.sql`](src/main/resources/tables.sql), [`init.sql`](init.sql), and [`schema-init.sql`](src/test/resources/schema-init.sql).
- Added comprehensive end-to-end UI integration tests in [`SleepSessionUIIT.java`](src/test/java/de/oberamsystems/slm/SleepSessionUIIT.java):
  - Verified modal form rendering with CSRF token on `GET /sessions?type=SLEEP`.
  - Verified empty stages auto-defaulting to 100% light sleep.
  - Verified single missing stage auto-calculation across REM, Deep, and Light sleep.
  - Verified balanced full-stage submission and persistence.
  - Verified input validation rejection of mismatched stage sums and ambiguous inputs.
  - Verified PostgreSQL database trigger rejection of raw SQL invariant violations.
  - Verified Power Nap submission, UI deletion, and CSRF protection.
- Added comprehensive end-to-end Course Details & Academic integration tests in [`CourseDetailIT.java`](src/test/java/de/oberamsystems/slm/CourseDetailIT.java) and [`AcademicIT.java`](src/test/java/de/oberamsystems/slm/AcademicIT.java):
  - Verified Course Details rendering with rich metadata, lecturer, attached lecture sessions timeline, and lecture notes list.
  - Verified empty state rendering for courses without attached sessions or notes.
  - Verified modal course editing (`POST /courses/{id}/edit`) with CSRF protection and subsequent HTML rendering.
  - Verified clearing/disassociating lecturer on course update.
  - Verified course deletion workflow (`POST /courses/{id}/delete`) with CSRF protection in both [`CourseDetailIT.java`](src/test/java/de/oberamsystems/slm/CourseDetailIT.java) and [`AcademicIT.java`](src/test/java/de/oberamsystems/slm/AcademicIT.java), verifying database cascade deletion of attached notes, detached persistence of lecture sessions (`SET NULL`), and preservation of the lecturer.
  - Verified lecture note deletion workflow (`POST /notes/{id}/delete`) with CSRF protection in both [`AcademicIT.java`](src/test/java/de/oberamsystems/slm/AcademicIT.java) and [`CourseDetailIT.java`](src/test/java/de/oberamsystems/slm/CourseDetailIT.java), verifying note entity removal, enclosing course preservation, and empty-state re-rendering on the course details view.
  - Verified note creation workflow from course detail (`GET /courses/{courseId}/notes/new` with `sessionId`) and view rendering (`GET /notes/{id}`).
  - Verified university courses hub (`GET /courses`) with KPI metrics, semester filtering, and modal enrollment form.
- Fixed and hardened Thymeleaf template rendering across academic views:
  - Corrected double-brace duration formatting expression in [`course-detail.html`](src/main/resources/templates/course-detail.html) to invoke Spring ConversionService.
  - Safeguarded `examRelevanceRating` and `focusRating` star sequence generation in [`course-detail.html`](src/main/resources/templates/course-detail.html) and [`lecture-note-view.html`](src/main/resources/templates/lecture-note-view.html) against null/non-positive values.
  - Safeguarded course status badge null checks in [`courses.html`](src/main/resources/templates/courses.html).

- Added **Comprehensive Controller Integration Test Architecture** (ADR 020):
  - Created end-to-end integration tests covering all 33 Spring MVC and REST controllers using Testcontainers PostgreSQL 18 with PostGIS (`postgis/postgis:18-3.6`) and Spring Security MockMvc:
    - [`DashboardAndManagementIT.java`](src/test/java/de/oberamsystems/slm/DashboardAndManagementIT.java): verifies [`IndexController`](src/main/java/de/oberamsystems/slm/controller/IndexController.java), [`ImprintController`](src/main/java/de/oberamsystems/slm/controller/ImprintController.java), and the 6 domain management hubs (`CodingManagementController`, `JobManagementController`, `LifeManagementController`, `MiscManagementController`, `SchoolManagementController`, `SportManagementController`).
    - [`SessionHubIT.java`](src/test/java/de/oberamsystems/slm/SessionHubIT.java): verifies polymorphic [`SessionController`](src/main/java/de/oberamsystems/slm/controller/SessionController.java) endpoints (`/sessions`, `/coding-sessions`, `/working-sessions`), Four Pillars KPIs aggregation, session creation, and UUIDv7 deletion.
    - [`ActivitiesAndMediaIT.java`](src/test/java/de/oberamsystems/slm/ActivitiesAndMediaIT.java): verifies [`ActivityController`](src/main/java/de/oberamsystems/slm/controller/ActivityController.java), [`SportController`](src/main/java/de/oberamsystems/slm/controller/SportController.java), [`LearningController`](src/main/java/de/oberamsystems/slm/controller/LearningController.java), [`MeditationController`](src/main/java/de/oberamsystems/slm/controller/MeditationController.java), [`GameController`](src/main/java/de/oberamsystems/slm/controller/GameController.java), and [`BookController`](src/main/java/de/oberamsystems/slm/controller/BookController.java).
    - [`DoctorControllerIT.java`](src/test/java/de/oberamsystems/slm/DoctorControllerIT.java): verifies [`DoctorController`](src/main/java/de/oberamsystems/slm/controller/DoctorController.java) (doctors, specialities, appointments, date range filtering, PostGIS geodesic air distance).
    - [`WellnessAndHabitsIT.java`](src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java): verifies [`HabitController`](src/main/java/de/oberamsystems/slm/controller/HabitController.java), [`MoodController`](src/main/java/de/oberamsystems/slm/controller/MoodController.java), [`MoodRestController`](src/main/java/de/oberamsystems/slm/controller/MoodRestController.java), [`SleepTimeController`](src/main/java/de/oberamsystems/slm/controller/SleepTimeController.java), and [`SleepTimeRestController`](src/main/java/de/oberamsystems/slm/controller/SleepTimeRestController.java).
    - [`SocialAndPeopleIT.java`](src/test/java/de/oberamsystems/slm/SocialAndPeopleIT.java): verifies [`HumanController`](src/main/java/de/oberamsystems/slm/controller/HumanController.java), [`HumanEventController`](src/main/java/de/oberamsystems/slm/controller/HumanEventController.java), and [`CalendarEventController`](src/main/java/de/oberamsystems/slm/controller/CalendarEventController.java).
    - [`PersonalJournalIT.java`](src/test/java/de/oberamsystems/slm/PersonalJournalIT.java): verifies [`DiaryEntryController`](src/main/java/de/oberamsystems/slm/controller/DiaryEntryController.java) and [`GratitudeController`](src/main/java/de/oberamsystems/slm/controller/GratitudeController.java).
    - [`FinanceManagementIT.java`](src/test/java/de/oberamsystems/slm/FinanceManagementIT.java): verifies [`EmployerController`](src/main/java/de/oberamsystems/slm/controller/EmployerController.java), [`ContractController`](src/main/java/de/oberamsystems/slm/controller/ContractController.java), and [`PurchaseController`](src/main/java/de/oberamsystems/slm/controller/PurchaseController.java).
    - [`TransitAndMobilityIT.java`](src/test/java/de/oberamsystems/slm/TransitAndMobilityIT.java): verifies [`RouteController`](src/main/java/de/oberamsystems/slm/controller/RouteController.java) (Points, Routes, Cars, CarTrips, PostGIS geodesic air distance) and [`TrainTripController2`](src/main/java/de/oberamsystems/slm/controller/TrainTripController2.java) (TrainLines, TrainStation2, TrainTrips).

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
- Resolved entity association discrepancy in [`TrainTrip2`](src/main/java/de/oberamsystems/slm/model/TrainTrip2.java):
  - Corrected `origin` and `destination` fields and accessors from legacy `TrainStation` to `TrainStation2`, aligning JPA entity mappings with database foreign key constraints referencing `trainstation2(id)`.
- Hardened Spring Security public access in [`SecurityConfig.java`](src/main/java/de/oberamsystems/slm/SecurityConfig.java):
  - Added missing `/doctor.html` and `/speciality.html` matchers to permit unauthenticated access across doctor directory pages.
- Added `@Transactional` to [`HumanEventIT.java`](src/test/java/de/oberamsystems/slm/HumanEventIT.java) test class level to ensure automated rollback and prevent test data pollution across shared container test suites.
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

[Unreleased]: https://github.com/sidietz/slm/compare/v0.0.3-rc9...HEAD
[0.0.3-rc9]: https://github.com/sidietz/slm/compare/v0.0.3-rc8...v0.0.3-rc9
[0.0.3-rc8]: https://github.com/sidietz/slm/compare/v0.0.3-rc7...v0.0.3-rc8
[0.0.3-rc7]: https://github.com/sidietz/slm/compare/v0.0.3-rc6...v0.0.3-rc7
[0.0.3-rc6]: https://github.com/sidietz/slm/compare/v0.0.3-rc5...v0.0.3-rc6
[0.0.3-rc5]: https://github.com/sidietz/slm/compare/v0.0.3-rc4...v0.0.3-rc5
[0.0.3-rc4]: https://github.com/sidietz/slm/compare/v0.0.3-rc3...v0.0.3-rc4
[0.0.3-rc3]: https://github.com/sidietz/slm/compare/v0.0.3-rc2...v0.0.3-rc3
[0.0.3-rc2]: https://github.com/sidietz/slm/compare/v0.0.3-rc1...v0.0.3-rc2
[0.0.3-rc1]: https://github.com/sidietz/slm/compare/v0.0.2i...v0.0.3-rc1
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


# Comprehensive Architectural Plan: Migration from `LocalDateTime` to `OffsetDateTime`

## 1. Context & Motivation

In version `0.0.3-rc12` ([ADR 029](file:///home/simon/code/eclipse/slm/DECISIONS.md#adr-029-universal-postgresql-timestamptz-migration--daylight-saving-time-europeberlin-integrity)), the database schema was fully modernized to `TIMESTAMPTZ` (`TIMESTAMP WITH TIME ZONE`) across 16 tables and 25 date-time columns.
However, the Java domain model entities retained `java.time.LocalDateTime`.

### The Problem with `LocalDateTime` with `TIMESTAMPTZ`
1. **Type Impedance Mismatch**:
   - PostgreSQL's `TIMESTAMPTZ` represents an instant in time with a known UTC offset.
   - `java.time.LocalDateTime` contains **no offset or timezone information**.
   - JDBC 4.2 specification and Hibernate 6/7 map `TIMESTAMPTZ` natively to `java.time.OffsetDateTime` (and `java.time.Instant`), not `LocalDateTime`.
   - Native SQL queries and JDBC result sets return `Instant` or `OffsetDateTime`, which caused runtime `ClassCastException` in DTO constructor transformations (`LastDoneHabitEntry`).
2. **Loss of Temporal Offset Integrity**:
   - When storing an event in Java, `LocalDateTime.now()` discards whether the timestamp occurred in CET (`+01:00`) or CEST (`+02:00`).
   - `java.time.OffsetDateTime` retains the exact instant and UTC offset, aligning Java domain objects 1:1 with PostgreSQL's `TIMESTAMPTZ` semantics.
3. **Seamless API & MCP Integration**:
   - Spring AI Model Context Protocol (MCP) tools and external JSON APIs serialize `OffsetDateTime` into standard ISO-8601 strings with offsets (e.g., `2026-10-24T14:30:00+02:00`), preventing client-side timezone ambiguity.

## 2. Comprehensive Pros, Cons & Trade-Offs Analysis

### 2.1 The Pros (Why Migrate to `OffsetDateTime`)

1. **Strict JDBC 4.2 & Hibernate 6/7 Specification Alignment**:
   - The JDBC 4.2 specification explicitly designates `java.time.OffsetDateTime` as the standard Java object mapping for SQL `TIMESTAMP WITH TIME ZONE` (`Types.TIMESTAMP_WITH_TIMEZONE`).
   - Native SQL queries, Spring Data projections, and Hibernate constructor transformers (`NativeQueryConstructorTransformer`) automatically receive `OffsetDateTime` or `Instant` from the PostgreSQL JDBC driver. Mappings expecting `LocalDateTime` fail with `ClassCastException: Cannot cast java.time.Instant to java.time.LocalDateTime` (as seen in `LastDoneHabitEntry`). Using `OffsetDateTime` eliminates this class of runtime errors natively at the driver boundary without custom reflection/object unwrapping.

2. **Physical Time & DST Calculation Correctness**:
   - `java.time.OffsetDateTime` represents an absolute point on the physical time continuum paired with an explicit UTC offset.
   - When calculating elapsed time using `Duration.between(session.getStart(), session.getEnd())`:
     - With `LocalDateTime`, `Duration.between` calculates pure wall-clock difference, ignoring DST jumps. On the spring-forward transition (02:00 -> 03:00 skipped), a session starting at 01:30 and ending at 03:30 produces `2 hours` instead of the physical `1 hour`.
     - With `OffsetDateTime`, `Duration.between` computes exact epoch instant differences across seasonal boundaries, guaranteeing physical accuracy for work, sleep, and sports metrics.

3. **Disambiguation of Daylight Saving Time Transitions**:
   - During the fall DST cutover (last Sunday of October), clocks fall back from 03:00 to 02:00 CEST/CET.
   - A `LocalDateTime` of `2026-10-25 02:30` is completely ambiguous—it is impossible to know whether the event occurred in the first 02:30 (summer time, `UTC+2`) or the second 02:30 (standard time, `UTC+1`).
   - `OffsetDateTime` explicitly records `2026-10-25T02:30:00+02:00` vs `2026-10-25T02:30:00+01:00`, preserving historical event order and temporal determinism.

4. **Standardized ISO-8601 Serialization for REST & Spring AI MCP**:
   - SLM exposes data to external consumers, web browsers, and LLM agents via Model Context Protocol (MCP) tools.
   - `OffsetDateTime` serializes to standard ISO-8601 with offset (e.g. `2026-10-24T14:30:00+02:00`). Clients in different time zones receive full temporal context without ambiguity or risk of local time misinterpretation.
   - With `LocalDateTime`, clients receive `2026-10-24T14:30:00` and are forced to guess whether the time is UTC, server local time, or client local time.

5. **Elimination of Implicit JVM Default Timezone Dependency**:
   - `LocalDateTime.now()` relies on `ZoneId.systemDefault()`, making application behavior vulnerable to changes in host environment configuration (`TZ` env var or JVM flags).
   - Standardizing on `OffsetDateTime.now(TimeZoneConfig.getZoneId())` ensures that all generated timestamps deterministically adhere to the configured application timezone property (`spring.jpa.properties.hibernate.jdbc.time_zone`).

---

### 2.2 The Cons, Risks & Operational Costs

1. **HTML5 `<input type="datetime-local">` Form Impedance**:
   - The HTML5 standard defines `<input type="datetime-local">` to submit strings strictly without timezone offsets (format: `yyyy-MM-dd'T'HH:mm`).
   - Direct binding of form submissions to `OffsetDateTime` in Spring MVC controllers fails out of the box with `DateTimeParseException`.
   - *Mitigation*: Requires custom bidirectional Spring converters (`Converter<String, OffsetDateTime>` and `Converter<OffsetDateTime, String>`) in `ConverterConfig.java` that leverage `TimeZoneConfig.getZoneId()` to attach the correct seasonal offset dynamically.

2. **PostgreSQL Storage Reality (No Offset Storage on Disk)**:
   - PostgreSQL's `TIMESTAMPTZ` is an 8-byte integer representing microseconds in UTC since 2000-01-01. **PostgreSQL does NOT store the original offset or timezone name in the database column**.
   - When a timestamp is saved, PostgreSQL converts it to UTC. When queried, PostgreSQL converts UTC to the client session's configured timezone (`Europe/Berlin`).
   - *Implication*: Even if an `OffsetDateTime` is supplied with a different offset (e.g. `+09:00` Tokyo), upon retrieval from PostgreSQL it will always return with the active connection offset (`+01:00` or `+02:00`). `OffsetDateTime` reflects the session-standard offset, not a persistent foreign user offset.

3. **Large Refactoring Surface Area & Blast Radius**:
   - Over 300 occurrences across 30+ files must be modified:
     - 16 entity models and 1 DTO.
     - 6 Spring Data JPA repositories (method signatures, `@Param` types, return types).
     - ~15 Spring MVC controllers (`@RequestParam` filters, form backing objects, `now()` initializations).
     - Background and CSV services (`SpritmonitorCsvService`).
     - Test fixtures across multiple integration tests (`DateTimePrecisionIT`, `SleepSessionUIIT`, `SessionHierarchyIT`, `WellnessAndHabitsIT`, etc.).
   - High risk of regression during mass refactoring if any controller or query binding is overlooked.

4. **Ambiguity Resolution in Form Input During Fall DST Cutover**:
   - When a user enters `02:30` on the fall fallback date in an HTML5 datetime picker, the input has no offset.
   - The converter must choose whether to map it to `+02:00` (daylight) or `+01:00` (standard). Java's `ZoneRules.getOffset(ldt)` defaults to the earlier offset (`+02:00`), which is reasonable but represents an implicit assumption for that specific one-hour annual window.

---

### 2.3 Options Matrix & Architectural Alternatives

| Dimension | Option A: Full `OffsetDateTime` (Proposed) | Option B: Status Quo (`LocalDateTime` + DTO Bridging) | Option C: `java.time.Instant` | Option D: `java.time.ZonedDateTime` |
| :--- | :--- | :--- | :--- | :--- |
| **JDBC 4.2 Spec Compliance** | **Full (Native mapping)** | Partial (Requires driver conversion) | Full (Native mapping) | Non-standard for single `TIMESTAMPTZ` column |
| **Native Query DTO Safety** | **100% Safe (No casting errors)** | Fragile (Requires `Object` parameter / reflection) | 100% Safe | Fragile |
| **DST Duration Accuracy** | **Physically Exact** | Inexact (Calculates wall-clock time) | Physically Exact | Physically Exact |
| **Fall Cutover Disambiguation** | **Yes (`+01:00` vs `+02:00`)** | No (Ambiguous 02:00-03:00) | Yes (Different UTC epochs) | Yes (`ZoneId` + offset) |
| **HTML5 Form Compatibility** | Requires Converter | Native (Direct match for `datetime-local`) | Requires Converter & Formatter | Requires Converter |
| **Developer Ergonomics** | High (Supports calendar units + offset) | High (Supports calendar units) | Low (No `.getHour()`, `.getDayOfWeek()` without zone) | High (Full zone rules, but redundant for single timezone) |
| **Refactoring Blast Radius** | **Large (~326 occurrences across 30+ files)** | **Zero (Already stabilized in `v0.0.3-rc13`)** | Very Large (Breaks all template `#temporals` and getters) | Large + JPA mapping complexity |

---

### 2.4 Strategic Recommendation

- **If Maximum Specification Purity & Cross-System Robustness is Desired**:
  Proceed with **Option A (`OffsetDateTime`)**. It establishes full technical harmony with PostgreSQL's `TIMESTAMPTZ`, permanently eliminates DTO constructor casting mismatches, guarantees physically correct duration calculations across DST boundaries, and standardizes ISO-8601 payloads for external/MCP integrations.
- **If Minimizing Refactoring Risk & Code Churn is Prioritized**:
  Retain **Option B (Status Quo with `TimeZoneConfig`)**. The issue encountered in `v0.0.3-rc13` was already solved cleanly by `TimeZoneConfig` and the single flexible constructor in `LastDoneHabitEntry`. Since Simon is currently the sole user located in Germany (`Europe/Berlin`), PostgreSQL `TIMESTAMPTZ` handles the storage layer correctly while `LocalDateTime` mirrors the HTML5 `<input type="datetime-local">` form interface directly without converter overhead.

---

## 3. Architecture & Form Binding Strategy

```mermaid
flowchart TD
    UI["HTML5 <input type='datetime-local'> (yyyy-MM-dd'T'HH:mm)"] -->|HTTP POST| ConvIn["Converter<String, OffsetDateTime>"]
    ConvIn -->|"atZone(TimeZoneConfig.getZoneId())"| Entity["JPA Entity (@Column TIMESTAMPTZ, OffsetDateTime)"]
    Entity -->|Hibernate 7 / JDBC 4.2| DB["PostgreSQL 18 (TIMESTAMPTZ)"]
    DB -->|JDBC ResultSet (OffsetDateTime / Instant)| Entity
    Entity -->|Model Attribute| ConvOut["Converter<OffsetDateTime, String> / #temporals"]
    ConvOut -->|Rendered HTML| UI
```

### 3.1 HTML5 `<input type="datetime-local">` Conversion Layer
HTML5 `<input type="datetime-local">` outputs strings in the format `yyyy-MM-dd'T'HH:mm` (e.g. `2026-10-24T14:30`), omitting timezone offsets.
Attempting to parse this directly via `OffsetDateTime.parse(...)` fails with `DateTimeParseException`.

**Solution**:
Register global bidirectional converters in [`ConverterConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/ConverterConfig.java):
1. **`Converter<String, OffsetDateTime>`**:
   - First attempts standard ISO `OffsetDateTime.parse(source)`.
   - On fallback, parses `LocalDateTime.parse(source)` and converts using [`TimeZoneConfig.getZoneId()`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/config/TimeZoneConfig.java):
     ```java
     ldt.atZone(TimeZoneConfig.getZoneId()).toOffsetDateTime();
     ```
   - Gracefully handles empty or blank inputs as `null`.
2. **`Converter<OffsetDateTime, String>`**:
   - Formats `OffsetDateTime` to `yyyy-MM-dd'T'HH:mm` for `<input type="datetime-local" th:field="*{start}">`.
3. **Thymeleaf Template Rendering**:
   - `#temporals.format(entity.field, 'dd.MM.yyyy HH:mm')` natively accepts `OffsetDateTime` as a `TemporalAccessor`, producing localized wall-clock formatting without template alterations.

---

## 4. Scope of Changes

### 4.1 Model Entities (16 Entities + 1 DTO)
Replace `private LocalDateTime` with `private OffsetDateTime` across all fields mapped to `TIMESTAMPTZ`:

| Entity | Fields | Annotations |
| :--- | :--- | :--- |
| [`Session.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java) | `start`, `end` | `@Column(name = "starttime", columnDefinition = "TIMESTAMPTZ")`, `@Column(name = "endtime", columnDefinition = "TIMESTAMPTZ")` |
| [`Appointment.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Appointment.java) | `departure`, `arrival`, `start`, `end`, `arrivalHome` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`CalendarEvent.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CalendarEvent.java) | `start`, `end` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`TrainTrip.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip.java) | `start`, `end` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`TrainTrip2.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/TrainTrip2.java) | `start`, `end` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`Activity.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Activity.java) | `start`, `end` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`SleepTime.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SleepTime.java) | `gotobed`, `getup` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`CarTrip.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarTrip.java) | `start`, `end` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`LearningItem.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LearningItem.java) | `lastUpdated` | `@Column(name = "last_updated", columnDefinition = "TIMESTAMPTZ")` |
| [`HabitEntry.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/HabitEntry.java) | `lastDone` | `@Column(name = "last_done", columnDefinition = "TIMESTAMPTZ")` |
| [`Course.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Course.java) | `examDate`, `createdAt`, `lastUpdated` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`LectureNote.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LectureNote.java) | `createdAt`, `lastUpdated` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`Refueling.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Refueling.java) | `fuelingDate`, `createdAt` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`Station.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Station.java) | `createdAt` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`ChargingPoint.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/ChargingPoint.java) | `createdAt` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`CarExpense.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/CarExpense.java) | `createdAt` | `@Column(columnDefinition = "TIMESTAMPTZ")` |
| [`LastDoneHabitEntry.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LastDoneHabitEntry.java) | `date` | DTO field: `private OffsetDateTime date;` |

*Note*: Pure dates (e.g. `Human.birthday`, `Contract.startDate`, `Car.purchaseDate`) remain `LocalDate`.

### 4.2 Spring Data JPA Repositories
Update query method parameters and return types:
- `SessionRepository`: `findByStartBetween(OffsetDateTime, OffsetDateTime)`, `findMinDate()`, `findMaxDate()`.
- `CalendarEventRepository`: `findByStartBetween(OffsetDateTime, OffsetDateTime)`, `findByStartAfter(OffsetDateTime)`, `findMinDate()`, `findMaxDate()`.
- `CourseRepository`: `findByExamDateBetweenOrderByExamDateAsc(OffsetDateTime, OffsetDateTime)`.
- `TrainTripRepository` & `TrainTrip2Repository`: `findByStartBetween(OffsetDateTime, OffsetDateTime)`.
- `ActivityRepository`: `findByStartBetween(OffsetDateTime, OffsetDateTime)`.
- `DoctorRepository` (appointment queries): `findMinDate()`, `findMaxDate()`.

### 4.3 Controllers & Services
- Update current time generation across controllers:
  ```java
  OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES)
  ```
- Update controller method `@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` parameters from `LocalDateTime` to `OffsetDateTime` in:
  - `SessionController.java` (`end`)
  - `DoctorController.java` (`fromDate`, `toDate`)
  - `CalendarEventController.java` (`fromDate`, `toDate`)
- Update `SpritmonitorCsvService.java`:
  Convert CSV dates to `OffsetDateTime`:
  ```java
  date.atTime(LocalTime.of(12, 0).plusSeconds(rowIndex % 60)).atZone(TimeZoneConfig.getZoneId()).toOffsetDateTime()
  ```
- Update Spring AI MCP tool methods in `de.oberamsystems.slm.mcp` to accept and return `OffsetDateTime`.

### 4.4 Utilities
- Update [`Utils.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/utils/Utils.java):
  Overload `LocalDateTimeToString(OffsetDateTime odt)` or replace with `OffsetDateTimeToString(OffsetDateTime odt)`.

---

## 5. Implementation Steps

1. **Phase 1: Converter Layer (`ConverterConfig.java`)**
   - Add `Converter<String, OffsetDateTime>` and `Converter<OffsetDateTime, String>` using `TimeZoneConfig.getZoneId()`.
2. **Phase 2: Entities & DTOs**
   - Refactor the 16 entities and `LastDoneHabitEntry` from `LocalDateTime` to `OffsetDateTime`.
3. **Phase 3: Repositories**
   - Update repository query methods.
4. **Phase 4: Controllers & Services**
   - Update web controllers, CSV services, and MCP tools.
5. **Phase 5: Test Suite Updates**
   - Update test fixtures in `DateTimePrecisionIT`, `SleepSessionUIIT`, `SessionHierarchyIT`, `WellnessAndHabitsIT`, etc.
   - Run full Gradle test suite.
6. **Phase 6: Documentation & ADR**
   - Record in `CHANGELOG.md` under `[0.0.3-rc13]` and author ADR 030 in `DECISIONS.md`.

---

## 6. Verification & Test Plan

1. **Automated Integration Tests**:
   - [`DateTimePrecisionIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/DateTimePrecisionIT.java): Verify spring/fall DST physical duration calculations and exact offset persistence.
   - [`WellnessAndHabitsIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/WellnessAndHabitsIT.java): Verify `/last-done-habit-entries` with `OffsetDateTime`.
   - [`SessionHierarchyIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SessionHierarchyIT.java) & [`SessionHubIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SessionHubIT.java): Verify all polymorphic session types.
   - Command:
     ```bash
     JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test -x jacocoTestReport
     ```
2. **Manual & UI Verification**:
   - Verify modal forms submitting datetime-local values bind correctly to `OffsetDateTime`.
   - Verify Thymeleaf formatted outputs (`dd.MM.yyyy HH:mm`) render correctly in `Europe/Berlin` wall-clock time.

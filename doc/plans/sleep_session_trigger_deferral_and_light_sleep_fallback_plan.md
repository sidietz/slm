# Architecture Plan: Deferred Constraint Triggers for Polymorphic Sleep Session Invariants & Light Sleep Fallback

## 1. Problem Statement & Root Cause Analysis

### 1.1 The Incident
When logging or completing a `SleepSession` with start time (bedtime) and wake-up time (e.g., 7 hours 14 minutes gross duration) where all sleep stage inputs are omitted (which should default entirely to light sleep), the system threw the following exception:

```
2026-10-24T08:16:29.135+02:00  WARN 569411 --- [slm] [http-nio-8080-exec-7] org.hibernate.orm.jdbc.error             : HHH000247: ErrorCode: 0, SQLState: P0001
2026-10-24T08:16:29.136+02:00  WARN 569411 --- [slm] [http-nio-8080-exec-7] org.hibernate.orm.jdbc.error             : ERROR: Sleep stages sum (00:00:00) does not equal total session duration (07:14:00) for session id 01a16aba-f307-7deb-b3a4-7ed50cfa0154
  Where: PL/pgSQL function check_session_sleep_duration() line 23 at RAISE
2026-10-24T08:16:29.139+02:00 ERROR 569411 --- [slm] [http-nio-8080-exec-7] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.orm.jpa.JpaSystemException: could not execute statement [ERROR: Sleep stages sum (00:00:00) does not equal total session duration (07:14:00) for session id 01a16aba-f307-7deb-b3a4-7ed50cfa0154
  Where: PL/pgSQL function check_session_sleep_duration() line 23 at RAISE] [update session set break_duration=?,comment=?,endtime=?,focus_rating=?,outcome_summary=?,starttime=?,status=? where id=?]] with root cause
...
at de.oberamsystems.slm.controller.SessionController.submitSleepSession(SessionController.java:461)
```

### 1.2 Root Cause Analysis
1. **Polymorphic Joined Inheritance Architecture**:
   - `SleepSession` extends `Session` using JPA joined-table inheritance (`@Inheritance(strategy = InheritanceType.JOINED)`).
   - The state is physically partitioned into two tables: `session` (parent table storing `id`, `starttime`, `endtime`, `break_duration`, `status`) and `sleep_session` (child table storing `deep_sleep_duration`, `rem_sleep_duration`, `light_sleep_duration`, `is_nap`, `awake_count`).
2. **Hibernate Mutation Ordering**:
   - When updating an existing `SleepSession` (e.g. concluding an ongoing sleep session via `submitSleepSession` or editing a sleep session with an existing `id`), Spring Data JPA executes an entity merge.
   - Hibernate flushes entity updates strictly according to the class hierarchy order:
     - **Statement 1**: `UPDATE session SET break_duration=?, comment=?, endtime=?, focus_rating=?, outcome_summary=?, starttime=?, status=? WHERE id=?`
     - **Statement 2**: `UPDATE sleep_session SET awake_count=?, deep_sleep_duration=?, is_nap=?, light_sleep_duration=?, rem_sleep_duration=? WHERE id=?`
3. **Immediate Cross-Table Trigger Execution**:
   - The database trigger `trg_check_session_sleep_duration` on `session` was declared as:
     ```sql
     CREATE TRIGGER trg_check_session_sleep_duration
     AFTER UPDATE OF starttime, endtime, break_duration ON session
     FOR EACH ROW
     EXECUTE FUNCTION check_session_sleep_duration();
     ```
   - This trigger is **immediate** (non-deferrable). PostgreSQL executes it at the statement boundary of **Statement 1**, before Hibernate has sent **Statement 2**.
   - The trigger queries `sleep_session WHERE id = NEW.id`. At this exact moment, `sleep_session` still holds the old values recorded when the ongoing session was initiated (all stages set to `00:00:00`).
   - Consequently, `v_sum` evaluates to `00:00:00`, while `v_total` evaluates to `07:14:00`.
   - The trigger immediately raises an exception and aborts the transaction before Statement 2 can ever execute.

---

## 2. Proposed Architecture & Solution Strategy

### 2.1 PostgreSQL Deferrable Constraint Triggers
In relational databases, cross-table integrity invariants that span multiple tables modified within a single transaction must be evaluated at transaction `COMMIT` time rather than per statement.

PostgreSQL provides `CREATE CONSTRAINT TRIGGER ... DEFERRABLE INITIALLY DEFERRED` precisely for this purpose:

```sql
-- Trigger on session: Validates consistency with sleep_session at COMMIT
DROP TRIGGER IF EXISTS trg_check_session_sleep_duration ON session;
CREATE CONSTRAINT TRIGGER trg_check_session_sleep_duration
AFTER UPDATE ON session
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION check_session_sleep_duration();

-- Trigger on sleep_session: Validates consistency with session at COMMIT
DROP TRIGGER IF EXISTS trg_check_sleep_session_duration ON sleep_session;
CREATE CONSTRAINT TRIGGER trg_check_sleep_session_duration
AFTER INSERT OR UPDATE ON sleep_session
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION check_sleep_session_duration();
```

#### Why Constraint Triggers Solve the Problem:
1. When Hibernate executes `UPDATE session ...`, PostgreSQL registers that `trg_check_session_sleep_duration` needs to run, but defers execution until `COMMIT`.
2. Hibernate then executes `UPDATE sleep_session ...`, writing the calculated `light_sleep_duration = 07:14:00` (and `deep = 0`, `rem = 0`).
3. When the transaction commits, PostgreSQL invokes the deferred triggers.
4. The trigger queries `sleep_session` and finds `light_sleep_duration = 07:14:00`.
5. `v_sum (07:14:00) == v_total (07:14:00)`. The invariant check succeeds seamlessly!

### 2.2 Trigger Function Guard Verifications
Ensure trigger functions remain robust for both new and completed sessions:
- `check_session_sleep_duration()`:
  - Guards `IF NEW.session_type = 'SLEEP' AND NEW.starttime IS NOT NULL AND NEW.endtime IS NOT NULL THEN` ensure that ongoing sessions (`endtime IS NULL`) and non-sleep sessions are ignored.
- `check_sleep_session_duration()`:
  - Guards `IF v_starttime IS NOT NULL AND v_endtime IS NOT NULL THEN` ensure that ongoing sessions are not blocked.

### 2.3 Java Controller Invariants
In [`SessionController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java):
- In `submitSleepSession`:
  - When `providedCount == 0`:
    `light = sleepMinutes; deep = 0; rem = 0;`
    This correctly assigns the entire net sleep duration to light sleep.
  - When `session.getId()` is present (updating an active or existing session), `session.setStatus("COMPLETED")`, `session.setBreakDuration(...)`, `session.setLightSleepDuration(...)`, and `session.setDeepSleepDuration(...)` are updated on the model before calling `sleepSessionRepo.save(session)`.
- In `stopSession` (`/sessions/{id}/stop`):
  - When stopping an ongoing sleep session via quick timer or modal, light sleep fallback is already properly populated.

---

## 3. Database Schema Synchronization & Migration Protocol

Per Section 5 & 6 of [`AGENTS.md`](file:///home/simon/code/eclipse/slm/AGENTS.md):

1. **Update Baseline Schema Files**:
   - [`src/main/resources/tables.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/tables.sql)
   - [`init.sql`](file:///home/simon/code/eclipse/slm/init.sql)
   - [`src/test/resources/schema-init.sql`](file:///home/simon/code/eclipse/slm/src/test/resources/schema-init.sql)
   Replace the non-deferrable triggers with `CREATE CONSTRAINT TRIGGER ... DEFERRABLE INITIALLY DEFERRED`.
2. **Create Transactional Migration Delta**:
   - Create [`src/main/resources/migration_2026-10-24_1.sql`](file:///home/simon/code/eclipse/slm/src/main/resources/migration_2026-10-24_1.sql) enclosed in `BEGIN;` and `COMMIT;` to upgrade developer and production databases cleanly without data loss.

---

## 4. Testing & Verification Plan

### 4.1 Integration Tests in [`SleepSessionUIIT.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/SleepSessionUIIT.java)
Add end-to-end integration tests using live PostgreSQL Testcontainers:
1. `testCompleteOngoingSleepSessionWithAllStagesEmptyDefaultsToLightSleep()`:
   - Start an ongoing sleep session: `POST /sessions/sleep` with `start = 2026-10-23T23:00`, `end = ""` (`status = 'IN_PROGRESS'`).
   - Conclude it via `POST /sessions/sleep` providing the same `id`, `start = 2026-10-23T23:00`, `end = 2026-10-24T06:14` (7 hours 14 minutes), `breakMinutes = 0`, and all sleep stages empty.
   - Verify it commits cleanly without PostgreSQL `check_session_sleep_duration()` violation.
   - Assert `lightSleepDuration = Duration.ofMinutes(434)` (7h 14m), `status = 'COMPLETED'`.
2. `testEditExistingCompletedSleepSessionDurationWithStagesEmpty()`:
   - Create a completed sleep session.
   - Edit the session times via `POST /sessions/sleep` with its `id`, changing end time by +30 minutes while omitting stages.
   - Verify it updates and commits cleanly with the new duration allocated to light sleep.

### 4.2 Test Suite Execution
Execute the test suite using Java 25 running Gradle with Java 27 bytecode:
```bash
JAVA_HOME=/usr/lib/jvm/java-25-amazon-corretto ./gradlew test --tests de.oberamsystems.slm.SleepSessionUIIT -x jacocoTestReport
JAVA_HOME=/usr/lib/jvm/java-25-amazon-corretto ./gradlew test --tests de.oberamsystems.slm.SessionHubIT -x jacocoTestReport
```

---

## 5. Documentation Updates

1. **[`CHANGELOG.md`](file:///home/simon/code/eclipse/slm/CHANGELOG.md)**:
   - Record bugfix under `## [0.0.3-rc11] - 2026-10-24` in `### Fixed`.
2. **[`DECISIONS.md`](file:///home/simon/code/eclipse/slm/DECISIONS.md)**:
   - Update ADR 019 with the Architectural Decision to use `CREATE CONSTRAINT TRIGGER ... DEFERRABLE INITIALLY DEFERRED` for cross-table polymorphic joined-table invariants.

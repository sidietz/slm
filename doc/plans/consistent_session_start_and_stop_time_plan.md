# Architectural Plan: Consistent Session Start Time & Stop Session End Time Initialization

## 1. Executive Summary & Objective

In **Simon's Life Manager (SLM)**, sessions across all Four Pillars (Productivity, Sport, Wellness, Leisure) can be logged either directly after completion or started as ongoing (`IN_PROGRESS`, `end == null`) and stopped later.

Currently, start time initialization is inconsistent across screens and session types:
1. In the Unified Sessions Hub ([`sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sessions.html)), opening "+ Log Session" resets forms and leaves the `<input type="datetime-local" name="start">` empty because blank session models (`new CodingSession()`, etc.) have `start == null`.
2. In legacy sub-views, arbitrary past offsets were hardcoded (e.g., [`viewCodingSessions`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java#L238) defaulted to `now().minusHours(1)`, and [`viewWorkingSessions`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java#L271) defaulted to `now().minusHours(2)`).
3. When stopping an ongoing session via the **Finish Session / Stop Session** modal ([`#stopSessionModal`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sessions.html#L519)), the `end` input field (`#stopModalEndTime`) is left blank with a hint "Leave blank to stamp with current time", rather than being explicitly pre-filled with the current time.

### Target Goals
- **Consistent Creation Start Time**: When any session is created (in both frontend modals/forms and backend Java models), `start` must consistently initialize with the current local time (`LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)`).
- **Auto-Populated Stop End Time**: When an ongoing session is running and the user clicks **Stop Session**, the end time input (`#stopModalEndTime`) must be automatically populated with the current time (`LocalDateTime.now()`), allowing immediate visual confirmation or retroactive fine-tuning.

---

## 2. Current State Analysis & Identified Discrepancies

### 2.1. Backend Session Initialization
- In [`Session.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java#L33):
  `private LocalDateTime start;` defaults to `null`.
- In [`SessionController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java#L219-L227):
  ```java
  model.addAttribute("codingSession", new CodingSession());
  model.addAttribute("workingSession", new WorkingSession());
  ...
  ```
  All 9 form-binding models have `start == null`.
- In [`SessionController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java#L238-L272):
  - `GET /coding-sessions`: `cs.setStart(LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES));`
  - `GET /working-sessions`: `ws.setStart(LocalDateTime.now().minusHours(2).truncatedTo(ChronoUnit.MINUTES));`
  These arbitrary offsets conflict with the user expectation of defaulting to `now()`.

### 2.2. Frontend Modal Behavior ([`sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sessions.html))
- In `newSessionModal` (`show.bs.modal` event, line 1280):
  Calling `f.reset()` clears all form inputs. Because the server-rendered values had no default `start` attribute, the client browser renders an empty datetime picker. Even if the page was rendered hours earlier, the user expects the current local time at the moment they open the modal.
- In `setStopModalTarget(btn)` (line 1200):
  ```javascript
  function setStopModalTarget(btn) {
      const id = btn.getAttribute('data-session-id');
      const form = document.getElementById('stopSessionForm');
      if (form && id) {
          form.action = '/sessions/' + id + '/stop';
      }
  }
  ```
  `#stopModalEndTime` is never assigned a value.

---

## 3. Architecture & Technical Design

### 3.1. Entity Level: Default `start` in [`Session.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Session.java)
Initialize `start` at declaration or in the default no-arg constructor:
```java
@Column(name = "starttime", columnDefinition = "TIMESTAMP")
private LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
```
- **JPA Invariant**: When Hibernate loads existing records from PostgreSQL, it instantiates the bean and immediately sets `start` from the database `starttime` column.
- **Java Bean Invariant**: When any `new Session()` subclass (`CodingSession`, `WorkingSession`, `LearningSession`, `GamingSession`, `ReadingSession`, `MeditationSession`, `SportSession`, `SleepSession`, `LectureSession`) is instantiated in memory for Thymeleaf form binding, `start` is automatically set to `now()`.

### 3.2. Controller Layer: [`SessionController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java)
1. **Sub-hub Views**:
   - `viewCodingSessions`: Change `cs.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));` and set `cs.setEnd(null);` (so it represents a clean start of a session or can be optionally filled).
   - `viewWorkingSessions`: Change `ws.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));` and set `ws.setEnd(null);`.
2. **Defensive Status Initialization**:
   In `initializeSessionStatus(Session session, Long breakMinutes)`:
   ```java
   if (session.getStart() == null) {
       session.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
   }
   ```
   Retain this check to guarantee that direct programmatic submissions or empty bindings never violate the PostgreSQL `starttime NOT NULL` constraint.
3. **Stop Endpoint**:
   `POST /sessions/{id}/stop`:
   ```java
   LocalDateTime stopTime = (end != null) ? end : LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
   ```
   Already supports both explicit `end` timestamp from the modal and implicit `now()` from Quick Stop.

### 3.3. Frontend Client-Side: [`sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sessions.html)
Add a standardized local ISO datetime formatter in JavaScript:
```javascript
function formatLocalDateTime(d) {
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
```

1. **Log Session Modal (`#newSessionModal`)**:
   On `show.bs.modal`, when in create mode (`!id`):
   ```javascript
   const nowStr = formatLocalDateTime(new Date());
   ['coding', 'working', 'learning', 'gaming', 'reading', 'meditation', 'sport', 'sleep', 'lecture'].forEach(t => {
       const f = document.getElementById('form-' + t);
       if (f) {
           f.reset();
           const startIn = f.querySelector('input[name="start"]');
           if (startIn) startIn.value = nowStr;
           const endIn = f.querySelector('input[name="end"]');
           if (endIn) endIn.value = '';
       }
       const idIn = document.getElementById(t + 'Id');
       if (idIn) idIn.value = '';
   });
   ```
   This ensures that even if the page has been idle in a browser tab for hours, clicking "+ Log Session" injects the exact current client time.

2. **Finish / Stop Session Modal (`#stopSessionModal`)**:
   In `setStopModalTarget(btn)` and `stopSessionModal.addEventListener('show.bs.modal', ...)`:
   ```javascript
   function setStopModalTarget(btn) {
       const id = btn.getAttribute('data-session-id');
       const form = document.getElementById('stopSessionForm');
       if (form && id) {
           form.action = '/sessions/' + id + '/stop';
       }
       const endTimeInput = document.getElementById('stopModalEndTime');
       if (endTimeInput) {
           endTimeInput.value = formatLocalDateTime(new Date());
       }
   }
   ```
   Update the helper text under `#stopModalEndTime`:
   ```html
   <div class="form-text text-muted">Pre-filled with the current time. Adjust if logging retroactively.</div>
   ```

### 3.4. Sub-Hub Consistency
Verify and align standalone views:
- [`coding-sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/coding-sessions.html): `start` bound to `codingSession.start` (`now()`), `end` left empty.
- [`working-sessions.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/working-sessions.html): `start` bound to `workingSession.start` (`now()`), `end` left empty.
- [`sports.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sports.html) & [`meditations.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/meditations.html): already initialize `now().truncatedTo(ChronoUnit.MINUTES)`.

---

## 4. Implementation Steps & Workflow

1. **Step 1: Entity Default (`Session.java`)**
   - Set default field value `private LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);`.
2. **Step 2: Controller Alignment (`SessionController.java`)**
   - Update `viewCodingSessions` to initialize `cs.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))` and `cs.setEnd(null)`.
   - Update `viewWorkingSessions` to initialize `ws.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))` and `ws.setEnd(null)`.
3. **Step 3: Frontend Enhancements (`sessions.html`)**
   - Add `formatLocalDateTime(date)` helper.
   - Update `newSessionModal` `show.bs.modal` event handler to set all `input[name="start"]` to `nowStr` on create.
   - Update `setStopModalTarget` and `stopSessionModal` to set `#stopModalEndTime` to `nowStr`.
   - Update modal hint text.
4. **Step 4: Tests & Verification**
   - Add test case `testStopSessionWithPrefilledEnd()` and `testSessionStartDefaultNow()` in `SessionControllerTest.java` and `SessionModelTest.java`.
   - Run integration tests: `./gradlew test -x jacocoTestReport -x compileAotTestJava`.
5. **Step 5: Documentation & Changelog**
   - Update [`CHANGELOG.md`](file:///home/simon/code/eclipse/slm/CHANGELOG.md) under active version.

---

## 5. Verification Plan

### Automated Tests
- Run unit tests:
  ```bash
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests de.oberamsystems.slm.controller.SessionControllerTest -x jacocoTestReport -x compileAotTestJava
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test --tests de.oberamsystems.slm.model.SessionModelTest -x jacocoTestReport -x compileAotTestJava
  ```
- Run full integration test suite:
  ```bash
  JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew test -x jacocoTestReport -x compileAotTestJava
  ```

### Manual Verification
- Open Sessions Hub `/sessions`.
- Click "+ Log Session": verify that all tabs have their "Start Time" initialized to the current minute.
- Start an ongoing session (leave End Time empty).
- Verify the active session hero banner and table row display with running timer.
- Click "Stop Session" on the active session: verify that `#stopModalEndTime` in the finish modal is pre-filled with the exact current time.
- Click "Save & Finish": verify session is saved as completed with correct end time and net duration calculated.

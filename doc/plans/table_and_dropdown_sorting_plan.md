# Plan: Consistent Table & Dropdown Sorting Across SLM

## 1. Problem Statement & Objectives

### Current State
Across Simon's Life Manager (SLM), many controllers currently fetch entities with unconstrained `repo.findAll()`:
- Tables frequently display rows in database insertion order (or arbitrary primary key order) rather than chronological order.
- Dropdown selection menus (`<select>`) display items in unpredictable order rather than alphabetical order, making finding entities (doctors, routes, cars, games, books, employers, etc.) difficult as data grows.
- Train lines currently appear in insertion order (`S4`, `RE90`, `RE28`, `S1`, `RE1`) rather than natural alphanumeric line order (`RE1`, `RE28`, `RE90`, `S1`, `S4`).

### Objectives
1. **Tables (Date Descending)**:
   - Ensure **all tables with date/timestamp fields** display entries **sorted by date descending** (most recent first: `DESC`).
   - For entity tables without date fields (e.g. `speciality`, `employer`, `point`, `trainline`), sort by name/title alphabetically ascending or natural line order.
2. **Dropdown Menus (Alphabetical Ascending)**:
   - Ensure **all `<select>` dropdown menus** are sorted **alphabetically ascending (`ASC`)** by their human-readable display property (`name`, `title`, `lastname`, etc.).
3. **Train Lines Exception (Natural Alphanumeric Order)**:
   - Train Lines in dropdown menus (and tables) must be sorted by **natural/numeric line order** (e.g. `RE1`, `RE28`, `RE90`, `S1`, `S4`), splitting the alphabetic category prefix (`RE`, `S`, `ICE`, etc.) and the line number (`1`, `28`, `90`).

---

## 2. Comprehensive System-Wide Audit

Below is the exhaustive matrix of all controllers, their tables, and their dropdown menus.

### A. Sessions & Time Tracking (`SessionController.java`, `SleepTimeController.java`)

| View / URL | Table & Sort Order | Dropdown Menus & Sort Order |
| :--- | :--- | :--- |
| `/sessions` | `sessions`: `starttime DESC` | Categories / quick-log modals: Alphabetical |
| `/coding-sessions` | `coding_sessions`: `starttime DESC` | Languages / Projects (if dropdown): Alphabetical |
| `/gaming-sessions` | `gaming_sessions`: `starttime DESC` | `games`: `title ASC`<br>`devices`: `name ASC` |
| `/sport-sessions` | `sport_sessions`: `starttime DESC` | `sports`: `name ASC` |
| `/reading-sessions` | `reading_sessions`: `starttime DESC` | `books`: `title ASC` |
| `/meditation-sessions` | `meditation_sessions`: `starttime DESC` | `types` (if dropdown): Alphabetical |
| `/learning-sessions` | `learning_sessions`: `starttime DESC` | `learningItems`: `title ASC` |
| `/working-sessions` | `working_sessions`: `starttime DESC` | `employers`: `name ASC` |
| `/sleep-times` | `sleeptimes`: `gotobed DESC` | — |
| `/sleeptime-bb` | `sleeptimes`: `gotobed DESC` | — |

### B. Mobility & Transport (`TrainTripController2.java`, `RouteController.java`)

| View / URL | Table & Sort Order | Dropdown Menus & Sort Order |
| :--- | :--- | :--- |
| `/train-trips` | `traintrips`: `start DESC` | **`lines`**: **Natural Line Order** (`RE1`, `RE28`, `RE90`, `S1`, `S4`)<br>`stations`: `name ASC` |
| `/train-stations` | `stations`: `name ASC` | **`lines`**: **Natural Line Order** (`RE1`, `RE28`, `RE90`, etc.) |
| `/train-lines` | `lines`: **Natural Line Order** | — |
| `/car-trips` | `cartrips`: `starttime DESC` | `cars`: `name ASC`<br>`routes`: `name ASC` |
| `/routes` | `routes`: `name ASC` | `startPoint`: `name ASC`<br>`endPoint`: `name ASC` |
| `/points` | `points`: `isHome DESC, name ASC` | — |
| `/cars` | `cars`: `name ASC` | `manufacturers`: `name ASC`<br>`vendors`: `name ASC` |

### C. Health & Medical (`DoctorController.java`, `SportController.java`, `MeditationController.java`)

| View / URL | Table & Sort Order | Dropdown Menus & Sort Order |
| :--- | :--- | :--- |
| `/appointments` | `appointments`: `departure DESC` (or `start DESC`) | `doctors`: `lastname ASC, firstname ASC`<br>`specialities`: `title ASC` |
| `/appointment` | `appointments`: `start DESC` | Filter range: date order |
| `/doctors` | `doctors`: `lastname ASC, firstname ASC` | `specialities`: `title ASC`<br>`points` (practice): `name ASC`<br>`basePoints` (origin): `name ASC` |
| `/specialities` | `specialities`: `title ASC` | — |
| `/sports` | `sports`: `name ASC` | — |
| `/meditations` | `meditations`: `starttime DESC` | — |

### D. Finance, Contracts & Purchases (`ContractController.java`, `PurchaseController.java`, `EmployerController.java`)

| View / URL | Table & Sort Order | Dropdown Menus & Sort Order |
| :--- | :--- | :--- |
| `/contracts` | `contracts`: `startDate DESC` | `contractors`: `name ASC` |
| `/contractors` | `contractors`: `name ASC` | — |
| `/employers` | `employers`: `name ASC` | — |
| `/purchases` | `purchases`: `date DESC` | `vendors`: `name ASC`<br>`products`: `name ASC` |
| `/vendors` | `vendors`: `name ASC` | — |

### E. Habits & Activities (`HabitController.java`, `HabitManagementController.java`, `ActivityController.java`, `ActivityManagementController.java`)

| View / URL | Table & Sort Order | Dropdown Menus & Sort Order |
| :--- | :--- | :--- |
| `/habit-entries` | `habit_entries`: `date DESC` (or `timestamp DESC`) | `habits`: `name ASC` |
| `/habits` | `habits`: `name ASC` | — |
| `/activity-entries` | `activity_entries`: `date DESC` | `activities`: `name ASC` |
| `/activities` | `activities`: `name ASC` | — |

### F. Personal Life & Social (`DiaryEntryController.java`, `SelfieController.java`, `HumanController.java`, `HumanManagementController.java`, `MoodController.java`, `GratitudeController.java`, `CalendarEventController.java`)

| View / URL | Table & Sort Order | Dropdown Menus & Sort Order |
| :--- | :--- | :--- |
| `/diary` | `diaryEntries`: `date DESC` | — |
| `/selfies` | `selfies`: `createdAt DESC` | — |
| `/humans` | `humans`: `lastname ASC, firstname ASC` | — |
| `/encounters` / `/interactions` | `encounters`: `date DESC` | `humans`: `lastname ASC, firstname ASC` |
| `/mood` | `moods`: `date DESC` | — |
| `/gratitudes` | `gratitudes`: `date DESC` | — |
| `/calendar-events` | `calendarEvents`: `start DESC` | — |

### G. Media & Books (`BookController.java`, `BookManagementController.java`, `GameController.java`, `LearningController.java`)

| View / URL | Table & Sort Order | Dropdown Menus & Sort Order |
| :--- | :--- | :--- |
| `/books` | `books`: `title ASC` | `authors`: `lastname ASC, firstname ASC`<br>`presses`: `name ASC` |
| `/authors` | `authors`: `lastname ASC, firstname ASC` | — |
| `/presses` | `presses`: `name ASC` | — |
| `/games` | `games`: `title ASC` | `publishers`: `name ASC`<br>`studios`: `name ASC` |
| `/publishers` | `publishers`: `name ASC` | — |
| `/studios` | `studios`: `name ASC` | — |
| `/devices` | `devices`: `name ASC` | — |
| `/learning-items` | `learningItems`: `lastUpdate DESC` (or `title ASC`) | — |

---

## 3. Technical Implementation Architecture

### A. Spring Data JPA Sorting (`Sort.by(...)`)
In Spring Data JPA, `JpaRepository` inherits `findAll(Sort sort)`:
- Date Descending Tables:
  ```java
  repo.findAll(Sort.by(Sort.Direction.DESC, "starttime"));
  repo.findAll(Sort.by(Sort.Direction.DESC, "departure"));
  repo.findAll(Sort.by(Sort.Direction.DESC, "gotobed"));
  repo.findAll(Sort.by(Sort.Direction.DESC, "date"));
  ```
- Alphabetical Dropdowns & Non-Date Tables:
  ```java
  doctorRepo.findAll(Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname")));
  pointRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
  carRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
  specRepo.findAll(Sort.by(Sort.Direction.ASC, "title"));
  employerRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
  ```

### B. Natural Alphanumeric Sort for Train Lines
Because train line names combine letters and numbers (e.g. `RE1`, `RE28`, `RE90`, `S1`, `S4`), standard ASCII sorting produces `RE1, RE28, RE90, S1, S4`. However, if lines like `RE2` or `S10` exist, ASCII sorting would place `RE10` before `RE2` and `S10` before `S4`.
To guarantee true natural/numeric order across all cases, we will introduce a clean comparator utility:
```java
public class TrainLineComparator implements Comparator<TrainLine> {
    private static final Pattern PATTERN = Pattern.compile("^([a-zA-Z]+)?(\\d+)?(.*)$");

    @Override
    public int compare(TrainLine a, TrainLine b) {
        if (a == null || a.getName() == null) return 1;
        if (b == null || b.getName() == null) return -1;

        Matcher m1 = PATTERN.matcher(a.getName().trim());
        Matcher m2 = PATTERN.matcher(b.getName().trim());

        if (m1.matches() && m2.matches()) {
            String prefix1 = m1.group(1) != null ? m1.group(1).toUpperCase() : "";
            String prefix2 = m2.group(1) != null ? m2.group(2).toUpperCase() : "";
            int prefixCmp = prefix1.compareTo(prefix2);
            if (prefixCmp != 0) return prefixCmp;

            String numStr1 = m1.group(2);
            String numStr2 = m2.group(2);
            if (numStr1 != null && numStr2 != null) {
                try {
                    int n1 = Integer.parseInt(numStr1);
                    int n2 = Integer.parseInt(numStr2);
                    int numCmp = Integer.compare(n1, n2);
                    if (numCmp != 0) return numCmp;
                } catch (NumberFormatException ignored) {}
            }
        }
        return a.getName().compareToIgnoreCase(b.getName());
    }
}
```
In `TrainTripController2.java`:
```java
List<TrainLine> sortedLines = lineRepo.findAll();
sortedLines.sort(new TrainLineComparator());
model.addAttribute("lines", sortedLines);
```
This guarantees that lines always display in natural line order: `RE1, RE28, RE90, S1, S4` in both the table and dropdowns.

---

## 4. Phased Implementation Plan

### Phase 1: Mobility & Transport (`TrainTripController2`, `RouteController`)
- **`TrainTripController2.java`**:
  - `traintrips2`: `Sort.by(Sort.Direction.DESC, "start")`
  - `lines`: sorted with `TrainLineComparator`
  - `stations`: `Sort.by(Sort.Direction.ASC, "name")`
- **`RouteController.java`**:
  - `cartrips`: `Sort.by(Sort.Direction.DESC, "starttime")`
  - `routes`: `Sort.by(Sort.Direction.ASC, "name")`
  - `cars`: `Sort.by(Sort.Direction.ASC, "name")`
  - `points`: `Sort.by(Sort.Direction.ASC, "name")`
  - `manufacturers`: `Sort.by(Sort.Direction.ASC, "name")`
  - `vendors`: `Sort.by(Sort.Direction.ASC, "name")`

### Phase 2: Medical & Health (`DoctorController.java`, `SportController.java`, `MeditationController.java`)
- **`DoctorController.java`**:
  - `appointments`: `Sort.by(Sort.Direction.DESC, "departure")` (or `start DESC`)
  - `doctors`: `Sort.by(Sort.Direction.ASC, "lastname").and(Sort.by(Sort.Direction.ASC, "firstname"))`
  - `specialities`: `Sort.by(Sort.Direction.ASC, "title")`
  - `points`: `Sort.by(Sort.Direction.ASC, "name")`

### Phase 3: Sessions & Sleep Tracking (`SessionController.java`, `SleepTimeController.java`)
- **`SessionController.java`**:
  - `sessions` and all category tables (`gaming`, `coding`, `reading`, `meditation`, `sport`, `learning`, `working`): `Sort.by(Sort.Direction.DESC, "starttime")`
  - Dropdowns:
    - Games: `title ASC`
    - Devices: `name ASC`
    - Books: `title ASC`
    - Sports: `name ASC`
    - Learning Items: `title ASC`
    - Employers: `name ASC`
- **`SleepTimeController.java`**:
  - `sleeptimes`: `Sort.by(Sort.Direction.DESC, "gotobed")`

### Phase 4: Finance, Contracts & Purchases (`ContractController.java`, `PurchaseController.java`, `EmployerController.java`)
- **`ContractController.java`**:
  - `contracts`: `Sort.by(Sort.Direction.DESC, "startDate")`
  - `contractors`: `Sort.by(Sort.Direction.ASC, "name")`
- **`PurchaseController.java`**:
  - `purchases`: `Sort.by(Sort.Direction.DESC, "date")`
  - `vendors`: `Sort.by(Sort.Direction.ASC, "name")`
- **`EmployerController.java`**:
  - `employers`: `Sort.by(Sort.Direction.ASC, "name")`

### Phase 5: Personal, Social & Media (`BookController.java`, `GameController.java`, `DiaryEntryController.java`, `SelfieController.java`, `HumanController.java`, `HabitController.java`, `ActivityController.java`, `CalendarEventController.java`)
- Standardize all remaining controllers to `date DESC` for history/log tables and `ASC` for catalogs and dropdowns.

---

## 5. Verification & Testing

| Target | Verification Method | Expected Result |
| :--- | :--- | :--- |
| **Train Lines** | Check `/train-lines` table and `/train-trips` dropdown in browser | Order is strictly `RE1, RE28, RE90, S1, S4` |
| **Train Trips** | Check `/train-trips` table | Trips ordered by most recent first |
| **Car Trips** | Check `/car-trips` table and dropdowns | Trips ordered by date DESC; cars & routes alphabetical |
| **Appointments & Doctors** | Check `/appointments` and `/doctors` | Appointments date DESC; doctors and specialities alphabetical |
| **Sessions Hub & Categories** | Check `/sessions`, `/gaming-sessions`, `/working-sessions` | Sessions date DESC; dropdowns (games, employers, books) alphabetical |
| **Automated Tests** | Run `./gradlew test -x jacocoTestReport -x compileAotTestJava` | All controller tests pass with sorted mock responses |
| **Changelog** | Document under `[0.0.2b] - 2026-10-18` | Changes listed clearly under Changed |

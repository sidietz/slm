# UI Modernization Plan: Leveraging the Generalized Session Architecture

## 1. Objective

Now that the backend domain models and database migration are fully in place with `InheritanceType.JOINED`, UUIDv7 keys, and database-calculated `duration` and `net_duration`, this plan outlines the UI overhaul to expose all these new capabilities to the user in a modern, intuitive, and cohesive interface.

---

## 2. Key New Features to Expose in the UI

| Backend Feature | UI Capability |
| :--- | :--- |
| **Polymorphic Session Model** | **Unified Sessions Hub (`/sessions`)**: A single centralized dashboard showing all activity (Coding, Working, Learning, Gaming, Reading, Meditation, Sport) with type filtering and aggregated stats. |
| **Gross vs. Net Duration & Breaks** | **Break Tracking & Net Duration Display**: Inputs for `breakDuration` (in minutes), display of both total elapsed duration and focused net duration. |
| **Productivity & Focus Metrics** | **Focus Rating (1–5 Stars)** and **Outcome Summary**: Visual rating selector and dedicated outcomes achieved card. |
| **New Session Types** | **Dedicated Coding & Working Pages**: Full support for tickets, repos, branches, clients, billable flags, and hourly earnings. |
| **Session Lifecycle (`status`)** | **Session Status Badges & Quick Timer**: Badges for `COMPLETED`, `IN_PROGRESS`, `PAUSED` and real-time start/stop stopwatch controls. |

---

## 3. UI Architecture & View Specifications

### 3.1. Unified Sessions Hub (`/sessions`)
The centerpiece of the session management system.

#### A. KPI Metric Cards (Top Section)
1. **Total Net Focused Time:** Sum of `net_duration` across all filtered sessions (formatted as `Xh Ym`).
2. **Total Break Time:** Sum of `break_duration` across filtered sessions.
3. **Average Focus Rating:** Visual star score (e.g. `4.2 / 5.0`).
4. **Activity Breakdown Chart / Distribution Bar:** Visual bar showing percentage of time spent on Working vs Coding vs Learning vs Gaming vs Others.

#### B. Filter Bar & Type Switcher
- Filter Pills: `All`, `Coding`, `Working`, `Learning`, `Gaming`, `Reading`, `Meditation`, `Sport`.
- Date Range Quick Filters: `Today`, `This Week`, `This Month`, `All Time`.
- Status Filter: `All`, `Completed`, `In Progress`.

#### C. Interactive "Log / Start Session" Card (Accordion / Modal)
- **Session Type Selector:** Dropdown or pill selector (`Coding`, `Working`, `Learning`, `Gaming`, etc.).
- **Dynamic Field Injection (Client-side JS):**
  - Selecting **Coding**: Shows inputs for Project, Repository, Git Branch, Ticket #, Language.
  - Selecting **Working**: Shows inputs for Client ([`Contractor`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Contractor.java) dropdown), Project, Billable checkbox, Hourly Rate, Task Category.
  - Selecting **Learning**: Shows inputs for [`LearningItem`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/LearningItem.java) dropdown, Units Completed.
  - Selecting **Gaming**: Shows inputs for [`Game`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Game.java) dropdown, [`Device`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Device.java) dropdown, Game Mode.
  - Selecting **Reading**: Shows inputs for [`Book`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/Book.java) dropdown, Start Page, End Page.
  - Selecting **Sport**: Shows inputs for [`SportType`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/model/SportType.java) dropdown.
- **Universal Fields:**
  - Start Time & End Time (with "Now" buttons).
  - Break Duration (minutes input).
  - Focus Rating (interactive 1–5 star rating widget).
  - Comment & Outcome Summary.

#### D. Unified Data Table
- Columns:
  1. **Type & Status:** Badge with distinct theme colors (`Coding` = Primary Blue, `Working` = Indigo, `Learning` = Purple, `Gaming` = Warning Orange, `Reading` = Cyan, `Meditation` = Success Green, `Sport` = Danger Red).
  2. **Context / Target:** Primary link/name (e.g., ticket + repo for Coding; client + project for Working; book title for Reading; game title for Gaming).
  3. **Timestamps:** Start & End (`dd.MM.yyyy HH:mm`).
  4. **Duration:** Gross duration (`duration`) and Net focus duration (`net_duration`).
  5. **Breaks:** Displayed in minutes (e.g. `15m`).
  6. **Focus Rating:** Visual stars (★ ★ ★ ★ ☆).
  7. **Outcome / Summary:** Popover or preview tooltip.
  8. **Actions:** Delete / View details.

---

### 3.2. Dedicated Session Pages

1. **Coding Sessions (`/coding-sessions`)**:
   - Tailored specifically for developer workflows.
   - Shows active Git branches, ticket badges, language badges.
2. **Working Sessions (`/working-sessions`)**:
   - Tailored for freelance / client work.
   - Shows billable tags, calculates total billable amount (`hourly_rate * net_duration_hours`), client grouping.
3. **Refactored Existing Pages (`/learning-sessions`, `/gaming-sessions`, `/reading-sessions`, `/meditations`, `/sports`)**:
   - Updated to include `break_duration`, `focus_rating`, and `outcome_summary`.
   - Updated to use UUID string IDs.

---

### 3.3. Navigation & Dashboard Updates

- **[`index.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/index.html)**:
  - Add a primary **"Sessions Hub"** quick action card leading to `/sessions`.
- **[`activity-management.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/activity-management.html)**:
  - Add links for **Coding Sessions** and **Working Sessions**.
  - Add direct link to **Unified Sessions**.
- **[`habit-management.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/habit-management.html)**:
  - Ensure links route to the modernized session pages.

---

## 4. Backend Controller & DTO Architecture

### 4.1. `SessionController`
Handles the unified `/sessions` endpoint:
- `GET /sessions`: Model attributes include:
  - `sessions`: List of all sessions (or filtered by `?type=CODING`).
  - `totalNetDurationFormatted`: Aggregated net duration.
  - `totalBreakDurationFormatted`: Aggregated break duration.
  - `avgFocusRating`: Average focus score.
  - Dropdown reference lists: `learningItems`, `games`, `devices`, `books`, `sportTypes`, `contractors`.
- `POST /sessions/coding`: Submit coding session.
- `POST /sessions/working`: Submit working session.
- `POST /sessions/learning`: Submit learning session.
- `POST /sessions/gaming`: Submit gaming session.
- `POST /sessions/reading`: Submit reading session.
- `POST /sessions/meditation`: Submit meditation session.
- `POST /sessions/sport`: Submit sport session.
- `POST /sessions/delete/{id}`: Delete any session polymorphically via `SessionRepository`.

### 4.2. Helper / Formatter
- Create `DurationFormatter` utility or Thymeleaf helper for converting PostgreSQL `Duration` into human-friendly strings (e.g. `2h 15m`).

---

## 5. Implementation Steps & Verification

1. **Step 1: Controller & Helper Implementation**
   - Create `DurationFormatter` utility.
   - Implement [`SessionController`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/SessionController.java) with endpoints for all session types.
2. **Step 2: Templates & Styling**
   - Create `templates/sessions.html` (the Unified Sessions Hub) with rich Bootstrap 5.3 styling, KPI metrics, dynamic session creation modal, and responsive table.
   - Create `templates/coding-sessions.html` and `templates/working-sessions.html`.
   - Modernize existing `templates/learning-sessions.html`, `templates/gaming-sessions.html`, etc., adding `breakDuration` and `focusRating`.
   - Update navigation in `index.html`, `activity-management.html`, and `habit-management.html`.
3. **Step 3: Controller Tests & Verification**
   - Create `SessionControllerTest` verifying all endpoints, model attributes, and redirects.
   - Run `./gradlew test -x jacocoTestReport` to ensure 100% test pass rate.

# Simon's Life Manager (SLM)

[![Version](https://img.shields.io/badge/version-0.0.3--rc8-informational?logo=git&logoColor=white)](CHANGELOG.md)
[![Java Version](https://img.shields.io/badge/Java-27-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.2.0--M2-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18_%7C_PostGIS_3.6-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3.8-7952B3?logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![JUnit 6](https://img.shields.io/badge/JUnit-6.1.3-25A162?logo=junit5&logoColor=white)](https://junit.org/)

**Simon's Life Manager (SLM)** is a comprehensive, self-hosted personal operating system designed to track, organize, and balance every facet of life—productivity, physical and mental health, recovery, habits, personal finances, library, and spatial travel.

---

## Table of Contents

- [Core Modules & Features](#core-modules--features)
  - [1. Sessions Hub & Time Tracking](#1-sessions-hub--time-tracking)
  - [2. Health, Sleep & Recovery](#2-health-sleep--recovery)
  - [3. Habit & Personal Growth](#3-habit--personal-growth)
  - [4. Professional & Financial Management](#4-professional--financial-management)
  - [5. Logistics, Spatial GIS & Travel](#5-logistics-spatial-gis--travel)
  - [6. Library & Media Catalog](#6-library--media-catalog)
  - [7. Academic Management & Studies](#7-academic-management--studies)
  - [8. Model Context Protocol (MCP) Server](#8-model-context-protocol-mcp-server)
  - [9. Universal Modal-First UI Architecture & In-Place Editing](#9-universal-modal-first-ui-architecture--in-place-editing)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [1. Local Database Setup](#1-local-database-setup)
  - [2. Running with Gradle](#2-running-with-gradle)
  - [3. Running via Docker Compose](#3-running-via-docker-compose)
  - [4. Default Authentication](#4-default-authentication)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Documentation & Decision Records](#documentation--decision-records)

---

## Core Modules & Features

### 1. Sessions Hub & Time Tracking (`/sessions`)
A polymorphic time-tracking system built around a four pillars life balance model:
- **Productivity**: Coding, Working, Learning, Reading, and Lecture sessions.
- **Sport**: Dedicated physical training, running, and workouts.
- **Wellness**: Biological restoration, meditation, and sleep sessions.
- **Leisure**: Recreational gaming sessions.

**Key Metrics & Analytics:**
- **Net Focused Time**: Strictly tracks intentional cognitive focus hours—sleep and meditation hours are decoupled to prevent biological rest from artificially inflating focus statistics.
- **Context-Adaptive KPI Cards**: Dynamically updates headers (e.g., *Net Focused Time* vs. *Active Training Time* vs. *Rest & Recovery Time*; *Avg Focus Rating* vs. *Avg Sleep Quality* vs. *Avg Workout Intensity*).
- **Break & Disruption Tracking**: Accurately accounts for pause durations and night-time sleep disruptions.

### 2. Health, Sleep & Recovery
- **Sleep Analytics (`/sleeptime-bb`, `/sleep-times`)**: Interactive Billboard.js charts visualizing bedtime, wake time, duration, deep sleep, REM sleep, awake periods, and dream notes.
- **Meditation (`/meditations`)**: Log mindfulness and meditation practice with focus ratings.
- **Mood Tracking (`/moods`, `/mood-bb`)**: Daily mood logging and time-series mood trends.
- **Doctor & Medical Care (`/doctors`, `/appointments`)**: Directory of healthcare providers, specialities, and medical appointments with spatial GIS location mapping.
- **Daily Selfies (`/selfies`)**: Secure photo logging with directory traversal protection (CWE-22) supporting `.jpg`, `.jpeg`, and `.heic` images without lossy transcoding.

### 3. Habit & Personal Growth
- **Habits (`/habits`, `/last-done-habit-entries`)**: Recurring habit tracking with streak and completion recency.
- **Learning Hub (`/learnings`, `/learning-sessions`)**: Track skills, topics, course units, and dedicated study time.
- **Diary & Gratitude (`/diary`, `/gratitudes`)**: Daily journaling and gratitude logging.

### 4. Professional & Financial Management
- **Working Sessions (`/working-sessions`)**: Client-specific work tracking with billable hours, hourly rates, and task categories.
- **Coding Sessions (`/coding-sessions`)**: Deep work tracking linked to repositories, git branches, programming languages, and issue tickets.
- **Contracts & Employers (`/contracts`, `/employers`)**: Manage client relationships, employment contracts, and terms.
- **Spend Management (`/purchases`)**: Log expenses, purchase categories, and vendor relationships.

### 5. Logistics, Spatial GIS, Vehicles & Fuel Economy
- **Vehicles & Refuelings (`/cars`, `/refuelings`)**: Fleet management, odometer readings, and refueling records with automated consumption (`L/100km`) and price per liter calculation.
- **Fuel & EV Charging Stations (`/stations`)**: Polymorphic station directory supporting Gas Stations (with TankenTanken UUID) and EV Charging Stations (with 1:N Charging Point stalls, power in kW, and European plug standards like CCS2 and Type 2).
- **Spritmonitor CSV Import**: One-click import for Spritmonitor CSV exports (`1676970_fuelings.csv`) with German decimal parsing, ISO country normalization, and station auto-provisioning.
- **Interactive Refueling Analytics**: Billboard.js time-series trend chart switchable dynamically between Consumption (`L/100km`), Trip Distance (`km`), and Fuel Price (`€/L`).
- **Train Trips (`/train-trips`)**: Detailed rail journey logs with lines, train stations, and connection tracking.
- **Spatial Routes & Points (`/routes`, `/points`)**: PostGIS-powered geospatial coordinate tracking and distance calculations.
- **Human Management (`/humans`)**: Personal CRM for organizing contacts, friends, and relationships.

### 6. Library & Media Catalog
- **Book Management (`/books`, `/authors`, `/readingsessions`)**: Personal library, reading speeds, pages read, and publisher details.
- **Gaming Hub (`/games`, `/gaming-sessions`)**: Video game catalog tracking platforms, hardware devices, game modes, and playtime.

### 7. Academic Management & Studies (`/courses`)
Comprehensive tracking for university coursework, lecture notes, syllabus management, and exam preparation:
- **University Courses (`/courses`)**: Manage course catalogs with ECTS credits tracking, semester alignment, lecturer assignments, default lecture halls, and virtual meeting links.
- **Deterministic Semester Ordering**: Automated chronological sorting of academic terms (`WiSe 2026/27` > `SoSe 2026` > `WS 2025/26`) and consistent intra-semester alphabetical course listings across both "All Semesters" and single-semester views.
- **Course Detail Dashboard (`/courses/{id}`)**: Dedicated course cockpit featuring lecturer contact cards, syllabus markdown, lecture sessions timeline, and linked lecture notes.
- **Lecture Notes (`/notes/{id}`)**: Rich markdown note-taking attached to courses and lecture sessions, with key concepts tags, homework reminders and todo tracking, and 1-5 star exam relevance ratings.
- **Lecture Sessions**: Polymorphic `LectureSession` entities extending the root `Session` hierarchy to track lecture attendance, live focus, and classroom locations.
- **Exams & Grading**: Track exam schedules within the next 90 days, exam locations, passing status, and final grade recordings.
- **Spring AI MCP Server Tools**: AI-callable tools for retrieving courses, listing chronologically sorted semesters, inspecting syllabus and course details, full-text searching lecture notes, and logging new notes.

### 8. Model Context Protocol (MCP) Server
SLM embeds a [Spring AI](https://spring.io/projects/spring-ai) Model Context Protocol (MCP) server providing external AI assistants (e.g. Claude Desktop, Antigravity IDE, custom autonomous agents) with direct, structured, read-write access to life tracking data via Server-Sent Events (SSE) at `/sse` and message handling at `/mcp/message`, secured with API Key authentication (`X-API-KEY` or `?apiKey=...`).

The server exposes 27 domain tools organized across 6 specialized controllers:

#### 1. Academic & University Management (`McpAcademicTools`)
- **`getSemesters()`**: Retrieve all distinct academic semesters, sorted chronologically descending (most recent semester first via `SemesterComparator.DESCENDING`).
- **`getCourses(semester)`**: Retrieve university courses, optionally filtered by semester, sorted deterministically by `CourseComparator`.
- **`getCourseDetails(courseId)`**: Get in-depth course information including syllabus markdown, lecturer details, lecture notes, and attended lectures.
- **`logLectureSession(...)`**: Log attendance at a university lecture session with start/end time, room/URL, focus rating, and topics.
- **`createLectureNote(...)`**: Save or update lecture notes in rich Markdown format with key concepts summary, homework assignments, and 1-5 star exam relevance rating.
- **`searchLectureNotes(query)`**: Search across all lecture notes by keyword in title, markdown content, or key concepts tags.

#### 2. Sessions & Life Balance (`McpSessionTools`)
- **`getFourPillarsDailyBreakdown(dateStr)`**: Calculate daily Four Pillars life balance breakdown (Productivity focus, Sport training, Wellness sleep/meditation, and Leisure gaming).
- **`getRecentSessions(category, limit)`**: Retrieve recent time tracking sessions filtered by pillar category (`PRODUCTIVITY`, `SPORT`, `WELLNESS`, `LEISURE`, or `ALL`).
- **`getActiveSessions()`**: Retrieve all currently active ongoing sessions (without end time set, status `IN_PROGRESS`).
- **`startSession(sessionType, titleOrProject, comment)`**: Start a new ongoing session without an end time across all polymorphic session types.
- **`stopSession(sessionId, breakMinutes, focusRating, outcomeSummary, comment)`**: Stop an ongoing active session, compute net duration, and record ratings.
- **`logCodingSession(...)`**: Log a completed coding session linked to project name, git branch, and language.
- **`logSleepSession(...)`**: Log a sleep session with bedtime, wake time, quality rating, and night disruptions.

#### 3. Wellness & Habit Tracking (`McpWellnessTools`)
- **`getHabits()`**: Retrieve all tracked habits and their last-done completion timestamp.
- **`completeHabit(habitIdentifier, timestampStr)`**: Record a habit as completed (by name or ID).
- **`logMood(happiness, impetus, stress, dateStr)`**: Log a daily mood rating with happiness, drive/energy, and stress levels (scores 1-5).
- **`logGratitude(description, dateStr)`**: Log a daily gratitude reflection note.

#### 4. Medical & Healthcare (`McpMedicalTools`)
- **`getUpcomingAppointments()`**: Retrieve upcoming medical and doctor appointments with doctor details, speciality, and geodesic air distance.
- **`searchDoctors(query)`**: Search doctors by speciality keyword or doctor name, with practice location and distance from home.
- **`createAppointment(doctorId, startStr, endStr, comment)`**: Schedule a new medical or doctor appointment.

#### 5. Contacts & Life Milestones (`McpLifeEventTools`)
- **`getUpcomingLifeEvents(daysAhead)`**: Retrieve upcoming life events, anniversaries, birthdays, memorials, and milestones sorted by days until occurrence.
- **`getContactLifeEvents(contactId)`**: Retrieve all life events and anniversaries recorded for a specific contact.
- **`createLifeEvent(contactId, type, dateStr, title, notes, secondaryContactId)`**: Create a new milestone or anniversary for a contact.

#### 6. Vehicles, Refuelings & Stations (`McpVehicleTools`)
- **`getRefuelings(carId, limit)`**: Query refueling history with volume in liters, fuel price per liter, total costs, odometer, and consumption.
- **`logRefueling(...)`**: Record a new refueling entry linked to a vehicle and gas station.
- **`getStations(stationType, country)`**: List gas stations (with TankenTanken UUID) and EV charging stations (with power in kW and plug types).
- **`createStation(...)`**: Provision a new gas or EV charging station.

### 9. Universal Modal-First UI Architecture & In-Place Editing
Comprehensive UX modernization and in-place editing across all 30+ entity domains (ADR 024):
- **Universal Modal-First Inputs**: Full-width (`col-12`) data tables paired with Bootstrap 5 dual-purpose modals serving both creation and editing without page reloads.
- **Client-Side Field Prefilling**: Row-level `[ Edit ]` actions instantly populate modal input fields via HTML5 `data-*` attributes and Vanilla JavaScript.
- **Binding Safeguards & Safety**: Global string trimming via `StringTrimmerEditor(true)` converts empty form inputs to `null`, ensuring error-free entity creation with boxed `Long` primary keys while protecting non-destructive in-place updates.

---

## Architecture & Tech Stack

| Layer | Technologies |
|---|---|
| **Language & Runtime** | Java 27 (OpenJDK) |
| **Backend Framework** | Spring Boot 4.2.0-M2 (Spring MVC, Spring Data JPA, Spring Security) |
| **Database** | PostgreSQL 18 with PostGIS 3.6 spatial extension |
| **Object-Relational Mapping** | Hibernate 7.3 with `io.hypersistence:hypersistence-utils-hibernate-73` |
| **Polymorphic Hierarchy** | `InheritanceType.JOINED` (`Session` base entity with concrete sub-tables) |
| **Frontend UI** | Thymeleaf 3, Bootstrap 5.3.8, Bootstrap Icons, Billboard.js / D3, Modal-First In-Place Editing (ADR 024) |
| **Testing** | JUnit 6 (`6.1.3`), Spring Boot Test, Testcontainers (`postgis/postgis:18-3.6`) |
| **AI & Agent Integration** | Spring AI 2.0.1 (Model Context Protocol / MCP Server) |

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 27
- **Database**: PostgreSQL 18 with PostGIS extension (or Docker)
- **Build Tool**: Gradle 8.x (wrapper included via `./gradlew`)

---

## Getting Started

### 1. Local Database Setup

Ensure PostgreSQL is running locally on port `5432` with a database named `slm` and the PostGIS extension enabled:

```sql
CREATE DATABASE slm;
\c slm
CREATE EXTENSION IF NOT EXISTS postgis;
```

Alternatively, launch the database container via Docker:

```bash
docker compose up -d db
```

### 2. Running with Gradle

Start the Spring Boot application:

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

### 3. Running via Docker Compose

To spin up the entire stack (PostgreSQL + Spring Boot application) in containers:

```bash
docker compose up --build
```

Access the service at `http://localhost:6868`.

### 4. Default Authentication

The application is secured with Spring Security. Default in-memory credentials for local development:

| Role | Username | Password |
|---|---|---|
| **User** | `simon` | `N0m1596.` |
| **Admin** | `admin` | `N0m1596.` |

*(Credentials and data source parameters can be overridden via standard Spring environment variables such as `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.)*

---

## Testing

The project incorporates both fast unit tests and realistic integration tests using Testcontainers running a live PostgreSQL 18 + PostGIS 3.6 container.

To execute the test suite:

```bash
./gradlew test -x jacocoTestReport -x compileAotTestJava
```

- **Unit Tests (`*Test.java`)**: Controller unit tests, service validations, and path traversal security checks.
- **Integration Tests (`*IT.java`)**: End-to-end integration workflows (`SleepTimeIT`, `RouteSpatialIT`, `DoctorSpatialIT`, `SessionHierarchyIT`, etc.) verifying JPA joined inheritance queries and real PostGIS spatial calculations.

---

## Project Structure

```
slm/
├── src/main/java/de/oberamsystems/slm/
│   ├── config/              # Global web data binding & conversion configuration
│   ├── controller/          # Spring MVC controllers (SessionController, AcademicController, etc.)
│   ├── model/               # JPA entities, polymorphic Session hierarchy, and repositories
│   ├── mcp/                 # Spring AI Model Context Protocol (MCP) tool endpoints
│   ├── service/             # Domain services (SelfieStorageService, etc.)
│   ├── util/                # Domain comparators (SemesterComparator, CourseComparator, TrainLineComparator)
│   ├── SecurityConfig.java  # Spring Security filter chain and endpoint policies
│   └── SlmApplication.java  # Main application entry point
├── src/main/resources/
│   ├── application.properties # Core database and environment configuration
│   ├── tables.sql           # Base DDL schema and tables
│   ├── enums.sql            # PostgreSQL ENUM type definitions
│   ├── static/              # CSS, JavaScript, and asset files
│   └── templates/           # Thymeleaf templates and UI fragments
├── src/test/java/de/oberamsystems/slm/
│   ├── controller/          # Controller slice tests
│   └── *IT.java             # Testcontainers-backed integration test suite
├── src/test/resources/
│   └── schema-init.sql      # Authoritative schema initialization for integration tests
├── build.gradle             # Build script, dependencies, and Jackson 3 BOM
├── CHANGELOG.md             # Detailed version history and release notes
├── DECISIONS.md             # Architectural Decision Records (ADRs)
└── docker-compose.yml       # Containerized environment definition
```

---

## Documentation & Decision Records

- **[CHANGELOG.md](CHANGELOG.md)**: Detailed historical log of all releases and architectural updates.
- **[DECISIONS.md](DECISIONS.md)**: Architectural Decision Records (ADRs), including the `InheritanceType.JOINED` polymorphic session strategy and duration calculation models.

# Simon's Life Manager (SLM)

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
- **Productivity**: Coding, Working, Learning, and Reading sessions.
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

### 5. Logistics, Spatial GIS & Travel
- **Train Trips (`/train-trips`)**: Detailed rail journey logs with lines, train stations, and connection tracking.
- **Spatial Routes & Points (`/routes`, `/points`)**: PostGIS-powered geospatial coordinate tracking and distance calculations.
- **Human Management (`/humans`)**: Personal CRM for organizing contacts, friends, and relationships.

### 6. Library & Media Catalog
- **Book Management (`/books`, `/authors`, `/readingsessions`)**: Personal library, reading speeds, pages read, and publisher details.
- **Gaming Hub (`/games`, `/gaming-sessions`)**: Video game catalog tracking platforms, hardware devices, game modes, and playtime.

---

## Architecture & Tech Stack

| Layer | Technologies |
|---|---|
| **Language & Runtime** | Java 27 (OpenJDK) |
| **Backend Framework** | Spring Boot 4.2.0-M2 (Spring MVC, Spring Data JPA, Spring Security) |
| **Database** | PostgreSQL 18 with PostGIS 3.6 spatial extension |
| **Object-Relational Mapping** | Hibernate 7.3 with `io.hypersistence:hypersistence-utils-hibernate-73` |
| **Polymorphic Hierarchy** | `InheritanceType.JOINED` (`Session` base entity with concrete sub-tables) |
| **Frontend UI** | Thymeleaf 3, Bootstrap 5.3.8, Bootstrap Icons, Billboard.js / D3 |
| **Testing** | JUnit 6 (`6.1.3`), Spring Boot Test, Testcontainers (`postgis/postgis:18-3.6`) |

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
│   ├── controller/          # Spring MVC controllers (SessionController, DoctorController, etc.)
│   ├── model/               # JPA entities, polymorphic Session hierarchy, and repositories
│   ├── service/             # Domain services (SelfieStorageService, etc.)
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

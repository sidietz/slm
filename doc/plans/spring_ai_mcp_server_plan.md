# Implementation Plan: Spring AI Model Context Protocol (MCP) Server for SLM

## Executive Summary
This architectural implementation plan defines the integration of a native **Model Context Protocol (MCP) Server** into **Simon's Life Manager (SLM)** using **Spring AI 2.0.x**.

By exposing SLM's domain capabilities through MCP, external AI agents (such as Google Antigravity IDE, Claude Desktop, Cursor, or autonomous local subagents) can securely inspect and manage Simon's personal life data—including **Life Events & Anniversaries**, **Polymorphic Focus & Sleep Sessions**, **Four Pillars Life Balance**, **Habits**, **Medical Appointments**, and **Expenses**.

---

## 1. Technical Context & Compatibility Baseline

### 1.1 Technology Alignment
- **Framework Baseline**: Spring Boot `4.2.0-M2` / Spring Framework `7.x`
- **Language**: Java 27 (`JavaLanguageVersion.of(27)`)
- **JSON Platform**: Jackson 3 BOM (`tools.jackson:jackson-bom:3.2.3`)
- **Database**: PostgreSQL 18 with PostGIS 3.6 (`postgis/postgis:18-3.6`)
- **Spring AI Release Stream**: **Spring AI 2.0.x** (BOM: `org.springframework.ai:spring-ai-bom:2.0.1`)
  - *Rationale*: Spring AI 2.0.x is the first generation built natively for the Spring Boot 4 / Spring Framework 7 line with Jackson 3 and Jakarta EE 11 support, avoiding classpath and serialization conflicts.

---

## 2. Architecture & Transport Layer Design

MCP supports two primary transport protocols:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MCP Clients                                     │
│   (Antigravity IDE / Claude Desktop / Cursor / Terminal Agents)        │
└───────────────────▲────────────────────────────────▲───────────────────┘
                    │                                │
            (A) WebMVC SSE / HTTP            (B) Stdio Transport
          (Default - Embedded)             (Optional CLI Subprocess)
                    │                                │
┌───────────────────▼────────────────────────────────▼───────────────────┐
│                      Spring AI MCP Server                              │
│                                                                        │
│   ┌─────────────────────┐  ┌──────────────────┐  ┌─────────────────┐   │
│   │   Tools (@Tool)     │  │    Resources     │  │     Prompts     │   │
│   │ • Life Events       │  │ • Day Summary    │  │ • Morning Brief │   │
│   │ • Focus Sessions    │  │ • Four Pillars   │  │ • Weekly Review │   │
│   │ • Sleep Tracking    │  │ • Contacts List  │  │ • Evening Log   │   │
│   │ • Habits & Medical  │  │                  │  │                 │   │
│   └──────────┬──────────┘  └────────┬─────────┘  └────────┬────────┘   │
│              │                      │                     │            │
│   ┌──────────▼──────────────────────▼─────────────────────▼────────┐   │
│   │                     SLM Domain Services & JPA                  │   │
│   │  (HumanEventRepository, SessionRepository, HumanRepository...) │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### 2.1 Decision: WebMVC Server-Sent Events (SSE) Transport (Primary)
- **Artifact**: `org.springframework.ai:spring-ai-starter-mcp-server-webmvc`
- **Endpoint Structure (Standard Spring AI Defaults)**:
  - **SSE Handshake & Streaming Endpoint**: **`/sse`** (Spring AI default `spring.ai.mcp.server.sse-endpoint=/sse`).
    - Clients initiate a GET request to `http://localhost:8080/sse` to establish the persistent Server-Sent Events stream.
  - **JSON-RPC Message Endpoint**: **`/mcp/message`** (Spring AI default `spring.ai.mcp.server.sse-message-endpoint=/mcp/message`).
    - Clients post tool execution requests and JSON-RPC 2.0 calls to `/mcp/message?sessionId={id}`.
  - **No Custom Remapping**:
    - We leave endpoint properties at their standard defaults, ensuring canonical out-of-the-box compatibility with Spring AI conventions and MCP tooling.
- **Advantages**:
  1. Runs inside the active SLM application container alongside PostgreSQL connection pools.
  2. Enables multiple concurrent AI assistants (e.g. IDE agent + mobile workflow + desktop AI) without starting multiple heavyweight JVM instances.
  3. No interference with `System.out` logging or banner outputs that break stdio JSON-RPC framing.

### 2.2 Optional Stdio Transport (Secondary Profile: `mcp-stdio`)
- Provide a dedicated configuration switch or CLI runner (`--spring.profiles.active=mcp-stdio`) if headless local subprocess execution is needed, redirecting Logback logging to stderr.

---

## 3. Security & Access Control Configuration

SLM enforces strict security boundaries in [`SecurityConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/SecurityConfig.java).

### 3.1 CSRF Exemption for MCP Endpoints
MCP clients communicate using HTTP POST JSON-RPC payloads without browser CSRF tokens.
Therefore, both `/mcp/**` and `/sse` are exempted from CSRF validation:
```java
http.cors(Customizer.withDefaults())
    .csrf(csrf -> csrf.ignoringRequestMatchers("/mcp/**", "/sse", "/api/**"))
```

### 3.2 API Key Authentication (`McpApiKeyAuthFilter`)
As chosen by user preference, MCP endpoints are secured using a dedicated **API Key**:
- **Header & Query Param Support**:
  - Checks header: `X-API-KEY: <key>`
  - Checks query parameter: `GET /sse?apiKey=<key>` (critical for standard EventSource and SSE clients where custom headers cannot be set during browser/client connection initialization).
- **Configuration Property**: `slm.mcp.api-key=${SLM_MCP_API_KEY:slm-mcp-secret-key-2026}` in `application.properties`.
- **Implementation**: Custom `OncePerRequestFilter` registered in `SecurityConfig` before standard authentication filters.
  - Matches paths: `requestURI.startsWith("/mcp") || requestURI.equals("/sse") || requestURI.startsWith("/sse/")`.
  - Validates key against configured property.
  - If valid, populates `SecurityContext` with `ROLE_MCP_CLIENT` and continues filter chain.
  - If invalid or missing, immediately halts with `HTTP 401 Unauthorized` JSON payload.


---

## 4. MCP Server Primitives & Domain Tools

We organize MCP capabilities into high-cohesion tool objects under `de.oberamsystems.slm.mcp`:

### 4.1 Life Events & Anniversaries Tools (`McpLifeEventTools`)
Exposes the ADR 015 events system:
- `get_upcoming_events(int daysAhead)`: Returns upcoming birthdays, wedding/relationship anniversaries, memorials, and career milestones sorted by days remaining.
- `get_events_for_contact(Long humanId)`: Retrieves all historical and upcoming events for a specific contact.
- `create_life_event(Long contactId, String type, String date, String title, String notes, Long secondaryContactId)`: Creates a new anniversary or milestone.

### 4.2 Focus & Session Tools (`McpSessionTools`)
Exposes the ADR 001/011 polymorphic session hierarchy and ADR 012 Net Focused Time purity:
- `get_daily_balance(String date)`: Computes the Four Pillars distribution (Productivity focus duration vs Sport active training vs Wellness rest/recovery vs Leisure recreation).
- `get_recent_sessions(String category, int limit)`: Retrieves recent sessions filtered by category (`PRODUCTIVITY`, `SPORT`, `WELLNESS`, `LEISURE`, `ALL`).
- `log_coding_session(String project, String repository, String start, String end, int breakMinutes, int focusRating, String comment, String outcome)`: Logs a coding session.
- `log_sleep_session(String gotobed, String getup, int breakMinutes, int qualityRating, String notes)`: Logs nightly sleep duration and disruptions.

### 4.3 Health & Doctor Tools (`McpMedicalTools`)
Exposes medical tracking with PostGIS geodesic distances:
- `get_upcoming_appointments()`: Returns upcoming appointments with doctor name, speciality, date, and calculated air distance from home base.
- `search_doctors(String speciality)`: Finds practice points and doctors.

### 4.4 Personal Wellness & Habit Tools (`McpWellnessTools`)
- `get_habits_status()`: Lists all configured habits and their last-done completion dates.
- `record_habit_completed(String habitName, String date)`: Records habit completion for a day.
- `log_daily_mood(int rating, String notes)`: Records daily mood rating (1–5) and journal reflections.

---

## 5. MCP Resources & Prompts

### 5.1 Resources (`McpResourceProvider`)
Read-only contextual URIs providing background context for agents:
1. `slm://dashboard/today`: Live summary of Simon's day (today's net focus time, sleep from previous night, pending habits, and upcoming anniversaries).
2. `slm://contacts/directory`: Compact overview of contacts, categories, and birthdays.
3. `slm://balance/four-pillars-weekly`: Aggregate productivity vs recovery distribution for the past 7 days.

### 5.2 Prompts (`McpPromptProvider`)
Pre-packaged prompt workflows:
1. `morning-briefing`:
   > "Inspect sleep data from last night, list today's scheduled medical appointments, identify pending habits, and highlight any birthdays or anniversaries occurring in the next 7 days."
2. `evening-review`:
   > "Calculate total Net Focused Time logged today across coding, learning, and working sessions. Check if habits were satisfied, and prompt for daily mood and gratitude entry."
3. `focus-session-wrapup`:
   > "Help summarize the outcomes of the latest coding session and assign appropriate focus ratings."

---

## 6. Implementation Steps & Milestones

### Phase 1: Build & Dependency Setup
1. Add `org.springframework.ai:spring-ai-bom:2.0.1` platform import in [`build.gradle`](file:///home/simon/code/eclipse/slm/build.gradle).
2. Add `org.springframework.ai:spring-ai-starter-mcp-server-webmvc` dependency.
3. Configure `application.properties`:
   ```properties
   spring.ai.mcp.server.name=slm-mcp-server
   spring.ai.mcp.server.version=0.0.2g
   spring.ai.mcp.server.type=sync
   # Uses standard Spring AI defaults: /sse for SSE stream and /mcp/message for JSON-RPC
   slm.mcp.api-key=${SLM_MCP_API_KEY:slm-mcp-secret-key-2026}
   ```

### Phase 2: Security & Endpoint Integration
1. Create `de.oberamsystems.slm.security.McpApiKeyAuthFilter` validating `X-API-KEY` header and `?apiKey=` query param on `/mcp/**` and `/sse`.
2. Update [`SecurityConfig.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/SecurityConfig.java):
   - Exempt `/mcp/**` and `/sse` from CSRF protection: `.csrf(csrf -> csrf.ignoringRequestMatchers("/mcp/**", "/sse", "/api/**"))`.
   - Register `McpApiKeyAuthFilter` before `UsernamePasswordAuthenticationFilter`.
   - Permit `/mcp/**` and `/sse` in `authorizeHttpRequests` (as authentication is handled by the dedicated filter).

### Phase 3: Tool Implementation
1. Create `de.oberamsystems.slm.mcp.McpLifeEventTools`.
2. Create `de.oberamsystems.slm.mcp.McpSessionTools`.
3. Create `de.oberamsystems.slm.mcp.McpWellnessTools`.
4. Create `de.oberamsystems.slm.mcp.McpMedicalTools`.
5. Register beans via `MethodToolCallbackProvider` in `McpServerConfig`.

### Phase 4: Resource & Prompt Implementation
1. Implement `McpDashboardResourceProvider` for `slm://dashboard/today`.
2. Implement prompt templates for morning briefing and evening debrief.

### Phase 5: Verification & Client Integration
1. Add Testcontainers integration test `McpServerIT` verifying tool discovery and execution via SSE / HTTP mock client.
2. Provide configuration snippet for `/home/simon/.gemini/config/mcp_config.json` and Claude Desktop.
3. Document in [`DECISIONS.md`](file:///home/simon/code/eclipse/slm/DECISIONS.md) as ADR 016 and update [`CHANGELOG.md`](file:///home/simon/code/eclipse/slm/CHANGELOG.md).

---

## 7. Finalized Architectural Decisions
1. **Transport**: **WebMVC SSE Transport** using canonical Spring AI default endpoints (`/sse` for SSE streaming handshake and `/mcp/message` for JSON-RPC messages).
2. **Security**: **API Key Authentication** enforced via `McpApiKeyAuthFilter` checking `X-API-KEY` or `?apiKey=...` against `slm.mcp.api-key`.
3. **Tool Scope**: **Full Life Management Suite** covering Life Events, Polymorphic Sessions & Four Pillars Balance, Habits, and Medical Appointments.

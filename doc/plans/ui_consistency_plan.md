# Consistent UI Design System & Universal Template Modernization Plan

## 1. Executive Summary & Goals
Currently, the SLM user interface suffers from visual and structural inconsistencies across its 57 templates:
- **Style Fragmentation**: Some templates use local WebJar Bootstrap 5.3.8, others reference external CDN Bootstrap 5.3.0, some use a 6-line `table.css`, and others use ad-hoc inline `<style>` tags with differing gradients, padding, and card hover rules.
- **Header & Layout Discrepancies**: Modern session pages feature high-elevation gradient banners and navigation bars, while older management and entity pages have plain raw `<h1>` headers, unbordered or stark black-bordered tables, and lack card wrappers.
- **Broken / Missing Fragments**: Several pages (`doctor.html`, `speciality.html`, `mood-bb.html`, `sleeptime-bb.html`, `press.html`, `imprint.html`) reference `th:insert="~{header :: header}"` or `doctorheader`/`bookheader` fragments that do not exist in the repository.
- **Form & Table Variance**: Form labels, input sizing, action buttons, table cell alignments, and breadcrumb/back-button locations vary widely between modules.

**Objective**: Develop a modern, coherent design system for SLM, establish a single central stylesheet (`/css/slm.css`) and reusable Thymeleaf layout fragments, and systematically modernize all templates across all 10 application domains.

---

## 2. Design System Architecture

### 2.1 Color Palette & Module Semantic Themes
The design system establishes a neutral, sleek base canvas with module-specific accent gradients:
- **Base Canvas**: Slate background (`#f8fafc`), pure white card surfaces (`#ffffff`), dark slate typography (`#0f172a`), muted text (`#64748b`), and crisp borders (`#e2e8f0`).
- **Module Accent Themes**:
  | Module Domain | Theme Name | Primary Gradient | Badge & Icon Accents |
  | :--- | :--- | :--- | :--- |
  | **Sessions Hub** | `theme-sessions` | `linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)` | Indigo / Violet |
  | **Coding** | `theme-coding` | `linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)` | Blue / Cobalt |
  | **Working / Spend**| `theme-work` | `linear-gradient(135deg, #1e1b4b 0%, #4338ca 100%)` | Navy / Indigo |
  | **Activities & Sports**| `theme-activity` | `linear-gradient(135deg, #991b1b 0%, #ef4444 100%)` | Red / Rose |
  | **Habits & Learning**| `theme-habit` | `linear-gradient(135deg, #5b21b6 0%, #8b5cf6 100%)` | Purple / Violet |
  | **Mindfulness & Sleep**| `theme-wellness` | `linear-gradient(135deg, #065f46 0%, #10b981 100%)` | Emerald / Mint |
  | **Library & Books** | `theme-books` | `linear-gradient(135deg, #0e7490 0%, #06b6d4 100%)` | Cyan / Sky |
  | **Gaming & Media** | `theme-gaming` | `linear-gradient(135deg, #b45309 0%, #f59e0b 100%)` | Amber / Gold |
  | **Travel & Transit** | `theme-transit` | `linear-gradient(135deg, #1e293b 0%, #475569 100%)` | Slate / Steel |
  | **Medical & Health** | `theme-medical` | `linear-gradient(135deg, #0f766e 0%, #14b8a6 100%)` | Teal / Aqua |
  | **Social & Humans** | `theme-humans` | `linear-gradient(135deg, #831843 0%, #ec4899 100%)` | Pink / Rose |

### 2.2 Reusable Core Components in `/css/slm.css`
1. **`.slm-header`**: Dynamic header hero banner with gradient background, rounded bottom corners (`1.25rem`), title, icon, subtitle, and top breadcrumb/dashboard actions.
2. **`.slm-card` & `.slm-card-hover`**: Card components with subtle borders (`1px solid #e2e8f0`), rounded corners (`1rem`), soft shadows, and smooth hover micro-animations (`transform: translateY(-4px)`).
3. **`.slm-table`**: Clean table styling with rounded card wrappers, soft striped rows, hover highlights, muted metadata fonts, and badge pill alignments.
4. **`.slm-form-card`**: Elegant card enclosures for data entry with clear section titles, consistent form-label weights (`fw-semibold`), responsive grids, and primary action buttons.
5. **`.slm-footer`**: Minimalist sticky or bottom footer linking to Home, Imprint, and SLM version.

---

## 3. Thymeleaf Layout & Fragments Architecture

Create `src/main/resources/templates/fragments/layout.html` providing:
- **`standard-head(title)`**: Loads WebJar Bootstrap 5.3.8, Bootstrap Icons 1.11.3, and `/css/slm.css` with UTF-8 and responsive viewport meta tags.
- **`header(title, subtitle, icon, themeClass, backUrl, backLabel)`**: Universal hero banner rendering the title, icon, description, and back-link navigation.
- **`footer`**: Standardized footer.
- **Legacy Compatibility Fragments**: Define `header`, `doctorheader`, `bookheader`, `moodheader`, `sleepheader` inside `header.html` so any existing legacy calls immediately resolve without throwing template parsing errors.

---

## 4. Implementation Phases & Template Categorization

### Phase 1: Design Tokens, Central Stylesheet & Layout Fragments
- Create [`src/main/resources/static/css/slm.css`](file:///home/simon/code/eclipse/slm/src/main/resources/static/css/slm.css).
- Create [`src/main/resources/templates/fragments/layout.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/fragments/layout.html).
- Provide fallback [`src/main/resources/templates/header.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/header.html) resolving all legacy `th:insert="~{header :: ...}"` calls.

### Phase 2: Navigation & Management Dashboards (7 templates)
Modernize the core portal and 6 domain dashboards with consistent card grids, icons, and theme gradients:
- `index.html` (Main Portal)
- `activity-management.html`
- `habit-management.html`
- `book-management.html`
- `spend-management.html`
- `misc-management.html`
- `human-management.html`

### Phase 3: Sessions Hub & Activity / Sport / Sleep (11 templates)
Align all session and activity views with the unified design tokens:
- `sessions.html`, `coding-sessions.html`, `working-sessions.html`, `learning-sessions.html`, `gaming-sessions.html`, `reading-sessions.html`
- `activities.html`, `activity-types.html`, `sports.html`, `sport-types.html`
- `sleep-times.html`, `sleeptime-bb.html`

### Phase 4: Habits, Mental Health, Reflection & Books (14 templates)
Transform all habits, moods, gratitudes, diary, and library templates:
- `habits.html`, `habit-entries.html`, `last-done-habit-entries.html`, `learning-items.html`
- `meditations.html`, `moods.html`, `mood-bb.html`, `gratitudes.html`, `diary.html`
- `books.html`, `authors.html`, `publishers.html`, `press.html`, `games.html`, `studios.html`

### Phase 5: Hardware, Finance, Mobility, Medical & Social (23 templates)
Modernize remaining hardware, finances, transit, healthcare, and contacts templates:
- **Devices & Hardware**: `devices.html`, `vendors.html`, `manufacturers.html`
- **Spend & Contracts**: `purchases.html`, `contracts.html`, `contractors.html`
- **Mobility & Transit**: `train-trips.html`, `train-stations.html`, `train-lines.html`, `cars.html`, `car-trips.html`, `routes.html`, `points.html`
- **Healthcare & Doctors**: `appointments.html`, `appointment.html`, `doctors.html`, `doctor.html`, `specialities.html`, `speciality.html`
- **Social & System**: `humans.html`, `selfies.html`, `imprint.html`

---

## 5. Verification & Test Plan
- **Template Rendering**: Verify all Thymeleaf expressions (`th:field`, `th:object`, `th:action`, `th:each`, `th:text`) are preserved with exact variable names and types.
- **Automated Test Suite**: Run `./gradlew test -x jacocoTestReport` across all phases to ensure all 49+ controller tests and context loads pass.
- **Visual Inspection**: Confirm responsive layout on desktop and mobile viewports, card hover transitions, and clean table alignment.

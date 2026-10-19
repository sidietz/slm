# Architectural Plan: Deterministic Domain-Specific Sorting & Course Semester Alignment

## 1. Problem Analysis & Root Cause

In **Simon's Life Manager (SLM)**, university courses can be viewed across all semesters (`GET /courses`) or filtered by a specific semester (`GET /courses?semester=WiSe+2026/27`).

Currently, in [`AcademicController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/AcademicController.java#L35-L40):
```java
if (semester != null && !semester.isBlank() && !"ALL".equalsIgnoreCase(semester)) {
    courses = courseRepo.findAllBySemesterOrderByTitleAsc(semester);
} else {
    courses = courseRepo.findAllByOrderByCreatedAtDesc();
}
```

### The Inconsistency
1. **Specific Semester View**: Courses are sorted **alphabetically by title ascending** (`title ASC`).
2. **All Semesters View**: Courses are sorted **by creation timestamp descending** (`createdAt DESC`).
3. **Symptom**: If two courses exist in the same semester (e.g. *"Cloud Architecture"* created first, *"Algorithms"* created second):
   - In **All Semesters**: *"Algorithms"* appears before *"Cloud Architecture"* (`createdAt DESC`).
   - In **WiSe 2026/27**: *"Algorithms"* appears before *"Cloud Architecture"* (`title ASC`).
   - If instead *"Distributed Systems"* was created second and *"Applied Cryptography"* was created first:
     - In **All Semesters**: *"Distributed Systems"* appears first (`createdAt DESC`).
     - In **WiSe 2026/27**: *"Applied Cryptography"* appears first (`title ASC`).
   - When a user toggles between "All Semesters" and "WiSe 2026/27", the courses within the semester flip order unpredictably.

---

## 2. Proposed Architecture & Solution

### 2.1. Domain-Specific Sorting Principles

Per user architectural guidance, all entities across SLM must follow deterministic domain-specific ordering:
1. **Academic Courses**: By **semester descending** (most recent semester first), then **courses alphabetically ascending** by title (`title ASC` case-insensitive).
2. **Train Lines**: Alphabetically by train line using [`TrainLineComparator`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/util/TrainLineComparator.java) (prefix category S/U/RE, then numeric line number).
3. **Temporal Logs & Activities** (Sessions, Trips, Appointments, Journal Entries): By **datetime descending** (most recent first, `start DESC`).
4. **Catalog & Reference Data** (Habits, Doctors, Employers, Contractors, Games, Books, Sport Types): **Alphabetically ascending** by primary name/title (`name ASC` case-insensitive).

---

### 2.2. Semester & Course Comparators

#### 1. `SemesterComparator` ([`de.oberamsystems.slm.util.SemesterComparator`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/util/))
Academic semesters in Germany / European universities follow a standardized term cycle:
- **Summer Semester** (SoSe / SS / Sommersemester): begins in spring (e.g. `SoSe 2026`).
- **Winter Semester** (WiSe / WS / Wintersemester): begins in autumn and spans across years (e.g. `WiSe 2026/27`).

**Chronological Ranking Function**:
- Extract 4-digit calendar year: e.g. `2026`.
- Determine term type: `isWinter = lower.contains("w")` (Winter) vs `Summer`.
- Calculate chronological rank:
  $$\text{rank} = (\text{year} \times 2) + (\text{isWinter} ? 1 : 0)$$

**Evaluation Sequence**:
- `SoSe 2025`: $2025 \times 2 = 4050$
- `WS 2025/26`: $2025 \times 2 + 1 = 4051$
- `SoSe 2026`: $2026 \times 2 = 4052$
- `WiSe 2026/27`: $2026 \times 2 + 1 = 4053$
- `SS 2027`: $2027 \times 2 = 4054$

**Descending Order (Most Recent First)**:
`WiSe 2026/27` $\rightarrow$ `SoSe 2026` $\rightarrow$ `WS 2025/26` $\rightarrow$ `SoSe 2025` $\rightarrow \dots$ (nulls / empty strings placed last).

#### 2. `CourseComparator` ([`de.oberamsystems.slm.util.CourseComparator`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/util/))
Compares two `Course` entities:
1. **Primary**: By semester using `SemesterComparator.DESCENDING` (most recent first, nulls last).
2. **Secondary**: When semesters are equal, compare by `Course::getTitle` case-insensitive ascending (`String.CASE_INSENSITIVE_ORDER`).
3. **Tertiary Fallback**: By `Course::getId` ascending for strict determinism.

---

### 2.3. Controller & Tool Integration

1. **[`AcademicController.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/controller/AcademicController.java)**:
   - In `listCourses`:
     ```java
     List<String> semesters = courseRepo.findDistinctSemesters();
     semesters.sort(SemesterComparator.DESCENDING); // Tabs also show most recent semester first

     List<Course> courses;
     if (semester != null && !semester.isBlank() && !"ALL".equalsIgnoreCase(semester)) {
         courses = courseRepo.findAllBySemesterOrderByTitleAsc(semester);
     } else {
         courses = courseRepo.findAll();
         courses.sort(CourseComparator.INSTANCE);
     }
     ```
   - When viewing "ALL", courses are ordered: most recent semester first, and within each semester, **alphabetically by title**.
   - When viewing a single semester, courses are ordered **alphabetically by title**.
   - **Result**: Courses within the same semester never jump or reorder when switching views.

2. **[`McpAcademicTools.java`](file:///home/simon/code/eclipse/slm/src/main/java/de/oberamsystems/slm/mcp/McpAcademicTools.java)**:
   - Update `getCourses` tool to apply `CourseComparator.INSTANCE` when returning the list for "ALL".

3. **[`AGENTS.md`](file:///home/simon/code/eclipse/slm/AGENTS.md)**:
   - Add new Section **8. Deterministic Domain-Specific Sorting & Ordering**:
     Document the 4 rules (Academic courses by semester descending then title ascending; train lines by TrainLineComparator; temporal logs by datetime descending; catalog data alphabetically).

---

## 3. Implementation Steps

1. **Create Utility Classes**:
   - `src/main/java/de/oberamsystems/slm/util/SemesterComparator.java`
   - `src/main/java/de/oberamsystems/slm/util/CourseComparator.java`
2. **Update Controllers & Tools**:
   - Update `AcademicController.java` to use `CourseComparator` and `SemesterComparator.DESCENDING`.
   - Update `McpAcademicTools.java`.
3. **Update Guidelines in `AGENTS.md`**:
   - Insert new Section 8 detailing domain-specific sorting invariants.
4. **Unit & Integration Tests**:
   - `SemesterComparatorTest.java`: test parsing of winter/summer formats, year transitions, null safety.
   - `CourseComparatorTest.java`: test courses sorting within same semester and across different semesters.
   - `AcademicIT.java`: add `testCourseSortingConsistencyBetweenAllAndSemesterFilter()`.
5. **Version Bump to `v0.0.3-rc3` & Living Documentation**:
   - Bump version to `0.0.3-rc3` in [`build.gradle`](file:///home/simon/code/eclipse/slm/build.gradle): `version = '0.0.3-rc3'`.
   - Bump version in [`src/main/resources/application.properties`](file:///home/simon/code/eclipse/slm/src/main/resources/application.properties): `spring.ai.mcp.server.version=0.0.3-rc3`.
   - In [`CHANGELOG.md`](file:///home/simon/code/eclipse/slm/CHANGELOG.md):
     - Add new release section `## [0.0.3-rc3] - 2026-10-19`.
     - Document course sorting alignment and domain-specific sorting guidelines under `### Added` / `### Fixed`.
     - Update release comparison links:
       - `[Unreleased]: https://github.com/sidietz/slm/compare/v0.0.3-rc3...HEAD`
       - `[0.0.3-rc3]: https://github.com/sidietz/slm/compare/v0.0.3-rc2...v0.0.3-rc3`
       - `[0.0.3-rc2]: https://github.com/sidietz/slm/compare/v0.0.3-rc1...v0.0.3-rc2`

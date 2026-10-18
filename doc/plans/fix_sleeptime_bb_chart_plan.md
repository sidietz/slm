# Architecture Plan: Fix `/sleeptime-bb` (Billboard.js Sleep Analytics Chart)

## 1. Executive Summary & Problem Diagnosis

When accessing `http://localhost:8080/sleeptime-bb`, the Billboard.js sleep analytics chart fails to render. Investigation revealed **two cascading root causes**:

```mermaid
flowchart TD
    A["GET /sleeptime-bb"] --> B["Thymeleaf Template Parsing"]
    B -->|CRASH: Line 128: columns: [[]]| C["TemplateProcessingException: Could not parse as expression: ''"]
    C --> D["HTML Truncated Mid-Stream: Script never completes"]
    
    E["Client JS: fetch('/api/sleeptimes')"] --> F["SleepTimeRestController"]
    F --> G["Jackson JSON Serialization"]
    G -->|CRASH: Jackson BOM conflict| H["NoClassDefFoundError: JsonApplyView"]
    H --> I["HTTP 500 Internal Server Error: No JSON data"]
```

1. **Thymeleaf Inlining Parser Crash in [`sleeptime-bb.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sleeptime-bb.html)**:
   - Line 128 contains `columns: [[]]`.
   - In Thymeleaf 3+, `[[...]]` is reserved syntax for text inlining.
   - AttoParser attempts to parse `""` between `[[` and `]]` as an expression, throwing `TemplateProcessingException: Could not parse as expression: ""` at line 128, col 24.
   - This aborts template rendering, leaving the HTML response truncated right before the script can close or run `getData2()`.
   - The `<script>` tag also lacks `th:inline="none"`.

2. **HTTP 500 on `/api/sleeptimes` (Jackson Version Mismatch)**:
   - In [`build.gradle`](file:///home/simon/code/eclipse/slm/build.gradle), `tools.jackson.core:jackson-databind:3.2.3` is used.
   - `tools.jackson.core:jackson-databind:3.2.3` requires `com.fasterxml.jackson.core:jackson-annotations:2.22+` which contains `JsonApplyView`.
   - However, Spring Boot 4.2.0-M2's dependency management BOM forces `com.fasterxml.jackson.core:jackson-annotations` down to `2.21`.
   - When `/api/sleeptimes` serializes `SleepTime`, Jackson's `JacksonAnnotationIntrospector` searches for `com.fasterxml.jackson.annotation.JsonApplyView` and crashes with:
     `java.lang.NoClassDefFoundError: com/fasterxml/jackson/annotation/JsonApplyView`.

3. **Client-side Duration Parsing**:
   - `parseDuration()` in [`sleeptime-bb.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sleeptime-bb.html) must be resilient to both numeric seconds and ISO-8601 strings (`PT...`) returned by Jackson.

---

## 2. Proposed Changes

### 2.1. Fix Jackson Classpath Conflict in [`build.gradle`](file:///home/simon/code/eclipse/slm/build.gradle)
Enforce `com.fasterxml.jackson.core:jackson-annotations:2.22` (or align with Jackson 3.2.3) so `JsonApplyView` is available:

```groovy
dependencies {
    // ...
    implementation 'tools.jackson.core:jackson-databind:3.2.3' // fixes a CVE
    implementation 'tools.jackson.core:jackson-core:3.2.3' // fixes a CVE
    implementation 'com.fasterxml.jackson.core:jackson-annotations:2.22' // provides JsonApplyView for Jackson 3.2.3
    // ...
}
```

### 2.2. Fix Thymeleaf Inlining and Chart Config in [`sleeptime-bb.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sleeptime-bb.html)
1. Add `th:inline="none"` to the `<script>` tag:
   ```html
   <script th:inline="none">
   ```
2. Replace `columns: [[]]` with valid Billboard.js empty columns array `columns: []`:
   ```javascript
   data: {
       x: "x",
       type: "spline",
       colors: {
           "Sleep Duration (h)": "#10b981"
       },
       columns: []
   }
   ```
3. Enhance `parseDuration(rawDuration)` to gracefully handle:
   - ISO-8601 strings (`PT7H30M`, `PT8H`, `PT45M`).
   - Raw numeric values (seconds or hours).
   - Missing or null durations.
4. Add empty state handling in case `sleeptimes` has 0 records.
5. Also fix identical `columns: [[]]` syntax in [`mood-bb.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/mood-bb.html).

### 2.3. Add Automated MockMvc Tests
Create integration/controller test covering:
1. `GET /sleeptime-bb`: Validates HTTP 200 and successful complete template rendering without truncation.
2. `GET /api/sleeptimes`: Validates HTTP 200 and valid JSON array serialization of `SleepTime` without Jackson classloader errors.

---

## 3. Implementation Steps

| Step | Action | Files |
| :--- | :--- | :--- |
| **1** | Align Jackson annotations dependency in `build.gradle` | [`build.gradle`](file:///home/simon/code/eclipse/slm/build.gradle) |
| **2** | Update `sleeptime-bb.html` with `th:inline="none"`, `columns: []`, and robust duration parser | [`src/main/resources/templates/sleeptime-bb.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/sleeptime-bb.html) |
| **3** | Update `mood-bb.html` with `th:inline="none"` and `columns: []` | [`src/main/resources/templates/mood-bb.html`](file:///home/simon/code/eclipse/slm/src/main/resources/templates/mood-bb.html) |
| **4** | Add automated MockMvc tests for `/sleeptime-bb` and `/api/sleeptimes` | [`src/test/java/de/oberamsystems/slm/controller/SleepTimeControllerTest.java`](file:///home/simon/code/eclipse/slm/src/test/java/de/oberamsystems/slm/controller/SleepTimeControllerTest.java) |
| **5** | Restart dev server and verify `/sleeptime-bb` visually in browser | Browser & curl checks |

---

## 4. Verification Plan

1. **API Verification**:
   ```bash
   curl -i http://localhost:8080/api/sleeptimes
   ```
   Must return HTTP `200 OK` with `Content-Type: application/json` and non-empty array of serialized sleep time records.

2. **Template Rendering Verification**:
   ```bash
   curl -i http://localhost:8080/sleeptime-bb
   ```
   Must return HTTP `200 OK` with complete HTML closing tags `</html>` and no truncation.

3. **Browser Verification**:
   Navigate to `http://localhost:8080/sleeptime-bb` using the browser subagent:
   - Confirm Billboard.js spline chart mounts cleanly in `#chart`.
   - Confirm x-axis displays dates and y-axis displays sleep hours (e.g. 7.5h, 8.0h).
   - Confirm no JavaScript errors in browser console.

4. **Test Suite**:
   Run `./gradlew test -x jacocoTestReport -x compileAotTestJava` to confirm all tests pass.

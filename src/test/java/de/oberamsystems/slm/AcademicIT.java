package de.oberamsystems.slm;

import de.oberamsystems.slm.mcp.McpAcademicTools;
import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class AcademicIT extends AbstractIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureSessionRepository lectureSessionRepository;

    @Autowired
    private LectureNoteRepository lectureNoteRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private HumanRepository humanRepository;

    @Autowired
    private McpAcademicTools mcpAcademicTools;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    public void testCourseAndLectureHierarchyAndUuidv7Note() {
        // 1. Create Lecturer Human
        Human lecturer = new Human();
        lecturer.setFirstname("Ada");
        lecturer.setLastname("Lovelace");
        lecturer.setCategory(HumanCategory.LECTURER);
        lecturer = humanRepository.saveAndFlush(lecturer);
        assertNotNull(lecturer.getId());

        // 2. Create Course with LocalDateTime examDate
        LocalDateTime examDate = LocalDateTime.of(2027, 2, 15, 9, 30);
        Course course = new Course();
        course.setTitle("Advanced Distributed Systems");
        course.setCode("CS-701");
        course.setSemester("WS 2026/27");
        course.setEctsCredits(BigDecimal.valueOf(6.0));
        course.setLecturer(lecturer);
        course.setDefaultRoom("Audimax / Room 101");
        course.setExamDate(examDate);
        course.setExamLocation("Hall B");
        course.setSyllabus("Consensus algorithms, Raft, Paxos, and CRDTs.");
        course.setStatus(StatusEnum.IN_PROGRESS);
        course = courseRepository.saveAndFlush(course);

        assertNotNull(course.getId());
        assertEquals("CS-701", course.getCode());
        assertEquals(examDate, course.getExamDate());
        assertEquals("Ada Lovelace", course.getLecturer().getName());

        // 3. Create polymorphic LectureSession
        LocalDateTime sessionStart = LocalDateTime.of(2026, 10, 20, 10, 15);
        LocalDateTime sessionEnd = LocalDateTime.of(2026, 10, 20, 11, 45); // 90 min gross
        LectureSession lectureSession = new LectureSession();
        lectureSession.setCourse(course);
        lectureSession.setLectureNumber(1);
        lectureSession.setTopic("Raft Consensus & Leader Election");
        lectureSession.setRoom("Audimax / Room 101");
        lectureSession.setStart(sessionStart);
        lectureSession.setEnd(sessionEnd);
        lectureSession.setBreakDuration(Duration.ofMinutes(15)); // 75 min net
        lectureSession.setFocusRating(5);
        lectureSession.setComment("Excellent demonstration of split votes.");
        lectureSession = lectureSessionRepository.saveAndFlush(lectureSession);

        assertNotNull(lectureSession.getId());
        assertInstanceOf(UUID.class, lectureSession.getId());
        assertEquals(7, lectureSession.getId().version(), "Session ID must be UUIDv7");
        assertEquals(Duration.ofMinutes(90), lectureSession.getDuration());
        assertEquals(Duration.ofMinutes(75), lectureSession.getNetDuration());

        // 4. Verify polymorphic retrieval via SessionRepository
        List<Session> allSessions = sessionRepository.findAll();
        boolean foundLecture = false;
        for (Session s : allSessions) {
            if (s instanceof LectureSession ls) {
                if ("Raft Consensus & Leader Election".equals(ls.getTopic())) {
                    foundLecture = true;
                    assertEquals("CS-701", ls.getCourse().getCode());
                    assertEquals(1, ls.getLectureNumber());
                }
            }
        }
        assertTrue(foundLecture, "LectureSession must be retrieved as polymorphic subclass of Session");

        // 5. Create LectureNote with UUIDv7
        LectureNote note = new LectureNote();
        note.setCourse(course);
        note.setLectureSession(lectureSession);
        note.setLectureNumber(1);
        note.setLectureDate(LocalDate.of(2026, 10, 20));
        note.setTitle("Lecture 1: Raft Consensus Foundation");
        note.setContentMarkdown("# Raft Consensus\n\n- Leader election\n- Log replication\n- Safety properties");
        note.setKeyConcepts("Raft, Consensus, Split Votes, RPC");
        note.setSummary("Introduced Raft algorithm fundamentals and leader election dynamics.");
        note.setHasHomework(true);
        note.setHomeworkTodo("Complete Lab 2 Raft implementation");
        note.setExamRelevanceRating(5);
        note = lectureNoteRepository.saveAndFlush(note);

        assertNotNull(note.getId());
        assertInstanceOf(UUID.class, note.getId());
        assertEquals(7, note.getId().version(), "UUIDv7 should have version 7");

        // 6. Test LectureNote Repository queries
        List<LectureNote> courseNotes = lectureNoteRepository.findByCourseIdOrderByLectureNumberAsc(course.getId());
        assertEquals(1, courseNotes.size());
        assertEquals("Lecture 1: Raft Consensus Foundation", courseNotes.get(0).getTitle());

        List<LectureNote> searchResults = lectureNoteRepository.searchNotes("Consensus");
        assertFalse(searchResults.isEmpty(), "Search for 'Consensus' should match note");
        assertEquals(courseNotes.get(0).getId(), searchResults.get(0).getId());

        // 7. Verify MCP Academic Tools
        List<Map<String, Object>> mcpCourses = mcpAcademicTools.getCourses("WS 2026/27");
        assertFalse(mcpCourses.isEmpty());
        assertEquals("CS-701", mcpCourses.get(0).get("code"));

        Map<String, Object> details = mcpAcademicTools.getCourseDetails(course.getId());
        assertEquals("Advanced Distributed Systems", details.get("title"));
        assertEquals(1, details.get("notesCount"));
        assertEquals(1, details.get("sessionsCount"));

        List<Map<String, Object>> mcpSearch = mcpAcademicTools.searchLectureNotes("Raft");
        assertFalse(mcpSearch.isEmpty());
    }

    @Test
    void testSecurityPermitsAcademicEndpoints() throws Exception {
        MockMvc mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. /courses should be publicly accessible (HTTP 200 OK)
        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk());

        // 2. Create course and note for path testing
        Course c = new Course();
        c.setTitle("Operating Systems");
        c.setSemester("WS 2026/27");
        c = courseRepository.saveAndFlush(c);

        LectureNote note = new LectureNote();
        note.setCourse(c);
        note.setTitle("Processes & Threads");
        note.setContentMarkdown("## Concurrency basics");
        note = lectureNoteRepository.saveAndFlush(note);

        // /courses/{id}
        mockMvc.perform(get("/courses/" + c.getId()))
                .andExpect(status().isOk());

        // /courses/{courseId}/notes/new
        mockMvc.perform(get("/courses/" + c.getId() + "/notes/new"))
                .andExpect(status().isOk());

        // /notes/{id}
        mockMvc.perform(get("/notes/" + note.getId()))
                .andExpect(status().isOk());

        // /notes/{id}/edit
        mockMvc.perform(get("/notes/" + note.getId() + "/edit"))
                .andExpect(status().isOk());

        // Non-permitted endpoints should trigger authentication challenge (HTTP 401 Unauthorized)
        mockMvc.perform(get("/unauthorized-academic-endpoint"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testDeleteLectureNoteWorkflow() throws Exception {
        MockMvc mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. Setup course and note
        Course course = new Course();
        course.setTitle("Database Internals");
        course.setSemester("SS 2027");
        course = courseRepository.saveAndFlush(course);

        LectureNote note = new LectureNote();
        note.setCourse(course);
        note.setTitle("B+ Trees & LSM Trees");
        note.setContentMarkdown("## Storage Engine Architecture");
        note = lectureNoteRepository.saveAndFlush(note);

        UUID noteId = note.getId();
        Long courseId = course.getId();

        entityManager.flush();
        entityManager.clear();
        assertTrue(lectureNoteRepository.existsById(noteId));

        // 2. Perform POST /notes/{id}/delete with CSRF
        mockMvc.perform(post("/notes/" + noteId + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/" + courseId));

        entityManager.flush();
        entityManager.clear();

        // 3. Verify note is deleted, course remains intact
        assertFalse(lectureNoteRepository.existsById(noteId));
        assertTrue(courseRepository.existsById(courseId));
    }

    @Test
    void testDeleteCourseWorkflow() throws Exception {
        MockMvc mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. Setup Lecturer, Course, LectureSession, and LectureNote
        Human lecturer = new Human();
        lecturer.setFirstname("Leslie");
        lecturer.setLastname("Lamport");
        lecturer.setCategory(HumanCategory.LECTURER);
        lecturer.setBirthday(java.sql.Date.valueOf("1941-02-07"));
        lecturer = humanRepository.saveAndFlush(lecturer);

        Course course = new Course();
        course.setTitle("Formal Verification & TLA+");
        course.setCode("CS-901");
        course.setSemester("WS 2026/27");
        course.setLecturer(lecturer);
        course = courseRepository.saveAndFlush(course);

        LectureSession session = new LectureSession();
        session.setCourse(course);
        session.setLectureNumber(1);
        session.setTopic("Temporal Logic of Actions");
        session.setStart(LocalDateTime.of(2026, 11, 2, 10, 0));
        session.setEnd(LocalDateTime.of(2026, 11, 2, 11, 30));
        session = lectureSessionRepository.saveAndFlush(session);

        LectureNote note = new LectureNote();
        note.setCourse(course);
        note.setLectureSession(session);
        note.setLectureNumber(1);
        note.setTitle("Introduction to TLA+");
        note.setContentMarkdown("Specifying state machines and safety properties");
        note = lectureNoteRepository.saveAndFlush(note);

        Long courseId = course.getId();
        UUID sessionId = session.getId();
        UUID noteId = note.getId();

        entityManager.flush();
        entityManager.clear();

        assertTrue(courseRepository.existsById(courseId));
        assertTrue(lectureNoteRepository.existsById(noteId));
        assertTrue(lectureSessionRepository.existsById(sessionId));

        // 2. Perform POST /courses/{id}/delete with CSRF
        mockMvc.perform(post("/courses/" + courseId + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));

        entityManager.flush();
        entityManager.clear();

        // 3. Verify course is deleted
        assertFalse(courseRepository.existsById(courseId));
        // Verify lecture note cascaded (deleted)
        assertFalse(lectureNoteRepository.existsById(noteId));
        // Verify lecture session is preserved with course set to null
        assertTrue(lectureSessionRepository.existsById(sessionId));
        assertNull(lectureSessionRepository.findById(sessionId).orElseThrow().getCourse());
        // Verify lecturer is preserved
        assertTrue(humanRepository.existsById(lecturer.getId()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testCourseSortingConsistencyBetweenAllAndSemesterFilter() throws Exception {
        MockMvc mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // 1. Create courses where createdAt order is the REVERSE of title ASC
        // Course A: "Distributed Systems" created at T0 (earlier)
        // Course B: "Applied Cryptography" created at T1 (later)
        LocalDateTime t0 = LocalDateTime.now().minusDays(5);
        LocalDateTime t1 = LocalDateTime.now().minusDays(1);

        Course c1 = new Course();
        c1.setTitle("Distributed Systems");
        c1.setCode("CS-601");
        c1.setSemester("WiSe 2026/27");
        c1.setCreatedAt(t0);
        courseRepository.saveAndFlush(c1);

        Course c2 = new Course();
        c2.setTitle("Applied Cryptography");
        c2.setCode("CS-602");
        c2.setSemester("WiSe 2026/27");
        c2.setCreatedAt(t1);
        courseRepository.saveAndFlush(c2);

        // Older semester course
        Course c3 = new Course();
        c3.setTitle("Linear Algebra");
        c3.setCode("MATH-101");
        c3.setSemester("SoSe 2026");
        c3.setCreatedAt(t0);
        courseRepository.saveAndFlush(c3);

        entityManager.flush();
        entityManager.clear();

        // 2. Fetch "ALL" courses via GET /courses
        MvcResult allResult = mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andReturn();

        List<Course> allCourses = (List<Course>) allResult.getModelAndView().getModel().get("courses");
        assertNotNull(allCourses);
        // Find WiSe 2026/27 courses
        List<Course> wiseCoursesInAll = allCourses.stream()
                .filter(c -> "WiSe 2026/27".equals(c.getSemester()))
                .toList();
        assertEquals(2, wiseCoursesInAll.size());
        assertEquals("Applied Cryptography", wiseCoursesInAll.get(0).getTitle());
        assertEquals("Distributed Systems", wiseCoursesInAll.get(1).getTitle());

        // Verify semester tabs sorting: WiSe 2026/27 must be before SoSe 2026
        List<String> distinctSemesters = (List<String>) allResult.getModelAndView().getModel().get("semesters");
        assertNotNull(distinctSemesters);
        int idxWise = distinctSemesters.indexOf("WiSe 2026/27");
        int idxSose = distinctSemesters.indexOf("SoSe 2026");
        assertTrue(idxWise != -1 && idxSose != -1);
        assertTrue(idxWise < idxSose, "WiSe 2026/27 must be listed before SoSe 2026");

        // 3. Fetch single semester via GET /courses?semester=WiSe+2026/27
        MvcResult wiseResult = mockMvc.perform(get("/courses").param("semester", "WiSe 2026/27"))
                .andExpect(status().isOk())
                .andReturn();

        List<Course> wiseFilteredCourses = (List<Course>) wiseResult.getModelAndView().getModel().get("courses");
        assertNotNull(wiseFilteredCourses);
        assertEquals(2, wiseFilteredCourses.size());
        // Verify EXACT SAME order as within "ALL": Applied Cryptography first, Distributed Systems second
        assertEquals("Applied Cryptography", wiseFilteredCourses.get(0).getTitle());
        assertEquals("Distributed Systems", wiseFilteredCourses.get(1).getTitle());

        // 4. Verify McpAcademicTools consistency
        List<Map<String, Object>> mcpAll = mcpAcademicTools.getCourses(null);
        List<Map<String, Object>> mcpWiseInAll = mcpAll.stream()
                .filter(m -> "WiSe 2026/27".equals(m.get("semester")))
                .toList();
        assertEquals("Applied Cryptography", mcpWiseInAll.get(0).get("title"));
        assertEquals("Distributed Systems", mcpWiseInAll.get(1).get("title"));

        List<Map<String, Object>> mcpWise = mcpAcademicTools.getCourses("WiSe 2026/27");
        assertEquals("Applied Cryptography", mcpWise.get(0).get("title"));
        assertEquals("Distributed Systems", mcpWise.get(1).get("title"));

        // 5. Verify McpAcademicTools.getSemesters() sorted chronologically descending
        List<String> mcpSemesters = mcpAcademicTools.getSemesters();
        assertNotNull(mcpSemesters);
        int mcpIdxWise = mcpSemesters.indexOf("WiSe 2026/27");
        int mcpIdxSose = mcpSemesters.indexOf("SoSe 2026");
        assertTrue(mcpIdxWise != -1 && mcpIdxSose != -1);
        assertTrue(mcpIdxWise < mcpIdxSose, "McpAcademicTools.getSemesters() must return WiSe 2026/27 before SoSe 2026");
    }
}

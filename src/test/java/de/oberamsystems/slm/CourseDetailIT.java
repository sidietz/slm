package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class CourseDetailIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LectureSessionRepository lectureSessionRepository;

    @Autowired
    private LectureNoteRepository lectureNoteRepository;

    @Autowired
    private HumanRepository humanRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testViewCourseDetailsWithFullMetadataSessionsAndNotes() throws Exception {
        // 1. Create Lecturer
        Human lecturer = new Human();
        lecturer.setFirstname("Margaret");
        lecturer.setLastname("Hamilton");
        lecturer.setCategory(HumanCategory.LECTURER);
        lecturer = humanRepository.saveAndFlush(lecturer);

        // 2. Create Course with rich metadata
        LocalDateTime examDate = LocalDateTime.of(2027, 2, 18, 10, 0);
        Course course = new Course();
        course.setTitle("Reliable Distributed Systems");
        course.setCode("CS-801");
        course.setSemester("WS 2026/27");
        course.setEctsCredits(BigDecimal.valueOf(6.0));
        course.setLecturer(lecturer);
        course.setDefaultRoom("Audimax II");
        course.setMeetingLink("https://conference.university.edu/cs801");
        course.setExamDate(examDate);
        course.setExamLocation("Hall C");
        course.setGrade(BigDecimal.valueOf(1.3));
        course.setStatus(StatusEnum.IN_PROGRESS);
        course.setSyllabus("Core topics:\n- Byzantine Fault Tolerance\n- Raft and Multi-Paxos\n- State Machine Replication");
        course = courseRepository.saveAndFlush(course);

        // 3. Create LectureSession attached to course
        LectureSession session = new LectureSession();
        session.setCourse(course);
        session.setLectureNumber(1);
        session.setTopic("Byzantine Fault Tolerance & PBFT");
        session.setRoom("Audimax II");
        session.setStart(LocalDateTime.of(2026, 10, 21, 10, 15));
        session.setEnd(LocalDateTime.of(2026, 10, 21, 11, 45)); // 90 min gross
        session.setBreakDuration(Duration.ofMinutes(15)); // 75 min net
        session.setFocusRating(5);
        session.setComment("In-depth discussion on safety vs liveness under network partitions.");
        session = lectureSessionRepository.saveAndFlush(session);

        // 4. Create LectureNote attached to course & session
        LectureNote note = new LectureNote();
        note.setCourse(course);
        note.setLectureSession(session);
        note.setLectureNumber(1);
        note.setLectureDate(LocalDate.of(2026, 10, 21));
        note.setTitle("Lecture 1: BFT Consensus & Quorums");
        note.setContentMarkdown("# BFT Consensus\n\n- Primary-backup replica coordination\n- View-change protocol\n- Cryptographic signatures");
        note.setKeyConcepts("PBFT, View Change, Byzantine, Quorums");
        note.setSummary("Fundamental proof of 3f+1 minimum replicas required for Byzantine fault tolerance.");
        note.setHasHomework(true);
        note.setHomeworkTodo("Read Castro-Liskov PBFT paper section 4");
        note.setExamRelevanceRating(5);
        note = lectureNoteRepository.saveAndFlush(note);

        // 5. Perform GET /courses/{id} and verify course-detail view rendering
        mockMvc.perform(get("/courses/" + course.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("course-detail"))
                .andExpect(model().attributeExists("course", "sessions", "notes", "lecturers", "newSession"))
                .andExpect(content().string(containsString("Reliable Distributed Systems")))
                .andExpect(content().string(containsString("CS-801")))
                .andExpect(content().string(containsString("WS 2026/27")))
                .andExpect(content().string(containsString("6.0 ECTS")))
                .andExpect(content().string(containsString("Margaret Hamilton")))
                .andExpect(content().string(containsString("Audimax II")))
                .andExpect(content().string(containsString("https://conference.university.edu/cs801")))
                .andExpect(content().string(containsString("Join Online Stream")))
                .andExpect(content().string(containsString("18.02.2027 10:00")))
                .andExpect(content().string(containsString("in Hall C")))
                .andExpect(content().string(containsString("1.3")))
                .andExpect(content().string(containsString("Byzantine Fault Tolerance")))
                // Lecture Notes assertions
                .andExpect(content().string(containsString("Lecture Notes (")))
                .andExpect(content().string(containsString("Lecture 1: BFT Consensus &amp; Quorums")))
                .andExpect(content().string(containsString("PBFT, View Change, Byzantine, Quorums")))
                .andExpect(content().string(containsString("Homework")))
                // Lecture Sessions timeline assertions
                .andExpect(content().string(containsString("Attended Lectures (")))
                .andExpect(content().string(containsString("Byzantine Fault Tolerance &amp; PBFT")))
                .andExpect(content().string(containsString("01:15"))) // Net duration formatted
                // Form & modal assertions
                .andExpect(content().string(containsString("id=\"editCourseModal\"")))
                .andExpect(content().string(containsString("action=\"/courses/" + course.getId() + "/edit\"")))
                .andExpect(content().string(containsString("action=\"/courses/" + course.getId() + "/delete\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void testViewCourseDetailsEmptyState() throws Exception {
        Course course = new Course();
        course.setTitle("Quantum Information Theory");
        course.setSemester("SS 2027");
        course = courseRepository.saveAndFlush(course);

        mockMvc.perform(get("/courses/" + course.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("course-detail"))
                .andExpect(model().attributeExists("course", "sessions", "notes"))
                .andExpect(content().string(containsString("Quantum Information Theory")))
                .andExpect(content().string(containsString("No lecture notes saved for this course yet.")))
                .andExpect(content().string(containsString("No lecture attendance sessions logged for this course yet.")))
                .andExpect(content().string(containsString("Write First Note")));
    }

    @Test
    void testUpdateCourseDetailsViaModalPost() throws Exception {
        Human initialLecturer = new Human();
        initialLecturer.setFirstname("Alan");
        initialLecturer.setLastname("Turing");
        initialLecturer.setCategory(HumanCategory.LECTURER);
        initialLecturer = humanRepository.saveAndFlush(initialLecturer);

        Human updatedLecturer = new Human();
        updatedLecturer.setFirstname("Grace");
        updatedLecturer.setLastname("Hopper");
        updatedLecturer.setCategory(HumanCategory.LECTURER);
        updatedLecturer = humanRepository.saveAndFlush(updatedLecturer);

        Course course = new Course();
        course.setTitle("Theory of Computation");
        course.setCode("TOC-101");
        course.setSemester("WS 2026/27");
        course.setLecturer(initialLecturer);
        course.setStatus(StatusEnum.PLANNED);
        course = courseRepository.saveAndFlush(course);

        // Submit edit form
        mockMvc.perform(post("/courses/" + course.getId() + "/edit")
                        .with(csrf())
                        .param("title", "Advanced Computability & Complexity")
                        .param("code", "TOC-201")
                        .param("semester", "SS 2027")
                        .param("ectsCredits", "8.0")
                        .param("lecturerId", String.valueOf(updatedLecturer.getId()))
                        .param("defaultRoom", "Seminar Room 404")
                        .param("meetingLink", "https://zoom.us/j/123456789")
                        .param("examDate", "2027-07-15T14:00")
                        .param("examLocation", "Lecture Hall 1")
                        .param("grade", "1.0")
                        .param("status", "COMPLETED")
                        .param("syllabus", "Turing machines, Rice theorem, NP-completeness."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/" + course.getId()));

        // Verify updated entity in database
        Course updated = courseRepository.findById(course.getId()).orElseThrow();
        assertEquals("Advanced Computability & Complexity", updated.getTitle());
        assertEquals("TOC-201", updated.getCode());
        assertEquals("SS 2027", updated.getSemester());
        assertEquals(0, BigDecimal.valueOf(8.0).compareTo(updated.getEctsCredits()));
        assertNotNull(updated.getLecturer());
        assertEquals("Grace Hopper", updated.getLecturer().getName());
        assertEquals("Seminar Room 404", updated.getDefaultRoom());
        assertEquals("https://zoom.us/j/123456789", updated.getMeetingLink());
        assertEquals(LocalDateTime.of(2027, 7, 15, 14, 0), updated.getExamDate());
        assertEquals("Lecture Hall 1", updated.getExamLocation());
        assertEquals(0, BigDecimal.valueOf(1.0).compareTo(updated.getGrade()));
        assertEquals(StatusEnum.COMPLETED, updated.getStatus());
        assertEquals("Turing machines, Rice theorem, NP-completeness.", updated.getSyllabus());

        // Verify GET renders the updated data
        mockMvc.perform(get("/courses/" + course.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Advanced Computability &amp; Complexity")))
                .andExpect(content().string(containsString("TOC-201")))
                .andExpect(content().string(containsString("Grace Hopper")))
                .andExpect(content().string(containsString("Seminar Room 404")))
                .andExpect(content().string(containsString("COMPLETED")));
    }

    @Test
    void testUpdateCourseDisassociateLecturer() throws Exception {
        Human lecturer = new Human();
        lecturer.setFirstname("Donald");
        lecturer.setLastname("Knuth");
        lecturer.setCategory(HumanCategory.LECTURER);
        lecturer = humanRepository.saveAndFlush(lecturer);

        Course course = new Course();
        course.setTitle("The Art of Computer Programming");
        course.setSemester("WS 2026/27");
        course.setLecturer(lecturer);
        course = courseRepository.saveAndFlush(course);

        // Edit course with empty lecturerId
        mockMvc.perform(post("/courses/" + course.getId() + "/edit")
                        .with(csrf())
                        .param("title", "The Art of Computer Programming")
                        .param("semester", "WS 2026/27")
                        .param("lecturerId", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses/" + course.getId()));

        Course updated = courseRepository.findById(course.getId()).orElseThrow();
        assertNull(updated.getLecturer());
    }

    @Test
    void testDeleteCourseCascadesToAttachedEntities() throws Exception {
        Course course = new Course();
        course.setTitle("Compilers & Program Analysis");
        course.setSemester("WS 2026/27");
        course = courseRepository.saveAndFlush(course);

        LectureSession session = new LectureSession();
        session.setCourse(course);
        session.setLectureNumber(1);
        session.setTopic("Lexical Analysis & Parsing");
        session.setStart(LocalDateTime.of(2026, 10, 22, 9, 0));
        session.setEnd(LocalDateTime.of(2026, 10, 22, 10, 30));
        session = lectureSessionRepository.saveAndFlush(session);

        LectureNote note = new LectureNote();
        note.setCourse(course);
        note.setLectureSession(session);
        note.setLectureNumber(1);
        note.setTitle("LL(1) and LR(1) Parsers");
        note.setContentMarkdown("Grammars and parsing tables");
        note = lectureNoteRepository.saveAndFlush(note);

        UUID sessionId = session.getId();
        UUID noteId = note.getId();

        // Flush and clear persistence context so in-memory entities are detached
        // and PostgreSQL native ON DELETE CASCADE and ON DELETE SET NULL handle the cascade
        entityManager.flush();
        entityManager.clear();

        // Perform course deletion
        mockMvc.perform(post("/courses/" + course.getId() + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/courses"));

        // Flush the pending controller delete and clear persistence context to query database directly
        entityManager.flush();
        entityManager.clear();

        // Verify course is deleted
        assertFalse(courseRepository.existsById(course.getId()));
        // Verify lecture note cascaded (deleted)
        assertFalse(lectureNoteRepository.existsById(noteId));
        // Verify lecture session is preserved with course set to null
        assertTrue(lectureSessionRepository.existsById(sessionId));
        assertNull(lectureSessionRepository.findById(sessionId).orElseThrow().getCourse());
    }

    @Test
    void testNewLectureNoteForCourseWorkflow() throws Exception {
        Course course = new Course();
        course.setTitle("Computer Networks");
        course.setCode("NET-301");
        course.setSemester("WS 2026/27");
        course = courseRepository.saveAndFlush(course);

        LectureSession session = new LectureSession();
        session.setCourse(course);
        session.setLectureNumber(2);
        session.setTopic("TCP Congestion Control & BBR");
        session.setStart(LocalDateTime.of(2026, 10, 23, 11, 0));
        session.setEnd(LocalDateTime.of(2026, 10, 23, 12, 30));
        session = lectureSessionRepository.saveAndFlush(session);

        // 1. GET /courses/{courseId}/notes/new?sessionId=... pre-populates form
        mockMvc.perform(get("/courses/" + course.getId() + "/notes/new")
                        .param("sessionId", session.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("lecture-note-edit"))
                .andExpect(model().attributeExists("note", "course", "sessions"))
                .andExpect(content().string(containsString("New Lecture Note")))
                .andExpect(content().string(containsString("Computer Networks")))
                .andExpect(content().string(containsString("TCP Congestion Control &amp; BBR")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));

        // 2. Submit new note via POST /notes
        mockMvc.perform(post("/notes")
                        .with(csrf())
                        .param("course.id", course.getId().toString())
                        .param("sessionId", session.getId().toString())
                        .param("title", "Lecture 2: TCP Congestion Control & BBR")
                        .param("lectureNumber", "2")
                        .param("lectureDate", "2026-10-23")
                        .param("keyConcepts", "TCP, BBR, AIMD, Cubic, Congestion Window")
                        .param("summary", "Analysis of delay-based vs loss-based congestion control algorithms.")
                        .param("examRelevanceRating", "4")
                        .param("hasHomework", "true")
                        .param("homeworkTodo", "Simulate BBR with Mininet")
                        .param("contentMarkdown", "## Congestion Avoidance\n\n- Slow start\n- Congestion avoidance\n- Fast retransmit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/notes/*"));

        // 3. Verify Note saved and accessible via GET /notes/{id}
        var notes = lectureNoteRepository.findByCourseIdOrderByLectureNumberAsc(course.getId());
        assertEquals(1, notes.size());
        LectureNote savedNote = notes.get(0);
        assertEquals("Lecture 2: TCP Congestion Control & BBR", savedNote.getTitle());
        assertTrue(savedNote.getHasHomework());

        mockMvc.perform(get("/notes/" + savedNote.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("lecture-note-view"))
                .andExpect(model().attributeExists("note"))
                .andExpect(content().string(containsString("Lecture 2: TCP Congestion Control &amp; BBR")))
                .andExpect(content().string(containsString("Key Concepts:")))
                .andExpect(content().string(containsString("TCP")))
                .andExpect(content().string(containsString("BBR")))
                .andExpect(content().string(containsString("Cubic")))
                .andExpect(content().string(containsString("Simulate BBR with Mininet")))
                .andExpect(content().string(containsString("action=\"/notes/" + savedNote.getId() + "/delete\"")));
    }

    @Test
    void testCoursesIndexPageRendersProperly() throws Exception {
        Course course = new Course();
        course.setTitle("Software Engineering & Architecture");
        course.setCode("SE-501");
        course.setSemester("WS 2026/27");
        course.setEctsCredits(BigDecimal.valueOf(5.0));
        course = courseRepository.saveAndFlush(course);

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(view().name("courses"))
                .andExpect(model().attributeExists("courses", "semesters", "totalEcts", "upcomingExams", "newCourse", "lecturers"))
                .andExpect(content().string(containsString("Software Engineering &amp; Architecture")))
                .andExpect(content().string(containsString("SE-501")))
                .andExpect(content().string(containsString("Enrolled Courses")))
                .andExpect(content().string(containsString("Total ECTS")))
                .andExpect(content().string(containsString("id=\"newCourseModal\"")))
                .andExpect(content().string(containsString("action=\"/courses\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }
}

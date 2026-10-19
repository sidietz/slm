package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class SessionHubIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private CodingSessionRepository codingSessionRepository;

    @Autowired
    private WorkingSessionRepository workingSessionRepository;

    @Autowired
    private LearningSessionRepository learningSessionRepository;

    @Autowired
    private LearningItemRepository learningItemRepository;

    @Autowired
    private GamingSessionRepository gamingSessionRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PressRepository pressRepository;

    @Autowired
    private MeditationSessionRepository meditationSessionRepository;

    @Autowired
    private SportSessionRepository sportSessionRepository;

    @Autowired
    private SportTypeRepository sportTypeRepository;

    @Autowired
    private SleepSessionRepository sleepSessionRepository;

    @Autowired
    private LectureSessionRepository lectureSessionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EmployerRepository employerRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private Game createGame(String title) {
        Publisher publisher = new Publisher();
        publisher.setName("Publisher for " + title);
        publisher = publisherRepository.saveAndFlush(publisher);

        Studio studio = new Studio();
        studio.setName("Studio for " + title);
        studio = studioRepository.saveAndFlush(studio);

        Game game = new Game();
        game.setTitle(title);
        game.setPublisher(publisher);
        game.setStudio(studio);
        return gameRepository.saveAndFlush(game);
    }

    @Test
    void testViewSessionsHubAllAndFourPillarsKpis() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        // 1. Productivity: Coding session (1 hour net)
        CodingSession coding = new CodingSession();
        coding.setStart(now.minusHours(4));
        coding.setEnd(now.minusHours(3));
        coding.setBreakDuration(Duration.ZERO);
        coding.setFocusRating(4);
        codingSessionRepository.saveAndFlush(coding);

        // 2. Sport: Sport session (45 minutes)
        SportType sportType = new SportType();
        sportType.setName("Cycling");
        sportType = sportTypeRepository.saveAndFlush(sportType);

        SportSession sport = new SportSession();
        sport.setType(sportType);
        sport.setStart(now.minusHours(2));
        sport.setEnd(now.minusHours(1).minusMinutes(15));
        sport.setBreakDuration(Duration.ZERO);
        sport.setFocusRating(5);
        sportSessionRepository.saveAndFlush(sport);

        // 3. Wellness: Meditation session (30 minutes)
        MeditationSession meditation = new MeditationSession();
        meditation.setStart(now.minusHours(6));
        meditation.setEnd(now.minusHours(5).minusMinutes(30));
        meditation.setBreakDuration(Duration.ZERO);
        meditation.setFocusRating(5);
        meditationSessionRepository.saveAndFlush(meditation);

        // 4. Leisure: Gaming session (90 minutes)
        Game game = createGame("Civilization VI");

        GamingSession gaming = new GamingSession();
        gaming.setGame(game);
        gaming.setStart(now.minusHours(8));
        gaming.setEnd(now.minusHours(6).minusMinutes(30));
        gaming.setBreakDuration(Duration.ZERO);
        gaming.setFocusRating(3);
        gamingSessionRepository.saveAndFlush(gaming);

        // Verify GET /sessions (default ALL)
        // Per ADR 012: On ALL, Net Focused Time strictly aggregates Productivity sessions (1h 00m)
        mockMvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("activeType", "ALL"))
                .andExpect(model().attribute("kpiDurationTitle", "Net Focused Time"))
                .andExpect(model().attribute("kpiRatingTitle", "Avg Focus Rating"))
                .andExpect(model().attribute("totalSessionsCount", 4))
                .andExpect(model().attribute("totalNetDuration", "1h 00m"))
                .andExpect(model().attribute("avgFocusRating", "4.0"))
                .andExpect(content().string(containsString("Civilization VI")))
                .andExpect(content().string(containsString("Cycling")));

        // Verify GET /sessions?type=SPORT
        mockMvc.perform(get("/sessions").param("type", "SPORT"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("activeType", "SPORT"))
                .andExpect(model().attribute("kpiDurationTitle", "Active Training Time"))
                .andExpect(model().attribute("kpiRatingTitle", "Avg Workout Intensity"))
                .andExpect(model().attribute("totalSessionsCount", 1))
                .andExpect(model().attribute("totalNetDuration", "45m"));

        // Verify GET /sessions?type=WELLNESS
        mockMvc.perform(get("/sessions").param("type", "WELLNESS"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("activeType", "WELLNESS"))
                .andExpect(model().attribute("kpiDurationTitle", "Rest & Recovery Time"))
                .andExpect(model().attribute("kpiRatingTitle", "Avg Sleep Quality"))
                .andExpect(model().attribute("totalSessionsCount", 1))
                .andExpect(model().attribute("totalNetDuration", "30m"));

        // Verify GET /sessions?type=LEISURE
        mockMvc.perform(get("/sessions").param("type", "LEISURE"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("activeType", "LEISURE"))
                .andExpect(model().attribute("kpiDurationTitle", "Recreation Time"))
                .andExpect(model().attribute("kpiRatingTitle", "Avg Game Rating"))
                .andExpect(model().attribute("totalSessionsCount", 1))
                .andExpect(model().attribute("totalNetDuration", "1h 30m"));

        // Verify GET /sessions?type=MEDITATION
        mockMvc.perform(get("/sessions").param("type", "MEDITATION"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("kpiDurationTitle", "Mindfulness Time"))
                .andExpect(model().attribute("kpiRatingTitle", "Avg Mindfulness Rating"));
    }

    @Test
    void testDirectCodingAndWorkingSessionViewsAndSubmissions() throws Exception {
        // Direct coding sessions
        mockMvc.perform(get("/coding-sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("coding-sessions"))
                .andExpect(model().attributeExists("sessions", "codingSession"));

        mockMvc.perform(post("/coding-sessions")
                        .with(csrf())
                        .param("start", "2026-10-19T10:00")
                        .param("end", "2026-10-19T12:00")
                        .param("breakMinutes", "15")
                        .param("focusRating", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/coding-sessions"));

        assertEquals(1, codingSessionRepository.count());

        // Direct working sessions
        Employer employer = new Employer();
        employer.setName("Acme Tech");
        employer = employerRepository.saveAndFlush(employer);

        mockMvc.perform(get("/working-sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("working-sessions"))
                .andExpect(model().attributeExists("sessions", "employers", "workingSession"));

        mockMvc.perform(post("/working-sessions")
                        .with(csrf())
                        .param("start", "2026-10-19T09:00")
                        .param("end", "2026-10-19T17:00")
                        .param("breakMinutes", "60")
                        .param("employer", employer.getId().toString())
                        .param("billable", "true")
                        .param("focusRating", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/working-sessions"));

        assertEquals(1, workingSessionRepository.count());
    }

    @Test
    void testSubmitAllPolymorphicSessionTypesViaModalEndpoints() throws Exception {
        // 1. POST /sessions/coding
        mockMvc.perform(post("/sessions/coding")
                        .with(csrf())
                        .param("start", "2026-10-19T10:00")
                        .param("end", "2026-10-19T11:30")
                        .param("breakMinutes", "10")
                        .param("focusRating", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=CODING"));
        assertEquals(1, codingSessionRepository.count());

        // 2. POST /sessions/working
        Employer employer = new Employer();
        employer.setName("Global Inc");
        employer = employerRepository.saveAndFlush(employer);

        mockMvc.perform(post("/sessions/working")
                        .with(csrf())
                        .param("start", "2026-10-19T08:00")
                        .param("end", "2026-10-19T16:00")
                        .param("breakMinutes", "45")
                        .param("employer", employer.getId().toString())
                        .param("billable", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=WORKING"));
        assertEquals(1, workingSessionRepository.count());

        // 3. POST /sessions/learning
        LearningItem item = new LearningItem();
        item.setTitle("Distributed Systems Concepts");
        item = learningItemRepository.saveAndFlush(item);

        mockMvc.perform(post("/sessions/learning")
                        .with(csrf())
                        .param("start", "2026-10-19T13:00")
                        .param("end", "2026-10-19T14:00")
                        .param("breakMinutes", "0")
                        .param("learningItem", item.getId().toString())
                        .param("unitsCompleted", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=LEARNING"));
        assertEquals(1, learningSessionRepository.count());
        // Verify lastUpdated set
        LearningItem updatedItem = learningItemRepository.findById(item.getId()).orElseThrow();
        assertNotNull(updatedItem.getLastUpdated());

        // 4. POST /sessions/gaming
        Game game = createGame("Elden Ring");

        mockMvc.perform(post("/sessions/gaming")
                        .with(csrf())
                        .param("start", "2026-10-19T20:00")
                        .param("end", "2026-10-19T22:00")
                        .param("breakMinutes", "0")
                        .param("game", game.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=GAMING"));
        assertEquals(1, gamingSessionRepository.count());

        // 5. POST /sessions/reading (tests reading speed calculation)
        Author author = new Author();
        author.setFirstname("Donald");
        author.setLastname("Knuth");
        author = authorRepository.saveAndFlush(author);

        Press press = new Press();
        press.setName("Addison-Wesley");
        press = pressRepository.saveAndFlush(press);

        Book book = new Book();
        book.setIsbn("9780201896831");
        book.setTitle("The Art of Computer Programming");
        book.setAuthor(author);
        book.setPress(press);
        book = bookRepository.saveAndFlush(book);

        // 60 minutes, 30 pages -> readingSpeed = (30 / 60) * 60 = 30 pages/h
        mockMvc.perform(post("/sessions/reading")
                        .with(csrf())
                        .param("start", "2026-10-19T14:00")
                        .param("end", "2026-10-19T15:00")
                        .param("breakMinutes", "0")
                        .param("book", book.getId().toString())
                        .param("startPageCount", "10")
                        .param("endPageCount", "40"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=READING"));
        ReadingSession savedReading = readingSessionRepository.findAll().getFirst();
        assertEquals(30.0F, savedReading.getReadingSpeed());

        // 6. POST /sessions/meditation
        mockMvc.perform(post("/sessions/meditation")
                        .with(csrf())
                        .param("start", "2026-10-19T07:00")
                        .param("end", "2026-10-19T07:20")
                        .param("breakMinutes", "0")
                        .param("focusRating", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=MEDITATION"));
        assertEquals(1, meditationSessionRepository.count());

        // 7. POST /sessions/sport
        SportType sportType = new SportType();
        sportType.setName("Swimming");
        sportType = sportTypeRepository.saveAndFlush(sportType);

        mockMvc.perform(post("/sessions/sport")
                        .with(csrf())
                        .param("start", "2026-10-19T18:00")
                        .param("end", "2026-10-19T19:00")
                        .param("breakMinutes", "0")
                        .param("type", String.valueOf(sportType.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SPORT"));
        assertEquals(1, sportSessionRepository.count());

        // 8. POST /sessions/lecture
        Course course = new Course();
        course.setTitle("Distributed Algorithms");
        course = courseRepository.saveAndFlush(course);

        mockMvc.perform(post("/sessions/lecture")
                        .with(csrf())
                        .param("start", "2026-10-19T10:15")
                        .param("end", "2026-10-19T11:45")
                        .param("breakMinutes", "0")
                        .param("courseId", course.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=LECTURE"));
        assertEquals(1, lectureSessionRepository.count());
    }

    @Test
    void testDeleteSession() throws Exception {
        CodingSession coding = new CodingSession();
        coding.setStart(LocalDateTime.now().minusHours(2));
        coding.setEnd(LocalDateTime.now().minusHours(1));
        coding.setBreakDuration(Duration.ZERO);
        coding = codingSessionRepository.saveAndFlush(coding);
        UUID sessionId = coding.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/sessions/delete/" + sessionId)
                        .with(csrf())
                        .param("returnType", "CODING"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=CODING"));

        entityManager.flush();
        entityManager.clear();

        assertFalse(sessionRepository.existsById(sessionId));
    }
}

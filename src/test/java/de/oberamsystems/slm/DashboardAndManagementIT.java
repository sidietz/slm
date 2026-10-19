package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class DashboardAndManagementIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SportTypeRepository sportTypeRepository;

    @Autowired
    private SportSessionRepository sportSessionRepository;

    @Autowired
    private MeditationSessionRepository meditationSessionRepository;

    @Autowired
    private HumanRepository humanRepository;

    @Autowired
    private HumanEventRepository humanEventRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private PressRepository pressRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReadingSessionRepository readingSessionRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testIndexDashboardWithActivitiesMilestonesAndBirthdays() throws Exception {
        // 1. Create Human with birthday
        Human human = new Human();
        human.setFirstname("Karin");
        human.setLastname("Muster");
        human.setBirthday(java.sql.Date.valueOf(LocalDate.now().plusDays(5)));
        human.setCategory(HumanCategory.FRIEND);
        human = humanRepository.saveAndFlush(human);

        // 2. Create HumanEvent milestone
        HumanEvent event = new HumanEvent();
        event.setTitle("Silver Wedding Anniversary");
        event.setEventType(EventType.WEDDING_ANNIVERSARY);
        event.setEventDate(LocalDate.now().minusYears(25).plusDays(10));
        event.setHuman(human);
        event = humanEventRepository.saveAndFlush(event);

        // 3. Create SportSession
        SportType sportType = new SportType();
        sportType.setName("Trail Running");
        sportType = sportTypeRepository.saveAndFlush(sportType);

        SportSession sport = new SportSession();
        sport.setType(sportType);
        sport.setStart(LocalDateTime.now().minusHours(2));
        sport.setEnd(LocalDateTime.now().minusHours(1));
        sport.setBreakDuration(Duration.ZERO);
        sport = sportSessionRepository.saveAndFlush(sport);

        // 4. Create MeditationSession
        MeditationSession meditation = new MeditationSession();
        meditation.setStart(LocalDateTime.now().minusHours(4));
        meditation.setEnd(LocalDateTime.now().minusHours(3).minusMinutes(40));
        meditation.setBreakDuration(Duration.ZERO);
        meditation = meditationSessionRepository.saveAndFlush(meditation);

        // 5. Create ReadingSession
        Author author = new Author();
        author.setFirstname("Martin");
        author.setLastname("Kleppmann");
        author = authorRepository.saveAndFlush(author);

        Press press = new Press();
        press.setName("O'Reilly");
        press = pressRepository.saveAndFlush(press);

        Book book = new Book();
        book.setIsbn("9780132350884");
        book.setTitle("Designing Data-Intensive Applications");
        book.setAuthor(author);
        book.setPress(press);
        book = bookRepository.saveAndFlush(book);

        ReadingSession reading = new ReadingSession();
        reading.setBook(book);
        reading.setStartPageCount(100);
        reading.setEndPageCount(145);
        reading.setStart(LocalDateTime.now().minusHours(6));
        reading.setEnd(LocalDateTime.now().minusHours(5));
        reading.setBreakDuration(Duration.ZERO);
        reading = readingSessionRepository.saveAndFlush(reading);

        // 6. Perform GET / and verify index dashboard
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("msgs", "upcomingEvents"))
                .andExpect(content().string(containsString("Trail Running")))
                .andExpect(content().string(containsString("Last sport activity was:")))
                .andExpect(content().string(containsString("Last meditation was at")))
                .andExpect(content().string(containsString("Last reading session was at")))
                .andExpect(content().string(containsString("Silver Wedding Anniversary")));
    }

    @Test
    void testIndexDashboardEmptyState() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("msgs", "upcomingEvents"));

        mockMvc.perform(get("/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testImprintPageRendering() throws Exception {
        mockMvc.perform(get("/imprint"))
                .andExpect(status().isOk())
                .andExpect(view().name("imprint"))
                .andExpect(model().attributeExists("msgs"))
                .andExpect(content().string(containsString("simon&#39;s life manager")))
                .andExpect(content().string(containsString("AGPL-3")));

        mockMvc.perform(get("/imprint.html"))
                .andExpect(status().isOk())
                .andExpect(view().name("imprint"));
    }

    @Test
    void testAllManagementDashboardsRender() throws Exception {
        mockMvc.perform(get("/activity-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("activity-management"));

        mockMvc.perform(get("/book-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-management"));

        mockMvc.perform(get("/habit-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("habit-management"));

        mockMvc.perform(get("/human-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("human-management"));

        mockMvc.perform(get("/misc-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("misc-management"));

        mockMvc.perform(get("/spend-management"))
                .andExpect(status().isOk())
                .andExpect(view().name("spend-management"));
    }
}

package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class ActivitiesAndMediaIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ActivityTypeRepository activityTypeRepository;

    @Autowired
    private SportTypeRepository sportTypeRepository;

    @Autowired
    private SportSessionRepository sportSessionRepository;

    @Autowired
    private LearningItemRepository learningItemRepository;

    @Autowired
    private LearningSessionRepository learningSessionRepository;

    @Autowired
    private MeditationSessionRepository meditationSessionRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PublisherRepository publisherRepository;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private GamingSessionRepository gamingSessionRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private DeviceTypeRepository deviceTypeRepository;

    @Autowired
    private DeviceRepository deviceRepository;

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
    void testActivityAndActivityTypeFlow() throws Exception {
        // 1. GET /activity-types
        mockMvc.perform(get("/activity-types"))
                .andExpect(status().isOk())
                .andExpect(view().name("activity-types"))
                .andExpect(model().attributeExists("activityTypes", "type"));

        // 2. POST /activity-types
        mockMvc.perform(post("/activity-types")
                        .with(csrf())
                        .param("name", "Gardening"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activity-types"));
        assertEquals(1, activityTypeRepository.count());
        ActivityType type = activityTypeRepository.findAll().getFirst();

        // 3. GET /activities
        mockMvc.perform(get("/activities"))
                .andExpect(status().isOk())
                .andExpect(view().name("activities"))
                .andExpect(model().attributeExists("activities", "activity", "activityTypes"))
                .andExpect(content().string(containsString("Gardening")));

        // 4. POST /activities
        mockMvc.perform(post("/activities")
                        .with(csrf())
                        .param("name", "Pruning Apple Trees")
                        .param("type", String.valueOf(type.getId()))
                        .param("start", "2026-10-19T14:00")
                        .param("end", "2026-10-19T16:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activities"));
        assertEquals(1, activityRepository.count());
    }

    @Test
    void testSportAndSportTypeFlow() throws Exception {
        // 1. GET /sport-types
        mockMvc.perform(get("/sport-types"))
                .andExpect(status().isOk())
                .andExpect(view().name("sport-types"))
                .andExpect(model().attributeExists("types", "type"));

        // 2. POST /sport-types
        mockMvc.perform(post("/sport-types")
                        .with(csrf())
                        .param("name", "Bouldering"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sport-types"));
        assertEquals(1, sportTypeRepository.count());
        SportType type = sportTypeRepository.findAll().getFirst();

        // 3. GET /sports
        mockMvc.perform(get("/sports"))
                .andExpect(status().isOk())
                .andExpect(view().name("sports"))
                .andExpect(model().attributeExists("sports", "sport", "sportTypes"))
                .andExpect(content().string(containsString("Bouldering")));

        // 4. POST /sports
        mockMvc.perform(post("/sports")
                        .with(csrf())
                        .param("type", String.valueOf(type.getId()))
                        .param("start", "2026-10-19T17:00")
                        .param("end", "2026-10-19T18:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sports"));
        assertEquals(1, sportSessionRepository.count());
    }

    @Test
    void testLearningItemAndSessionFlow() throws Exception {
        // 1. GET /learning-items
        mockMvc.perform(get("/learning-items"))
                .andExpect(status().isOk())
                .andExpect(view().name("learning-items"))
                .andExpect(model().attributeExists("items", "sources", "statuss", "learningitem"));

        // 2. POST /learning-items
        mockMvc.perform(post("/learning-items")
                        .with(csrf())
                        .param("title", "PostgreSQL Internals")
                        .param("description", "MVCC and WAL replication deep-dive")
                        .param("author", "Egor Rogov")
                        .param("progress", "25.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/learning-items"));
        assertEquals(1, learningItemRepository.count());
        LearningItem item = learningItemRepository.findAll().getFirst();

        // 3. GET /learning-sessions
        mockMvc.perform(get("/learning-sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("learning-sessions"))
                .andExpect(model().attributeExists("learningsessions", "learningsession", "items"))
                .andExpect(content().string(containsString("PostgreSQL Internals")));

        // 4. POST /learning-sessions
        mockMvc.perform(post("/learning-sessions")
                        .with(csrf())
                        .param("learningItem", item.getId().toString())
                        .param("unitsCompleted", "1")
                        .param("breakMinutes", "10")
                        .param("start", "2026-10-19T09:00")
                        .param("end", "2026-10-19T10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/learning-sessions"));
        assertEquals(1, learningSessionRepository.count());
    }

    @Test
    void testMeditationFlow() throws Exception {
        // 1. GET /meditations
        mockMvc.perform(get("/meditations"))
                .andExpect(status().isOk())
                .andExpect(view().name("meditations"))
                .andExpect(model().attributeExists("meditations", "meditation"));

        // 2. POST /meditations
        mockMvc.perform(post("/meditations")
                        .with(csrf())
                        .param("start", "2026-10-19T06:30")
                        .param("end", "2026-10-19T06:50")
                        .param("focusRating", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/meditations"));
        assertEquals(1, meditationSessionRepository.count());
    }

    @Test
    void testGameEcosystemFlow() throws Exception {
        // 1. Publishers
        mockMvc.perform(get("/publishers"))
                .andExpect(status().isOk())
                .andExpect(view().name("publishers"))
                .andExpect(model().attributeExists("publishers", "publisher"));

        mockMvc.perform(post("/publishers")
                        .with(csrf())
                        .param("name", "FromSoftware Inc.")
                        .param("hq", "Tokyo, Japan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));
        Publisher publisher = publisherRepository.findAll().getFirst();

        // 2. Studios
        mockMvc.perform(get("/studios"))
                .andExpect(status().isOk())
                .andExpect(view().name("studios"))
                .andExpect(model().attributeExists("studios", "studio"));

        mockMvc.perform(post("/studios")
                        .with(csrf())
                        .param("name", "FROMSOFTWARE Tokyo")
                        .param("hq", "Tokyo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studios"));
        Studio studio = studioRepository.findAll().getFirst();

        // 3. Vendors and Manufacturers
        mockMvc.perform(get("/vendors"))
                .andExpect(status().isOk())
                .andExpect(view().name("vendors"))
                .andExpect(model().attributeExists("vendors", "vendor"));

        mockMvc.perform(post("/vendors")
                        .with(csrf())
                        .param("name", "Amazon EU"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendors"));
        Vendor vendor = vendorRepository.findAll().getFirst();

        mockMvc.perform(get("/manufacturers"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturers"))
                .andExpect(model().attributeExists("manufacturers", "manufacturer"));

        mockMvc.perform(post("/manufacturers")
                        .with(csrf())
                        .param("name", "Sony Interactive Entertainment"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));
        Manufacturer manufacturer = manufacturerRepository.findAll().getFirst();

        // 4. Device Type and Device
        DeviceType deviceType = new DeviceType();
        deviceType.setName("Console");
        deviceType = deviceTypeRepository.saveAndFlush(deviceType);

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(view().name("devices"))
                .andExpect(model().attributeExists("devices", "deviceTypes", "vendors", "manufacturers", "device"));

        mockMvc.perform(post("/devices")
                        .with(csrf())
                        .param("name", "PlayStation 5 Pro")
                        .param("deviceType", deviceType.getId().toString())
                        .param("vendor", vendor.getId().toString())
                        .param("manufacturer", manufacturer.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devices"));
        assertEquals(1, deviceRepository.count());
        Device device = deviceRepository.findAll().getFirst();

        // 5. Game
        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attributeExists("games", "studios", "publishers", "game"));

        mockMvc.perform(post("/games")
                        .with(csrf())
                        .param("title", "Armored Core VI")
                        .param("publisher", publisher.getId().toString())
                        .param("studio", studio.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games"));
        assertEquals(1, gameRepository.count());
        Game game = gameRepository.findAll().getFirst();

        // 6. Gaming Session
        mockMvc.perform(get("/gaming-sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("gaming-sessions"))
                .andExpect(model().attributeExists("gamingsessions", "devices", "games", "gamingsession"))
                .andExpect(content().string(containsString("Armored Core VI")));

        mockMvc.perform(post("/gaming-sessions")
                        .with(csrf())
                        .param("game", game.getId().toString())
                        .param("device", device.getId().toString())
                        .param("breakMinutes", "5")
                        .param("start", "2026-10-19T21:00")
                        .param("end", "2026-10-19T22:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gaming-sessions"));
        assertEquals(1, gamingSessionRepository.count());
    }

    @Test
    void testBookAndReadingSessionFlow() throws Exception {
        // 1. Authors
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors"))
                .andExpect(model().attributeExists("authors", "author"));

        mockMvc.perform(post("/authors")
                        .with(csrf())
                        .param("firstname", "Joshua")
                        .param("lastname", "Bloch"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));
        Author author = authorRepository.findAll().getFirst();

        // 2. Press
        Press press = new Press();
        press.setName("Addison-Wesley Professional");
        press = pressRepository.saveAndFlush(press);

        // 3. Books
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attributeExists("books", "authors", "presses", "types", "genres", "book"));

        mockMvc.perform(post("/books")
                        .with(csrf())
                        .param("title", "Effective Java")
                        .param("isbn", "9780134685991")
                        .param("author", author.getId().toString())
                        .param("press", press.getId().toString())
                        .param("pageCount", "416"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        assertEquals(1, bookRepository.count());
        Book book = bookRepository.findAll().getFirst();

        // 4. Reading Session
        mockMvc.perform(get("/reading-sessions"))
                .andExpect(status().isOk())
                .andExpect(view().name("reading-sessions"))
                .andExpect(model().attributeExists("readingsessions", "readingsession", "books"))
                .andExpect(content().string(containsString("Effective Java")));

        // Read 50 pages in 60 minutes -> readingSpeed = 50 pages/h
        mockMvc.perform(post("/reading-sessions")
                        .with(csrf())
                        .param("book", book.getId().toString())
                        .param("startPageCount", "1")
                        .param("endPageCount", "51")
                        .param("breakMinutes", "0")
                        .param("start", "2026-10-19T20:00")
                        .param("end", "2026-10-19T21:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reading-sessions"));
        assertEquals(1, readingSessionRepository.count());
        ReadingSession saved = readingSessionRepository.findAll().getFirst();
        assertEquals(50.0F, saved.getReadingSpeed());
    }
}

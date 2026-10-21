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

    @Test
    void testUpdateActivityAndTypeFlow() throws Exception {
        ActivityType type = new ActivityType(null, "Woodworking");
        type = activityTypeRepository.saveAndFlush(type);
        Long typeId = type.getId();

        mockMvc.perform(post("/activity-types")
                        .with(csrf())
                        .param("id", typeId.toString())
                        .param("name", "Fine Woodworking"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activity-types"));

        assertEquals(1, activityTypeRepository.count());
        entityManager.clear();
        ActivityType updatedType = activityTypeRepository.findById(typeId).orElseThrow();
        assertEquals("Fine Woodworking", updatedType.getName());

        Activity act = new Activity();
        act.setType(updatedType);
        act.setTitle("Oak Table Build");
        act.setLocation("Workshop");
        act.setStart(LocalDateTime.of(2026, 10, 20, 14, 0));
        act.setEnd(LocalDateTime.of(2026, 10, 20, 16, 0));
        act = activityRepository.saveAndFlush(act);
        Long actId = act.getId();

        mockMvc.perform(post("/activities")
                        .with(csrf())
                        .param("id", actId.toString())
                        .param("type", updatedType.getId().toString())
                        .param("title", "Dining Table Finishing")
                        .param("location", "Main Studio")
                        .param("start", "2026-10-20T14:30")
                        .param("end", "2026-10-20T17:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activities"));

        assertEquals(1, activityRepository.count());
        entityManager.clear();
        Activity updatedAct = activityRepository.findById(actId).orElseThrow();
        assertEquals("Dining Table Finishing", updatedAct.getTitle());
        assertEquals("Main Studio", updatedAct.getLocation());
        assertEquals(LocalDateTime.of(2026, 10, 20, 14, 30), updatedAct.getStart());
        assertEquals(LocalDateTime.of(2026, 10, 20, 17, 30), updatedAct.getEnd());
        assertNotNull(updatedAct.getDuration());
    }

    @Test
    void testUpdateBookEcosystemFlow() throws Exception {
        Author author = new Author();
        author.setFirstname("Martin");
        author.setLastname("Fowler");
        author.setBirthday(java.time.LocalDate.of(1963, 12, 18));
        author = authorRepository.saveAndFlush(author);
        Long authorId = author.getId();

        mockMvc.perform(post("/authors")
                        .with(csrf())
                        .param("id", authorId.toString())
                        .param("firstname", "Martin")
                        .param("lastname", "Fowler (Refactored)")
                        .param("birthday", "1963-12-18"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));

        assertEquals(1, authorRepository.count());
        entityManager.clear();
        Author updatedAuthor = authorRepository.findById(authorId).orElseThrow();
        assertEquals("Fowler (Refactored)", updatedAuthor.getLastname());

        Press press = new Press();
        press.setName("O'Reilly Media");
        press.setHq("Sebastopol, CA");
        press = pressRepository.saveAndFlush(press);
        Long pressId = press.getId();

        mockMvc.perform(post("/press")
                        .with(csrf())
                        .param("id", pressId.toString())
                        .param("name", "O'Reilly Media Inc.")
                        .param("hq", "Sebastopol, California"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/press"));

        assertEquals(1, pressRepository.count());
        entityManager.clear();
        Press updatedPress = pressRepository.findById(pressId).orElseThrow();
        assertEquals("O'Reilly Media Inc.", updatedPress.getName());
        assertEquals("Sebastopol, California", updatedPress.getHq());

        Book book = new Book();
        book.setTitle("Refactoring");
        book.setIsbn("9780201485677");
        book.setAuthor(updatedAuthor);
        book.setPress(updatedPress);
        book.setPageCount(431);
        book.setPrice(49.99F);
        book = bookRepository.saveAndFlush(book);
        Long bookId = book.getId();

        mockMvc.perform(post("/books")
                        .with(csrf())
                        .param("id", bookId.toString())
                        .param("title", "Refactoring (2nd Edition)")
                        .param("isbn", "9780201485677")
                        .param("author", updatedAuthor.getId().toString())
                        .param("press", updatedPress.getId().toString())
                        .param("pageCount", "448")
                        .param("price", "54.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        assertEquals(1, bookRepository.count());
        entityManager.clear();
        Book updatedBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals("Refactoring (2nd Edition)", updatedBook.getTitle());
        assertEquals(448, updatedBook.getPageCount());
        assertEquals(54.99F, updatedBook.getPrice());

        ReadingSession rs = new ReadingSession();
        rs.setBook(updatedBook);
        rs.setStart(LocalDateTime.of(2026, 10, 20, 19, 0));
        rs.setEnd(LocalDateTime.of(2026, 10, 20, 20, 0));
        rs.setStartPageCount(1);
        rs.setEndPageCount(31);
        rs.setReadingSpeed(30.0F);
        rs = readingSessionRepository.saveAndFlush(rs);
        java.util.UUID sessionId = rs.getId();

        mockMvc.perform(post("/reading-sessions")
                        .with(csrf())
                        .param("id", sessionId.toString())
                        .param("book", updatedBook.getId().toString())
                        .param("startPageCount", "1")
                        .param("endPageCount", "61")
                        .param("breakMinutes", "0")
                        .param("start", "2026-10-20T19:00")
                        .param("end", "2026-10-20T20:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reading-sessions"));

        assertEquals(1, readingSessionRepository.count());
        entityManager.clear();
        ReadingSession updatedSession = readingSessionRepository.findById(sessionId).orElseThrow();
        assertEquals(61, updatedSession.getEndPageCount());
        assertEquals(60.0F, updatedSession.getReadingSpeed());
    }

    @Test
    void testUpdateGameEcosystemFlow() throws Exception {
        Publisher publisher = new Publisher();
        publisher.setName("Valve");
        publisher.setHq("Bellevue");
        publisher = publisherRepository.saveAndFlush(publisher);
        Long publisherId = publisher.getId();

        mockMvc.perform(post("/publishers")
                        .with(csrf())
                        .param("id", publisherId.toString())
                        .param("name", "Valve Corporation")
                        .param("hq", "Bellevue, WA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));

        assertEquals(1, publisherRepository.count());
        entityManager.clear();
        Publisher updatedPublisher = publisherRepository.findById(publisherId).orElseThrow();
        assertEquals("Valve Corporation", updatedPublisher.getName());

        Studio studio = new Studio();
        studio.setName("Valve Software");
        studio.setHq("Bellevue");
        studio = studioRepository.saveAndFlush(studio);
        Long studioId = studio.getId();

        mockMvc.perform(post("/studios")
                        .with(csrf())
                        .param("id", studioId.toString())
                        .param("name", "Valve Studios")
                        .param("hq", "Bellevue, WA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studios"));

        assertEquals(1, studioRepository.count());
        entityManager.clear();
        Studio updatedStudio = studioRepository.findById(studioId).orElseThrow();
        assertEquals("Valve Studios", updatedStudio.getName());

        Manufacturer m = manufacturerRepository.saveAndFlush(new Manufacturer(null, "Valve Corp", "Bellevue", null));
        Vendor v = vendorRepository.saveAndFlush(new Vendor(null, "Steam Store"));
        DeviceType dt = deviceTypeRepository.saveAndFlush(new DeviceType(null, "Handheld Console"));

        Device dev = new Device();
        dev.setName("Steam Deck");
        dev.setModelName("LCD 512GB");
        dev.setManufacturer(m);
        dev.setVendor(v);
        dev.setDeviceType(dt);
        dev.setPrice(549.0F);
        dev = deviceRepository.saveAndFlush(dev);
        Long devId = dev.getId();

        mockMvc.perform(post("/devices")
                        .with(csrf())
                        .param("id", devId.toString())
                        .param("name", "Steam Deck OLED")
                        .param("modelName", "OLED 1TB")
                        .param("manufacturer", m.getId().toString())
                        .param("vendor", v.getId().toString())
                        .param("deviceType", dt.getId().toString())
                        .param("price", "649.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devices"));

        assertEquals(1, deviceRepository.count());
        entityManager.clear();
        Device updatedDev = deviceRepository.findById(devId).orElseThrow();
        assertEquals("Steam Deck OLED", updatedDev.getName());
        assertEquals("OLED 1TB", updatedDev.getModelName());
        assertEquals(649.0F, updatedDev.getPrice());

        Game game = new Game();
        game.setTitle("Half-Life: Alyx");
        game.setPublisher(updatedPublisher);
        game.setStudio(updatedStudio);
        game.setPrice(59.99F);
        game = gameRepository.saveAndFlush(game);
        Long gameId = game.getId();

        mockMvc.perform(post("/games")
                        .with(csrf())
                        .param("id", gameId.toString())
                        .param("title", "Half-Life: Alyx (Definitive)")
                        .param("publisher", updatedPublisher.getId().toString())
                        .param("studio", updatedStudio.getId().toString())
                        .param("price", "49.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games"));

        assertEquals(1, gameRepository.count());
        entityManager.clear();
        Game updatedGame = gameRepository.findById(gameId).orElseThrow();
        assertEquals("Half-Life: Alyx (Definitive)", updatedGame.getTitle());
        assertEquals(49.99F, updatedGame.getPrice());
    }

    @Test
    void testModalCreateActivitiesAndMediaWithEmptyIdParam() throws Exception {
        // 1. ActivityType via modal with empty id
        mockMvc.perform(post("/activity-types")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Coding Project"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activity-types"));
        assertEquals(1, activityTypeRepository.count());
        ActivityType at = activityTypeRepository.findAll().getFirst();

        // 2. Activity via modal with empty id and explicit type
        mockMvc.perform(post("/activities")
                        .with(csrf())
                        .param("id", "")
                        .param("title", "Build Feature")
                        .param("type", at.getId().toString())
                        .param("start", "2026-10-21T09:00")
                        .param("end", "2026-10-21T10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activities"));
        assertEquals(1, activityRepository.count());

        // 3. Activity via modal with empty id and omitted type (tests fallback)
        mockMvc.perform(post("/activities")
                        .with(csrf())
                        .param("id", "")
                        .param("title", "Review PR")
                        .param("start", "2026-10-21T11:00")
                        .param("end", "2026-10-21T11:45"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/activities"));
        assertEquals(2, activityRepository.count());

        // 4. Author via modal with empty id
        mockMvc.perform(post("/authors")
                        .with(csrf())
                        .param("id", "")
                        .param("firstname", "Robert")
                        .param("lastname", "Martin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/authors"));
        assertEquals(1, authorRepository.count());
        Author author = authorRepository.findAll().getFirst();

        // 5. Press via modal with empty id
        mockMvc.perform(post("/press")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Prentice Hall"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/press"));
        assertEquals(1, pressRepository.count());
        Press press = pressRepository.findAll().getFirst();

        // 6. Book via modal with empty id and explicit author/press
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .param("id", "")
                        .param("title", "Clean Code")
                        .param("isbn", "9780132350884")
                        .param("author", author.getId().toString())
                        .param("press", press.getId().toString())
                        .param("pageCount", "464")
                        .param("price", "42.50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        assertEquals(1, bookRepository.count());

        // 7. Book via modal with empty id and omitted author/press (tests fallback)
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .param("id", "")
                        .param("title", "Clean Architecture")
                        .param("pageCount", "430")
                        .param("price", "39.90"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
        assertEquals(2, bookRepository.count());

        // 8. Publisher via modal with empty id
        mockMvc.perform(post("/publishers")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Nintendo Co."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/publishers"));
        assertEquals(1, publisherRepository.count());
        Publisher pub = publisherRepository.findAll().getFirst();

        // 9. Studio via modal with empty id
        mockMvc.perform(post("/studios")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Nintendo EPD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/studios"));
        assertEquals(1, studioRepository.count());
        Studio stu = studioRepository.findAll().getFirst();

        // 10. Game via modal with empty id and explicit publisher/studio
        mockMvc.perform(post("/games")
                        .with(csrf())
                        .param("id", "")
                        .param("title", "Zelda: Tears of the Kingdom")
                        .param("publisher", pub.getId().toString())
                        .param("studio", stu.getId().toString())
                        .param("price", "69.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games"));
        assertEquals(1, gameRepository.count());

        // 11. Game via modal with empty id and omitted publisher/studio (tests fallback)
        mockMvc.perform(post("/games")
                        .with(csrf())
                        .param("id", "")
                        .param("title", "Super Mario Odyssey")
                        .param("price", "59.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/games"));
        assertEquals(2, gameRepository.count());

        // 12. Manufacturer & Vendor via modal
        mockMvc.perform(post("/manufacturers")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Nintendo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));
        assertEquals(1, manufacturerRepository.count());
        Manufacturer man = manufacturerRepository.findAll().getFirst();

        mockMvc.perform(post("/vendors")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "My Nintendo Store"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vendors"));
        assertEquals(1, vendorRepository.count());
        Vendor ven = vendorRepository.findAll().getFirst();

        DeviceType devType = deviceTypeRepository.saveAndFlush(new DeviceType(null, "Gaming Console"));

        // 13. Device via modal with empty id and explicit relations
        mockMvc.perform(post("/devices")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Nintendo Switch OLED")
                        .param("manufacturer", man.getId().toString())
                        .param("vendor", ven.getId().toString())
                        .param("deviceType", devType.getId().toString())
                        .param("price", "349.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devices"));
        assertEquals(1, deviceRepository.count());

        // 14. Device via modal with empty id and omitted relations (tests fallback)
        mockMvc.perform(post("/devices")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Pro Controller")
                        .param("price", "69.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/devices"));
        assertEquals(2, deviceRepository.count());
    }
}

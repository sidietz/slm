package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class SocialAndPeopleIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private HumanRepository humanRepository;

    @Autowired
    private HumanEventRepository humanEventRepository;

    @Autowired
    private CalendarEventRepository calendarEventRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testHumanFlowAndAutomaticEventSync() throws Exception {
        // 1. GET /humans
        mockMvc.perform(get("/humans"))
                .andExpect(status().isOk())
                .andExpect(view().name("humans"))
                .andExpect(model().attributeExists("humans", "categories", "human"));

        // 2. POST /humans with birthday
        mockMvc.perform(post("/humans")
                        .with(csrf())
                        .param("firstname", "Ada")
                        .param("lastname", "Lovelace")
                        .param("category", "FRIEND")
                        .param("birthday", "1815-12-10"))
                .andExpect(status().isOk())
                .andExpect(view().name("humans"))
                .andExpect(content().string(containsString("Ada")))
                .andExpect(content().string(containsString("Lovelace")));

        List<Human> humans = humanRepository.findAll();
        Human savedHuman = humans.stream()
                .filter(h -> "Ada".equals(h.getFirstname()) && "Lovelace".equals(h.getLastname()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Saved human Ada Lovelace not found"));
        assertNotNull(savedHuman.getBirthday());

        // Verify syncHumanEvents automatically created a BIRTHDAY event
        List<HumanEvent> events = humanEventRepository.findByHumanIdOrderByDaysUntilNextAsc(savedHuman.getId());
        assertFalse(events.isEmpty());
        assertEquals(EventType.BIRTHDAY, events.getFirst().getEventType());
    }

    @Test
    void testHumanEventFlowFilteringAndDeletion() throws Exception {
        Human human = new Human();
        human.setFirstname("Grace");
        human.setLastname("Hopper");
        human.setCategory(HumanCategory.FRIEND);
        human = humanRepository.saveAndFlush(human);

        // 1. GET /human-events empty
        mockMvc.perform(get("/human-events"))
                .andExpect(status().isOk())
                .andExpect(view().name("human-events"))
                .andExpect(model().attributeExists("events", "types", "humans", "newEvent"));

        // 2. POST /human-events
        mockMvc.perform(post("/human-events")
                        .with(csrf())
                        .param("title", "PhD Defense Anniversary")
                        .param("eventType", "CUSTOM")
                        .param("eventDate", "2026-05-15")
                        .param("humanId", String.valueOf(human.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/human-events"));

        HumanEvent event = humanEventRepository.findAll().stream()
                .filter(e -> "PhD Defense Anniversary".equals(e.getTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("HumanEvent PhD Defense Anniversary not found"));
        assertEquals("PhD Defense Anniversary", event.getTitle());

        // 3. GET /events?type=CUSTOM
        mockMvc.perform(get("/events").param("type", "CUSTOM"))
                .andExpect(status().isOk())
                .andExpect(view().name("human-events"))
                .andExpect(model().attribute("activeType", "CUSTOM"))
                .andExpect(content().string(containsString("PhD Defense Anniversary")));

        // 4. POST /human-events/delete
        mockMvc.perform(post("/human-events/delete")
                        .with(csrf())
                        .param("id", event.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/human-events"));

        assertFalse(humanEventRepository.findById(event.getId()).isPresent());
    }

    @Test
    void testCalendarEventTimelineFilter() throws Exception {
        CalendarEvent event = new CalendarEvent();
        event.setTitle("ACM Keynote Presentation");
        event.setDescription("Keynote on High-Performance Concurrent Architectures");
        event.setStart(LocalDateTime.now().plusDays(2));
        event.setEnd(LocalDateTime.now().plusDays(2).plusHours(2));
        calendarEventRepository.saveAndFlush(event);

        mockMvc.perform(get("/event")
                        .param("fromDate", LocalDateTime.now().minusDays(5).toString())
                        .param("toDate", LocalDateTime.now().plusDays(10).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("event"))
                .andExpect(model().attributeExists("events", "fromDate", "toDate", "minDate", "maxDate"))
                .andExpect(content().string(containsString("ACM Keynote Presentation")))
                .andExpect(content().string(containsString("Keynote on High-Performance Concurrent Architectures")));
    }

    @Test
    void testUpdateHumanFlow() throws Exception {
        Human human = new Human();
        human.setFirstname("Marie");
        human.setLastname("Curie");
        human.setCategory(HumanCategory.FRIEND);
        human.setBirthday(java.sql.Date.valueOf("1867-11-07"));
        human = humanRepository.saveAndFlush(human);
        Long humanId = human.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/humans")
                        .with(csrf())
                        .param("id", String.valueOf(humanId))
                        .param("firstname", "Marie")
                        .param("lastname", "Skłodowska-Curie")
                        .param("category", "FAMILY")
                        .param("birthday", "1867-11-07"))
                .andExpect(status().isOk())
                .andExpect(view().name("humans"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, humanRepository.count(), "Editing human must not duplicate row");
        Human updated = humanRepository.findById(humanId).orElseThrow();
        assertEquals("Skłodowska-Curie", updated.getLastname());
        assertEquals(HumanCategory.FAMILY, updated.getCategory());
    }

    @Test
    void testUpdateHumanEventFlow() throws Exception {
        Human human = new Human();
        human.setFirstname("Pierre");
        human.setLastname("Curie");
        human.setCategory(HumanCategory.FAMILY);
        human.setBirthday(java.sql.Date.valueOf("1859-05-15"));
        human = humanRepository.saveAndFlush(human);

        HumanEvent event = new HumanEvent();
        event.setHuman(human);
        event.setTitle("Nobel Prize Banquet");
        event.setEventType(EventType.CUSTOM);
        event.setEventDate(LocalDate.of(2026, 12, 10));
        event = humanEventRepository.saveAndFlush(event);
        Long eventId = event.getId();

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/human-events")
                        .with(csrf())
                        .param("id", eventId.toString())
                        .param("humanId", String.valueOf(human.getId()))
                        .param("title", "Nobel Prize Gala Ceremony")
                        .param("eventType", "CUSTOM")
                        .param("eventDate", "2026-12-11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/human-events"));

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, humanEventRepository.count(), "Editing human event must not duplicate row");
        HumanEvent updated = humanEventRepository.findById(eventId).orElseThrow();
        assertEquals("Nobel Prize Gala Ceremony", updated.getTitle());
        assertEquals(LocalDate.of(2026, 12, 11), updated.getEventDate());
    }

    @Test
    void testAddLecturerViaModalFormWithEmptyIdParam() throws Exception {
        // Modal form on /humans sends <input type="hidden" id="modalHumanId" name="id" value="">
        // Verify that id="" binds cleanly to null and persists a new Human with category LECTURER and syncs birthday event.
        mockMvc.perform(post("/humans")
                        .with(csrf())
                        .param("id", "")
                        .param("category", "LECTURER")
                        .param("firstname", "Andrew")
                        .param("lastname", "Ng")
                        .param("birthday", "1976-04-18"))
                .andExpect(status().isOk())
                .andExpect(view().name("humans"))
                .andExpect(content().string(containsString("Andrew")))
                .andExpect(content().string(containsString("Ng")));

        List<Human> humans = humanRepository.findAll();
        Human savedLecturer = humans.stream()
                .filter(h -> "Andrew".equals(h.getFirstname()) && "Ng".equals(h.getLastname()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Saved lecturer Andrew Ng not found"));
        assertEquals(HumanCategory.LECTURER, savedLecturer.getCategory());
        assertNotNull(savedLecturer.getId());
        assertTrue(savedLecturer.getId() > 0);

        List<HumanEvent> events = humanEventRepository.findByHumanIdOrderByDaysUntilNextAsc(savedLecturer.getId());
        assertFalse(events.isEmpty(), "Birthday event must be automatically created for lecturer");
        assertEquals(EventType.BIRTHDAY, events.getFirst().getEventType());
    }
}

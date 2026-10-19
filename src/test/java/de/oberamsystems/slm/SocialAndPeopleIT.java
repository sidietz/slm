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
}

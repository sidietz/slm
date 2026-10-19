package de.oberamsystems.slm;

import de.oberamsystems.slm.model.Session;
import de.oberamsystems.slm.model.SessionRepository;
import de.oberamsystems.slm.model.SleepSession;
import de.oberamsystems.slm.model.SleepSessionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
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
public class SleepSessionUIIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SleepSessionRepository sleepSessionRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testViewSessionsHubRendersSleepModalForm() throws Exception {
        // GET /sessions should render the Sessions Hub view with the sleep modal form
        mockMvc.perform(get("/sessions").param("type", "SLEEP"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attributeExists("sleepSession"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(content().string(containsString("id=\"form-sleep\"")))
                .andExpect(content().string(containsString("action=\"/sessions/sleep\"")))
                .andExpect(content().string(containsString("name=\"deepSleepMinutes\"")))
                .andExpect(content().string(containsString("name=\"remSleepMinutes\"")))
                .andExpect(content().string(containsString("name=\"awakeCount\"")))
                .andExpect(content().string(containsString("name=\"isNap\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void testAddNightSleepSessionOverUI() throws Exception {
        String comment = "Deep restorative night sleep with dream recall";

        // 1. Submit sleep session via UI POST /sessions/sleep with CSRF
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-18T22:30")
                        .param("end", "2026-10-19T06:45")
                        .param("isNap", "false")
                        .param("deepSleepMinutes", "105")
                        .param("remSleepMinutes", "90")
                        .param("awakeCount", "1")
                        .param("breakMinutes", "15")
                        .param("focusRating", "5")
                        .param("comment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        // Flush and refresh JPA state to read database-generated columns
        entityManager.flush();

        // 2. Verify database persistence in PostgreSQL 18
        List<SleepSession> sleepSessions = sleepSessionRepository.findAll();
        Optional<SleepSession> createdOpt = sleepSessions.stream()
                .filter(s -> comment.equals(s.getComment()))
                .findFirst();

        assertTrue(createdOpt.isPresent(), "Sleep session must be persisted in database");
        SleepSession saved = createdOpt.get();
        entityManager.refresh(saved);

        assertNotNull(saved.getId(), "SleepSession must have an ID");
        assertInstanceOf(UUID.class, saved.getId());
        assertEquals(7, saved.getId().version(), "SleepSession ID must be a native UUIDv7");

        assertEquals(LocalDateTime.of(2026, 10, 18, 22, 30), saved.getStart());
        assertEquals(LocalDateTime.of(2026, 10, 19, 6, 45), saved.getEnd());
        assertFalse(saved.getIsNap(), "Must be night sleep");
        assertEquals(1, saved.getAwakeCount());
        assertEquals(5, saved.getFocusRating());
        assertEquals(Duration.ofMinutes(15), saved.getBreakDuration());
        assertEquals(Duration.ofMinutes(105), saved.getDeepSleepDuration());
        assertEquals(Duration.ofMinutes(90), saved.getRemSleepDuration());

        // Verify PostgreSQL GENERATED columns
        assertNotNull(saved.getDuration());
        assertEquals(Duration.ofMinutes(495), saved.getDuration()); // 8h 15m gross
        assertNotNull(saved.getNetDuration());
        assertEquals(Duration.ofMinutes(480), saved.getNetDuration()); // 8h 00m net

        // 3. Verify polymorphic retrieval via root SessionRepository
        Optional<Session> polySession = sessionRepository.findById(saved.getId());
        assertTrue(polySession.isPresent());
        assertInstanceOf(SleepSession.class, polySession.get());

        // 4. Verify rendering on UI page GET /sessions?type=SLEEP
        mockMvc.perform(get("/sessions").param("type", "SLEEP"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(content().string(containsString("Night Sleep")))
                .andExpect(content().string(containsString("1 wake-ups")))
                .andExpect(content().string(containsString("Deep:")))
                .andExpect(content().string(containsString("REM:")))
                .andExpect(content().string(containsString(comment)))
                .andExpect(content().string(containsString("/sessions/delete/" + saved.getId())));
    }

    @Test
    void testAddPowerNapSessionOverUI() throws Exception {
        String napComment = "Energizing 30-minute afternoon power nap";

        // 1. Submit power nap via UI POST /sessions/sleep with CSRF
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-19T13:30")
                        .param("end", "2026-10-19T14:00")
                        .param("isNap", "true")
                        .param("deepSleepMinutes", "10")
                        .param("remSleepMinutes", "0")
                        .param("awakeCount", "0")
                        .param("breakMinutes", "0")
                        .param("focusRating", "4")
                        .param("comment", napComment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        // 2. Verify database persistence
        List<SleepSession> naps = sleepSessionRepository.findAll();
        Optional<SleepSession> napOpt = naps.stream()
                .filter(s -> napComment.equals(s.getComment()))
                .findFirst();

        assertTrue(napOpt.isPresent(), "Power nap must be persisted in database");
        SleepSession savedNap = napOpt.get();
        entityManager.refresh(savedNap);

        assertTrue(savedNap.getIsNap(), "Must be flagged as power nap");
        assertEquals(0, savedNap.getAwakeCount());
        assertEquals(Duration.ofMinutes(30), savedNap.getDuration());
        assertEquals(Duration.ofMinutes(10), savedNap.getDeepSleepDuration());

        // 3. Verify rendering in UI table
        mockMvc.perform(get("/sessions").param("type", "SLEEP"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Power Nap")))
                .andExpect(content().string(containsString(napComment)));
    }

    @Test
    void testDeleteSleepSessionOverUI() throws Exception {
        // 1. Create and persist a sleep session
        SleepSession session = new SleepSession();
        session.setStart(LocalDateTime.of(2026, 10, 17, 23, 0));
        session.setEnd(LocalDateTime.of(2026, 10, 18, 7, 0));
        session.setBreakDuration(Duration.ZERO);
        session.setIsNap(false);
        session.setComment("To be deleted via UI");
        session = sleepSessionRepository.saveAndFlush(session);
        UUID sessionId = session.getId();
        assertNotNull(sessionId);

        // 2. Perform delete via UI POST /sessions/delete/{id}
        mockMvc.perform(post("/sessions/delete/" + sessionId)
                        .with(csrf())
                        .param("returnType", "SLEEP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        // 3. Verify deletion from both sleep_session and polymorphic session tables
        assertFalse(sleepSessionRepository.findById(sessionId).isPresent(), "SleepSession must be removed from sleep_session table");
        assertFalse(sessionRepository.findById(sessionId).isPresent(), "Session must be removed from session table");
    }

    @Test
    void testCsrfProtectionEnforcedOnSleepSubmission() throws Exception {
        // Without CSRF token, POST to /sessions/sleep should be rejected with 403 Forbidden
        mockMvc.perform(post("/sessions/sleep")
                        .param("start", "2026-10-18T23:00")
                        .param("end", "2026-10-19T07:00")
                        .param("isNap", "false"))
                .andExpect(status().isForbidden());
    }
}

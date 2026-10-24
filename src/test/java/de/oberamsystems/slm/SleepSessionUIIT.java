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
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import de.oberamsystems.slm.config.TimeZoneConfig;
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

    private OffsetDateTime toOffset(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, TimeZoneConfig.getZoneId()).toOffsetDateTime();
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testViewSessionsHubRendersSleepModalForm() throws Exception {
        mockMvc.perform(get("/sessions").param("type", "SLEEP"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attributeExists("sleepSession"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(content().string(containsString("id=\"form-sleep\"")))
                .andExpect(content().string(containsString("action=\"/sessions/sleep\"")))
                .andExpect(content().string(containsString("name=\"deepSleepMinutes\"")))
                .andExpect(content().string(containsString("name=\"remSleepMinutes\"")))
                .andExpect(content().string(containsString("name=\"lightSleepMinutes\"")))
                .andExpect(content().string(containsString("name=\"awakeCount\"")))
                .andExpect(content().string(containsString("name=\"isNap\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void testAddSleepSessionWithAllStagesEmptyDefaultsToLightSleep() throws Exception {
        String comment = "Untracked sleep defaulting entirely to light sleep";

        // 8h bedtime (480m) minus 15m awake disruptions -> 465m (07h 45m) net sleep
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-18T22:30")
                        .param("end", "2026-10-19T06:30")
                        .param("isNap", "false")
                        .param("breakMinutes", "15")
                        .param("focusRating", "4")
                        .param("comment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        List<SleepSession> list = sleepSessionRepository.findAll();
        Optional<SleepSession> opt = list.stream().filter(s -> comment.equals(s.getComment())).findFirst();
        assertTrue(opt.isPresent(), "Session must be persisted");
        SleepSession saved = opt.get();
        entityManager.refresh(saved);

        assertNotNull(saved.getId());
        assertEquals(7, saved.getId().version(), "Native UUIDv7 required");
        assertEquals(Duration.ofMinutes(15), saved.getBreakDuration());
        assertEquals(Duration.ofMinutes(465), saved.getLightSleepDuration(), "All net sleep must default to light sleep");
        assertEquals(Duration.ZERO, saved.getDeepSleepDuration());
        assertEquals(Duration.ZERO, saved.getRemSleepDuration());

        // Verify UI rendering shows Light sleep
        mockMvc.perform(get("/sessions").param("type", "SLEEP"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Night Sleep")))
                .andExpect(content().string(containsString("Light:")))
                .andExpect(content().string(containsString(comment)));
    }

    @Test
    void testAddSleepSessionWithMissingRemSleepAutoCalculated() throws Exception {
        String comment = "Auto-calculating missing REM sleep";

        // 8h bedtime (480m), 0 break. Deep 90m + Light 290m = 380m -> REM must be auto-calculated as 100m
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-18T23:00")
                        .param("end", "2026-10-19T07:00")
                        .param("isNap", "false")
                        .param("deepSleepMinutes", "90")
                        .param("lightSleepMinutes", "290")
                        .param("breakMinutes", "0")
                        .param("focusRating", "5")
                        .param("comment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        List<SleepSession> list = sleepSessionRepository.findAll();
        Optional<SleepSession> opt = list.stream().filter(s -> comment.equals(s.getComment())).findFirst();
        assertTrue(opt.isPresent());
        SleepSession saved = opt.get();
        entityManager.refresh(saved);

        assertEquals(Duration.ofMinutes(90), saved.getDeepSleepDuration());
        assertEquals(Duration.ofMinutes(290), saved.getLightSleepDuration());
        assertEquals(Duration.ofMinutes(100), saved.getRemSleepDuration(), "REM sleep must be calculated as 480 - 90 - 290 = 100m");

        mockMvc.perform(get("/sessions").param("type", "SLEEP"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("REM:")))
                .andExpect(content().string(containsString("Light:")))
                .andExpect(content().string(containsString("Deep:")));
    }

    @Test
    void testAddSleepSessionWithMissingDeepSleepAutoCalculated() throws Exception {
        String comment = "Auto-calculating missing Deep sleep";

        // 8h bedtime (480m), 0 break. REM 100m + Light 300m = 400m -> Deep must be auto-calculated as 80m
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-18T23:00")
                        .param("end", "2026-10-19T07:00")
                        .param("isNap", "false")
                        .param("remSleepMinutes", "100")
                        .param("lightSleepMinutes", "300")
                        .param("breakMinutes", "0")
                        .param("focusRating", "4")
                        .param("comment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        List<SleepSession> list = sleepSessionRepository.findAll();
        Optional<SleepSession> opt = list.stream().filter(s -> comment.equals(s.getComment())).findFirst();
        assertTrue(opt.isPresent());
        SleepSession saved = opt.get();
        entityManager.refresh(saved);

        assertEquals(Duration.ofMinutes(80), saved.getDeepSleepDuration(), "Deep sleep must be calculated as 480 - 100 - 300 = 80m");
        assertEquals(Duration.ofMinutes(100), saved.getRemSleepDuration());
        assertEquals(Duration.ofMinutes(300), saved.getLightSleepDuration());
    }

    @Test
    void testAddSleepSessionWithMissingLightSleepAutoCalculated() throws Exception {
        String comment = "Auto-calculating missing Light sleep";

        // 8h 15m bedtime (495m), 15m break -> 480m net sleep. Deep 105m + REM 90m = 195m -> Light must be 285m
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

        entityManager.flush();

        List<SleepSession> list = sleepSessionRepository.findAll();
        Optional<SleepSession> opt = list.stream().filter(s -> comment.equals(s.getComment())).findFirst();
        assertTrue(opt.isPresent());
        SleepSession saved = opt.get();
        entityManager.refresh(saved);

        assertEquals(Duration.ofMinutes(105), saved.getDeepSleepDuration());
        assertEquals(Duration.ofMinutes(90), saved.getRemSleepDuration());
        assertEquals(Duration.ofMinutes(285), saved.getLightSleepDuration(), "Light sleep must be calculated as 480 - 105 - 90 = 285m");
    }

    @Test
    void testAddNightSleepSessionWithAllStagesSpecifiedAndBalanced() throws Exception {
        String comment = "Fully specified balanced sleep stages";

        // 8h bedtime (480m), 20m break -> 460m net sleep. Deep 100m + REM 90m + Light 270m = 460m
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-18T22:00")
                        .param("end", "2026-10-19T06:00")
                        .param("isNap", "false")
                        .param("deepSleepMinutes", "100")
                        .param("remSleepMinutes", "90")
                        .param("lightSleepMinutes", "270")
                        .param("breakMinutes", "20")
                        .param("focusRating", "5")
                        .param("comment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        List<SleepSession> list = sleepSessionRepository.findAll();
        Optional<SleepSession> opt = list.stream().filter(s -> comment.equals(s.getComment())).findFirst();
        assertTrue(opt.isPresent());
        SleepSession saved = opt.get();
        entityManager.refresh(saved);

        assertEquals(Duration.ofMinutes(100), saved.getDeepSleepDuration());
        assertEquals(Duration.ofMinutes(90), saved.getRemSleepDuration());
        assertEquals(Duration.ofMinutes(270), saved.getLightSleepDuration());
        assertEquals(Duration.ofMinutes(20), saved.getBreakDuration());
        assertEquals(Duration.ofMinutes(480), saved.getDuration());
    }

    @Test
    void testRejectInconsistentSleepStageDurations() throws Exception {
        // 8h bedtime (480m), 0 break. Deep 120m + REM 120m + Light 120m = 360m != 480m -> MUST REJECT
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-18T22:00")
                        .param("end", "2026-10-19T06:00")
                        .param("isNap", "false")
                        .param("deepSleepMinutes", "120")
                        .param("remSleepMinutes", "120")
                        .param("lightSleepMinutes", "120")
                        .param("breakMinutes", "0")
                        .param("comment", "Inconsistent stages"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"))
                .andExpect(flash().attributeExists("errorMessage"));

        entityManager.flush();

        // Verify session was NOT saved in database
        List<SleepSession> list = sleepSessionRepository.findAll();
        assertFalse(list.stream().anyMatch(s -> "Inconsistent stages".equals(s.getComment())),
                "Inconsistent sleep session must not be persisted");
    }

    @Test
    void testRejectSingleStageWithTwoStagesMissing() throws Exception {
        // 8h bedtime (480m). Only deep sleep is provided (100m) while REM and Light are missing -> Ambiguous
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-18T22:00")
                        .param("end", "2026-10-19T06:00")
                        .param("isNap", "false")
                        .param("deepSleepMinutes", "100")
                        .param("breakMinutes", "0")
                        .param("comment", "Only deep specified"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"))
                .andExpect(flash().attributeExists("errorMessage"));

        entityManager.flush();

        List<SleepSession> list = sleepSessionRepository.findAll();
        assertFalse(list.stream().anyMatch(s -> "Only deep specified".equals(s.getComment())),
                "Ambiguous single-stage sleep session must not be persisted");
    }

    @Test
    void testDatabaseTriggerRejectsDirectInconsistentInsert() {
        // Attempt direct SQL insert bypassing application validation with mismatched sleep stages
        UUID sessionId = UUID.randomUUID();
        OffsetDateTime start = toOffset(2026, 10, 18, 22, 0);
        OffsetDateTime end = toOffset(2026, 10, 19, 6, 0); // 8 hours

        entityManager.createNativeQuery(
                "INSERT INTO session (id, session_type, status, starttime, endtime, break_duration) " +
                "VALUES (:id, 'SLEEP', 'COMPLETED', :start, :end, '0 seconds'::interval)")
                .setParameter("id", sessionId)
                .setParameter("start", start)
                .setParameter("end", end)
                .executeUpdate();

        // Stages sum = 2h + 2h = 4h != 8h session duration -> trigger must abort insert
        Exception exception = assertThrows(Exception.class, () -> {
            entityManager.createNativeQuery(
                    "INSERT INTO sleep_session (id, is_nap, deep_sleep_duration, rem_sleep_duration, light_sleep_duration, awake_count) " +
                    "VALUES (:id, false, '2 hours'::interval, '2 hours'::interval, '0 hours'::interval, 0)")
                    .setParameter("id", sessionId)
                    .executeUpdate();
            entityManager.flush();
        });

        assertTrue(exception.getMessage().contains("Sleep stages sum") || exception.getCause().getMessage().contains("Sleep stages sum"),
                "Database trigger trg_check_sleep_session_duration must reject inconsistent stage sum");
    }

    @Test
    void testAddPowerNapSessionOverUI() throws Exception {
        String napComment = "Energizing 30-minute afternoon power nap";

        // 30 min nap, 10 min deep -> 20 min light auto-calculated
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

        List<SleepSession> naps = sleepSessionRepository.findAll();
        Optional<SleepSession> napOpt = naps.stream().filter(s -> napComment.equals(s.getComment())).findFirst();
        assertTrue(napOpt.isPresent());
        SleepSession savedNap = napOpt.get();
        entityManager.refresh(savedNap);

        assertTrue(savedNap.getIsNap());
        assertEquals(Duration.ofMinutes(30), savedNap.getDuration());
        assertEquals(Duration.ofMinutes(10), savedNap.getDeepSleepDuration());
        assertEquals(Duration.ofMinutes(20), savedNap.getLightSleepDuration());
    }

    @Test
    void testDeleteSleepSessionOverUI() throws Exception {
        SleepSession session = new SleepSession();
        session.setStart(toOffset(2026, 10, 17, 23, 0));
        session.setEnd(toOffset(2026, 10, 18, 7, 0)); // 8h
        session.setBreakDuration(Duration.ZERO);
        session.setDeepSleepDuration(Duration.ZERO);
        session.setRemSleepDuration(Duration.ZERO);
        session.setLightSleepDuration(Duration.ofHours(8));
        session.setIsNap(false);
        session.setComment("To be deleted via UI");
        session = sleepSessionRepository.saveAndFlush(session);
        UUID sessionId = session.getId();
        assertNotNull(sessionId);

        mockMvc.perform(post("/sessions/delete/" + sessionId)
                        .with(csrf())
                        .param("returnType", "SLEEP"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        assertFalse(sleepSessionRepository.findById(sessionId).isPresent());
        assertFalse(sessionRepository.findById(sessionId).isPresent());
    }

    @Test
    void testCsrfProtectionEnforcedOnSleepSubmission() throws Exception {
        mockMvc.perform(post("/sessions/sleep")
                        .param("start", "2026-10-18T23:00")
                        .param("end", "2026-10-19T07:00")
                        .param("isNap", "false"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCompleteOngoingSleepSessionWithAllStagesEmptyDefaultsToLightSleep() throws Exception {
        String comment = "Ongoing sleep completed with light sleep default";

        // 1. Start ongoing sleep session (bedtime known, wake-up unknown)
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("start", "2026-10-23T23:00")
                        .param("end", "")
                        .param("isNap", "false")
                        .param("comment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        SleepSession ongoing = sleepSessionRepository.findAll().stream()
                .filter(s -> comment.equals(s.getComment()))
                .findFirst().orElseThrow();
        assertEquals("IN_PROGRESS", ongoing.getStatus());
        assertNull(ongoing.getEnd());
        UUID sessionId = ongoing.getId();

        // 2. Wake up and complete session via POST /sessions/sleep with id and 7h 14m duration (07:14:00)
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("id", sessionId.toString())
                        .param("start", "2026-10-23T23:00")
                        .param("end", "2026-10-24T06:14")
                        .param("breakMinutes", "0")
                        .param("isNap", "false")
                        .param("focusRating", "4")
                        .param("comment", comment))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        SleepSession completed = sleepSessionRepository.findById(sessionId).orElseThrow();
        entityManager.refresh(completed);

        assertEquals("COMPLETED", completed.getStatus());
        assertTrue(toOffset(2026, 10, 24, 6, 14).isEqual(completed.getEnd()));
        assertEquals(Duration.ofMinutes(434), completed.getDuration(), "7h 14m gross duration");
        assertEquals(Duration.ofMinutes(434), completed.getLightSleepDuration(), "All net sleep must default to light sleep");
        assertEquals(Duration.ZERO, completed.getDeepSleepDuration());
        assertEquals(Duration.ZERO, completed.getRemSleepDuration());
    }

    @Test
    void testEditExistingCompletedSleepSessionDurationWithStagesEmpty() throws Exception {
        SleepSession session = new SleepSession();
        session.setStart(toOffset(2026, 10, 22, 23, 0));
        session.setEnd(toOffset(2026, 10, 23, 7, 0)); // 8h = 480m
        session.setBreakDuration(Duration.ZERO);
        session.setDeepSleepDuration(Duration.ZERO);
        session.setRemSleepDuration(Duration.ZERO);
        session.setLightSleepDuration(Duration.ofHours(8));
        session.setIsNap(false);
        session.setComment("Initial completed sleep");
        session = sleepSessionRepository.saveAndFlush(session);
        UUID sessionId = session.getId();

        // Edit end time to 07:30 (8h 30m = 510m) with sleep stages omitted
        mockMvc.perform(post("/sessions/sleep")
                        .with(csrf())
                        .param("id", sessionId.toString())
                        .param("start", "2026-10-22T23:00")
                        .param("end", "2026-10-23T07:30")
                        .param("breakMinutes", "0")
                        .param("isNap", "false")
                        .param("comment", "Edited completed sleep"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));

        entityManager.flush();

        SleepSession updated = sleepSessionRepository.findById(sessionId).orElseThrow();
        entityManager.refresh(updated);

        assertEquals(Duration.ofMinutes(510), updated.getDuration());
        assertEquals(Duration.ofMinutes(510), updated.getLightSleepDuration(), "Updated duration must default to light sleep");
    }
}

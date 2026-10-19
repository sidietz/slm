package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class SleepTimeIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SleepTimeRepository sleepTimeRepository;

    @Autowired
    private SleepSessionRepository sleepSessionRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSleepTimeIntervalAndJsonSerializationEndToEnd() throws Exception {
        // 1. Create and persist sleep record
        SleepTime st = new SleepTime();
        st.setGotobed(LocalDateTime.of(2026, 10, 18, 23, 0));
        st.setGetup(LocalDateTime.of(2026, 10, 19, 7, 30));
        st = sleepTimeRepository.saveAndFlush(st);
        entityManager.refresh(st);

        // Verify generated interval in PostgreSQL 18
        assertNotNull(st.getSleeptime(), "PostgreSQL generated interval sleeptime should be calculated");

        // 2. Test GET /api/sleeptimes JSON serialization with tools.jackson 3
        mockMvc.perform(get("/api/sleeptimes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[?(@.id == " + st.getId() + ")].gotobed", notNullValue()))
                .andExpect(jsonPath("$[?(@.id == " + st.getId() + ")].getup", notNullValue()))
                .andExpect(jsonPath("$[?(@.id == " + st.getId() + ")].sleeptime", notNullValue()));

        // 3. Test GET /sleeptime-bb template rendering (no Thymeleaf parse exception or stream truncation)
        mockMvc.perform(get("/sleeptime-bb"))
                .andExpect(status().isOk())
                .andExpect(view().name("sleeptime-bb"))
                .andExpect(content().string(containsString("<!DOCTYPE html>")))
                .andExpect(content().string(containsString("<div id=\"chart\"")))
                .andExpect(content().string(containsString("</html>")));

        // 4. Test GET /api/moods and GET /mood-bb
        mockMvc.perform(get("/api/moods")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/mood-bb"))
                .andExpect(status().isOk())
                .andExpect(view().name("mood-bb"))
                .andExpect(content().string(containsString("<!DOCTYPE html>")))
                .andExpect(content().string(containsString("<div id=\"chart\"")))
                .andExpect(content().string(containsString("</html>")));
    }

    @Test
    public void testSleepSessionPolymorphicInheritanceAndEndpoints() throws Exception {
        // 1. Create and persist SleepSession (joined inheritance to session and sleep_session tables)
        SleepSession ss = new SleepSession();
        ss.setStart(LocalDateTime.of(2026, 10, 18, 22, 30));
        ss.setEnd(LocalDateTime.of(2026, 10, 19, 7, 0));
        ss.setBreakDuration(Duration.ofMinutes(15));
        ss.setDeepSleepDuration(Duration.ofMinutes(95));
        ss.setRemSleepDuration(Duration.ofMinutes(85));
        ss.setLightSleepDuration(Duration.ofMinutes(315));
        ss.setAwakeCount(2);
        ss.setIsNap(false);
        ss.setFocusRating(4);
        ss = sleepSessionRepository.saveAndFlush(ss);
        entityManager.refresh(ss);

        assertNotNull(ss.getId(), "SleepSession ID should be auto-assigned");
        assertNotNull(ss.getDuration(), "Generated column duration should be computed");
        assertNotNull(ss.getNetDuration(), "Generated column net_duration should be computed");
        assertEquals(ss.getStart(), ss.getGotobed());
        assertEquals(ss.getEnd(), ss.getGetup());
        assertEquals(ss.getDuration(), ss.getSleeptime());

        // 2. Polymorphic query via generic SessionRepository
        Optional<Session> optSession = sessionRepository.findById(ss.getId());
        assertTrue(optSession.isPresent(), "Polymorphic Session lookup should succeed");
        assertTrue(optSession.get() instanceof SleepSession, "Session instance should be polymorphically SleepSession");
        SleepSession poly = (SleepSession) optSession.get();
        assertEquals(2, poly.getAwakeCount());
        assertFalse(poly.getIsNap());

        // 3. Test GET /sessions?type=SLEEP rendering
        mockMvc.perform(get("/sessions").param("type", "SLEEP"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attributeExists("sessions"))
                .andExpect(model().attribute("kpiDurationTitle", "Rest & Recovery Time"))
                .andExpect(model().attributeExists("productivityDuration"))
                .andExpect(model().attributeExists("sportDuration"))
                .andExpect(model().attributeExists("wellnessDuration"))
                .andExpect(model().attributeExists("leisureDuration"))
                .andExpect(content().string(containsString("badge-sleep")))
                .andExpect(content().string(containsString("Night Sleep")));

        // 4. Test GET /sessions?type=ALL - verify Sleep is excluded from Net Focused Time
        mockMvc.perform(get("/sessions").param("type", "ALL"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("kpiDurationTitle", "Net Focused Time"))
                .andExpect(model().attribute("totalNetDuration", "0m"))
                .andExpect(model().attribute("wellnessDuration", not("0m")));

        // 5. Test GET /sessions?type=WELLNESS (Four Pillars category)
        mockMvc.perform(get("/sessions").param("type", "WELLNESS"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("kpiDurationTitle", "Rest & Recovery Time"))
                .andExpect(model().attributeExists("sessions"));

        // 5. Test POST /sessions/sleep submission
        mockMvc.perform(post("/sessions/sleep")
                        .param("start", "2026-10-18T23:00")
                        .param("end", "2026-10-19T07:00")
                        .param("isNap", "false")
                        .param("deepSleepMinutes", "90")
                        .param("remSleepMinutes", "80")
                        .param("awakeCount", "1")
                        .param("breakMinutes", "10")
                        .param("focusRating", "4"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?type=SLEEP"));
    }
}

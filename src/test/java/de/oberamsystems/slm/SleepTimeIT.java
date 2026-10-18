package de.oberamsystems.slm;

import de.oberamsystems.slm.model.SleepTime;
import de.oberamsystems.slm.model.SleepTimeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class SleepTimeIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SleepTimeRepository sleepTimeRepository;

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
}

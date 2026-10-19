package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class WellnessAndHabitsIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private HabitEntryRepository habitEntryRepository;

    @Autowired
    private MoodRepository moodRepository;

    @Autowired
    private SleepTimeRepository sleepTimeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testHabitAndHabitEntryFlow() throws Exception {
        // 1. GET /habits
        mockMvc.perform(get("/habits"))
                .andExpect(status().isOk())
                .andExpect(view().name("habits"))
                .andExpect(model().attributeExists("habits", "habit"));

        // 2. POST /habits
        mockMvc.perform(post("/habits")
                        .with(csrf())
                        .param("name", "Daily Reading")
                        .param("description", "Read 30 pages every morning"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habits"));
        assertEquals(1, habitRepository.count());
        Habit habit = habitRepository.findAll().getFirst();

        // 3. GET /habit-entries
        mockMvc.perform(get("/habit-entries"))
                .andExpect(status().isOk())
                .andExpect(view().name("habit-entries"))
                .andExpect(model().attributeExists("habitentries", "habitentry", "habits"));

        // 4. POST /habit-entries
        mockMvc.perform(post("/habit-entries")
                        .with(csrf())
                        .param("habit", habit.getId().toString())
                        .param("lastDone", "2026-10-19T08:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habit-entries"));
        assertEquals(1, habitEntryRepository.count());

        // 5. GET /last-done-habit-entries with specific habit id
        mockMvc.perform(get("/last-done-habit-entries").param("id", habit.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("last-done-habit-entries"))
                .andExpect(model().attributeExists("habit", "habitId", "habits", "lastdonehabitentries", "habitentry"))
                .andExpect(content().string(containsString("Daily Reading")));

        // 6. POST /last-done-habit-entries
        mockMvc.perform(post("/last-done-habit-entries")
                        .with(csrf())
                        .param("id", habit.getId().toString())
                        .param("lastDone", "2026-10-19T08:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/last-done-habit-entries"));
        assertEquals(2, habitEntryRepository.count());
    }

    @Test
    void testMoodWebAndRestFlow() throws Exception {
        // 1. GET /moods
        mockMvc.perform(get("/moods"))
                .andExpect(status().isOk())
                .andExpect(view().name("moods"))
                .andExpect(model().attributeExists("moods", "mood"));

        // 2. POST /moods
        mockMvc.perform(post("/moods")
                        .with(csrf())
                        .param("date", "2026-10-19")
                        .param("happiness", "8")
                        .param("impetus", "7")
                        .param("stress", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/moods"));
        assertEquals(1, moodRepository.count());

        // 3. GET /mood-bb
        mockMvc.perform(get("/mood-bb"))
                .andExpect(status().isOk())
                .andExpect(view().name("mood-bb"))
                .andExpect(model().attributeExists("moods"));

        // 4. GET /api/moods (REST endpoint)
        mockMvc.perform(get("/api/moods")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].happiness", is(8)))
                .andExpect(jsonPath("$[0].impetus", is(7)))
                .andExpect(jsonPath("$[0].stress", is(3)));
    }

    @Test
    void testSleepTimeWebAndRestFlow() throws Exception {
        // 1. GET /sleep-times
        mockMvc.perform(get("/sleep-times"))
                .andExpect(status().isOk())
                .andExpect(view().name("sleep-times"))
                .andExpect(model().attributeExists("sleeptimes", "sleeptime"));

        // 2. POST /sleep-times
        mockMvc.perform(post("/sleep-times")
                        .with(csrf())
                        .param("gotobed", "2026-10-18T23:00")
                        .param("getup", "2026-10-19T07:15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sleep-times"));
        assertEquals(1, sleepTimeRepository.count());

        // 3. GET /sleeptime-bb
        mockMvc.perform(get("/sleeptime-bb"))
                .andExpect(status().isOk())
                .andExpect(view().name("sleeptime-bb"))
                .andExpect(model().attributeExists("sleeptimes"));

        // 4. GET /api/sleeptimes (REST endpoint)
        mockMvc.perform(get("/api/sleeptimes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));
    }
}

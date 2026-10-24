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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        // 1. GET /sleep-times (read-only view with deprecation banner and disabled form)
        mockMvc.perform(get("/sleep-times"))
                .andExpect(status().isOk())
                .andExpect(view().name("sleep-times"))
                .andExpect(model().attributeExists("sleeptimes", "sleeptime"));

        // 2. POST /sleep-times (blocked write attempt)
        mockMvc.perform(post("/sleep-times")
                        .with(csrf())
                        .param("gotobed", "2026-10-18T23:00")
                        .param("getup", "2026-10-19T07:15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sleep-times"))
                .andExpect(flash().attribute("errorMessage", "Logging legacy sleep times is disabled. Please log sleep sessions via the Sessions Hub."));
        assertEquals(0, sleepTimeRepository.count(), "Legacy writes must be rejected without inserting new records");

        // Seed historical record directly to verify read-only chart and REST query
        SleepTime historicalSt = new SleepTime();
        historicalSt.setGotobed(OffsetDateTime.of(2026, 10, 18, 23, 0, 0, 0, ZoneOffset.UTC));
        historicalSt.setGetup(OffsetDateTime.of(2026, 10, 19, 7, 15, 0, 0, ZoneOffset.UTC));
        sleepTimeRepository.saveAndFlush(historicalSt);

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

    @Test
    void testUpdateHabitFlowAndCompletionLogSafety() throws Exception {
        Habit habit = new Habit(null, "Drink Water");
        habit.setDescription("Drink 2L daily");
        habit = habitRepository.saveAndFlush(habit);
        Long habitId = habit.getId();

        mockMvc.perform(post("/habits")
                        .with(csrf())
                        .param("id", habitId.toString())
                        .param("name", "Hydration Goal")
                        .param("description", "Drink 3L of water daily"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habits"));

        assertEquals(1, habitRepository.count());
        entityManager.clear();
        Habit updatedHabit = habitRepository.findById(habitId).orElseThrow();
        assertEquals("Hydration Goal", updatedHabit.getName());
        assertEquals("Drink 3L of water daily", updatedHabit.getDescription());

        HabitEntry entry = new HabitEntry(null, updatedHabit, OffsetDateTime.of(2026, 10, 20, 8, 0, 0, 0, ZoneOffset.UTC));
        entry = habitEntryRepository.saveAndFlush(entry);
        Long entryId = entry.getId();

        mockMvc.perform(post("/habit-entries")
                        .with(csrf())
                        .param("id", entryId.toString())
                        .param("habit", updatedHabit.getId().toString())
                        .param("lastDone", "2026-10-20T08:45"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habit-entries"));

        assertEquals(1, habitEntryRepository.count());
        entityManager.clear();
        HabitEntry updatedEntry = habitEntryRepository.findById(entryId).orElseThrow();
        assertTrue(java.time.LocalDate.of(2026, 10, 20).atTime(8, 45).atZone(de.oberamsystems.slm.config.TimeZoneConfig.getZoneId()).toOffsetDateTime().isEqual(updatedEntry.getLastDone()));

        // Verify completion log endpoint safety: /last-done-habit-entries must ALWAYS append a new row
        mockMvc.perform(post("/last-done-habit-entries")
                        .with(csrf())
                        .param("id", updatedHabit.getId().toString())
                        .param("lastDone", "2026-10-20T12:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/last-done-habit-entries"));

        assertEquals(2, habitEntryRepository.count(), "Quick log via last-done-habit-entries must append a new entry");
    }

    @Test
    void testUpdateMoodFlow() throws Exception {
        Mood mood = new Mood(null, LocalDate.of(2026, 10, 20), 6, 5, 4);
        mood = moodRepository.saveAndFlush(mood);
        Long moodId = mood.getId();

        mockMvc.perform(post("/moods")
                        .with(csrf())
                        .param("id", moodId.toString())
                        .param("date", "2026-10-20")
                        .param("happiness", "9")
                        .param("impetus", "8")
                        .param("stress", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/moods"));

        assertEquals(1, moodRepository.count());
        entityManager.clear();
        Mood updatedMood = moodRepository.findById(moodId).orElseThrow();
        assertEquals(9, updatedMood.getHappiness());
        assertEquals(8, updatedMood.getImpetus());
        assertEquals(2, updatedMood.getStress());
    }

    @Test
    void testUpdateSleepTimeFlow() throws Exception {
        SleepTime st = new SleepTime();
        st.setGotobed(OffsetDateTime.of(2026, 10, 19, 22, 30, 0, 0, ZoneOffset.UTC));
        st.setGetup(OffsetDateTime.of(2026, 10, 20, 6, 30, 0, 0, ZoneOffset.UTC));
        st = sleepTimeRepository.saveAndFlush(st);
        long stId = st.getId();

        mockMvc.perform(post("/sleep-times")
                        .with(csrf())
                        .param("id", String.valueOf(stId))
                        .param("gotobed", "2026-10-19T23:00")
                        .param("getup", "2026-10-20T07:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sleep-times"))
                .andExpect(flash().attribute("errorMessage", "Logging legacy sleep times is disabled. Please log sleep sessions via the Sessions Hub."));

        assertEquals(1, sleepTimeRepository.count());
        entityManager.clear();
        SleepTime unchangedSt = sleepTimeRepository.findById(stId).orElseThrow();
        assertTrue(OffsetDateTime.of(2026, 10, 19, 22, 30, 0, 0, ZoneOffset.UTC).isEqual(unchangedSt.getGotobed()),
                "Legacy sleep time must remain unchanged when write operations are disabled");
    }

    @Test
    void testModalCreateWellnessAndHabitsWithEmptyIdParam() throws Exception {
        // 1. Habit via modal with empty id
        mockMvc.perform(post("/habits")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Hydration")
                        .param("description", "Drink 2.5L water daily"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habits"));
        assertEquals(1, habitRepository.count());
        Habit habit = habitRepository.findAll().getFirst();

        // 2. HabitEntry via modal with empty id and explicit habit
        mockMvc.perform(post("/habit-entries")
                        .with(csrf())
                        .param("id", "")
                        .param("habit", habit.getId().toString())
                        .param("lastDone", "2026-10-21T09:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habit-entries"));
        assertEquals(1, habitEntryRepository.count());

        // 3. HabitEntry via modal with empty id and omitted habit (tests validation rejection)
        mockMvc.perform(post("/habit-entries")
                        .with(csrf())
                        .param("id", "")
                        .param("lastDone", "2026-10-21T13:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/habit-entries"))
                .andExpect(flash().attribute("errorMessage", "Habit is required."));
        assertEquals(1, habitEntryRepository.count());

        // 4. Mood via modal with empty id
        mockMvc.perform(post("/moods")
                        .with(csrf())
                        .param("id", "")
                        .param("date", "2026-10-21")
                        .param("happiness", "8")
                        .param("impetus", "7")
                        .param("stress", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/moods"));
        assertEquals(1, moodRepository.count());

        // 5. SleepTime via modal with empty id (confirms write rejection)
        mockMvc.perform(post("/sleep-times")
                        .with(csrf())
                        .param("id", "")
                        .param("gotobed", "2026-10-20T23:30")
                        .param("getup", "2026-10-21T07:15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sleep-times"))
                .andExpect(flash().attribute("errorMessage", "Logging legacy sleep times is disabled. Please log sleep sessions via the Sessions Hub."));
        assertEquals(0, sleepTimeRepository.count(), "No SleepTime record should be created");
    }
}

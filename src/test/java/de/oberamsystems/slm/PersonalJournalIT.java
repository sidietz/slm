package de.oberamsystems.slm;

import de.oberamsystems.slm.model.DiaryEntry;
import de.oberamsystems.slm.model.DiaryEntryRepository;
import de.oberamsystems.slm.model.Gratitude;
import de.oberamsystems.slm.model.GratitudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class PersonalJournalIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DiaryEntryRepository diaryEntryRepository;

    @Autowired
    private GratitudeRepository gratitudeRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testDiaryEntryFlow() throws Exception {
        // 1. GET /diary
        mockMvc.perform(get("/diary"))
                .andExpect(status().isOk())
                .andExpect(view().name("diary"))
                .andExpect(model().attributeExists("diaryentries", "diaryentry"));

        // 2. POST /diary
        mockMvc.perform(post("/diary")
                        .with(csrf())
                        .param("date", "2026-10-19")
                        .param("description", "Reflected on system resilience and microservices decoupling."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/diary"));

        assertEquals(1, diaryEntryRepository.count());
        DiaryEntry entry = diaryEntryRepository.findAll().getFirst();
        assertEquals("Reflected on system resilience and microservices decoupling.", entry.getDescription());

        // 3. GET /diary with saved content
        mockMvc.perform(get("/diary"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reflected on system resilience")));
    }

    @Test
    void testGratitudeFlow() throws Exception {
        // 1. GET /gratitudes
        mockMvc.perform(get("/gratitudes"))
                .andExpect(status().isOk())
                .andExpect(view().name("gratitudes"))
                .andExpect(model().attributeExists("gratitudes", "gratitude"));

        // 2. POST /gratitudes
        mockMvc.perform(post("/gratitudes")
                        .with(csrf())
                        .param("date", "2026-10-19")
                        .param("description", "Grateful for crisp autumn air and quiet focus time."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gratitudes"));

        assertEquals(1, gratitudeRepository.count());
        Gratitude gratitude = gratitudeRepository.findAll().getFirst();
        assertEquals("Grateful for crisp autumn air and quiet focus time.", gratitude.getDescription());

        // 3. GET /gratitudes with saved content
        mockMvc.perform(get("/gratitudes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Grateful for crisp autumn air")));
    }
}

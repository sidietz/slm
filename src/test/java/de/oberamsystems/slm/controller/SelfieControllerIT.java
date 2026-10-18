package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.AbstractIntegrationTest;
import de.oberamsystems.slm.model.Selfie;
import de.oberamsystems.slm.model.SelfieRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SelfieControllerIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SelfieRepository selfieRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterEach
    public void cleanup() {
        File dir = new File(System.getProperty("user.dir") + "/selfies");
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    @Test
    public void testUploadJpgAndHeicEndToEnd() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 18);

        // 1. Upload valid JPG
        MockMultipartFile jpgFile = new MockMultipartFile(
                "file", "snapshot.jpg", "image/jpeg", new byte[]{1, 2, 3, 4});

        mockMvc.perform(multipart("/selfies")
                        .file(jpgFile)
                        .param("date", date.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/selfies?success=Selfie uploaded successfully."));

        Selfie saved = selfieRepository.findByDate(date);
        assertNotNull(saved);
        assertTrue(saved.getLocation().endsWith("2026-10-18.jpg"));
        assertTrue(new File(saved.getLocation()).exists());

        // 2. Upload valid HEIC (replaces format cleanly)
        MockMultipartFile heicFile = new MockMultipartFile(
                "file", "snapshot.heic", "image/heic", new byte[]{5, 6, 7, 8});

        mockMvc.perform(multipart("/selfies")
                        .file(heicFile)
                        .param("date", date.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/selfies?success=Selfie uploaded successfully."));

        Selfie updated = selfieRepository.findByDate(date);
        assertNotNull(updated);
        assertTrue(updated.getLocation().endsWith("2026-10-18.heic"));
        assertTrue(new File(updated.getLocation()).exists());
        assertFalse(new File(saved.getLocation()).exists(), "Previous JPG should be deleted on format replacement");
    }

    @Test
    public void testPathTraversalRejectionEndToEnd() throws Exception {
        LocalDate date = LocalDate.of(2026, 10, 19);

        MockMultipartFile traversalFile = new MockMultipartFile(
                "file", "../../evil.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/selfies")
                        .file(traversalFile)
                        .param("date", date.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/selfies?error=Security violation: Path traversal or invalid characters detected."));

        assertNull(selfieRepository.findByDate(date), "No database record should be saved on traversal rejection");
    }
}

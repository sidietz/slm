package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.Selfie;
import de.oberamsystems.slm.model.SelfieRepository;
import de.oberamsystems.slm.service.SelfieStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SelfieControllerTest {

    private SelfieController controller;
    private SelfieRepository repo;
    private SelfieStorageService storageService;
    private File selfiesDir;

    @BeforeEach
    public void setup() throws Exception {
        controller = new SelfieController();
        repo = mock(SelfieRepository.class);
        storageService = new SelfieStorageService(System.getProperty("user.dir") + "/selfies");
        storageService.init();

        Field f1 = SelfieController.class.getDeclaredField("selfieRepository");
        f1.setAccessible(true);
        f1.set(controller, repo);

        Field f2 = SelfieController.class.getDeclaredField("selfieStorageService");
        f2.setAccessible(true);
        f2.set(controller, storageService);

        selfiesDir = new File(System.getProperty("user.dir") + "/selfies");
        cleanupDirectory(selfiesDir);
    }

    @AfterEach
    public void teardown() {
        cleanupDirectory(selfiesDir);
    }

    private void cleanupDirectory(File dir) {
        if (dir != null && dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    @Test
    public void testSelfies() throws Exception {
        ConcurrentModel model = new ConcurrentModel();

        // GET /selfies
        String view = controller.listSelfies(model);
        assertEquals("selfies", view);

        // POST /selfies with invalid file (empty)
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.isEmpty()).thenReturn(true);
        String view2 = controller.uploadSelfie(badFile, LocalDate.now());
        assertTrue(view2.startsWith("redirect:/selfies?error="));

        // POST /selfies with valid jpg file
        MultipartFile goodFile = mock(MultipartFile.class);
        when(goodFile.isEmpty()).thenReturn(false);
        when(goodFile.getOriginalFilename()).thenReturn("test.jpg");
        when(goodFile.getBytes()).thenReturn(new byte[]{1, 2, 3});

        String view3 = controller.uploadSelfie(goodFile, LocalDate.now());
        assertEquals("redirect:/selfies?success=Selfie uploaded successfully.", view3);

        // POST /selfies with valid file but existing selfie
        Selfie existing = new Selfie();
        existing.setLocation(System.getProperty("user.dir") + "/selfies/" + LocalDate.now() + ".jpg");
        when(repo.findByDate(any(LocalDate.class))).thenReturn(existing);
        String view3b = controller.uploadSelfie(goodFile, LocalDate.now());
        assertEquals("redirect:/selfies?success=Selfie uploaded successfully.", view3b);

        // POST /selfies with valid heic file (zero-conversion, stored as-is)
        MultipartFile heicFile = mock(MultipartFile.class);
        when(heicFile.isEmpty()).thenReturn(false);
        when(heicFile.getOriginalFilename()).thenReturn("test.heic");
        when(heicFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        String view3c = controller.uploadSelfie(heicFile, LocalDate.now());
        assertEquals("redirect:/selfies?success=Selfie uploaded successfully.", view3c);

        // POST /selfies with path traversal attempt
        MultipartFile traversalFile = mock(MultipartFile.class);
        when(traversalFile.isEmpty()).thenReturn(false);
        when(traversalFile.getOriginalFilename()).thenReturn("../../etc/passwd.jpg");
        when(traversalFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        String viewTraversal = controller.uploadSelfie(traversalFile, LocalDate.now());
        assertEquals("redirect:/selfies?error=Security violation: Path traversal or invalid characters detected.", viewTraversal);

        // POST /selfies with invalid extension
        MultipartFile invalidExtFile = mock(MultipartFile.class);
        when(invalidExtFile.isEmpty()).thenReturn(false);
        when(invalidExtFile.getOriginalFilename()).thenReturn("malicious.sh");
        when(invalidExtFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        String viewInvalidExt = controller.uploadSelfie(invalidExtFile, LocalDate.now());
        assertTrue(viewInvalidExt.contains("error="));

        // POST /selfies with IOException
        MultipartFile ioExceptionFile = mock(MultipartFile.class);
        when(ioExceptionFile.isEmpty()).thenReturn(false);
        when(ioExceptionFile.getOriginalFilename()).thenReturn("test2.heic");
        when(ioExceptionFile.getBytes()).thenThrow(new IOException("Test Exception"));

        String view4 = controller.uploadSelfie(ioExceptionFile, LocalDate.now());
        assertEquals("redirect:/selfies?error=Failed to upload file.", view4);
    }
}

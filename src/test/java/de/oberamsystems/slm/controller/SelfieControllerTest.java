package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class SelfieControllerTest {

    @Test
    public void testSelfies() throws Exception {
        SelfieController controller = new SelfieController();
        
        SelfieRepository repo = mock(SelfieRepository.class);
        
        java.lang.reflect.Field f1 = SelfieController.class.getDeclaredField("selfieRepository");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        java.io.File dir = new java.io.File(System.getProperty("user.dir") + "/selfies");
        if (dir.exists()) {
            for (java.io.File f : dir.listFiles()) f.delete();
            dir.delete();
        }
        
        // GET /selfies
        String view = controller.listSelfies(model);
        assertEquals("selfies", view);
        
        // POST /selfies with invalid file
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.isEmpty()).thenReturn(true);
        String view2 = controller.uploadSelfie(badFile, LocalDate.now());
        assertTrue(view2.startsWith("redirect:/selfies?error"));
        
        // POST /selfies with valid file
        MultipartFile goodFile = mock(MultipartFile.class);
        when(goodFile.isEmpty()).thenReturn(false);
        when(goodFile.getOriginalFilename()).thenReturn("test.jpg");
        when(goodFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        
        String view3 = controller.uploadSelfie(goodFile, LocalDate.now());
        assertEquals("redirect:/selfies?success=Selfie uploaded successfully.", view3);
        
        // POST /selfies with valid file but existing selfie
        when(repo.findByDate(any(LocalDate.class))).thenReturn(new Selfie());
        String view3b = controller.uploadSelfie(goodFile, LocalDate.now());
        assertEquals("redirect:/selfies?success=Selfie uploaded successfully.", view3b);
        
        // POST /selfies with valid heic file
        MultipartFile heicFile = mock(MultipartFile.class);
        when(heicFile.isEmpty()).thenReturn(false);
        when(heicFile.getOriginalFilename()).thenReturn("test.heic");
        when(heicFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        String view3c = controller.uploadSelfie(heicFile, LocalDate.now());
        assertEquals("redirect:/selfies?success=Selfie uploaded successfully.", view3c);
        
        // POST /selfies with IOException
        MultipartFile ioExceptionFile = mock(MultipartFile.class);
        when(ioExceptionFile.isEmpty()).thenReturn(false);
        when(ioExceptionFile.getOriginalFilename()).thenReturn("test2.heic");
        when(ioExceptionFile.getBytes()).thenThrow(new java.io.IOException("Test Exception"));
        
        String view4 = controller.uploadSelfie(ioExceptionFile, LocalDate.now());
        assertEquals("redirect:/selfies?error=Failed to upload file.", view4);
    }
}

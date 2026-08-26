package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class MeditationControllerTest {

    @Test
    public void testMeditationController() throws Exception {
        MeditationController controller = new MeditationController();
        
        MeditationSessionRepository repo = mock(MeditationSessionRepository.class);
        
        java.lang.reflect.Field f1 = MeditationController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // meditations
        String view1 = controller.addMeditations(1L, model);
        assertEquals("meditations", view1);
        
        MeditationSession ms = new MeditationSession();
        String view2 = controller.submitMeditations(ms, model);
        assertEquals("redirect:/meditations", view2);
    }
}

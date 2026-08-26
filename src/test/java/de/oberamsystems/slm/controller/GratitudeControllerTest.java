package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class GratitudeControllerTest {

    @Test
    public void testGratitudeController() throws Exception {
        GratitudeController controller = new GratitudeController();
        
        GratitudeRepository repo = mock(GratitudeRepository.class);
        
        java.lang.reflect.Field f1 = GratitudeController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // gratitudes
        String view1 = controller.addGratitude(1L, model);
        assertEquals("gratitudes", view1);
        
        Gratitude g = new Gratitude();
        String view2 = controller.submitGratitude(g, model);
        assertEquals("redirect:/gratitudes", view2);
    }
}

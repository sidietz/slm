package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class HumanControllerTest {

    @Test
    public void testHumanController() throws Exception {
        HumanController controller = new HumanController();
        
        HumanRepository repo = mock(HumanRepository.class);
        
        java.lang.reflect.Field f1 = HumanController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // humans
        String view1 = controller.addHumans(1L, model);
        assertEquals("humans", view1);
        
        Human h = new Human();
        String view2 = controller.submitHumans(h, model);
        assertEquals("humans", view2);
    }
}

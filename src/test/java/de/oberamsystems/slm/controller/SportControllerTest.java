package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class SportControllerTest {

    @Test
    public void testSportController() throws Exception {
        SportController controller = new SportController();
        
        SportSessionRepository repo = mock(SportSessionRepository.class);
        SportTypeRepository typeRepo = mock(SportTypeRepository.class);
        
        java.lang.reflect.Field f1 = SportController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = SportController.class.getDeclaredField("typeRepo");
        f2.setAccessible(true);
        f2.set(controller, typeRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // sports
        String view1 = controller.addSports(1L, model);
        assertEquals("sports", view1);
        
        SportSession s = new SportSession();
        String view2 = controller.submitSports(s, model);
        assertEquals("redirect:/sports", view2);
        
        // sport-types
        String view3 = controller.addSportType(1L, model);
        assertEquals("sport-types", view3);
        
        SportType t = new SportType();
        String view4 = controller.submitSportType(t, model);
        assertEquals("redirect:/sport-types", view4);
    }
}

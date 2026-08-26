package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ActivityControllerTest {

    @Test
    public void testActivityController() throws Exception {
        ActivityController controller = new ActivityController();
        
        ActivityRepository repo = mock(ActivityRepository.class);
        ActivityTypeRepository typeRepo = mock(ActivityTypeRepository.class);
        
        java.lang.reflect.Field f1 = ActivityController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = ActivityController.class.getDeclaredField("typeRepo");
        f2.setAccessible(true);
        f2.set(controller, typeRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // activities
        String view1 = controller.addActivity(1L, model);
        assertEquals("activities", view1);
        
        Activity a = new Activity();
        String view2 = controller.submitActivity(a, model);
        assertEquals("redirect:/activities", view2);
        
        // activity-types
        String view3 = controller.addActivityType(1L, model);
        assertEquals("activity-types", view3);
        
        ActivityType t = new ActivityType();
        String view4 = controller.submitActivityType(t, model);
        assertEquals("redirect:/activity-types", view4);
    }
}

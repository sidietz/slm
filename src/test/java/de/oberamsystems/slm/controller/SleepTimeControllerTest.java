package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class SleepTimeControllerTest {

    @Test
    public void testSleepTimeController() throws Exception {
        SleepTimeController controller = new SleepTimeController();
        
        SleepTimeRepository repo = mock(SleepTimeRepository.class);
        
        java.lang.reflect.Field f1 = SleepTimeController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // sleeptime-bb
        String view1 = controller.sleepTimeC3(model);
        assertEquals("sleeptime-bb", view1);
        
        // sleep-times
        String view2 = controller.addSleepTimes(model);
        assertEquals("sleep-times", view2);
        
        SleepTime st = new SleepTime();
        String view3 = controller.submitSleepTimes(st, model);
        assertEquals("redirect:/sleep-times", view3);
    }
}

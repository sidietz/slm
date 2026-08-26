package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

public class LearningControllerTest {

    @Test
    public void testLearningController() throws Exception {
        LearningController controller = new LearningController();
        
        LearningItemRepository repo = mock(LearningItemRepository.class);
        LearningSessionRepository sessionRepo = mock(LearningSessionRepository.class);
        
        java.lang.reflect.Field f1 = LearningController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = LearningController.class.getDeclaredField("learningSessionRepo");
        f2.setAccessible(true);
        f2.set(controller, sessionRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // learning-sessions
        String view1 = controller.addLearningSessions(1L, model);
        assertEquals("learning-sessions", view1);
        
        LearningSession ls = new LearningSession();
        ls.setStart(LocalDateTime.now().minusHours(1));
        ls.setEnd(LocalDateTime.now());
        ls.setLearningItem(new LearningItem());
        
        String view2 = controller.submitLearningSessions(ls, model);
        assertEquals("redirect:/learning-sessions", view2);
        
        // learning-items
        String view3 = controller.addLearningItems(1L, model);
        assertEquals("learning-items", view3);
        
        LearningItem li = new LearningItem();
        String view4 = controller.submitLearningItems(li, model);
        assertEquals("redirect:/learning-items", view4);
    }
}

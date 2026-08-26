package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class MoodControllerTest {

    @Test
    public void testMoodController() throws Exception {
        MoodController controller = new MoodController();
        
        MoodRepository repo = mock(MoodRepository.class);
        
        java.lang.reflect.Field f1 = MoodController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // moods
        String view1 = controller.addMoods(1L, model);
        assertEquals("moods", view1);
        
        Mood m = new Mood();
        String view2 = controller.submitMoods(m, model);
        assertEquals("redirect:/moods", view2);
        
        // mood-bb
        String view3 = controller.moodBb(model);
        assertEquals("mood-bb", view3);
    }
}

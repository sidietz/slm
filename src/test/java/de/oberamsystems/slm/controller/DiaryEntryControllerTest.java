package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class DiaryEntryControllerTest {

    @Test
    public void testDiaryEntryController() throws Exception {
        DiaryEntryController controller = new DiaryEntryController();
        
        DiaryEntryRepository repo = mock(DiaryEntryRepository.class);
        
        java.lang.reflect.Field f1 = DiaryEntryController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // diary
        String view1 = controller.addDiaryEntry(1L, model);
        assertEquals("diary", view1);
        
        DiaryEntry d = new DiaryEntry();
        String view2 = controller.submitDiaryEntry(d, model);
        assertEquals("redirect:/diary", view2);
    }
}

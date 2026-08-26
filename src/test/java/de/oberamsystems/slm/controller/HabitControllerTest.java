package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

public class HabitControllerTest {

    @Test
    public void testHabitController() throws Exception {
        HabitController controller = new HabitController();
        
        HabitRepository repo = mock(HabitRepository.class);
        HabitEntryRepository entryRepo = mock(HabitEntryRepository.class);
        
        java.lang.reflect.Field f1 = HabitController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = HabitController.class.getDeclaredField("entryRepo");
        f2.setAccessible(true);
        f2.set(controller, entryRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // habits
        String view1 = controller.addHabits(1L, model);
        assertEquals("habits", view1);
        
        Habit h = new Habit();
        String view2 = controller.submitHabits(h, model);
        assertEquals("redirect:/habits", view2);
        
        // habit-entries
        String view3 = controller.addHabitEntries(1L, model);
        assertEquals("habit-entries", view3);
        
        HabitEntry e = new HabitEntry();
        String view4 = controller.submitHabitEntries(e, model);
        assertEquals("redirect:/habit-entries", view4);
        
        // last-done-habit-entries
        when(repo.getById(anyLong())).thenReturn(new Habit());
        String view5 = controller.getLastDoneHabitEntries(1L, model);
        assertEquals("last-done-habit-entries", view5);
        
        String view6 = controller.submitLastDoneHabitEntries(1L, e, model);
        assertEquals("redirect:/last-done-habit-entries", view6);
        
        // testing with null id
        String view7 = controller.getLastDoneHabitEntries(null, model);
        assertEquals("last-done-habit-entries", view7);
        
        String view8 = controller.submitLastDoneHabitEntries(null, e, model);
        assertEquals("redirect:/last-done-habit-entries", view8);
    }
}

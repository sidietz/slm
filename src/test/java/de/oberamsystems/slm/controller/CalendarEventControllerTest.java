package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalendarEventControllerTest {

    @Test
    public void testCalendarEventController() throws Exception {
        CalendarEventController controller = new CalendarEventController();
        
        CalendarEventRepository repo = mock(CalendarEventRepository.class);
        
        java.lang.reflect.Field f1 = CalendarEventController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // event
        when(repo.findMinDate()).thenReturn(LocalDateTime.now().minusDays(10));
        when(repo.findMaxDate()).thenReturn(LocalDateTime.now().plusDays(10));
        
        String view1 = controller.getEvent(null, null, model);
        assertEquals("event", view1);
        
        String view2 = controller.getEvent(LocalDateTime.now(), LocalDateTime.now(), model);
        assertEquals("event", view2);
        
        when(repo.findMinDate()).thenReturn(null);
        when(repo.findMaxDate()).thenReturn(null);
        String view3 = controller.getEvent(null, null, model);
        assertEquals("event", view3);
        
        // add-event
        String view4 = controller.addEvent(1L, model);
        assertEquals("add-event", view4);
        
        CalendarEvent e = new CalendarEvent();
        String view5 = controller.submitEvent(e, model);
        assertEquals("add-event", view5);
    }
}

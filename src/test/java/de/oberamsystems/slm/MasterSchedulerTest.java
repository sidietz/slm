package de.oberamsystems.slm;

import de.oberamsystems.slm.mail.EmailService;
import de.oberamsystems.slm.model.CalendarEvent;
import de.oberamsystems.slm.model.CalendarEventRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MasterSchedulerTest {

    @Test
    public void testScheduleTest() throws Exception {
        MasterScheduler scheduler = new MasterScheduler();

        CalendarEventRepository repo = mock(CalendarEventRepository.class);
        EmailService mailer = mock(EmailService.class);

        java.lang.reflect.Field f1 = MasterScheduler.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(scheduler, repo);

        java.lang.reflect.Field f2 = MasterScheduler.class.getDeclaredField("mailer");
        f2.setAccessible(true);
        f2.set(scheduler, mailer);

        // Test with empty list
        when(repo.findByStartBetween(any(), any())).thenReturn(new ArrayList<>());
        scheduler.scheduleTest();
        verify(mailer, times(0)).sendHtmlEmail(any(), any());

        // Test with non-empty list
        List<CalendarEvent> events = new ArrayList<>();
        events.add(new CalendarEvent());
        when(repo.findByStartBetween(any(), any())).thenReturn(events);
        scheduler.scheduleTest();
        verify(mailer, times(1)).sendHtmlEmail(any(), any());
    }
}

package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IndexControllerTest {

    @Test
    public void testIndex() throws Exception {
        IndexController controller = new IndexController();
        
        SportSessionRepository sportRepo = mock(SportSessionRepository.class);
        MeditationSessionRepository meditationRepo = mock(MeditationSessionRepository.class);
        HumanRepository humanRepo = mock(HumanRepository.class);
        ReadingSessionRepository readingRepo = mock(ReadingSessionRepository.class);
        
        java.lang.reflect.Field f1 = IndexController.class.getDeclaredField("sportRepo");
        f1.setAccessible(true);
        f1.set(controller, sportRepo);
        
        java.lang.reflect.Field f2 = IndexController.class.getDeclaredField("meditationRepo");
        f2.setAccessible(true);
        f2.set(controller, meditationRepo);
        
        java.lang.reflect.Field f3 = IndexController.class.getDeclaredField("humanRepo");
        f3.setAccessible(true);
        f3.set(controller, humanRepo);
        
        java.lang.reflect.Field f4 = IndexController.class.getDeclaredField("readingsessionRepo");
        f4.setAccessible(true);
        f4.set(controller, readingRepo);
        
        // Setup mocks
        SportSession ss = new SportSession();
        SportType st = new SportType();
        st.setName("Running");
        ss.setType(st);
        ss.setStart(LocalDateTime.now().minusHours(1));
        when(sportRepo.findFirstByOrderByStartDesc()).thenReturn(ss);
        
        Human h = new Human();
        h.setFirstname("Simon");
        h.setLastname("Dietz");
        h.setBirthday(new java.util.Date());
        when(humanRepo.findFirstByOrderByDaysUntilBirthdayAsc()).thenReturn(h);
        
        MeditationSession ms = new MeditationSession();
        ms.setStart(LocalDateTime.now().minusHours(2));
        ms.setEnd(LocalDateTime.now().minusHours(1));
        when(meditationRepo.findFirstByOrderByStartDesc()).thenReturn(ms);
        
        ReadingSession rs = new ReadingSession();
        rs.setStart(LocalDateTime.now().minusHours(3));
        rs.setEnd(LocalDateTime.now().minusHours(2));
        when(readingRepo.findFirstByOrderByStartDesc()).thenReturn(rs);
        
        ConcurrentModel model = new ConcurrentModel();
        String view = controller.index(model);
        
        assertNotNull(view);
    }
}

package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MoodRestControllerTest {

    @Test
    public void testMoodRestController() throws Exception {
        MoodRestController controller = new MoodRestController();
        
        MoodRepository repo = mock(MoodRepository.class);
        
        java.lang.reflect.Field f1 = MoodRestController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        List<Mood> list = new ArrayList<>();
        when(repo.findAll(any(Sort.class))).thenReturn(list);
        
        // api
        List<Mood> res = controller.getMoods();
        assertEquals(0, res.size());
    }
}

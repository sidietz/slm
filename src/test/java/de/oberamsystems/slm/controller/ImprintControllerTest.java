package de.oberamsystems.slm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ImprintControllerTest {

    @Test
    public void testImprintController() throws Exception {
        ImprintController controller = new ImprintController();
        controller.version = "1.0.0";
        
        ConcurrentModel model = new ConcurrentModel();
        
        // imprint
        String view1 = controller.index(model);
        assertEquals("imprint", view1);
    }
}

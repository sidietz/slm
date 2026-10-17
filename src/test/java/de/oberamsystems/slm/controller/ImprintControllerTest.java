package de.oberamsystems.slm.controller;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImprintControllerTest {

    @Test
    public void testImprintController() throws Exception {
        ImprintController controller = new ImprintController();
        controller.version = "1.0.0";
        
        ConcurrentModel model = new ConcurrentModel();
        
        // imprint
        String view1 = controller.index(model);
        assertEquals("imprint", view1);
        
        @SuppressWarnings("unchecked")
        List<String> msgs = (List<String>) model.getAttribute("msgs");
        assertTrue(msgs.contains("version: 1.0.0"));
    }

    @Test
    public void testImprintControllerWithBuildProperties() {
        Properties props = new Properties();
        props.put("version", "0.0.2-SNAPSHOT");
        BuildProperties buildProperties = new BuildProperties(props);

        ImprintController controller = new ImprintController(buildProperties);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.index(model);
        assertEquals("imprint", view);

        @SuppressWarnings("unchecked")
        List<String> msgs = (List<String>) model.getAttribute("msgs");
        assertTrue(msgs.contains("version: 0.0.2-SNAPSHOT"));
    }
}


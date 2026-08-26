package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class GameControllerTest {

    @Test
    public void testGameController() throws Exception {
        GameController controller = new GameController();
        
        GameRepository repo = mock(GameRepository.class);
        PublisherRepository publisherRepo = mock(PublisherRepository.class);
        StudioRepository studioRepo = mock(StudioRepository.class);
        GamingSessionRepository sessionRepo = mock(GamingSessionRepository.class);
        ManufacturerRepository manufacturerRepo = mock(ManufacturerRepository.class);
        DeviceRepository deviceRepo = mock(DeviceRepository.class);
        DeviceTypeRepository deviceTypeRepo = mock(DeviceTypeRepository.class);
        VendorRepository vendorRepo = mock(VendorRepository.class);
        
        java.lang.reflect.Field f1 = GameController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = GameController.class.getDeclaredField("publisherRepo");
        f2.setAccessible(true);
        f2.set(controller, publisherRepo);
        
        java.lang.reflect.Field f3 = GameController.class.getDeclaredField("studioRepo");
        f3.setAccessible(true);
        f3.set(controller, studioRepo);
        
        java.lang.reflect.Field f4 = GameController.class.getDeclaredField("sessionRepo");
        f4.setAccessible(true);
        f4.set(controller, sessionRepo);
        
        java.lang.reflect.Field f5 = GameController.class.getDeclaredField("manufacturerRepo");
        f5.setAccessible(true);
        f5.set(controller, manufacturerRepo);
        
        java.lang.reflect.Field f6 = GameController.class.getDeclaredField("deviceRepo");
        f6.setAccessible(true);
        f6.set(controller, deviceRepo);
        
        java.lang.reflect.Field f7 = GameController.class.getDeclaredField("deviceTypeRepo");
        f7.setAccessible(true);
        f7.set(controller, deviceTypeRepo);
        
        java.lang.reflect.Field f8 = GameController.class.getDeclaredField("vendorRepo");
        f8.setAccessible(true);
        f8.set(controller, vendorRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // gaming-sessions
        String view1 = controller.addGamingSessions(1L, model);
        assertEquals("gaming-sessions", view1);
        
        GamingSession gs = new GamingSession();
        String view2 = controller.submitGamingSessions(gs, model);
        assertEquals("redirect:/gaming-sessions", view2);
        
        // games
        String view3 = controller.addGames(1L, model);
        assertEquals("games", view3);
        
        Game g = new Game();
        String view4 = controller.submitGames(g, model);
        assertEquals("redirect:/games", view4);
        
        // devices
        String view5 = controller.addDevices(1L, model);
        assertEquals("devices", view5);
        
        Device d = new Device();
        String view6 = controller.submitDevices(d, model);
        assertEquals("redirect:/devices", view6);
        
        // publishers
        String view7 = controller.addPublishers(1L, model);
        assertEquals("publishers", view7);
        
        Publisher p = new Publisher();
        String view8 = controller.submitPublishers(p, model);
        assertEquals("redirect:/publishers", view8);
        
        // studios
        String view9 = controller.addStudios(1L, model);
        assertEquals("studios", view9);
        
        Studio s = new Studio();
        String view10 = controller.submitStudios(s, model);
        assertEquals("redirect:/studios", view10);
        
        // vendors
        String view11 = controller.addVendors(1L, model);
        assertEquals("vendors", view11);
        
        Vendor v = new Vendor();
        String view12 = controller.submitVendors(v, model);
        assertEquals("redirect:/vendors", view12);
        
        // manufacturers
        String view13 = controller.addManufacturers(1L, model);
        assertEquals("manufacturers", view13);
        
        Manufacturer m = new Manufacturer();
        String view14 = controller.submitManufacturers(m, model);
        assertEquals("redirect:/manufacturers", view14);
    }
}

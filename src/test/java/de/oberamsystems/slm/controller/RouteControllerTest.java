package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RouteControllerTest {

    @Test
    public void testRouteController() throws Exception {
        RouteController controller = new RouteController();
        
        RouteRepository repo = mock(RouteRepository.class);
        CarRepository carRepo = mock(CarRepository.class);
        PointRepository pointRepo = mock(PointRepository.class);
        CarTripRepository carTripRepo = mock(CarTripRepository.class);
        ManufacturerRepository manufacturerRepo = mock(ManufacturerRepository.class);
        VendorRepository vendorRepo = mock(VendorRepository.class);
        
        Field f1 = RouteController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        Field f2 = RouteController.class.getDeclaredField("carRepo");
        f2.setAccessible(true);
        f2.set(controller, carRepo);
        
        Field f3 = RouteController.class.getDeclaredField("pointRepo");
        f3.setAccessible(true);
        f3.set(controller, pointRepo);
        
        Field f4 = RouteController.class.getDeclaredField("carTripRepo");
        f4.setAccessible(true);
        f4.set(controller, carTripRepo);
        
        Field f5 = RouteController.class.getDeclaredField("manufacturerRepo");
        f5.setAccessible(true);
        f5.set(controller, manufacturerRepo);
        
        Field f6 = RouteController.class.getDeclaredField("vendorRepo");
        f6.setAccessible(true);
        f6.set(controller, vendorRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // routes
        String view1 = controller.addBooks(1L, model);
        assertEquals("routes", view1);
        
        Route r = new Route();
        String view2 = controller.submitBooks(r, model);
        assertEquals("redirect:/routes", view2);
        
        Point p1 = new Point(1L, "Start", "Start Desc", 49.0, 10.0);
        Point p2 = new Point(2L, "End", "End Desc", 49.1, 10.1);
        Route r2 = new Route();
        r2.setStart(p1);
        r2.setEnd(p2);
        when(repo.calculateAirDistance(1L, 2L)).thenReturn(13.45);
        String view2b = controller.submitBooks(r2, model);
        assertEquals("redirect:/routes", view2b);
        assertEquals(13.45, r2.getAirDistance());
        
        // points
        String view3 = controller.addPoints(1L, model);
        assertEquals("points", view3);
        
        Point p = new Point();
        String view4 = controller.submitPoints(p, model);
        assertEquals("redirect:/points", view4);
        
        // car-trips
        String view5 = controller.addCarTrips(1L, model);
        assertEquals("car-trips", view5);
        
        CarTrip ct = new CarTrip();
        String view6 = controller.submitCarTrips(ct, model);
        assertEquals("redirect:/car-trips", view6);
        
        // cars
        String view7 = controller.addCars(1L, model);
        assertEquals("cars", view7);
        
        Car c = new Car();
        String view8 = controller.submitCars(c, model);
        assertEquals("redirect:/cars", view8);
    }
}

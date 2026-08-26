package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

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
        
        java.lang.reflect.Field f1 = RouteController.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = RouteController.class.getDeclaredField("carRepo");
        f2.setAccessible(true);
        f2.set(controller, carRepo);
        
        java.lang.reflect.Field f3 = RouteController.class.getDeclaredField("pointRepo");
        f3.setAccessible(true);
        f3.set(controller, pointRepo);
        
        java.lang.reflect.Field f4 = RouteController.class.getDeclaredField("carTripRepo");
        f4.setAccessible(true);
        f4.set(controller, carTripRepo);
        
        java.lang.reflect.Field f5 = RouteController.class.getDeclaredField("manufacturerRepo");
        f5.setAccessible(true);
        f5.set(controller, manufacturerRepo);
        
        java.lang.reflect.Field f6 = RouteController.class.getDeclaredField("vendorRepo");
        f6.setAccessible(true);
        f6.set(controller, vendorRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        // routes
        String view1 = controller.addBooks(1L, model);
        assertEquals("routes", view1);
        
        Route r = new Route();
        String view2 = controller.submitBooks(r, model);
        assertEquals("redirect:/routes", view2);
        
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

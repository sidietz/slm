package de.oberamsystems.slm.controller;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;

public class TrainTripController2Test {

    @Test
    public void testTrainTrips() throws Exception {
        TrainTripController2 controller = new TrainTripController2();
        
        TrainTrip2Repository repo = mock(TrainTrip2Repository.class);
        TrainStation2Repository stationRepo = mock(TrainStation2Repository.class);
        TrainLineRepository lineRepo = mock(TrainLineRepository.class);
        
        java.lang.reflect.Field f1 = TrainTripController2.class.getDeclaredField("repo");
        f1.setAccessible(true);
        f1.set(controller, repo);
        
        java.lang.reflect.Field f2 = TrainTripController2.class.getDeclaredField("stationRepo");
        f2.setAccessible(true);
        f2.set(controller, stationRepo);
        
        java.lang.reflect.Field f3 = TrainTripController2.class.getDeclaredField("lineRepo");
        f3.setAccessible(true);
        f3.set(controller, lineRepo);
        
        ConcurrentModel model = new ConcurrentModel();
        
        TrainLine line = new TrainLine(1L, "Line", "Desc", new java.util.HashSet<>());
        when(lineRepo.getReferenceById(anyLong())).thenReturn(line);
        
        // GET /train-trips
        String view = controller.addTrainTrips(1L, model);
        assertEquals("train-trips", view);
        
        // POST /train-trips
        TrainTrip2 tt = new TrainTrip2();
        String view2 = controller.submitTrainTrips(1L, tt, model);
        assertEquals("train-trips", view2);
        
        // GET /train-trips with null
        String view2b = controller.addTrainTrips(null, model);
        assertEquals("train-trips", view2b);
        
        // POST /train-trips with null
        String view2c = controller.submitTrainTrips(null, tt, model);
        assertEquals("train-trips", view2c);
        
        // GET /train-stations
        String view3 = controller.addTrainStations(1L, model);
        assertEquals("train-stations", view3);
        
        // POST /train-stations
        TrainStation2Dto tsdto = new TrainStation2Dto();
        tsdto.setLineId(1L);
        tsdto.setDs100("TEST");
        tsdto.setName("Test Station");
        String view4 = controller.submitTrainStations(tsdto, model);
        assertEquals("redirect:/train-stations", view4);
        
        // GET /train-lines
        String view5 = controller.addTrainLines(1L, model);
        assertEquals("train-lines", view5);
        
        // POST /train-lines
        TrainLine tl = new TrainLine();
        String view6 = controller.submitTrainLines(tl, model);
        assertEquals("redirect:/train-lines", view6);
    }
}

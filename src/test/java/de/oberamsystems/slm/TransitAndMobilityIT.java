package de.oberamsystems.slm;

import de.oberamsystems.slm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
public class TransitAndMobilityIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarTripRepository carTripRepository;

    @Autowired
    private TrainLineRepository trainLineRepository;

    @Autowired
    private TrainStation2Repository trainStation2Repository;

    @Autowired
    private TrainTrip2Repository trainTrip2Repository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testPointAndRouteEcosystemFlow() throws Exception {
        // 1. Points: GET /points & POST /points
        mockMvc.perform(get("/points"))
                .andExpect(status().isOk())
                .andExpect(view().name("points"))
                .andExpect(model().attributeExists("points", "point"));

        mockMvc.perform(post("/points")
                        .with(csrf())
                        .param("name", "Munich Central")
                        .param("description", "Central Station Area")
                        .param("latitude", "48.140232")
                        .param("longitude", "11.558335")
                        .param("isHome", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/points"));
        assertEquals(1, pointRepository.count());
        Point startPoint = pointRepository.findAll().getFirst();

        Point endPoint = new Point(null, "Garmisch-Partenkirchen", "Alpine Resort", 47.492094, 11.095767, false);
        endPoint = pointRepository.saveAndFlush(endPoint);

        // 2. Routes: GET /routes & POST /routes with PostGIS air distance calculation
        mockMvc.perform(get("/routes"))
                .andExpect(status().isOk())
                .andExpect(view().name("routes"))
                .andExpect(model().attributeExists("routes", "points", "route"))
                .andExpect(content().string(containsString("Munich Central")));

        mockMvc.perform(post("/routes")
                        .with(csrf())
                        .param("name", "B2 Alpine Express")
                        .param("description", "Scenic route Munich to Garmisch")
                        .param("start", startPoint.getId().toString())
                        .param("end", endPoint.getId().toString())
                        .param("driveDistance", "88.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes"));

        assertEquals(1, routeRepository.count());
        Route savedRoute = routeRepository.findAll().getFirst();
        assertEquals("B2 Alpine Express", savedRoute.getName());
        assertNotNull(savedRoute.getAirDistance());

        // 3. Cars: GET /cars & POST /cars
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName("BMW AG");
        manufacturer = manufacturerRepository.saveAndFlush(manufacturer);

        Vendor vendor = new Vendor();
        vendor.setName("BMW Niederlassung München");
        vendor = vendorRepository.saveAndFlush(vendor);

        mockMvc.perform(get("/cars"))
                .andExpect(status().isOk())
                .andExpect(view().name("cars"))
                .andExpect(model().attributeExists("cars", "manufacturers", "vendors", "car"));

        mockMvc.perform(post("/cars")
                        .with(csrf())
                        .param("name", "BMW i4 M50")
                        .param("modelName", "i4 Gran Coupe")
                        .param("manufacturer", manufacturer.getId().toString())
                        .param("vendor", String.valueOf(vendor.getId()))
                        .param("purchasePrice", "65000.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"));

        assertEquals(1, carRepository.count());
        Car savedCar = carRepository.findAll().getFirst();

        // 4. CarTrips: GET /car-trips & POST /car-trips
        mockMvc.perform(get("/car-trips"))
                .andExpect(status().isOk())
                .andExpect(view().name("car-trips"))
                .andExpect(model().attributeExists("cartrips", "routes", "cars", "cartrip"))
                .andExpect(content().string(containsString("B2 Alpine Express")));

        mockMvc.perform(post("/car-trips")
                        .with(csrf())
                        .param("car", savedCar.getId().toString())
                        .param("route", savedRoute.getId().toString())
                        .param("start", "2026-10-19T07:30")
                        .param("end", "2026-10-19T08:45")
                        .param("isWayBack", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/car-trips"));

        assertEquals(1, carTripRepository.count());
    }

    @Test
    void testTrainEcosystemFlow() throws Exception {
        // 1. TrainLine: GET /train-lines & POST /train-lines
        mockMvc.perform(get("/train-lines"))
                .andExpect(status().isOk())
                .andExpect(view().name("train-lines"))
                .andExpect(model().attributeExists("lines", "trainline"));

        mockMvc.perform(post("/train-lines")
                        .with(csrf())
                        .param("name", "S6")
                        .param("description", "Tutzing – Munich – Ebersberg"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/train-lines"));

        assertEquals(1, trainLineRepository.count());
        TrainLine line = trainLineRepository.findAll().getFirst();

        // 2. TrainStation: GET /train-stations & POST /train-stations
        mockMvc.perform(get("/train-stations"))
                .andExpect(status().isOk())
                .andExpect(view().name("train-stations"))
                .andExpect(model().attributeExists("stations", "trainstation", "lineId", "lines"))
                .andExpect(content().string(containsString("S6")));

        mockMvc.perform(post("/train-stations")
                        .with(csrf())
                        .param("ds100", "MH")
                        .param("name", "München Hbf")
                        .param("lineId", line.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/train-stations"));

        assertEquals(1, trainStation2Repository.count());
        TrainStation2 station = trainStation2Repository.findAll().getFirst();

        TrainStation2 destStation = new TrainStation2("MTZ", "Tutzing");
        destStation.addLine(line);
        destStation = trainStation2Repository.saveAndFlush(destStation);

        // 3. TrainTrip: GET /train-trips & POST /train-trips
        mockMvc.perform(get("/train-trips").param("id", line.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("train-trips"))
                .andExpect(model().attributeExists("traintrips2", "lineId", "traintrip", "lines"))
                .andExpect(content().string(containsString("S6")));

        mockMvc.perform(post("/train-trips")
                        .with(csrf())
                        .param("id", line.getId().toString())
                        .param("line", line.getId().toString())
                        .param("origin", station.getId().toString())
                        .param("destination", destStation.getId().toString())
                        .param("start", "2026-10-19T08:00")
                        .param("end", "2026-10-19T08:35"))
                .andExpect(status().isOk())
                .andExpect(view().name("train-trips"));

        assertEquals(1, trainTrip2Repository.count());
    }

    @Test
    void testUpdatePointAndRouteFlow() throws Exception {
        Point p = new Point(null, "Initial Point", "Init Desc", 48.0, 11.0, false);
        p = pointRepository.saveAndFlush(p);
        Long pointId = p.getId();

        mockMvc.perform(post("/points")
                        .with(csrf())
                        .param("id", pointId.toString())
                        .param("name", "Updated Point")
                        .param("description", "Updated Desc")
                        .param("latitude", "48.15")
                        .param("longitude", "11.58")
                        .param("isHome", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/points"));

        assertEquals(1, pointRepository.count());
        entityManager.clear();
        Point updatedPoint = pointRepository.findById(pointId).orElseThrow();
        assertEquals("Updated Point", updatedPoint.getName());
        assertEquals("Updated Desc", updatedPoint.getDescription());
        assertEquals(48.15, updatedPoint.getLatitude());
        assertEquals(11.58, updatedPoint.getLongitude());
        assertEquals(true, updatedPoint.getIsHome());

        Point endPoint = new Point(null, "End Point", "End Desc", 48.2, 11.6, false);
        endPoint = pointRepository.saveAndFlush(endPoint);

        Route r = new Route(null, "Route A", "Desc A", updatedPoint, endPoint, 10.0, 12.0);
        r = routeRepository.saveAndFlush(r);
        Long routeId = r.getId();

        mockMvc.perform(post("/routes")
                        .with(csrf())
                        .param("id", routeId.toString())
                        .param("name", "Route A Updated")
                        .param("description", "Desc A Updated")
                        .param("start", updatedPoint.getId().toString())
                        .param("end", endPoint.getId().toString())
                        .param("driveDistance", "15.5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes"));

        assertEquals(1, routeRepository.count());
        entityManager.clear();
        Route updatedRoute = routeRepository.findById(routeId).orElseThrow();
        assertEquals("Route A Updated", updatedRoute.getName());
        assertEquals("Desc A Updated", updatedRoute.getDescription());
        assertEquals(15.5, updatedRoute.getDriveDistance());
    }

    @Test
    void testUpdateCarAndTripFlow() throws Exception {
        Manufacturer m = manufacturerRepository.saveAndFlush(new Manufacturer(null, "Audi AG", "Ingolstadt", LocalDate.of(1909, 7, 16)));
        Vendor v = new Vendor(null, "Audi Zentrum");
        v = vendorRepository.saveAndFlush(v);

        Car car = new Car();
        car.setName("Audi A4");
        car.setModelName("A4 Avant");
        car.setManufacturer(m);
        car.setVendor(v);
        car.setPurchasePrice(45000.0);
        car = carRepository.saveAndFlush(car);
        Long carId = car.getId();

        mockMvc.perform(post("/cars")
                        .with(csrf())
                        .param("id", carId.toString())
                        .param("name", "Audi A4 Black Edition")
                        .param("modelName", "A4 Avant S-Line")
                        .param("manufacturer", m.getId().toString())
                        .param("vendor", v.getId().toString())
                        .param("purchasePrice", "48000.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"));

        assertEquals(1, carRepository.count());
        entityManager.clear();
        Car updatedCar = carRepository.findById(carId).orElseThrow();
        assertEquals("Audi A4 Black Edition", updatedCar.getName());
        assertEquals("A4 Avant S-Line", updatedCar.getModelName());
        assertEquals(48000.0, updatedCar.getPurchasePrice());

        Point p1 = pointRepository.saveAndFlush(new Point(null, "P1", "P1", 48.0, 11.0, true));
        Point p2 = pointRepository.saveAndFlush(new Point(null, "P2", "P2", 48.5, 11.5, false));
        Route route = routeRepository.saveAndFlush(new Route(null, "Commute", "Commute route", p1, p2, 50.0, 60.0));

        CarTrip trip = new CarTrip();
        trip.setCar(updatedCar);
        trip.setRoute(route);
        trip.setIsWayBack(false);
        trip.setStart(LocalDateTime.of(2026, 10, 20, 8, 0));
        trip.setEnd(LocalDateTime.of(2026, 10, 20, 9, 0));
        trip = carTripRepository.saveAndFlush(trip);
        Long tripId = trip.getId();

        mockMvc.perform(post("/car-trips")
                        .with(csrf())
                        .param("id", tripId.toString())
                        .param("car", updatedCar.getId().toString())
                        .param("route", route.getId().toString())
                        .param("start", "2026-10-20T08:30")
                        .param("end", "2026-10-20T09:45")
                        .param("isWayBack", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/car-trips"));

        assertEquals(1, carTripRepository.count());
        entityManager.clear();
        CarTrip updatedTrip = carTripRepository.findById(tripId).orElseThrow();
        assertEquals(LocalDateTime.of(2026, 10, 20, 8, 30), updatedTrip.getStart());
        assertEquals(LocalDateTime.of(2026, 10, 20, 9, 45), updatedTrip.getEnd());
        assertEquals(true, updatedTrip.getIsWayBack());
        assertNotNull(updatedTrip.getDuration());
    }

    @Test
    void testUpdateTrainLineStationAndTripFlow() throws Exception {
        TrainLine line = new TrainLine();
        line.setName("S1");
        line.setDescription("Freising/Airport");
        line = trainLineRepository.saveAndFlush(line);
        Long lineId = line.getId();

        mockMvc.perform(post("/train-lines")
                        .with(csrf())
                        .param("id", lineId.toString())
                        .param("name", "S1 (Airport Line)")
                        .param("description", "Airport Express"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/train-lines"));

        assertEquals(1, trainLineRepository.count());
        entityManager.clear();
        TrainLine updatedLine = trainLineRepository.findById(lineId).orElseThrow();
        assertEquals("S1 (Airport Line)", updatedLine.getName());
        assertEquals("Airport Express", updatedLine.getDescription());

        TrainStation2 station = new TrainStation2("MUC", "Munich Airport");
        station.addLine(updatedLine);
        station = trainStation2Repository.saveAndFlush(station);
        Long stationId = station.getId();

        mockMvc.perform(post("/train-stations")
                        .with(csrf())
                        .param("id", stationId.toString())
                        .param("ds100", "MMAP")
                        .param("name", "Flughafen München Terminal")
                        .param("lineId", updatedLine.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/train-stations"));

        assertEquals(1, trainStation2Repository.count());
        entityManager.clear();
        TrainStation2 updatedStation = trainStation2Repository.findById(stationId).orElseThrow();
        assertEquals("MMAP", updatedStation.getDs100());
        assertEquals("Flughafen München Terminal", updatedStation.getName());

        TrainStation2 dest = trainStation2Repository.saveAndFlush(new TrainStation2("MNO", "Neufahrn"));

        TrainTrip2 tt = new TrainTrip2(null, updatedLine, updatedStation, dest,
                LocalDateTime.of(2026, 10, 21, 10, 0), LocalDateTime.of(2026, 10, 21, 10, 15), null);
        tt = trainTrip2Repository.saveAndFlush(tt);
        Long tripId = tt.getId();

        mockMvc.perform(post("/train-trips")
                        .with(csrf())
                        .param("id", tripId.toString())
                        .param("lineId", updatedLine.getId().toString())
                        .param("origin", updatedStation.getId().toString())
                        .param("destination", dest.getId().toString())
                        .param("start", "2026-10-21T10:05")
                        .param("end", "2026-10-21T10:22"))
                .andExpect(status().isOk());

        assertEquals(1, trainTrip2Repository.count());
        entityManager.clear();
        TrainTrip2 updatedTrip = trainTrip2Repository.findById(tripId).orElseThrow();
        assertEquals(LocalDateTime.of(2026, 10, 21, 10, 5), updatedTrip.getStart());
        assertEquals(LocalDateTime.of(2026, 10, 21, 10, 22), updatedTrip.getEnd());
    }

    @Test
    void testAddTrainTripViaModalForm() throws Exception {
        TrainLine line = new TrainLine();
        line.setName("RE1");
        line.setDescription("Nürnberg Express");
        line = trainLineRepository.saveAndFlush(line);

        TrainStation2 origin = new TrainStation2("MH", "München Hbf");
        origin = trainStation2Repository.saveAndFlush(origin);

        TrainStation2 dest = new TrainStation2("NN", "Nürnberg Hbf");
        dest = trainStation2Repository.saveAndFlush(dest);

        // 1. Create train trip via modal POST with empty id and lineId
        mockMvc.perform(post("/train-trips")
                        .with(csrf())
                        .param("id", "")
                        .param("lineId", line.getId().toString())
                        .param("origin", origin.getId().toString())
                        .param("destination", dest.getId().toString())
                        .param("start", "2026-10-21T07:19")
                        .param("end", "2026-10-21T07:55"))
                .andExpect(status().isOk())
                .andExpect(view().name("train-trips"))
                .andExpect(model().attributeExists("traintrips2", "lineId", "lines", "stations"));

        assertEquals(1, trainTrip2Repository.count());
        TrainTrip2 savedTrip = trainTrip2Repository.findAll().getFirst();
        assertNotNull(savedTrip.getId());
        assertEquals(line.getId(), savedTrip.getLine().getId());
        assertEquals(origin.getId(), savedTrip.getOrigin().getId());
        assertEquals(dest.getId(), savedTrip.getDestination().getId());
        assertEquals(LocalDateTime.of(2026, 10, 21, 7, 19), savedTrip.getStart());
        assertEquals(LocalDateTime.of(2026, 10, 21, 7, 55), savedTrip.getEnd());

        // 2. Also verify submitting with omitted lineId is REJECTED via validation
        mockMvc.perform(post("/train-trips")
                        .with(csrf())
                        .param("id", "")
                        .param("origin", origin.getId().toString())
                        .param("destination", dest.getId().toString())
                        .param("start", "2026-10-21T08:00")
                        .param("end", "2026-10-21T08:45"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("errorMessage", "Train line is required."));

        assertEquals(1, trainTrip2Repository.count(), "Train trip without line must be rejected");
    }

    @Test
    void testModalCreateTransitAndMobilityWithEmptyIdParam() throws Exception {
        // 1. Point via modal with empty id
        mockMvc.perform(post("/points")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "Odeonsplatz")
                        .param("latitude", "48.1428")
                        .param("longitude", "11.5775")
                        .param("isHome", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/points"));
        assertEquals(1, pointRepository.count());
        Point p1 = pointRepository.findAll().getFirst();

        Point p2 = pointRepository.saveAndFlush(new Point(null, "Marienplatz", "City Center", 48.137154, 11.576124, true));

        // 2. Route via modal with empty id and explicit start/end
        mockMvc.perform(post("/routes")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "City Center Stroll")
                        .param("start", p1.getId().toString())
                        .param("end", p2.getId().toString())
                        .param("driveDistance", "1.2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes"));
        assertEquals(1, routeRepository.count());

        // 3. Route via modal with empty id and omitted start/end (must be REJECTED via validation)
        mockMvc.perform(post("/routes")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "City Center Loop")
                        .param("driveDistance", "2.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/routes"))
                .andExpect(flash().attribute("errorMessage", "Start and end points are required."));
        assertEquals(1, routeRepository.count(), "Route without points must be rejected");
        Route route = routeRepository.findAll().getFirst();

        // 4. Car via modal with empty id
        Manufacturer m = manufacturerRepository.saveAndFlush(new Manufacturer(null, "BMW AG", "Munich", null));
        Vendor v = vendorRepository.saveAndFlush(new Vendor(null, "BMW Niederlassung"));

        mockMvc.perform(post("/cars")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "BMW 330e")
                        .param("modelName", "330e Touring")
                        .param("manufacturer", m.getId().toString())
                        .param("vendor", v.getId().toString())
                        .param("purchasePrice", "52000.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cars"));
        assertEquals(1, carRepository.count());
        Car car = carRepository.findAll().getFirst();

        // 5. CarTrip via modal with empty id and explicit car/route
        mockMvc.perform(post("/car-trips")
                        .with(csrf())
                        .param("id", "")
                        .param("car", car.getId().toString())
                        .param("route", route.getId().toString())
                        .param("start", "2026-10-21T08:00")
                        .param("end", "2026-10-21T08:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/car-trips"));
        assertEquals(1, carTripRepository.count());

        // 6. CarTrip via modal with empty id and omitted car/route (must be REJECTED via validation)
        mockMvc.perform(post("/car-trips")
                        .with(csrf())
                        .param("id", "")
                        .param("start", "2026-10-21T18:00")
                        .param("end", "2026-10-21T18:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/car-trips"))
                .andExpect(flash().attribute("errorMessage", "Car is required."));
        assertEquals(1, carTripRepository.count(), "Car trip without car must be rejected");

        // 7. TrainLine via modal with empty id
        mockMvc.perform(post("/train-lines")
                        .with(csrf())
                        .param("id", "")
                        .param("name", "U3")
                        .param("description", "Moosach - Fürstenried West"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/train-lines"));
        assertEquals(1, trainLineRepository.count());
        TrainLine line = trainLineRepository.findAll().getFirst();

        // 8. TrainStation2 via modal with empty id
        mockMvc.perform(post("/train-stations")
                        .with(csrf())
                        .param("id", "")
                        .param("ds100", "MMOP")
                        .param("name", "München Moosach")
                        .param("lineId", line.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/train-stations"));
        assertEquals(1, trainStation2Repository.count());
    }
}


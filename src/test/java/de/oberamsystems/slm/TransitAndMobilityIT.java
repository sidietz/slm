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
}

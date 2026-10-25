package de.oberamsystems.slm;

import de.oberamsystems.slm.mcp.McpDroneTools;
import de.oberamsystems.slm.model.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class DroneIT extends AbstractIntegrationTest {

    @Autowired
    private DroneRepository droneRepository;

    @Autowired
    private DroneFlightRepository droneFlightRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private McpDroneTools mcpDroneTools;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Transactional
    void testCreateDroneAndAllFiveCategories() {
        Manufacturer dji = manufacturerRepository.save(new Manufacturer(null, "DJI Innovations", "Shenzhen", LocalDate.of(2006, 11, 6)));
        Vendor amazon = vendorRepository.save(new Vendor(null, "Amazon"));

        for (DroneCategory category : DroneCategory.values()) {
            Drone drone = new Drone("Drone " + category.name(), "Model " + category.name(), category, 249, LocalDate.of(2026, 6, 1));
            drone.setManufacturer(dji);
            drone.setVendor(amazon);
            drone.setSerialNumber("SN-" + category.name() + "-12345");
            drone.setUasOperatorId("DEU987654321");
            drone.setPurchasePrice(BigDecimal.valueOf(899.99));
            drone.setNotes("Category test");

            Drone saved = droneRepository.save(drone);
            assertNotNull(saved.getId(), "Drone ID must be generated (UUIDv7)");
            assertEquals(category, saved.getCategory());
        }

        List<Drone> all = droneRepository.findAllByOrderByNameAsc();
        assertTrue(all.size() >= 5);
    }

    @Test
    @Transactional
    void testDroneFlightGeneratedDurationAndAirDistance() {
        Drone drone = new Drone("Flight Test Mini", "Mini 4", DroneCategory.C0, 249, LocalDate.of(2026, 4, 15));
        drone = droneRepository.save(drone);

        OffsetDateTime start = toOffset(2026, 10, 25, 14, 0);
        OffsetDateTime end = toOffset(2026, 10, 25, 14, 25);

        DroneFlight flight = new DroneFlight(drone, start, end);
        // Marienplatz (48.137154, 11.576124) -> Odeonsplatz (48.141756, 11.577239) ~518 meters
        flight.setStartLatitude(48.137154);
        flight.setStartLongitude(11.576124);
        flight.setLandingLatitude(48.141756);
        flight.setLandingLongitude(11.577239);
        flight.setMaxAltitudeMeters(115.0);
        flight.setNotes("Visual line of sight flight test");

        DroneFlight saved = droneFlightRepository.saveAndFlush(flight);
        entityManager.clear();

        DroneFlight reloaded = droneFlightRepository.findById(saved.getId()).orElseThrow();

        assertNotNull(reloaded.getDuration(), "Generated interval duration must be calculated by PostgreSQL");
        assertEquals(Duration.ofMinutes(25), reloaded.getDuration());

        assertNotNull(reloaded.getAirDistance(), "PostGIS geodesic air distance must be generated");
        assertTrue(reloaded.getAirDistance() > 500.0 && reloaded.getAirDistance() < 550.0,
                "Expected geodesic distance between Marienplatz and Odeonsplatz to be ~518m, was: " + reloaded.getAirDistance());
        assertEquals(115.0, reloaded.getMaxAltitudeMeters());
    }

    @Test
    @Transactional
    void testWebEndpoints() throws Exception {
        Drone drone = droneRepository.save(new Drone("Web Test Drone", "Spark", DroneCategory.C0, 245, LocalDate.of(2026, 5, 1)));

        // 1. GET /drones
        mockMvc.perform(get("/drones"))
                .andExpect(status().isOk())
                .andExpect(view().name("drones"))
                .andExpect(model().attributeExists("drones", "categories"))
                .andExpect(content().string(containsString("Web Test Drone")));

        // 2. GET /drone-flights
        mockMvc.perform(get("/drone-flights"))
                .andExpect(status().isOk())
                .andExpect(view().name("drone-flights"))
                .andExpect(model().attributeExists("flights", "drones"));

        // 3. POST /drones
        mockMvc.perform(post("/drones")
                        .with(csrf())
                        .param("name", "Avata 2")
                        .param("modelName", "Avata 2 FPV")
                        .param("category", "C1")
                        .param("weightGrams", "377")
                        .param("buyDate", "2026-07-20")
                        .param("purchasePrice", "999.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/drones"));

        // 4. POST /drone-flights
        mockMvc.perform(post("/drone-flights")
                        .with(csrf())
                        .param("droneId", drone.getId().toString())
                        .param("start", "2026-10-25T15:00")
                        .param("end", "2026-10-25T15:20")
                        .param("startLatitude", "48.137")
                        .param("startLongitude", "11.576")
                        .param("landingLatitude", "48.137")
                        .param("landingLongitude", "11.576")
                        .param("maxAltitudeMeters", "50.0")
                        .param("notes", "Roundtrip garden test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/drone-flights"));
    }

    @Test
    @Transactional
    void testMcpDroneTools() {
        Map<String, Object> reg = mcpDroneTools.registerDrone(
                "Mcp Drone", "C0", 249, "Mini 3", "2026-08-01", "SN9999", "DEU555", 450.0, "MCP test");
        assertEquals("SUCCESS", reg.get("status"));
        UUID droneId = UUID.fromString((String) reg.get("id"));

        List<Map<String, Object>> drones = mcpDroneTools.listDrones("C0");
        assertFalse(drones.isEmpty());

        Map<String, Object> flightRes = mcpDroneTools.logDroneFlight(
                droneId.toString(),
                "2026-10-25T16:00",
                "2026-10-25T16:15",
                48.137,
                11.576,
                48.137,
                11.576,
                80.0,
                "Autonomous inspection"
        );
        assertEquals("SUCCESS", flightRes.get("status"));

        List<Map<String, Object>> flights = mcpDroneTools.listDroneFlights(droneId.toString(), 10);
        assertEquals(1, flights.size());
        assertEquals("Autonomous inspection", flights.get(0).get("notes"));
    }
}

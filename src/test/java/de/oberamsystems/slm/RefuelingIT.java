package de.oberamsystems.slm;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.CarRepository;
import de.oberamsystems.slm.model.ChargingPoint;
import de.oberamsystems.slm.model.ChargingPointRepository;
import de.oberamsystems.slm.model.ChargingStation;
import de.oberamsystems.slm.model.ChargingStationRepository;
import de.oberamsystems.slm.model.FuelType;
import de.oberamsystems.slm.model.FuelingType;
import de.oberamsystems.slm.model.GasStation;
import de.oberamsystems.slm.model.GasStationRepository;
import de.oberamsystems.slm.model.PlugType;
import de.oberamsystems.slm.model.Refueling;
import de.oberamsystems.slm.model.RefuelingRepository;
import de.oberamsystems.slm.model.StationRepository;

@Transactional
public class RefuelingIT extends AbstractIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private CarRepository carRepository;

	@Autowired
	private StationRepository stationRepository;

	@Autowired
	private GasStationRepository gasStationRepository;

	@Autowired
	private ChargingStationRepository chargingStationRepository;

	@Autowired
	private ChargingPointRepository chargingPointRepository;

	@Autowired
	private RefuelingRepository refuelingRepository;

	@Autowired
	private jakarta.persistence.EntityManager entityManager;

	@Autowired
	private de.oberamsystems.slm.model.ManufacturerRepository manufacturerRepository;

	@Autowired
	private de.oberamsystems.slm.model.VendorRepository vendorRepository;

	private MockMvc mockMvc;
	private Car testCar;

	@BeforeEach
	void setUp() {
		this.mockMvc = webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		de.oberamsystems.slm.model.Manufacturer manufacturer = new de.oberamsystems.slm.model.Manufacturer();
		manufacturer.setName("Volkswagen AG");
		manufacturer = manufacturerRepository.save(manufacturer);

		de.oberamsystems.slm.model.Vendor vendor = new de.oberamsystems.slm.model.Vendor();
		vendor.setName("Autohaus München");
		vendor = vendorRepository.save(vendor);

		testCar = new Car();
		testCar.setName("Golf VII GTD");
		testCar.setVin("WVWZZZAUZHP123456");
		testCar.setModelName("Golf VII");
		testCar.setManufacturer(manufacturer);
		testCar.setVendor(vendor);
		testCar.setPurchaseDate(LocalDate.of(2024, 1, 15));
		testCar = carRepository.save(testCar);
	}

	@Test
	void testRefuelingAndStationsCompleteLifecycle() throws Exception {
		// 1. Verify GET /stations
		mockMvc.perform(get("/stations"))
				.andExpect(status().isOk())
				.andExpect(view().name("stations"))
				.andExpect(model().attributeExists("gasStations", "chargingStations", "plugTypes"));

		// 2. Create Gas Station with TankenTanken ID
		UUID tankenTankenId = UUID.fromString("9123b96c-a68e-485e-b5e6-3095d42d55bc");
		mockMvc.perform(post("/stations/gas")
						.with(csrf())
						.param("name", "Aral Tankstelle")
						.param("brand", "Aral")
						.param("city", "München")
						.param("country", "DE")
						.param("tankentankenId", tankenTankenId.toString()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/stations?tab=gas"));

		List<GasStation> gasStations = gasStationRepository.findAllByOrderByNameAsc();
		assertFalse(gasStations.isEmpty());
		GasStation gasStation = gasStations.stream()
				.filter(g -> "Aral Tankstelle".equals(g.getName()))
				.findFirst()
				.orElseThrow();
		assertNotNull(gasStation.getId());
		assertEquals(tankenTankenId, gasStation.getTankentankenId());

		// 3. Create EV Charging Station with Multiple Charging Points & Plug Types
		mockMvc.perform(post("/stations/charging")
						.with(csrf())
						.param("name", "Ionity Hub")
						.param("brand", "Ionity")
						.param("operator", "Ionity GmbH")
						.param("city", "Holzkirchen")
						.param("country", "DE")
						.param("maxPowerKw", "400.0", "100.0", "100.0")
						.param("plugType", "CCS_TYPE2", "CCS_TYPE2", "CHADEMO")
						.param("pointCount", "2", "4", "1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/stations?tab=charging"));

		List<ChargingStation> chargingStations = chargingStationRepository.findAllByOrderByNameAsc();
		assertFalse(chargingStations.isEmpty());
		ChargingStation chargingStation = chargingStations.stream()
				.filter(c -> "Ionity Hub".equals(c.getName()))
				.findFirst()
				.orElseThrow();
		assertEquals(7, chargingStation.getChargingPoints().size());
		assertEquals(new BigDecimal("400.0"), chargingStation.getMaxPowerKw());
		assertEquals(3, chargingStation.getChargingPointGroups().size());
		assertTrue(chargingStation.getChargingPointGroupsJson().contains("400.0"));
		assertTrue(chargingStation.getChargingPointGroupsJson().contains("CHADEMO"));

		// 4. Log a manual Refueling linked to vehicle and gas station
		mockMvc.perform(post("/refuelings")
						.with(csrf())
						.param("carId", testCar.getId().toString())
						.param("stationId", gasStation.getId().toString())
						.param("fuelingDate", "2026-10-19T14:30")
						.param("liters", "45.50")
						.param("costPerLiter", "1.659")
						.param("totalCost", "75.48")
						.param("odometer", "155000.0")
						.param("tripDistance", "750.0")
						.param("fuelType", "DIESEL")
						.param("fuelingType", "FULL")
						.param("country", "DE"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/refuelings?carId=" + testCar.getId()));

		List<Refueling> refuelings = refuelingRepository.findByCarIdOrderByFuelingDateDesc(testCar.getId());
		assertEquals(1, refuelings.size());
		Refueling savedRefueling = refuelings.get(0);
		assertNotNull(savedRefueling.getId());
		assertEquals(gasStation.getId(), savedRefueling.getStation().getId());
		assertEquals(new BigDecimal("6.07"), savedRefueling.getCalculatedConsumption());

		// 5. Query Timeseries API GET /api/refuelings
		mockMvc.perform(get("/api/refuelings").param("carId", testCar.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].rawDate").value("2026-10-19"))
				.andExpect(jsonPath("$[0].liters").value(45.50))
				.andExpect(jsonPath("$[0].consumption").value(6.07));

		// 6. Test Spritmonitor CSV Import
		File sampleCsv = new File("1676970_fuelings.csv");
		assertTrue(sampleCsv.exists());
		try (FileInputStream fis = new FileInputStream(sampleCsv)) {
			MockMultipartFile multipartFile = new MockMultipartFile(
					"file", "1676970_fuelings.csv", "text/csv", fis.readAllBytes());

			mockMvc.perform(multipart("/refuelings/import")
							.file(multipartFile)
							.with(csrf())
							.param("carId", testCar.getId().toString()))
					.andExpect(status().is3xxRedirection())
					.andExpect(redirectedUrl("/refuelings?carId=" + testCar.getId()));
		}

		List<Refueling> afterImport = refuelingRepository.findByCarIdOrderByFuelingDateDesc(testCar.getId());
		assertEquals(9, afterImport.size(), "1 manual + 8 imported refuelings");

		// 7. Verify station deletion foreign key set null behavior
		UUID stationId = gasStation.getId();
		entityManager.detach(savedRefueling);

		mockMvc.perform(post("/stations/" + stationId + "/delete")
						.with(csrf())
						.param("tab", "gas"))
				.andExpect(status().is3xxRedirection());

		entityManager.flush();
		entityManager.clear();

		assertFalse(gasStationRepository.existsById(stationId));
		Refueling refreshed = refuelingRepository.findById(savedRefueling.getId()).orElseThrow();
		assertNull(refreshed.getStation(), "Station foreign key on refueling must be set to NULL upon station deletion");
	}

	@Test
	void testUpdateGasStationFlow() throws Exception {
		GasStation gs = new GasStation();
		gs.setName("Shell Station Süd");
		gs.setBrand("Shell");
		gs.setCity("München");
		gs.setCountry("DE");
		gs = gasStationRepository.saveAndFlush(gs);
		UUID stationId = gs.getId();

		mockMvc.perform(post("/stations/gas")
						.with(csrf())
						.param("id", stationId.toString())
						.param("name", "Shell Express Süd")
						.param("brand", "Shell Express")
						.param("city", "Unterhaching")
						.param("country", "DE"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/stations?tab=gas"));

		assertEquals(1, gasStationRepository.count());
		entityManager.clear();
		GasStation updated = gasStationRepository.findById(stationId).orElseThrow();
		assertEquals("Shell Express Süd", updated.getName());
		assertEquals("Shell Express", updated.getBrand());
		assertEquals("Unterhaching", updated.getCity());
	}

	@Test
	void testUpdateRefuelingFlow() throws Exception {
		GasStation gs = new GasStation();
		gs.setName("Total Energies");
		gs.setBrand("Total");
		gs.setCity("München");
		gs.setCountry("DE");
		gs = gasStationRepository.saveAndFlush(gs);

		Refueling r = new Refueling();
		r.setCar(testCar);
		r.setStation(gs);
		r.setFuelingDate(OffsetDateTime.of(2026, 10, 20, 11, 0, 0, 0, ZoneOffset.UTC));
		r.setLiters(new BigDecimal("50.00"));
		r.setTotalCost(new BigDecimal("85.00"));
		r.setTripDistance(new BigDecimal("800.0"));
		r.setFuelType(FuelType.DIESEL);
		r.setFuelingType(FuelingType.FULL);
		r.setCountry("DE");
		r.autoCalculateMetrics();
		r = refuelingRepository.saveAndFlush(r);
		UUID refuelingId = r.getId();

		mockMvc.perform(post("/refuelings")
						.with(csrf())
						.param("id", refuelingId.toString())
						.param("carId", testCar.getId().toString())
						.param("stationId", gs.getId().toString())
						.param("fuelingDate", "2026-10-20T11:30")
						.param("liters", "52.00")
						.param("totalCost", "88.40")
						.param("tripDistance", "820.0")
						.param("fuelType", "DIESEL")
						.param("fuelingType", "FULL")
						.param("country", "DE")
						.param("note", "Updated note"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/refuelings?carId=" + testCar.getId()));

		assertEquals(1, refuelingRepository.count());
		entityManager.clear();
		Refueling updated = refuelingRepository.findById(refuelingId).orElseThrow();
		assertEquals(new BigDecimal("52.00"), updated.getLiters());
		assertEquals(new BigDecimal("88.40"), updated.getTotalCost());
		assertEquals(new BigDecimal("820.00"), updated.getTripDistance());
		assertEquals("Updated note", updated.getNote());
		assertEquals(new BigDecimal("1.700"), updated.getCostPerLiter());
	}

	@Test
	void testCreateAndUpdateMultiPlugChargingStationFlow() throws Exception {
		// 1. Create multi-plug charging station: 2x CCS 400kW, 4x CCS 100kW, 1x CHAdeMO 100kW
		mockMvc.perform(post("/stations/charging")
						.with(csrf())
						.param("name", "Fastned Charging Hub")
						.param("brand", "Fastned")
						.param("operator", "Fastned B.V.")
						.param("street", "Autobahn A8")
						.param("city", "Holzkirchen")
						.param("country", "DE")
						.param("maxPowerKw", "400.0", "100.0", "100.0")
						.param("plugType", "CCS_TYPE2", "CCS_TYPE2", "CHADEMO")
						.param("pointCount", "2", "4", "1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/stations?tab=charging"));

		assertEquals(1, chargingStationRepository.count());
		ChargingStation created = chargingStationRepository.findAll().getFirst();
		assertEquals("Fastned Charging Hub", created.getName());
		assertEquals(7, created.getChargingPoints().size());
		assertEquals(new BigDecimal("400.0"), created.getMaxPowerKw());
		assertEquals(3, created.getChargingPointGroups().size());

		// 2. Verify GET /stations?tab=charging displays grouped badges and data-points
		mockMvc.perform(get("/stations").param("tab", "charging"))
				.andExpect(status().isOk())
				.andExpect(view().name("stations"))
				.andExpect(content().string(containsString("Fastned Charging Hub")))
				.andExpect(content().string(containsString("2x CCS Type 2 (400.0 kW)")))
				.andExpect(content().string(containsString("4x CCS Type 2 (100.0 kW)")))
				.andExpect(content().string(containsString("1x CHAdeMO (100.0 kW)")))
				.andExpect(content().string(containsString("data-points=")));

		// 3. Update charging station to new configuration: 6x CCS 350kW + 2x Type 2 22kW
		mockMvc.perform(post("/stations/charging")
						.with(csrf())
						.param("id", created.getId().toString())
						.param("name", "Fastned Hyper Hub")
						.param("brand", "Fastned")
						.param("operator", "Fastned B.V.")
						.param("city", "Holzkirchen")
						.param("country", "DE")
						.param("maxPowerKw", "350.0", "22.0")
						.param("plugType", "CCS_TYPE2", "TYPE_2")
						.param("pointCount", "6", "2"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/stations?tab=charging"));

		assertEquals(1, chargingStationRepository.count());
		entityManager.clear();
		ChargingStation updated = chargingStationRepository.findById(created.getId()).orElseThrow();
		assertEquals("Fastned Hyper Hub", updated.getName());
		assertEquals(8, updated.getChargingPoints().size());
		assertEquals(new BigDecimal("350.0"), updated.getMaxPowerKw());
		assertEquals(2, updated.getChargingPointGroups().size());

		// 4. Verify validation rejection when station name is empty
		mockMvc.perform(post("/stations/charging")
						.with(csrf())
						.param("name", ""))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/stations?tab=charging"))
				.andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash().attribute("errorMessage", "Station name is required."));
		assertEquals(1, chargingStationRepository.count());
	}
}

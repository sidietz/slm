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
import java.time.LocalDateTime;
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

		// 3. Create EV Charging Station with Charging Points
		mockMvc.perform(post("/stations/charging")
						.with(csrf())
						.param("name", "Ionity Hub")
						.param("brand", "Ionity")
						.param("operator", "Ionity GmbH")
						.param("city", "Holzkirchen")
						.param("country", "DE")
						.param("maxPowerKw", "350.0")
						.param("plugType", "CCS_TYPE2")
						.param("pointCount", "4"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/stations?tab=charging"));

		List<ChargingStation> chargingStations = chargingStationRepository.findAllByOrderByNameAsc();
		assertFalse(chargingStations.isEmpty());
		ChargingStation chargingStation = chargingStations.stream()
				.filter(c -> "Ionity Hub".equals(c.getName()))
				.findFirst()
				.orElseThrow();
		assertEquals(4, chargingStation.getChargingPoints().size());
		assertEquals(new BigDecimal("350.0"), chargingStation.getMaxPowerKw());
		assertEquals(PlugType.CCS_TYPE2, chargingStation.getChargingPoints().get(0).getPlugType());

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
}

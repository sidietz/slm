package de.oberamsystems.slm;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.CarExpense;
import de.oberamsystems.slm.model.CarExpenseRepository;
import de.oberamsystems.slm.model.CarExpenseType;
import de.oberamsystems.slm.model.CarRepository;
import de.oberamsystems.slm.model.FuelType;
import de.oberamsystems.slm.model.Manufacturer;
import de.oberamsystems.slm.model.ManufacturerRepository;
import de.oberamsystems.slm.model.Refueling;
import de.oberamsystems.slm.model.RefuelingRepository;
import de.oberamsystems.slm.model.Vendor;
import de.oberamsystems.slm.model.VendorRepository;

@Transactional
public class CarExpenseIT extends AbstractIntegrationTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private CarRepository carRepository;

	@Autowired
	private CarExpenseRepository carExpenseRepository;

	@Autowired
	private ManufacturerRepository manufacturerRepository;

	@Autowired
	private VendorRepository vendorRepository;

	@Autowired
	private RefuelingRepository refuelingRepository;

	private MockMvc mockMvc;
	private Car carA;
	private Car carB;

	@BeforeEach
	void setUp() {
		this.mockMvc = webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		Manufacturer m = new Manufacturer();
		m.setName("BMW AG");
		m = manufacturerRepository.save(m);

		Vendor v = new Vendor();
		v.setName("BMW Niederlassung");
		v = vendorRepository.save(v);

		carA = new Car();
		carA.setName("BMW i4 eDrive40");
		carA.setVin("WBA00000000000001");
		carA.setModelName("i4");
		carA.setManufacturer(m);
		carA.setVendor(v);
		carA.setPurchaseDate(LocalDate.now().minusYears(2));
		carA.setPurchasePrice(55000.0);
		carA.setResidualValue(28000.0);
		carA.setTargetSellDate(LocalDate.now().plusYears(3));
		carA.setPurchaseKilometers(10000L);
		carA = carRepository.save(carA);

		carB = new Car();
		carB.setName("BMW 320d Touring");
		carB.setVin("WBA00000000000002");
		carB.setModelName("320d");
		carB.setManufacturer(m);
		carB.setVendor(v);
		carB.setPurchaseDate(LocalDate.now().minusYears(2));
		carB.setPurchasePrice(42000.0);
		carB.setResidualValue(18000.0);
		carB.setTargetSellDate(LocalDate.now().plusYears(3));
		carB.setPurchaseKilometers(15000L);
		carB = carRepository.save(carB);
	}

	@Test
	public void testGetCarExpensesLogTab() throws Exception {
		mockMvc.perform(get("/car-expenses")
						.param("carId", carA.getId().toString())
						.param("tab", "LOG"))
				.andExpect(status().isOk())
				.andExpect(view().name("car-expenses"))
				.andExpect(model().attributeExists("cars"))
				.andExpect(model().attributeExists("expenseTypes"))
				.andExpect(model().attributeExists("expenses"))
				.andExpect(model().attribute("currentTab", "LOG"));
	}

	@Test
	public void testRecordCarExpenseDebitAndCredit() throws Exception {
		// 1. Post a debit expense (MAINTENANCE)
		mockMvc.perform(post("/car-expenses")
						.with(csrf())
						.param("carId", carA.getId().toString())
						.param("expenseType", "MAINTENANCE")
						.param("expenseDate", LocalDate.now().toString())
						.param("amount", "420.50")
						.param("odometer", "24500")
						.param("title", "Regular 2-Year Service Inspection")
						.param("note", "Brake fluid and cabin air filter replaced"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/car-expenses?carId=" + carA.getId()));

		List<CarExpense> expenses = carExpenseRepository.findByCarIdOrderByExpenseDateDesc(carA.getId());
		assertEquals(1, expenses.size());
		CarExpense debit = expenses.get(0);
		assertEquals(CarExpenseType.MAINTENANCE, debit.getExpenseType());
		assertEquals(new BigDecimal("420.50"), debit.getAmount());
		assertFalse(debit.isCredit());

		// 2. Post a credit expense (GHG_QUOTA / THG-Prämie)
		mockMvc.perform(post("/car-expenses")
						.with(csrf())
						.param("carId", carA.getId().toString())
						.param("expenseType", "GHG_QUOTA")
						.param("expenseDate", LocalDate.now().toString())
						.param("amount", "320.00")
						.param("odometer", "25000")
						.param("title", "THG-Prämie 2026")
						.param("note", "Elektrovorteil payout"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/car-expenses?carId=" + carA.getId()));

		expenses = carExpenseRepository.findByCarIdOrderByExpenseDateDesc(carA.getId());
		assertEquals(2, expenses.size());
		assertTrue(expenses.stream().anyMatch(e -> e.getExpenseType() == CarExpenseType.GHG_QUOTA && e.isCredit()));
	}

	@Test
	public void testDeleteCarExpense() throws Exception {
		CarExpense exp = new CarExpense(carA, CarExpenseType.PARKING_FEE, LocalDate.now(),
				new BigDecimal("21000"), new BigDecimal("15.00"), "Airport Parking", null);
		exp = carExpenseRepository.save(exp);
		UUID expId = exp.getId();
		assertNotNull(expId);

		mockMvc.perform(post("/car-expenses/" + expId + "/delete")
						.with(csrf())
						.param("carId", carA.getId().toString()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/car-expenses?carId=" + carA.getId()));

		assertFalse(carExpenseRepository.existsById(expId));
	}

	@Test
	public void testUpdateLifecycleParameters() throws Exception {
		LocalDate newTargetSellDate = LocalDate.of(2029, 6, 30);
		Double newResidualValue = 22500.0;

		mockMvc.perform(post("/car-expenses/cars/" + carA.getId() + "/parameters")
						.with(csrf())
						.param("targetSellDate", newTargetSellDate.toString())
						.param("residualValue", newResidualValue.toString()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/car-expenses?carId=" + carA.getId() + "&tab=ANALYTICS"));

		Car updated = carRepository.findById(carA.getId()).orElseThrow();
		assertEquals(newTargetSellDate, updated.getTargetSellDate());
		assertEquals(newResidualValue, updated.getResidualValue());
	}

	@Test
	public void testGetAnalyticsTabCalculations() throws Exception {
		// Log an expense
		CarExpense exp = new CarExpense(carA, CarExpenseType.INSURANCE, LocalDate.now(),
				new BigDecimal("25000"), new BigDecimal("750.00"), "Full Comprehensive", null);
		carExpenseRepository.save(exp);

		mockMvc.perform(get("/car-expenses")
						.param("carId", carA.getId().toString())
						.param("tab", "ANALYTICS"))
				.andExpect(status().isOk())
				.andExpect(view().name("car-expenses"))
				.andExpect(model().attributeExists("pricePer100Km"))
				.andExpect(model().attributeExists("totalNetTco"))
				.andExpect(model().attributeExists("realizedDepreciation"))
				.andExpect(model().attributeExists("netOperatingExpenses"))
				.andExpect(model().attribute("currentTab", "ANALYTICS"));
	}

	@Test
	public void testGetComparisonTabMultiVehicleTrajectory() throws Exception {
		mockMvc.perform(get("/car-expenses")
						.param("tab", "COMPARISON")
						.param("carAId", carA.getId().toString())
						.param("carBId", carB.getId().toString())
						.param("annualDistanceKm", "15000"))
				.andExpect(status().isOk())
				.andExpect(view().name("car-expenses"))
				.andExpect(model().attribute("currentTab", "COMPARISON"))
				.andExpect(model().attribute("annualDistanceKm", 15000))
				.andExpect(model().attributeExists("selectedCarA"))
				.andExpect(model().attributeExists("selectedCarB"))
				.andExpect(model().attributeExists("coeffA"))
				.andExpect(model().attributeExists("coeffB"))
				.andExpect(model().attribute("trajectoryA", hasSize(10)))
				.andExpect(model().attribute("trajectoryB", hasSize(10)));
	}

	@Test
	public void testComparisonWithElectricAndCombustionRefuelings() throws Exception {
		// Log Electric charging for carA
		Refueling rElec = new Refueling();
		rElec.setCar(carA);
		rElec.setFuelType(FuelType.ELECTRIC);
		rElec.setFuelingDate(java.time.LocalDateTime.now().minusDays(5));
		rElec.setLiters(new BigDecimal("75.00")); // 75 kWh
		rElec.setTotalCost(new BigDecimal("27.00")); // 0.36 €/kWh
		rElec.setTripDistance(new BigDecimal("400.00")); // 18.75 kWh/100km
		rElec.setOdometer(new BigDecimal("12000.00"));
		refuelingRepository.save(rElec);

		// Log Diesel refueling for carB
		Refueling rDiesel = new Refueling();
		rDiesel.setCar(carB);
		rDiesel.setFuelType(FuelType.DIESEL);
		rDiesel.setFuelingDate(java.time.LocalDateTime.now().minusDays(3));
		rDiesel.setLiters(new BigDecimal("50.00")); // 50 L
		rDiesel.setTotalCost(new BigDecimal("82.50")); // 1.65 €/L
		rDiesel.setTripDistance(new BigDecimal("800.00")); // 6.25 L/100km
		rDiesel.setOdometer(new BigDecimal("17000.00"));
		refuelingRepository.save(rDiesel);

		mockMvc.perform(get("/car-expenses")
						.param("tab", "COMPARISON")
						.param("carAId", carA.getId().toString())
						.param("carBId", carB.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(view().name("car-expenses"))
				.andExpect(model().attributeExists("coeffA", "coeffB"))
				.andExpect(content().string(containsString("kWh")))
				.andExpect(content().string(containsString("Electric (EV)")))
				.andExpect(content().string(containsString("Diesel")));
	}
}

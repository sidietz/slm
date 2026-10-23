package de.oberamsystems.slm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import de.oberamsystems.slm.controller.CarExpenseController.CarCoefficients;

public class CarExpenseModelTest {

	@Test
	public void testSpritmonitorEnumTaxonomyMapping() {
		assertEquals(23, CarExpenseType.values().length);

		assertEquals(CarExpenseType.MAINTENANCE, CarExpenseType.fromSpritmonitorId(1).orElseThrow());
		assertEquals(CarExpenseType.REPAIR, CarExpenseType.fromSpritmonitorId(2).orElseThrow());
		assertEquals(CarExpenseType.TIRE_CHANGE, CarExpenseType.fromSpritmonitorId(3).orElseThrow());
		assertEquals(CarExpenseType.OIL_CHANGE, CarExpenseType.fromSpritmonitorId(4).orElseThrow());
		assertEquals(CarExpenseType.INSURANCE, CarExpenseType.fromSpritmonitorId(5).orElseThrow());
		assertEquals(CarExpenseType.TAX, CarExpenseType.fromSpritmonitorId(6).orElseThrow());
		assertEquals(CarExpenseType.INSPECTION, CarExpenseType.fromSpritmonitorId(7).orElseThrow());
		assertEquals(CarExpenseType.TUNING, CarExpenseType.fromSpritmonitorId(8).orElseThrow());
		assertEquals(CarExpenseType.ACCESSORIES, CarExpenseType.fromSpritmonitorId(9).orElseThrow());
		assertEquals(CarExpenseType.PURCHASE_PRICE, CarExpenseType.fromSpritmonitorId(10).orElseThrow());
		assertEquals(CarExpenseType.OTHER, CarExpenseType.fromSpritmonitorId(11).orElseThrow());
		assertEquals(CarExpenseType.CARE, CarExpenseType.fromSpritmonitorId(12).orElseThrow());
		assertEquals(CarExpenseType.LEASING_RATE, CarExpenseType.fromSpritmonitorId(13).orElseThrow());
		assertEquals(CarExpenseType.REGISTRATION, CarExpenseType.fromSpritmonitorId(14).orElseThrow());
		assertEquals(CarExpenseType.FINANCING_RATE, CarExpenseType.fromSpritmonitorId(15).orElseThrow());
		assertEquals(CarExpenseType.REFUND, CarExpenseType.fromSpritmonitorId(16).orElseThrow());
		assertEquals(CarExpenseType.FINE, CarExpenseType.fromSpritmonitorId(17).orElseThrow());
		assertEquals(CarExpenseType.PARKING_FEE, CarExpenseType.fromSpritmonitorId(18).orElseThrow());
		assertEquals(CarExpenseType.TOLL, CarExpenseType.fromSpritmonitorId(19).orElseThrow());
		assertEquals(CarExpenseType.SPARE_PARTS, CarExpenseType.fromSpritmonitorId(20).orElseThrow());
		assertEquals(CarExpenseType.CHARGING_BASE_FEE, CarExpenseType.fromSpritmonitorId(21).orElseThrow());
		assertEquals(CarExpenseType.DIGITAL_SERVICES, CarExpenseType.fromSpritmonitorId(22).orElseThrow());
		assertEquals(CarExpenseType.GHG_QUOTA, CarExpenseType.fromSpritmonitorId(23).orElseThrow());

		// Test Credits vs Debits
		assertTrue(CarExpenseType.REFUND.isCredit());
		assertTrue(CarExpenseType.GHG_QUOTA.isCredit());
		assertFalse(CarExpenseType.MAINTENANCE.isCredit());
		assertFalse(CarExpenseType.INSURANCE.isCredit());
		assertFalse(CarExpenseType.TAX.isCredit());
		assertFalse(CarExpenseType.REPAIR.isCredit());
	}

	@Test
	public void testCarExpenseEntity() {
		Car car = new Car();
		car.setName("Tesla Model 3");

		CarExpense exp = new CarExpense(car, CarExpenseType.TIRE_CHANGE, LocalDate.now(),
				new BigDecimal("35000.00"), new BigDecimal("450.00"), "Winter Tires", "Nokian Tyres");

		assertEquals(car, exp.getCar());
		assertEquals(CarExpenseType.TIRE_CHANGE, exp.getExpenseType());
		assertEquals("Winter Tires", exp.getTitle());
		assertEquals("Nokian Tyres", exp.getNote());
		assertEquals(new BigDecimal("450.00"), exp.getAmount());
		assertFalse(exp.isCredit());

		exp.prePersist();
		assertNotNull(exp.getCreatedAt());

		CarExpense creditExp = new CarExpense(car, CarExpenseType.GHG_QUOTA, LocalDate.now(),
				new BigDecimal("35000.00"), new BigDecimal("280.00"), "THG Prämie 2026", null);
		assertTrue(creditExp.isCredit());
	}

	@Test
	public void testCarLifecycleParameters() {
		Car car = new Car();
		car.setPurchasePrice(30000.0);
		car.setTargetSellDate(LocalDate.of(2030, 10, 1));
		car.setResidualValue(12500.0);

		assertEquals(LocalDate.of(2030, 10, 1), car.getTargetSellDate());
		assertEquals(12500.0, car.getResidualValue());
	}

	@Test
	public void testCarCoefficientsTrajectoryCalculation() {
		// Purchase: 30000, Residual: 10000 over 5 years -> 4000/yr depreciation
		// Variable: 0.10 EUR/km * 10,000 km = 1000/yr running cost
		// Fixed: 600/yr, Credit: 100/yr -> Net fixed: 500/yr
		// Annual total cost = 4000 + 1000 + 500 = 5500/yr
		CarCoefficients coeff = new CarCoefficients(30000.0, 10000.0, 5.0, 0.10, 600.0, 100.0);

		double year1 = coeff.calculateTotalCost(1, 10000);
		assertEquals(5500.0, year1, 0.01);

		double year3 = coeff.calculateTotalCost(3, 10000);
		assertEquals(16500.0, year3, 0.01);

		double year5 = coeff.calculateTotalCost(5, 10000);
		assertEquals(27500.0, year5, 0.01);

		// Year 6: depreciation capped at totalDepreciation (20000)
		// 20000 + 6 * (1000 + 500) = 20000 + 9000 = 29000
		double year6 = coeff.calculateTotalCost(6, 10000);
		assertEquals(29000.0, year6, 0.01);
	}
}

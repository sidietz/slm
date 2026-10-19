package de.oberamsystems.slm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.DrivingStyle;
import de.oberamsystems.slm.model.FuelType;
import de.oberamsystems.slm.model.FuelingType;
import de.oberamsystems.slm.model.GasStation;
import de.oberamsystems.slm.model.GasStationRepository;
import de.oberamsystems.slm.model.Refueling;
import de.oberamsystems.slm.model.TireType;

public class SpritmonitorCsvServiceTest {

	private GasStationRepository gasStationRepository;
	private SpritmonitorCsvService csvService;
	private Car car;

	@BeforeEach
	public void setUp() {
		gasStationRepository = mock(GasStationRepository.class);
		when(gasStationRepository.findByNameIgnoreCaseAndCityIgnoreCase(any(), any()))
				.thenReturn(Optional.empty());
		when(gasStationRepository.save(any(GasStation.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		csvService = new SpritmonitorCsvService(gasStationRepository);

		car = new Car();
		car.setId(1L);
		car.setName("Test Car");
	}

	@Test
	public void testParseSampleSpritmonitorCsv() throws IOException {
		File csvFile = new File("1676970_fuelings.csv");
		assertTrue(csvFile.exists(), "Sample CSV file 1676970_fuelings.csv must exist in project root");

		try (FileInputStream fis = new FileInputStream(csvFile)) {
			List<Refueling> refuelings = csvService.parseCsv(fis, car);

			assertNotNull(refuelings);
			assertFalse(refuelings.isEmpty(), "Should have parsed refueling entries from sample CSV");
			assertEquals(8, refuelings.size(), "Sample CSV contains 8 data rows");

			// Check first record
			Refueling first = refuelings.get(0);
			assertNotNull(first.getFuelingDate());
			assertEquals(new BigDecimal("69982.00"), first.getOdometer());
			assertEquals(new BigDecimal("601.00"), first.getTripDistance());
			assertEquals(new BigDecimal("38.49"), first.getLiters());
			assertEquals(new BigDecimal("85.41"), first.getTotalCost());
			assertEquals("EUR", first.getCurrency());
			assertEquals(FuelingType.FULL, first.getFuelingType());
			assertEquals(TireType.WINTER, first.getTireType());
			assertEquals(DrivingStyle.NORMAL, first.getDrivingStyle());
			assertEquals(FuelType.SUPER_E10, first.getFuelType());
			assertEquals(new BigDecimal("6.40"), first.getCalculatedConsumption());
			assertEquals("DE", first.getCountry(), "Country 'D' should be normalized to ISO 3166-1 alpha-2 'DE'");
			assertNotNull(first.getStation());
			assertEquals("Orlen", first.getStation().getName());

			// Check cost per liter computation: 85.41 / 38.49 = 2.219
			assertEquals(new BigDecimal("2.219"), first.getCostPerLiter());

			// Check initial fueling record (row index 7, 28.10.2025)
			Refueling last = refuelings.get(7);
			assertEquals(new BigDecimal("65973.00"), last.getOdometer());
			assertEquals(new BigDecimal("15.93"), last.getLiters());
			assertEquals(new BigDecimal("25.95"), last.getTotalCost());
			assertEquals(FuelingType.INITIAL, last.getFuelingType(), "Code 3 should map to INITIAL fueling");
		}
	}
}

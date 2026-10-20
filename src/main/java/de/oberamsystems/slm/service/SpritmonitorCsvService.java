package de.oberamsystems.slm.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.DrivingStyle;
import de.oberamsystems.slm.model.FuelType;
import de.oberamsystems.slm.model.FuelingType;
import de.oberamsystems.slm.model.GasStation;
import de.oberamsystems.slm.model.GasStationRepository;
import de.oberamsystems.slm.model.Refueling;
import de.oberamsystems.slm.model.TireType;

@Service
public class SpritmonitorCsvService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private static final Map<String, String> COUNTRY_CODE_MAP = new HashMap<>();

	static {
		COUNTRY_CODE_MAP.put("D", "DE");
		COUNTRY_CODE_MAP.put("A", "AT");
		COUNTRY_CODE_MAP.put("CH", "CH");
		COUNTRY_CODE_MAP.put("F", "FR");
		COUNTRY_CODE_MAP.put("I", "IT");
		COUNTRY_CODE_MAP.put("PL", "PL");
		COUNTRY_CODE_MAP.put("CZ", "CZ");
		COUNTRY_CODE_MAP.put("NL", "NL");
		COUNTRY_CODE_MAP.put("BE", "BE");
		COUNTRY_CODE_MAP.put("LU", "LU");
		COUNTRY_CODE_MAP.put("DK", "DK");
		COUNTRY_CODE_MAP.put("GB", "GB");
		COUNTRY_CODE_MAP.put("UK", "GB");
		COUNTRY_CODE_MAP.put("E", "ES");
		COUNTRY_CODE_MAP.put("S", "SE");
	}

	private final GasStationRepository gasStationRepository;

	public SpritmonitorCsvService(GasStationRepository gasStationRepository) {
		this.gasStationRepository = gasStationRepository;
	}

	@Transactional
	public List<Refueling> parseCsv(InputStream inputStream, Car targetCar) throws IOException {
		List<Refueling> refuelings = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String headerLine = reader.readLine();
			if (headerLine == null) {
				return refuelings;
			}

			String line;
			int rowCounter = 0;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				List<String> tokens = parseCsvLine(line);
				if (tokens.size() < 5) {
					continue;
				}

				Refueling refueling = parseRefuelingRow(tokens, targetCar, rowCounter++);
				if (refueling != null) {
					refuelings.add(refueling);
				}
			}
		}
		return refuelings;
	}

	private Refueling parseRefuelingRow(List<String> tokens, Car targetCar, int rowIndex) {
		// Expected Headers:
		// 0: Datum, 1: Km-Stand, 2: Teil-Km, 3: Spritmenge, 4: Kosten, 5: Währung,
		// 6: Tankart, 7: Reifen, 8: Strecken, 9: Fahrweise, 10: Kraftstoff, 11: Bemerkung,
		// 12: Verbrauch, 13: BC-Verbrauch, 14: BC-Spritmenge, 15: BC-Geschwindigkeit,
		// 16: Tankstelle, 17: Land, 18: Großraum, 19: Ort, 20: Breitengrad, 21: Längengrad

		String rawDate = getToken(tokens, 0);
		if (rawDate == null || rawDate.isEmpty()) {
			return null;
		}

		LocalDate date;
		try {
			date = LocalDate.parse(rawDate, DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}

		// Use a noon timestamp or adjust slightly for ordering within same day
		LocalDateTime fuelingDateTime = date.atTime(LocalTime.of(12, 0).plusSeconds(rowIndex % 60));

		BigDecimal liters = parseGermanDecimal(getToken(tokens, 3));
		BigDecimal totalCost = parseGermanDecimal(getToken(tokens, 4));

		if (liters == null || totalCost == null) {
			return null;
		}

		Refueling refueling = new Refueling();
		refueling.setCar(targetCar);
		refueling.setFuelingDate(fuelingDateTime);
		refueling.setLiters(liters);
		refueling.setTotalCost(totalCost);

		refueling.setOdometer(parseGermanDecimal(getToken(tokens, 1)));
		refueling.setTripDistance(parseGermanDecimal(getToken(tokens, 2)));

		String currency = cleanString(getToken(tokens, 5));
		if (currency != null && currency.length() == 3) {
			refueling.setCurrency(currency);
		} else {
			refueling.setCurrency("EUR");
		}

		Integer tankartCode = parseInteger(getToken(tokens, 6));
		refueling.setFuelingType(FuelingType.fromSpritmonitorCode(tankartCode));

		Integer reifenCode = parseInteger(getToken(tokens, 7));
		refueling.setTireType(TireType.fromSpritmonitorCode(reifenCode));

		refueling.setRoutes(cleanString(getToken(tokens, 8)));

		Integer fahrweiseCode = parseInteger(getToken(tokens, 9));
		refueling.setDrivingStyle(DrivingStyle.fromSpritmonitorCode(fahrweiseCode));

		Integer kraftstoffCode = parseInteger(getToken(tokens, 10));
		refueling.setFuelType(FuelType.fromSpritmonitorCode(kraftstoffCode));

		refueling.setNote(cleanString(getToken(tokens, 11)));

		BigDecimal verbrauch = parseGermanDecimal(getToken(tokens, 12));
		refueling.setCalculatedConsumption(verbrauch);

		refueling.setBoardComputerConsumption(parseGermanDecimal(getToken(tokens, 13)));
		refueling.setBoardComputerQuantity(parseGermanDecimal(getToken(tokens, 14)));
		refueling.setBoardComputerSpeed(parseGermanDecimal(getToken(tokens, 15)));

		String stationName = cleanString(getToken(tokens, 16));
		String rawCountry = cleanString(getToken(tokens, 17));
		String city = cleanString(getToken(tokens, 19));

		String mappedCountry = mapToIsoCountry(rawCountry);
		refueling.setCountry(mappedCountry);
		refueling.setLocation(city);

		// Resolve or provision GasStation
		if (stationName != null && !stationName.isEmpty()) {
			GasStation station = resolveGasStation(stationName, city, mappedCountry);
			refueling.setStation(station);
		}

		refueling.autoCalculateMetrics();
		return refueling;
	}

	private GasStation resolveGasStation(String name, String city, String country) {
		if (city != null && !city.isEmpty()) {
			var existing = gasStationRepository.findByNameIgnoreCaseAndCityIgnoreCase(name, city);
			if (existing.isPresent()) {
				return existing.get();
			}
		}

		var existingByName = gasStationRepository.findByNameIgnoreCase(name);
		if (existingByName.isPresent()) {
			GasStation station = existingByName.get();
			if (station.getCity() == null && city != null) {
				station.setCity(city);
				if (station.getCountry() == null && country != null) {
					station.setCountry(country);
				}
				return gasStationRepository.save(station);
			}
			return station;
		}

		// Provision new GasStation
		GasStation newStation = new GasStation();
		newStation.setName(name);
		newStation.setBrand(name);
		newStation.setCity(city);
		if (country != null) {
			newStation.setCountry(country);
		}
		return gasStationRepository.save(newStation);
	}

	public String mapToIsoCountry(String rawCountry) {
		if (rawCountry == null || rawCountry.trim().isEmpty()) {
			return "DE";
		}
		String clean = rawCountry.trim().toUpperCase();
		if (COUNTRY_CODE_MAP.containsKey(clean)) {
			return COUNTRY_CODE_MAP.get(clean);
		}
		if (clean.matches("^[A-Z]{2}$")) {
			return clean;
		}
		return "DE";
	}

	public static List<String> parseCsvLine(String line) {
		List<String> tokens = new ArrayList<>();
		if (line == null) return tokens;

		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ';' && !inQuotes) {
				tokens.add(current.toString());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		tokens.add(current.toString());
		return tokens;
	}

	private static String getToken(List<String> tokens, int index) {
		if (index >= 0 && index < tokens.size()) {
			return tokens.get(index);
		}
		return null;
	}

	private static String cleanString(String val) {
		if (val == null) return null;
		String clean = val.trim();
		if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() >= 2) {
			clean = clean.substring(1, clean.length() - 1).trim();
		}
		return clean.isEmpty() ? null : clean;
	}

	private static BigDecimal parseGermanDecimal(String val) {
		String clean = cleanString(val);
		if (clean == null || clean.isEmpty()) return null;
		try {
			String normalized = clean.replace(",", ".");
			return new BigDecimal(normalized);
		} catch (Exception e) {
			return null;
		}
	}

	private static Integer parseInteger(String val) {
		String clean = cleanString(val);
		if (clean == null || clean.isEmpty()) return null;
		try {
			return Integer.valueOf(clean);
		} catch (Exception e) {
			return null;
		}
	}
}

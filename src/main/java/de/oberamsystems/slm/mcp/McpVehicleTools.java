package de.oberamsystems.slm.mcp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import de.oberamsystems.slm.config.TimeZoneConfig;
import de.oberamsystems.slm.converter.StringToOffsetDateTimeConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.CarExpense;
import de.oberamsystems.slm.model.CarExpenseRepository;
import de.oberamsystems.slm.model.CarExpenseType;
import de.oberamsystems.slm.model.CarRepository;
import de.oberamsystems.slm.model.ChargingPoint;
import de.oberamsystems.slm.model.ChargingStation;
import de.oberamsystems.slm.model.ChargingStationRepository;
import de.oberamsystems.slm.model.DrivingStyle;
import de.oberamsystems.slm.model.FuelType;
import de.oberamsystems.slm.model.FuelingType;
import de.oberamsystems.slm.model.GasStation;
import de.oberamsystems.slm.model.GasStationRepository;
import de.oberamsystems.slm.model.PlugType;
import de.oberamsystems.slm.model.Refueling;
import de.oberamsystems.slm.model.RefuelingRepository;
import de.oberamsystems.slm.model.Station;
import de.oberamsystems.slm.model.StationRepository;
import de.oberamsystems.slm.model.StationType;
import de.oberamsystems.slm.model.TireType;

@Service
public class McpVehicleTools {

	@Autowired
	private RefuelingRepository refuelingRepository;

	@Autowired
	private CarRepository carRepository;

	@Autowired
	private CarExpenseRepository carExpenseRepository;

	@Autowired
	private StationRepository stationRepository;

	@Autowired
	private GasStationRepository gasStationRepository;

	@Autowired
	private ChargingStationRepository chargingStationRepository;

	@Tool(description = "Retrieve vehicle refuelings history with fuel volume, costs, consumption, and gas stations")
	public List<Map<String, Object>> getRefuelings(
			@ToolParam(description = "Optional car ID to filter by. If omitted, returns refuelings for all vehicles") Long carId,
			@ToolParam(description = "Maximum number of records to return (defaults to 20)") Integer limit) {

		int max = (limit != null && limit > 0) ? limit : 20;
		List<Refueling> list = (carId != null)
				? refuelingRepository.findByCarIdOrderByFuelingDateDesc(carId)
				: refuelingRepository.findAllByOrderByFuelingDateDesc();

		List<Map<String, Object>> result = new ArrayList<>();
		for (int i = 0; i < list.size() && i < max; i++) {
			Refueling r = list.get(i);
			Map<String, Object> m = new HashMap<>();
			m.put("id", r.getId() != null ? r.getId().toString() : null);
			m.put("carId", r.getCar() != null ? r.getCar().getId() : null);
			m.put("carName", r.getCar() != null ? r.getCar().getName() : null);
			m.put("fuelingDate", r.getFuelingDate() != null ? r.getFuelingDate().toString() : null);
			m.put("liters", r.getLiters());
			m.put("costPerLiter", r.getCostPerLiter());
			m.put("totalCost", r.getTotalCost());
			m.put("odometer", r.getOdometer());
			m.put("tripDistance", r.getTripDistance());
			m.put("calculatedConsumption", r.getCalculatedConsumption());
			m.put("stationName", r.getStation() != null ? r.getStation().getName() : (r.getLocation() != null ? r.getLocation() : null));
			m.put("country", r.getCountry());
			m.put("fuelType", r.getFuelType() != null ? r.getFuelType().name() : null);
			m.put("fuelingType", r.getFuelingType() != null ? r.getFuelingType().name() : null);
			result.add(m);
		}
		return result;
	}

	@Tool(description = "Record a new vehicle refueling entry")
	public Map<String, Object> logRefueling(
			@ToolParam(description = "ID of the vehicle refueled") Long carId,
			@ToolParam(description = "Date and time of refueling in ISO-8601 format (e.g. 2026-10-19T14:30:00)") String dateTime,
			@ToolParam(description = "Amount of fuel in liters") Double liters,
			@ToolParam(description = "Optional total cost in currency (e.g. 75.50)") Double totalCost,
			@ToolParam(description = "Optional cost per liter in currency (e.g. 1.689)") Double costPerLiter,
			@ToolParam(description = "Optional odometer reading in kilometers") Double odometer,
			@ToolParam(description = "Optional trip distance in kilometers since last fueling") Double tripDistance,
			@ToolParam(description = "Optional fuel type (DIESEL, PREMIUM_DIESEL, SUPER_E10, SUPER_E5, SUPER_PLUS, LPG, CNG, ELECTRICITY, HYDROGEN, OTHER)") String fuelType,
			@ToolParam(description = "Optional fueling type (FULL, PARTIAL, FIRST)") String fuelingType,
			@ToolParam(description = "Optional gas station name or brand") String stationName,
			@ToolParam(description = "Optional ISO 3166-1 alpha-2 country code (e.g. DE, AT, IT)") String country) {

		Map<String, Object> resp = new HashMap<>();
		Car car = carRepository.findById(carId).orElse(null);
		if (car == null) {
			resp.put("error", "Vehicle with ID " + carId + " not found");
			return resp;
		}

		Refueling r = new Refueling();
		r.setCar(car);
		if (dateTime != null) {
			r.setFuelingDate(StringToOffsetDateTimeConverter.parse(dateTime));
		} else {
			r.setFuelingDate(OffsetDateTime.now(TimeZoneConfig.getZoneId()));
		}
		if (liters != null) {
			r.setLiters(BigDecimal.valueOf(liters));
		}
		if (totalCost != null) {
			r.setTotalCost(BigDecimal.valueOf(totalCost));
		}
		if (costPerLiter != null) {
			r.setCostPerLiter(BigDecimal.valueOf(costPerLiter));
		}
		if (odometer != null) {
			r.setOdometer(BigDecimal.valueOf(odometer));
		}
		if (tripDistance != null) {
			r.setTripDistance(BigDecimal.valueOf(tripDistance));
		}
		if (fuelType != null) {
			try {
				r.setFuelType(FuelType.valueOf(fuelType.toUpperCase()));
			} catch (IllegalArgumentException ignored) {
			}
		}
		if (fuelingType != null) {
			try {
				r.setFuelingType(FuelingType.valueOf(fuelingType.toUpperCase()));
			} catch (IllegalArgumentException ignored) {
			}
		}
		if (country != null && !country.isBlank()) {
			r.setCountry(country.trim().toUpperCase());
		}

		if (stationName != null && !stationName.isBlank()) {
			GasStation gs = gasStationRepository.findByNameIgnoreCase(stationName.trim())
					.orElseGet(() -> {
						GasStation newGs = new GasStation();
						newGs.setName(stationName.trim());
						newGs.setBrand(stationName.trim());
						if (r.getCountry() != null) {
							newGs.setCountry(r.getCountry());
						}
						return gasStationRepository.save(newGs);
					});
			r.setStation(gs);
		}

		r.autoCalculateMetrics();
		Refueling saved = refuelingRepository.save(r);

		resp.put("id", saved.getId().toString());
		resp.put("carName", car.getName());
		resp.put("fuelingDate", saved.getFuelingDate().toString());
		resp.put("liters", saved.getLiters());
		resp.put("totalCost", saved.getTotalCost());
		resp.put("calculatedConsumption", saved.getCalculatedConsumption());
		resp.put("status", "SUCCESS");
		return resp;
	}

	@Tool(description = "List fuel and EV charging stations")
	public List<Map<String, Object>> getStations(
			@ToolParam(description = "Optional filter by station type: GAS_STATION, CHARGING_STATION, H2_STATION, OTHER") String stationType,
			@ToolParam(description = "Optional ISO 3166-1 alpha-2 country code (e.g. DE, AT)") String country) {

		List<Station> stations;
		if (stationType != null && !stationType.isBlank()) {
			try {
				StationType st = StationType.valueOf(stationType.toUpperCase());
				stations = stationRepository.findByStationTypeOrderByNameAsc(st);
			} catch (IllegalArgumentException e) {
				stations = stationRepository.findAllByOrderByNameAsc();
			}
		} else if (country != null && !country.isBlank()) {
			stations = stationRepository.findByCountryOrderByNameAsc(country.trim().toUpperCase());
		} else {
			stations = stationRepository.findAllByOrderByNameAsc();
		}

		List<Map<String, Object>> result = new ArrayList<>();
		for (Station s : stations) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", s.getId().toString());
			m.put("name", s.getName());
			m.put("brand", s.getBrand());
			m.put("stationType", s.getStationType() != null ? s.getStationType().name() : null);
			m.put("city", s.getCity());
			m.put("country", s.getCountry());
			if (s instanceof GasStation gs) {
				m.put("tankentankenId", gs.getTankentankenId() != null ? gs.getTankentankenId().toString() : null);
			} else if (s instanceof ChargingStation cs) {
				m.put("operator", cs.getOperator());
				m.put("maxPowerKw", cs.getMaxPowerKw());
				m.put("pointsCount", cs.getChargingPoints() != null ? cs.getChargingPoints().size() : 0);
			}
			result.add(m);
		}
		return result;
	}

	@Tool(description = "Provision a new gas or EV charging station")
	public Map<String, Object> createStation(
			@ToolParam(description = "Station name (e.g. Shell Tankstelle or Ionity Charging Hub)") String name,
			@ToolParam(description = "Station type: GAS_STATION or CHARGING_STATION") String stationType,
			@ToolParam(description = "Optional brand (e.g. Shell, Aral, Ionity)") String brand,
			@ToolParam(description = "Optional operator for EV charging stations (e.g. EnBW, Tesla)") String operator,
			@ToolParam(description = "Optional city") String city,
			@ToolParam(description = "Optional ISO 3166-1 alpha-2 country code (e.g. DE)") String country,
			@ToolParam(description = "Optional TankenTanken UUID for gas stations") String tankentankenId) {

		Map<String, Object> resp = new HashMap<>();
		Station saved;
		if ("CHARGING_STATION".equalsIgnoreCase(stationType)) {
			ChargingStation cs = new ChargingStation();
			cs.setName(name);
			cs.setBrand(brand);
			cs.setOperator(operator);
			cs.setCity(city);
			if (country != null) cs.setCountry(country.trim().toUpperCase());
			saved = chargingStationRepository.save(cs);
		} else {
			GasStation gs = new GasStation();
			gs.setName(name);
			gs.setBrand(brand);
			gs.setCity(city);
			if (country != null) gs.setCountry(country.trim().toUpperCase());
			if (tankentankenId != null && !tankentankenId.isBlank()) {
				try {
					gs.setTankentankenId(UUID.fromString(tankentankenId.trim()));
				} catch (IllegalArgumentException ignored) {
				}
			}
			saved = gasStationRepository.save(gs);
		}

		resp.put("id", saved.getId().toString());
		resp.put("name", saved.getName());
		resp.put("stationType", saved.getStationType().name());
		resp.put("status", "SUCCESS");
		return resp;
	}

	@Tool(description = "Retrieve vehicle expense history across the 23 Spritmonitor categories (maintenance, repairs, tax, insurance, etc.)")
	public List<Map<String, Object>> getCarExpenses(
			@ToolParam(description = "Optional car ID to filter by. If omitted, returns expenses for all vehicles") Long carId,
			@ToolParam(description = "Optional category filter (e.g. MAINTENANCE, REPAIR, INSURANCE, TAX, etc.)") String expenseType,
			@ToolParam(description = "Maximum number of records to return (defaults to 25)") Integer limit) {

		int max = (limit != null && limit > 0) ? limit : 25;
		List<CarExpense> list = (carId != null)
				? carExpenseRepository.findByCarIdOrderByExpenseDateDesc(carId)
				: carExpenseRepository.findAllByOrderByExpenseDateDesc();

		List<Map<String, Object>> result = new ArrayList<>();
		for (CarExpense exp : list) {
			if (expenseType != null && !expenseType.isBlank()
					&& !exp.getExpenseType().name().equalsIgnoreCase(expenseType.trim())) {
				continue;
			}
			Map<String, Object> m = new HashMap<>();
			m.put("id", exp.getId() != null ? exp.getId().toString() : null);
			m.put("carId", exp.getCar() != null ? exp.getCar().getId() : null);
			m.put("carName", exp.getCar() != null ? exp.getCar().getName() : null);
			m.put("expenseDate", exp.getExpenseDate() != null ? exp.getExpenseDate().toString() : null);
			m.put("expenseType", exp.getExpenseType().name());
			m.put("categoryLabel", exp.getExpenseType().getDisplayName());
			m.put("amount", exp.getAmount());
			m.put("isCredit", exp.isCredit());
			m.put("odometer", exp.getOdometer());
			m.put("title", exp.getTitle());
			m.put("note", exp.getNote());
			result.add(m);
			if (result.size() >= max) {
				break;
			}
		}
		return result;
	}

	@Tool(description = "Log a car-related expense using the 23 Spritmonitor categories (e.g. MAINTENANCE, REPAIR, INSURANCE, TAX, REFUND, GHG_QUOTA)")
	public Map<String, Object> logCarExpense(
			@ToolParam(description = "ID of the vehicle") Long carId,
			@ToolParam(description = "Expense type from Spritmonitor taxonomy (e.g. MAINTENANCE, REPAIR, TIRE_CHANGE, OIL_CHANGE, INSURANCE, TAX, INSPECTION, CARE, REFUND, FINE, PARKING_FEE, TOLL, SPARE_PARTS, CHARGING_BASE_FEE, DIGITAL_SERVICES, GHG_QUOTA)") String expenseType,
			@ToolParam(description = "Expense amount in EUR (positive number)") Double amount,
			@ToolParam(description = "Expense date in YYYY-MM-DD format (defaults to today if omitted)") String expenseDate,
			@ToolParam(description = "Optional vehicle mileage (odometer in km)") Double odometer,
			@ToolParam(description = "Optional title / short description") String title,
			@ToolParam(description = "Optional detailed note or invoice reference") String note) {

		Map<String, Object> resp = new HashMap<>();
		Optional<Car> carOpt = carRepository.findById(carId);
		if (carOpt.isEmpty()) {
			resp.put("status", "ERROR");
			resp.put("message", "Vehicle not found with ID: " + carId);
			return resp;
		}

		CarExpenseType type;
		try {
			type = CarExpenseType.valueOf(expenseType.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			resp.put("status", "ERROR");
			resp.put("message", "Unknown expenseType: " + expenseType + ". Valid types: " + java.util.Arrays.toString(CarExpenseType.values()));
			return resp;
		}

		LocalDate date = (expenseDate != null && !expenseDate.isBlank())
				? LocalDate.parse(expenseDate.trim())
				: LocalDate.now();

		BigDecimal amountBd = BigDecimal.valueOf(amount != null ? amount : 0.0);
		BigDecimal odoBd = odometer != null ? BigDecimal.valueOf(odometer) : null;
		String t = (title != null && !title.isBlank()) ? title.trim() : type.getDisplayName();

		CarExpense exp = new CarExpense(carOpt.get(), type, date, odoBd, amountBd, t, note);
		CarExpense saved = carExpenseRepository.save(exp);

		resp.put("id", saved.getId().toString());
		resp.put("carId", carId);
		resp.put("expenseType", saved.getExpenseType().name());
		resp.put("amount", saved.getAmount());
		resp.put("isCredit", saved.isCredit());
		resp.put("status", "SUCCESS");
		return resp;
	}

	@Tool(description = "Update vehicle lifecycle parameters: target sell date and estimated residual value for TCO calculations")
	public Map<String, Object> updateVehicleLifecycleParameters(
			@ToolParam(description = "ID of the vehicle") Long carId,
			@ToolParam(description = "Expected future sell date in YYYY-MM-DD format") String targetSellDate,
			@ToolParam(description = "Expected residual value in EUR at target sell date") Double residualValue) {

		Map<String, Object> resp = new HashMap<>();
		Optional<Car> carOpt = carRepository.findById(carId);
		if (carOpt.isEmpty()) {
			resp.put("status", "ERROR");
			resp.put("message", "Vehicle not found with ID: " + carId);
			return resp;
		}

		Car car = carOpt.get();
		if (targetSellDate != null && !targetSellDate.isBlank()) {
			car.setTargetSellDate(LocalDate.parse(targetSellDate.trim()));
		}
		if (residualValue != null) {
			car.setResidualValue(residualValue);
		}
		carRepository.save(car);

		resp.put("carId", car.getId());
		resp.put("targetSellDate", car.getTargetSellDate() != null ? car.getTargetSellDate().toString() : null);
		resp.put("residualValue", car.getResidualValue());
		resp.put("status", "SUCCESS");
		return resp;
	}
}

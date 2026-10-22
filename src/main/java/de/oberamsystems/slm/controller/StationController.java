package de.oberamsystems.slm.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.oberamsystems.slm.model.ChargingPoint;
import de.oberamsystems.slm.model.ChargingPointRepository;
import de.oberamsystems.slm.model.ChargingStation;
import de.oberamsystems.slm.model.ChargingStationRepository;
import de.oberamsystems.slm.model.GasStation;
import de.oberamsystems.slm.model.GasStationRepository;
import de.oberamsystems.slm.model.PlugType;
import de.oberamsystems.slm.model.PointRepository;
import de.oberamsystems.slm.model.StationRepository;

@Controller
public class StationController {

	private static final Logger log = LoggerFactory.getLogger(StationController.class);

	private final StationRepository stationRepository;
	private final GasStationRepository gasStationRepository;
	private final ChargingStationRepository chargingStationRepository;
	private final ChargingPointRepository chargingPointRepository;
	private final PointRepository pointRepository;

	public StationController(StationRepository stationRepository,
							 GasStationRepository gasStationRepository,
							 ChargingStationRepository chargingStationRepository,
							 ChargingPointRepository chargingPointRepository,
							 PointRepository pointRepository) {
		this.stationRepository = stationRepository;
		this.gasStationRepository = gasStationRepository;
		this.chargingStationRepository = chargingStationRepository;
		this.chargingPointRepository = chargingPointRepository;
		this.pointRepository = pointRepository;
	}

	@GetMapping("/stations")
	public String listStations(@RequestParam(required = false, defaultValue = "gas") String tab, Model model) {
		model.addAttribute("activeTab", tab);
		model.addAttribute("gasStations", gasStationRepository.findAllByOrderByNameAsc());
		model.addAttribute("chargingStations", chargingStationRepository.findAllByOrderByNameAsc());
		model.addAttribute("plugTypes", PlugType.values());
		model.addAttribute("points", pointRepository.findAll());
		return "stations";
	}

	@PostMapping("/stations/gas")
	public String createGasStation(@RequestParam(required = false) UUID id,
								   @RequestParam String name,
								   @RequestParam(required = false) String brand,
								   @RequestParam(required = false) String street,
								   @RequestParam(required = false) String houseNumber,
								   @RequestParam(required = false) String postalCode,
								   @RequestParam(required = false) String city,
								   @RequestParam(required = false) String country,
								   @RequestParam(required = false) Double latitude,
								   @RequestParam(required = false) Double longitude,
								   @RequestParam(required = false) Long pointId,
								   @RequestParam(required = false) String tankentankenId,
								   @RequestParam(required = false) String note,
								   RedirectAttributes redirectAttributes) {
		try {
			GasStation station = (id != null) 
					? gasStationRepository.findById(id).orElseGet(GasStation::new) 
					: new GasStation();
			station.setName(name);
			station.setBrand(brand);
			station.setStreet(street);
			station.setHouseNumber(houseNumber);
			station.setPostalCode(postalCode);
			station.setCity(city);
			if (country != null && !country.isBlank()) {
				station.setCountry(country.trim().toUpperCase());
			}
			station.setLatitude(latitude);
			station.setLongitude(longitude);
			if (pointId != null) {
				pointRepository.findById(pointId).ifPresent(station::setPoint);
			} else {
				station.setPoint(null);
			}
			if (tankentankenId != null && !tankentankenId.isBlank()) {
				try {
					station.setTankentankenId(UUID.fromString(tankentankenId.trim()));
				} catch (IllegalArgumentException e) {
					log.warn("Invalid TankenTanken UUID format: {}", tankentankenId);
				}
			}
			station.setNote(note);
			gasStationRepository.save(station);
			redirectAttributes.addFlashAttribute("successMessage", id != null ? "Gas station updated successfully." : "Gas station created successfully.");
		} catch (Exception e) {
			log.error("Error creating/updating gas station", e);
			redirectAttributes.addFlashAttribute("errorMessage", "Error saving gas station: " + e.getMessage());
		}
		return "redirect:/stations?tab=gas";
	}

	@PostMapping("/stations/charging")
	public String createChargingStation(@RequestParam(required = false) UUID id,
										@RequestParam(required = false) String name,
										@RequestParam(required = false) String brand,
										@RequestParam(required = false) String operator,
										@RequestParam(required = false) String street,
										@RequestParam(required = false) String houseNumber,
										@RequestParam(required = false) String postalCode,
										@RequestParam(required = false) String city,
										@RequestParam(required = false) String country,
										@RequestParam(required = false) Double latitude,
										@RequestParam(required = false) Double longitude,
										@RequestParam(required = false) Long pointId,
										@RequestParam(required = false) String note,
										@RequestParam(required = false) List<BigDecimal> maxPowerKw,
										@RequestParam(required = false) List<PlugType> plugType,
										@RequestParam(required = false) List<Integer> pointCount,
										RedirectAttributes redirectAttributes) {
		if (name == null || name.isBlank()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Station name is required.");
			return "redirect:/stations?tab=charging";
		}
		try {
			ChargingStation station = (id != null)
					? chargingStationRepository.findById(id).orElseGet(ChargingStation::new)
					: new ChargingStation();
			station.setName(name.trim());
			station.setBrand(brand != null ? brand.trim() : null);
			station.setOperator(operator != null ? operator.trim() : null);
			station.setStreet(street != null ? street.trim() : null);
			station.setHouseNumber(houseNumber != null ? houseNumber.trim() : null);
			station.setPostalCode(postalCode != null ? postalCode.trim() : null);
			station.setCity(city != null ? city.trim() : null);
			if (country != null && !country.isBlank()) {
				station.setCountry(country.trim().toUpperCase());
			}
			station.setLatitude(latitude);
			station.setLongitude(longitude);
			if (pointId != null) {
				pointRepository.findById(pointId).ifPresent(station::setPoint);
			} else {
				station.setPoint(null);
			}
			station.setNote(note != null ? note.trim() : null);

			if (maxPowerKw != null && plugType != null) {
				station.getChargingPoints().clear();
				for (int i = 0; i < maxPowerKw.size() && i < plugType.size(); i++) {
					BigDecimal power = maxPowerKw.get(i);
					PlugType plug = plugType.get(i);
					if (power == null || power.compareTo(BigDecimal.ZERO) <= 0 || plug == null) {
						continue;
					}
					int count = (pointCount != null && i < pointCount.size() && pointCount.get(i) != null && pointCount.get(i) > 0)
							? Math.min(pointCount.get(i), 100)
							: 1;
					for (int c = 1; c <= count; c++) {
						ChargingPoint point = new ChargingPoint();
						point.setMaxPowerKw(power);
						point.setPlugType(plug);
						point.setPointIdentifier("Stall " + plug.getDisplayName() + " #" + c);
						station.addChargingPoint(point);
					}
				}
			}

			chargingStationRepository.save(station);
			redirectAttributes.addFlashAttribute("successMessage", id != null ? "Charging station updated successfully." : "Charging station created successfully.");
		} catch (Exception e) {
			log.error("Error creating/updating charging station", e);
			redirectAttributes.addFlashAttribute("errorMessage", "Error saving charging station: " + e.getMessage());
		}
		return "redirect:/stations?tab=charging";
	}

	@PostMapping("/stations/{id}/delete")
	public String deleteStation(@PathVariable UUID id,
								@RequestParam(required = false, defaultValue = "gas") String tab,
								RedirectAttributes redirectAttributes) {
		try {
			stationRepository.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Station deleted successfully.");
		} catch (Exception e) {
			log.error("Error deleting station {}", id, e);
			redirectAttributes.addFlashAttribute("errorMessage", "Error deleting station: " + e.getMessage());
		}
		return "redirect:/stations?tab=" + tab;
	}
}

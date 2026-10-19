package de.oberamsystems.slm.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.CarRepository;
import de.oberamsystems.slm.model.DrivingStyle;
import de.oberamsystems.slm.model.FuelType;
import de.oberamsystems.slm.model.FuelingType;
import de.oberamsystems.slm.model.GasStation;
import de.oberamsystems.slm.model.GasStationRepository;
import de.oberamsystems.slm.model.Refueling;
import de.oberamsystems.slm.model.RefuelingRepository;
import de.oberamsystems.slm.model.TireType;
import de.oberamsystems.slm.service.SpritmonitorCsvService;

@Controller
public class RefuelingController {

	private static final Logger log = LoggerFactory.getLogger(RefuelingController.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private final RefuelingRepository refuelingRepository;
	private final CarRepository carRepository;
	private final GasStationRepository gasStationRepository;
	private final SpritmonitorCsvService spritmonitorCsvService;

	public RefuelingController(RefuelingRepository refuelingRepository,
							   CarRepository carRepository,
							   GasStationRepository gasStationRepository,
							   SpritmonitorCsvService spritmonitorCsvService) {
		this.refuelingRepository = refuelingRepository;
		this.carRepository = carRepository;
		this.gasStationRepository = gasStationRepository;
		this.spritmonitorCsvService = spritmonitorCsvService;
	}

	@GetMapping("/refuelings")
	public String listRefuelings(@RequestParam(required = false) Long carId, Model model) {
		List<Car> cars = carRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
		List<Refueling> refuelings = (carId != null)
				? refuelingRepository.findByCarIdOrderByFuelingDateDesc(carId)
				: refuelingRepository.findAllByOrderByFuelingDateDesc();

		// Calculate KPIs
		BigDecimal totalCostSum = BigDecimal.ZERO;
		BigDecimal totalLitersSum = BigDecimal.ZERO;
		BigDecimal totalTripDistanceSum = BigDecimal.ZERO;
		int consumptionCount = 0;
		BigDecimal consumptionSum = BigDecimal.ZERO;

		for (Refueling r : refuelings) {
			if (r.getTotalCost() != null) {
				totalCostSum = totalCostSum.add(r.getTotalCost());
			}
			if (r.getLiters() != null) {
				totalLitersSum = totalLitersSum.add(r.getLiters());
			}
			if (r.getTripDistance() != null) {
				totalTripDistanceSum = totalTripDistanceSum.add(r.getTripDistance());
			}
			if (r.getCalculatedConsumption() != null) {
				consumptionSum = consumptionSum.add(r.getCalculatedConsumption());
				consumptionCount++;
			}
		}

		BigDecimal averagePricePerLiter = (totalLitersSum.compareTo(BigDecimal.ZERO) > 0)
				? totalCostSum.divide(totalLitersSum, 3, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;

		BigDecimal averageConsumption = (consumptionCount > 0)
				? consumptionSum.divide(BigDecimal.valueOf(consumptionCount), 2, RoundingMode.HALF_UP)
				: BigDecimal.ZERO;

		model.addAttribute("cars", cars);
		model.addAttribute("selectedCarId", carId);
		model.addAttribute("refuelings", refuelings);
		model.addAttribute("totalCostSum", totalCostSum.setScale(2, RoundingMode.HALF_UP));
		model.addAttribute("totalLitersSum", totalLitersSum.setScale(2, RoundingMode.HALF_UP));
		model.addAttribute("averagePricePerLiter", averagePricePerLiter);
		model.addAttribute("averageConsumption", averageConsumption);

		model.addAttribute("stations", gasStationRepository.findAllByOrderByNameAsc());
		model.addAttribute("fuelTypes", FuelType.values());
		model.addAttribute("fuelingTypes", FuelingType.values());
		model.addAttribute("drivingStyles", DrivingStyle.values());
		model.addAttribute("tireTypes", TireType.values());
		model.addAttribute("newRefueling", new Refueling());

		return "refuelings";
	}

	@GetMapping("/api/refuelings")
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getRefuelingsApi(@RequestParam(required = false) Long carId) {
		List<Refueling> refuelings = (carId != null)
				? refuelingRepository.findByCarIdOrderByFuelingDateAsc(carId)
				: refuelingRepository.findAllByOrderByFuelingDateAsc();

		List<Map<String, Object>> result = new ArrayList<>();
		for (Refueling r : refuelings) {
			Map<String, Object> map = new HashMap<>();
			map.put("id", r.getId() != null ? r.getId().toString() : "");
			map.put("date", r.getFuelingDate() != null ? r.getFuelingDate().format(DATE_FORMATTER) : "");
			map.put("rawDate", r.getFuelingDate() != null ? r.getFuelingDate().toLocalDate().toString() : "");
			map.put("consumption", r.getCalculatedConsumption());
			map.put("bcConsumption", r.getBoardComputerConsumption());
			map.put("tripDistance", r.getTripDistance());
			map.put("costPerLiter", r.getCostPerLiter());
			map.put("totalCost", r.getTotalCost());
			map.put("liters", r.getLiters());
			map.put("stationName", r.getStation() != null ? r.getStation().getName() : (r.getLocation() != null ? r.getLocation() : ""));
			map.put("carName", r.getCar() != null ? r.getCar().getName() : "");
			result.add(map);
		}
		return ResponseEntity.ok(result);
	}

	@PostMapping("/refuelings")
	public String createRefueling(@ModelAttribute Refueling refueling,
								  @RequestParam Long carId,
								  @RequestParam(required = false) UUID stationId,
								  @RequestParam(required = false) String quickStationName,
								  RedirectAttributes redirectAttributes) {
		Car car = carRepository.findById(carId).orElse(null);
		if (car == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Selected vehicle not found.");
			return "redirect:/refuelings";
		}
		refueling.setCar(car);

		if (stationId != null) {
			gasStationRepository.findById(stationId).ifPresent(refueling::setStation);
		} else if (quickStationName != null && !quickStationName.trim().isEmpty()) {
			GasStation quickStation = gasStationRepository.findByNameIgnoreCase(quickStationName.trim())
					.orElseGet(() -> {
						GasStation gs = new GasStation();
						gs.setName(quickStationName.trim());
						gs.setBrand(quickStationName.trim());
						if (refueling.getCountry() != null) {
							gs.setCountry(refueling.getCountry());
						}
						return gasStationRepository.save(gs);
					});
			refueling.setStation(quickStation);
		}

		refueling.autoCalculateMetrics();
		refuelingRepository.save(refueling);
		redirectAttributes.addFlashAttribute("successMessage", "Refueling logged successfully.");
		return "redirect:/refuelings?carId=" + carId;
	}

	@PostMapping("/refuelings/import")
	public String importCsv(@RequestParam("file") MultipartFile file,
							@RequestParam("carId") Long carId,
							RedirectAttributes redirectAttributes) {
		Car car = carRepository.findById(carId).orElse(null);
		if (car == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Please select a valid vehicle for import.");
			return "redirect:/refuelings";
		}

		if (file.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Please choose a valid Spritmonitor CSV file.");
			return "redirect:/refuelings?carId=" + carId;
		}

		try {
			List<Refueling> parsed = spritmonitorCsvService.parseCsv(file.getInputStream(), car);
			if (parsed.isEmpty()) {
				redirectAttributes.addFlashAttribute("errorMessage", "No valid refueling records found in the uploaded CSV file.");
			} else {
				refuelingRepository.saveAll(parsed);
				redirectAttributes.addFlashAttribute("successMessage",
						"Successfully imported " + parsed.size() + " refuelings for " + car.getName() + ".");
			}
		} catch (Exception e) {
			log.error("Failed to parse Spritmonitor CSV: {}", e.getMessage(), e);
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to parse CSV file: " + e.getMessage());
		}

		return "redirect:/refuelings?carId=" + carId;
	}

	@PostMapping("/refuelings/{id}/delete")
	public String deleteRefueling(@PathVariable UUID id,
								  @RequestParam(required = false) Long carId,
								  RedirectAttributes redirectAttributes) {
		refuelingRepository.deleteById(id);
		redirectAttributes.addFlashAttribute("successMessage", "Refueling entry deleted successfully.");
		return carId != null ? "redirect:/refuelings?carId=" + carId : "redirect:/refuelings";
	}
}

package de.oberamsystems.slm.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.CarExpense;
import de.oberamsystems.slm.model.CarExpenseRepository;
import de.oberamsystems.slm.model.CarExpenseType;
import de.oberamsystems.slm.model.CarRepository;
import de.oberamsystems.slm.model.FuelType;
import de.oberamsystems.slm.model.Refueling;
import de.oberamsystems.slm.model.RefuelingRepository;

@Controller
public class CarExpenseController {

	private static final Logger log = LoggerFactory.getLogger(CarExpenseController.class);

	private final CarExpenseRepository expenseRepository;
	private final CarRepository carRepository;
	private final RefuelingRepository refuelingRepository;

	public CarExpenseController(CarExpenseRepository expenseRepository,
								CarRepository carRepository,
								RefuelingRepository refuelingRepository) {
		this.expenseRepository = expenseRepository;
		this.carRepository = carRepository;
		this.refuelingRepository = refuelingRepository;
	}

	@GetMapping("/car-expenses")
	public String getCarExpenses(@RequestParam(required = false) Long carId,
								 @RequestParam(required = false, defaultValue = "LOG") String tab,
								 @RequestParam(required = false) Long carAId,
								 @RequestParam(required = false) Long carBId,
								 @RequestParam(required = false, defaultValue = "10000") Integer annualDistanceKm,
								 Model model) {
		List<Car> cars = carRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
		model.addAttribute("cars", cars);
		model.addAttribute("expenseTypes", Arrays.asList(CarExpenseType.values()));
		model.addAttribute("currentTab", tab.toUpperCase());

		Car selectedCar = null;
		if (carId != null) {
			selectedCar = carRepository.findById(carId).orElse(null);
		}
		if (selectedCar == null && !cars.isEmpty()) {
			selectedCar = cars.get(0);
		}
		model.addAttribute("selectedCar", selectedCar);

		// Tab 1: Expense Log
		List<CarExpense> expenses;
		if (selectedCar != null) {
			expenses = expenseRepository.findByCarIdOrderByExpenseDateDesc(selectedCar.getId());
		} else {
			expenses = expenseRepository.findAllByOrderByExpenseDateDesc();
		}
		model.addAttribute("expenses", expenses);
		model.addAttribute("newExpense", new CarExpense());

		// Tab 2: Analytics & TCO
		if (selectedCar != null) {
			populateAnalytics(selectedCar, model);
		}

		// Tab 3: Comparison
		populateComparison(cars, carAId, carBId, annualDistanceKm, model);

		return "car-expenses";
	}

	@PostMapping("/car-expenses")
	public String saveCarExpense(@ModelAttribute CarExpense expense,
								 @RequestParam("carId") Long carId,
								 RedirectAttributes redirectAttributes) {
		Car car = carRepository.findById(carId).orElse(null);
		if (car == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vehicle is required.");
			return "redirect:/car-expenses";
		}
		if (expense.getExpenseType() == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Expense type is required.");
			return "redirect:/car-expenses?carId=" + carId;
		}
		if (expense.getExpenseDate() == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Expense date is required.");
			return "redirect:/car-expenses?carId=" + carId;
		}
		if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "Valid positive amount is required.");
			return "redirect:/car-expenses?carId=" + carId;
		}
		if (expense.getTitle() == null || expense.getTitle().trim().isEmpty()) {
			expense.setTitle(expense.getExpenseType().getDisplayName());
		}

		expense.setCar(car);
		expenseRepository.save(expense);
		redirectAttributes.addFlashAttribute("successMessage", "Expense recorded successfully.");
		return "redirect:/car-expenses?carId=" + carId;
	}

	@PostMapping("/car-expenses/{id}/delete")
	public String deleteCarExpense(@PathVariable UUID id,
								   @RequestParam(required = false) Long carId,
								   RedirectAttributes redirectAttributes) {
		if (expenseRepository.existsById(id)) {
			expenseRepository.deleteById(id);
			redirectAttributes.addFlashAttribute("successMessage", "Expense entry deleted.");
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Expense not found.");
		}
		return "redirect:/car-expenses" + (carId != null ? "?carId=" + carId : "");
	}

	@PostMapping("/car-expenses/cars/{carId}/parameters")
	public String updateCarLifecycleParameters(@PathVariable Long carId,
											   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate targetSellDate,
											   @RequestParam(required = false) Double residualValue,
											   RedirectAttributes redirectAttributes) {
		Car car = carRepository.findById(carId).orElse(null);
		if (car == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vehicle not found.");
			return "redirect:/car-expenses";
		}

		car.setTargetSellDate(targetSellDate);
		car.setResidualValue(residualValue);
		carRepository.save(car);

		redirectAttributes.addFlashAttribute("successMessage", "Vehicle lifecycle parameters updated.");
		return "redirect:/car-expenses?carId=" + carId + "&tab=ANALYTICS";
	}

	private void populateAnalytics(Car car, Model model) {
		List<CarExpense> carExpenses = expenseRepository.findByCarIdOrderByExpenseDateAsc(car.getId());
		List<Refueling> refuelings = refuelingRepository.findByCarIdOrderByFuelingDateAsc(car.getId());

		BigDecimal totalOperatingDebits = BigDecimal.ZERO;
		BigDecimal totalOperatingCredits = BigDecimal.ZERO;
		Map<CarExpenseType, BigDecimal> categoryTotals = new LinkedHashMap<>();
		for (CarExpenseType type : CarExpenseType.values()) {
			categoryTotals.put(type, BigDecimal.ZERO);
		}

		BigDecimal maxExpenseOdometer = BigDecimal.ZERO;
		for (CarExpense exp : carExpenses) {
			if (exp.isCredit()) {
				totalOperatingCredits = totalOperatingCredits.add(exp.getAmount());
			} else {
				totalOperatingDebits = totalOperatingDebits.add(exp.getAmount());
			}
			categoryTotals.put(exp.getExpenseType(), categoryTotals.get(exp.getExpenseType()).add(exp.getAmount()));
			if (exp.getOdometer() != null && exp.getOdometer().compareTo(maxExpenseOdometer) > 0) {
				maxExpenseOdometer = exp.getOdometer();
			}
		}
		BigDecimal netOperatingExpenses = totalOperatingDebits.subtract(totalOperatingCredits);

		BigDecimal totalFuelCost = BigDecimal.ZERO;
		BigDecimal maxFuelOdometer = BigDecimal.ZERO;
		for (Refueling r : refuelings) {
			if (r.getTotalCost() != null) {
				totalFuelCost = totalFuelCost.add(r.getTotalCost());
			}
			if (r.getOdometer() != null && r.getOdometer().compareTo(maxFuelOdometer) > 0) {
				maxFuelOdometer = r.getOdometer();
			}
		}

		BigDecimal purchaseOdo = BigDecimal.valueOf(car.getPurchaseKilometers());
		BigDecimal currentOdometer = purchaseOdo.max(maxFuelOdometer).max(maxExpenseOdometer);
		if (car.getSellKilometers() > 0 && BigDecimal.valueOf(car.getSellKilometers()).compareTo(currentOdometer) > 0) {
			currentOdometer = BigDecimal.valueOf(car.getSellKilometers());
		}
		BigDecimal distanceDriven = currentOdometer.subtract(purchaseOdo).max(BigDecimal.ZERO);

		// Depreciation calculation
		double purchasePrice = car.getPurchasePrice();
		double sellPrice = car.getSellPrice();
		double residualVal = car.getResidualValue() != null ? car.getResidualValue() : 0.0;
		LocalDate purchaseDate = car.getPurchaseDate() != null ? car.getPurchaseDate() : LocalDate.now().minusYears(1);
		LocalDate targetSellDate = car.getTargetSellDate();

		double totalDepreciationPotential;
		double realizedDepreciation;
		if (sellPrice > 0) {
			realizedDepreciation = Math.max(0.0, purchasePrice - sellPrice);
			totalDepreciationPotential = realizedDepreciation;
		} else {
			totalDepreciationPotential = Math.max(0.0, purchasePrice - residualVal);
			if (targetSellDate != null && targetSellDate.isAfter(purchaseDate)) {
				long totalLifespanDays = Math.max(1, ChronoUnit.DAYS.between(purchaseDate, targetSellDate));
				long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(purchaseDate, LocalDate.now()));
				double fractionElapsed = Math.min(1.0, (double) elapsedDays / totalLifespanDays);
				realizedDepreciation = totalDepreciationPotential * fractionElapsed;
			} else {
				realizedDepreciation = totalDepreciationPotential;
			}
		}

		BigDecimal realizedDepreciationBd = BigDecimal.valueOf(realizedDepreciation).setScale(2, RoundingMode.HALF_UP);
		BigDecimal totalNetTco = realizedDepreciationBd.add(totalFuelCost).add(netOperatingExpenses);

		// Per 100km calculations
		BigDecimal pricePer100Km = BigDecimal.ZERO;
		BigDecimal fuelPer100Km = BigDecimal.ZERO;
		BigDecimal depreciationPer100Km = BigDecimal.ZERO;
		BigDecimal operatingPer100Km = BigDecimal.ZERO;

		if (distanceDriven.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal factor100 = BigDecimal.valueOf(100);
			pricePer100Km = totalNetTco.multiply(factor100).divide(distanceDriven, 2, RoundingMode.HALF_UP);
			fuelPer100Km = totalFuelCost.multiply(factor100).divide(distanceDriven, 2, RoundingMode.HALF_UP);
			depreciationPer100Km = realizedDepreciationBd.multiply(factor100).divide(distanceDriven, 2, RoundingMode.HALF_UP);
			operatingPer100Km = netOperatingExpenses.multiply(factor100).divide(distanceDriven, 2, RoundingMode.HALF_UP);
		}

		model.addAttribute("totalOperatingDebits", totalOperatingDebits);
		model.addAttribute("totalOperatingCredits", totalOperatingCredits);
		model.addAttribute("netOperatingExpenses", netOperatingExpenses);
		model.addAttribute("totalFuelCost", totalFuelCost);
		model.addAttribute("realizedDepreciation", realizedDepreciationBd);
		model.addAttribute("totalNetTco", totalNetTco);
		model.addAttribute("currentOdometer", currentOdometer);
		model.addAttribute("distanceDriven", distanceDriven);
		model.addAttribute("pricePer100Km", pricePer100Km);
		model.addAttribute("fuelPer100Km", fuelPer100Km);
		model.addAttribute("depreciationPer100Km", depreciationPer100Km);
		model.addAttribute("operatingPer100Km", operatingPer100Km);
		model.addAttribute("categoryTotals", categoryTotals);
	}

	private void populateComparison(List<Car> cars, Long carAId, Long carBId, Integer annualDistanceKm, Model model) {
		if (cars.isEmpty()) {
			model.addAttribute("comparisonEnabled", false);
			return;
		}

		Car carA = null;
		Car carB = null;

		if (carAId != null) {
			carA = carRepository.findById(carAId).orElse(null);
		}
		if (carA == null && !cars.isEmpty()) {
			carA = cars.get(0);
		}

		if (carBId != null) {
			carB = carRepository.findById(carBId).orElse(null);
		}
		if (carB == null && cars.size() > 1) {
			carB = cars.get(1);
		} else if (carB == null) {
			carB = carA;
		}

		int distPerYear = (annualDistanceKm != null && annualDistanceKm > 0) ? annualDistanceKm : 10000;
		model.addAttribute("selectedCarA", carA);
		model.addAttribute("selectedCarB", carB);
		model.addAttribute("annualDistanceKm", distPerYear);
		model.addAttribute("comparisonEnabled", true);

		CarCoefficients coeffA = computeCarCoefficients(carA);
		CarCoefficients coeffB = computeCarCoefficients(carB);

		model.addAttribute("coeffA", coeffA);
		model.addAttribute("coeffB", coeffB);

		// Generate 10-year trajectory points for initial server render
		List<Double> trajectoryA = new ArrayList<>();
		List<Double> trajectoryB = new ArrayList<>();
		for (int year = 1; year <= 10; year++) {
			trajectoryA.add(coeffA.calculateTotalCost(year, distPerYear));
			trajectoryB.add(coeffB.calculateTotalCost(year, distPerYear));
		}
		model.addAttribute("trajectoryA", trajectoryA);
		model.addAttribute("trajectoryB", trajectoryB);

		// Calculate Break-Even Year if applicable
		Double breakEvenYear = calculateBreakEven(coeffA, coeffB, distPerYear);
		model.addAttribute("breakEvenYear", breakEvenYear);
	}

	private CarCoefficients computeCarCoefficients(Car car) {
		if (car == null) {
			return new CarCoefficients(0, 0, 5.0, 0.05, 500.0, 0.0);
		}

		double purchasePrice = car.getPurchasePrice();
		double residualValue = car.getResidualValue() != null ? car.getResidualValue() : 0.0;

		LocalDate purchaseDate = car.getPurchaseDate() != null ? car.getPurchaseDate() : LocalDate.now().minusYears(1);
		LocalDate targetSellDate = car.getTargetSellDate();
		double horizonYears = 5.0;
		if (targetSellDate != null && targetSellDate.isAfter(purchaseDate)) {
			long days = ChronoUnit.DAYS.between(purchaseDate, targetSellDate);
			horizonYears = Math.max(1.0, (double) days / 365.25);
		}

		// 1. Refuelings analysis: fuel type is strictly inferred from recorded refuelings, NEVER by vehicle name
		List<Refueling> refuelings = (car.getId() != null)
				? refuelingRepository.findByCarIdOrderByFuelingDateAsc(car.getId())
				: List.of();

		FuelType dominantFuelType = null;
		String fuelUnit = "L";
		double avgConsumption = 0.0;
		double avgPricePerUnit = 0.0;
		double variableFuelCostPerKm = 0.0;
		boolean hasRefuelings = false;

		if (!refuelings.isEmpty()) {
			hasRefuelings = true;
			Map<FuelType, Integer> fuelTypeCounts = new HashMap<>();
			double totalCostSum = 0.0;
			double totalUnitsSum = 0.0;
			double totalTripDistSum = 0.0;
			int consumptionCount = 0;
			double consumptionSum = 0.0;

			for (Refueling r : refuelings) {
				if (r.getFuelType() != null) {
					fuelTypeCounts.put(r.getFuelType(), fuelTypeCounts.getOrDefault(r.getFuelType(), 0) + 1);
				}
				if (r.getTotalCost() != null) {
					totalCostSum += r.getTotalCost().doubleValue();
				}
				if (r.getLiters() != null) {
					totalUnitsSum += r.getLiters().doubleValue();
				}
				if (r.getTripDistance() != null) {
					totalTripDistSum += r.getTripDistance().doubleValue();
				}
				if (r.getCalculatedConsumption() != null && r.getCalculatedConsumption().doubleValue() > 0) {
					consumptionSum += r.getCalculatedConsumption().doubleValue();
					consumptionCount++;
				}
			}

			dominantFuelType = fuelTypeCounts.entrySet().stream()
					.max(Map.Entry.comparingByValue())
					.map(Map.Entry::getKey)
					.orElse(FuelType.OTHER);

			boolean isElectric = (dominantFuelType == FuelType.ELECTRIC);
			fuelUnit = isElectric ? "kWh" : "L";

			// Weighted average price per unit (€/L or €/kWh)
			if (totalUnitsSum > 0.0) {
				avgPricePerUnit = totalCostSum / totalUnitsSum;
			} else {
				avgPricePerUnit = isElectric ? 0.38 : 1.75;
			}

			// Weighted average consumption (L/100km or kWh/100km)
			if (totalTripDistSum >= 50.0) {
				avgConsumption = (totalUnitsSum / totalTripDistSum) * 100.0;
			} else if (consumptionCount > 0) {
				avgConsumption = consumptionSum / consumptionCount;
			} else {
				avgConsumption = isElectric ? 18.5 : 7.2;
			}

			variableFuelCostPerKm = (avgConsumption / 100.0) * avgPricePerUnit;
		} else {
			// No refuelings recorded: strictly do NOT guess fuel type by vehicle name
			if (car.getPricePer100km() != null && car.getPricePer100km() > 0) {
				variableFuelCostPerKm = car.getPricePer100km() / 100.0;
				avgConsumption = 0.0;
				avgPricePerUnit = 0.0;
				dominantFuelType = FuelType.OTHER;
				fuelUnit = "L";
			} else {
				// Standard fleet fallback
				dominantFuelType = FuelType.OTHER;
				fuelUnit = "L";
				avgConsumption = 7.2;
				avgPricePerUnit = 1.75;
				variableFuelCostPerKm = (7.2 / 100.0) * 1.75;
			}
		}

		double fuelCostPer100Km = Math.round(variableFuelCostPerKm * 100.0 * 100.0) / 100.0;
		avgConsumption = Math.round(avgConsumption * 100.0) / 100.0;
		avgPricePerUnit = Math.round(avgPricePerUnit * 1000.0) / 1000.0;

		// 2. Operating expenses analysis
		List<CarExpense> expenses = expenseRepository.findByCarIdOrderByExpenseDateAsc(car.getId());
		double annualFixedCosts = 0.0;
		double variableExpenseCostPerKm = 0.0;
		double annualCredits = 0.0;

		long elapsedDays = Math.max(30, ChronoUnit.DAYS.between(purchaseDate, LocalDate.now()));
		double elapsedYears = Math.max(0.25, (double) elapsedDays / 365.25);

		double variableExpensesSum = 0.0;
		for (CarExpense exp : expenses) {
			double amt = exp.getAmount().doubleValue();
			CarExpenseType type = exp.getExpenseType();
			if (type.isCredit()) {
				annualCredits += (amt / elapsedYears);
			} else if (type == CarExpenseType.INSURANCE || type == CarExpenseType.TAX
					|| type == CarExpenseType.DIGITAL_SERVICES || type == CarExpenseType.CHARGING_BASE_FEE
					|| type == CarExpenseType.LEASING_RATE || type == CarExpenseType.FINANCING_RATE) {
				annualFixedCosts += (amt / elapsedYears);
			} else if (type == CarExpenseType.INSPECTION) {
				annualFixedCosts += (amt / Math.max(elapsedYears, 2.0)); // TÜV is biennial
			} else {
				variableExpensesSum += amt;
			}
		}

		// If historical distance exists, compute variable maintenance cost per km
		double totalRecordedKm = 0.0;
		if (!refuelings.isEmpty() && refuelings.get(refuelings.size() - 1).getOdometer() != null) {
			totalRecordedKm = Math.max(0.0, refuelings.get(refuelings.size() - 1).getOdometer().doubleValue() - car.getPurchaseKilometers());
		}
		if (totalRecordedKm > 500.0) {
			variableExpenseCostPerKm = variableExpensesSum / totalRecordedKm;
		} else {
			variableExpenseCostPerKm = 0.03; // default 3c/km maintenance & tires
		}

		double totalVariableCostPerKm = variableFuelCostPerKm + variableExpenseCostPerKm;
		if (annualFixedCosts <= 0.0) {
			annualFixedCosts = 600.0; // default standard insurance + tax
		}

		return new CarCoefficients(purchasePrice, residualValue, horizonYears, totalVariableCostPerKm, annualFixedCosts,
				annualCredits, dominantFuelType, fuelUnit, avgConsumption, avgPricePerUnit, variableFuelCostPerKm,
				fuelCostPer100Km, variableExpenseCostPerKm, hasRefuelings);
	}

	private Double calculateBreakEven(CarCoefficients a, CarCoefficients b, int annualDistance) {
		// Cost_A(t) = Dep_A(t) + t * (Var_A * dist + Fixed_A - Cred_A)
		// Cost_B(t) = Dep_B(t) + t * (Var_B * dist + Fixed_B - Cred_B)
		// Dep_i(t) = t * ( (Pur_i - Res_i) / Horizon_i )
		double slopeA = ((a.purchasePrice - a.residualValue) / a.horizonYears) + (a.variableCostPerKm * annualDistance) + a.fixedCostPerYear - a.annualCredits;
		double slopeB = ((b.purchasePrice - b.residualValue) / b.horizonYears) + (b.variableCostPerKm * annualDistance) + b.fixedCostPerYear - b.annualCredits;

		double costA1 = a.calculateTotalCost(1, annualDistance);
		double costB1 = b.calculateTotalCost(1, annualDistance);
		double costA10 = a.calculateTotalCost(10, annualDistance);
		double costB10 = b.calculateTotalCost(10, annualDistance);

		// If one starts lower and ends higher, there is an intersection between year 1 and 10
		if ((costA1 - costB1) * (costA10 - costB10) < 0) {
			// Find approximate intersection year
			double diffSlope = slopeB - slopeA;
			if (Math.abs(diffSlope) > 1e-4) {
				// CostA(0) approx: if initial capital difference
				double initDiff = (b.purchasePrice - a.purchasePrice) * 0.1; // small dampener
				double t = (costA1 - costB1) / (diffSlope);
				if (t >= 0.5 && t <= 10.0) {
					return Math.round(t * 10.0) / 10.0;
				}
			}
		}
		return null;
	}

	public static class CarCoefficients {
		private final double purchasePrice;
		private final double residualValue;
		private final double horizonYears;
		private final double variableCostPerKm;
		private final double fixedCostPerYear;
		private final double annualCredits;
		private final FuelType fuelType;
		private final String fuelUnit;
		private final double avgConsumption;
		private final double avgPricePerUnit;
		private final double fuelCostPerKm;
		private final double fuelCostPer100Km;
		private final double maintenanceCostPerKm;
		private final boolean hasRefuelings;

		public CarCoefficients(double purchasePrice, double residualValue, double horizonYears,
							   double variableCostPerKm, double fixedCostPerYear, double annualCredits) {
			this(purchasePrice, residualValue, horizonYears, variableCostPerKm, fixedCostPerYear, annualCredits,
					FuelType.OTHER, "L", 7.2, 1.75, 0.126, 12.6, 0.03, false);
		}

		public CarCoefficients(double purchasePrice, double residualValue, double horizonYears,
							   double variableCostPerKm, double fixedCostPerYear, double annualCredits,
							   FuelType fuelType, String fuelUnit, double avgConsumption, double avgPricePerUnit,
							   double fuelCostPerKm, double fuelCostPer100Km, double maintenanceCostPerKm,
							   boolean hasRefuelings) {
			this.purchasePrice = purchasePrice;
			this.residualValue = residualValue;
			this.horizonYears = Math.max(1.0, horizonYears);
			this.variableCostPerKm = variableCostPerKm;
			this.fixedCostPerYear = fixedCostPerYear;
			this.annualCredits = annualCredits;
			this.fuelType = fuelType != null ? fuelType : FuelType.OTHER;
			this.fuelUnit = fuelUnit != null ? fuelUnit : "L";
			this.avgConsumption = avgConsumption;
			this.avgPricePerUnit = avgPricePerUnit;
			this.fuelCostPerKm = fuelCostPerKm;
			this.fuelCostPer100Km = fuelCostPer100Km;
			this.maintenanceCostPerKm = maintenanceCostPerKm;
			this.hasRefuelings = hasRefuelings;
		}

		public double calculateTotalCost(int year, int annualDistanceKm) {
			double totalDepreciation = Math.max(0.0, purchasePrice - residualValue);
			double annualDepreciation = totalDepreciation / horizonYears;
			double depreciationAtYear = Math.min((double) year, horizonYears) * annualDepreciation;

			double runningCost = (double) year * (variableCostPerKm * annualDistanceKm);
			double fixedCost = (double) year * fixedCostPerYear;
			double credits = (double) year * annualCredits;

			double total = depreciationAtYear + runningCost + fixedCost - credits;
			return Math.round(total * 100.0) / 100.0;
		}

		public double getPurchasePrice() {
			return purchasePrice;
		}

		public double getResidualValue() {
			return residualValue;
		}

		public double getHorizonYears() {
			return horizonYears;
		}

		public double getVariableCostPerKm() {
			return variableCostPerKm;
		}

		public double getFixedCostPerYear() {
			return fixedCostPerYear;
		}

		public double getAnnualCredits() {
			return annualCredits;
		}

		public FuelType getFuelType() {
			return fuelType;
		}

		public String getFuelUnit() {
			return fuelUnit;
		}

		public double getAvgConsumption() {
			return avgConsumption;
		}

		public double getAvgPricePerUnit() {
			return avgPricePerUnit;
		}

		public double getFuelCostPerKm() {
			return fuelCostPerKm;
		}

		public double getFuelCostPer100Km() {
			return fuelCostPer100Km;
		}

		public double getMaintenanceCostPerKm() {
			return maintenanceCostPerKm;
		}

		public boolean isHasRefuelings() {
			return hasRefuelings;
		}
	}
}

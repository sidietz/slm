package de.oberamsystems.slm.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.CarRepository;
import de.oberamsystems.slm.model.CarTrip;
import de.oberamsystems.slm.model.CarTripRepository;
import de.oberamsystems.slm.model.Manufacturer;
import de.oberamsystems.slm.model.ManufacturerRepository;
import de.oberamsystems.slm.model.Point;
import de.oberamsystems.slm.model.PointRepository;
import de.oberamsystems.slm.model.Route;
import de.oberamsystems.slm.model.RouteRepository;
import de.oberamsystems.slm.model.Vendor;
import de.oberamsystems.slm.model.VendorRepository;


@Controller
public class RouteController {

	Logger log = LoggerFactory.getLogger(RouteController.class);
	
	@Autowired
	private RouteRepository repo;
	
	@Autowired
	private CarRepository carRepo;
	
	@Autowired
	private PointRepository pointRepo;
	
	@Autowired
	private CarTripRepository carTripRepo;
	
	@Autowired
	private ManufacturerRepository manufacturerRepo;
	
	@Autowired
	private VendorRepository vendorRepo;
	
	@GetMapping("/routes")
	public String addBooks(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("routes", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("points", pointRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		Route r = new Route();
		model.addAttribute("route", r);
		return "routes";
	}
	
	public String submitBooks(Route tt, Model model) {
		return submitBooks(tt, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/routes")
	public String submitBooks(@ModelAttribute Route tt, Model model, RedirectAttributes redirectAttributes) {
		if (tt.getStart() == null || tt.getStart().getId() == null || !pointRepo.existsById(tt.getStart().getId())
				|| tt.getEnd() == null || tt.getEnd().getId() == null || !pointRepo.existsById(tt.getEnd().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Start and end points are required.");
			return "redirect:/routes";
		}
		if (tt.getStart().getId() != null && tt.getEnd().getId() != null) {
			try {
				Double dist = repo.calculateAirDistance(tt.getStart().getId(), tt.getEnd().getId());
				if (dist != null) {
					tt.setAirDistance(dist);
				}
			} catch (Exception e) {
				log.warn("Could not calculate air distance via PostGIS: {}", e.getMessage());
			}
		}
		if (tt.getId() != null && (tt.getId() <= 0 || !repo.existsById(tt.getId()))) {
			tt.setId(null);
		}
		repo.save(tt);
		return "redirect:/routes";
	}
	
	@GetMapping("/points")
	public String addPoints(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("points", pointRepo.findAll(Sort.by(Sort.Direction.DESC, "isHome").and(Sort.by(Sort.Direction.ASC, "name"))));
		model.addAttribute("point", new Point());
		return "points";
	}
	
	@PostMapping("/points")
	public String submitPoints(@ModelAttribute Point a, Model model) {
		model.addAttribute("points", pointRepo.findAll(Sort.by(Sort.Direction.DESC, "isHome").and(Sort.by(Sort.Direction.ASC, "name"))));
		if (a.getId() != null && (a.getId() <= 0 || !pointRepo.existsById(a.getId()))) {
			a.setId(null);
		}
		pointRepo.save(a);
		model.addAttribute("point", a);
		return "redirect:/points";
	}
	
	
	@GetMapping("/car-trips")
	public String addCarTrips(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("cartrips", carTripRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("routes", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("cars", carRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("cartrip", new CarTrip());
		return "car-trips";
	}
	
	public String submitCarTrips(CarTrip rs, Model model) {
		return submitCarTrips(rs, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/car-trips")
	public String submitCarTrips(@ModelAttribute CarTrip rs, Model model, RedirectAttributes redirectAttributes) {
		if (rs.getCar() == null || rs.getCar().getId() == null || !carRepo.existsById(rs.getCar().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Car is required.");
			return "redirect:/car-trips";
		}
		if (rs.getRoute() == null || rs.getRoute().getId() == null || !repo.existsById(rs.getRoute().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Route is required.");
			return "redirect:/car-trips";
		}
		if (rs.getId() != null && (rs.getId() <= 0 || !carTripRepo.existsById(rs.getId()))) {
			rs.setId(null);
		}
		carTripRepo.save(rs);
		return "redirect:/car-trips";
	}
	
	@GetMapping("/cars")
	public String addCars(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("cars", carRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("manufacturers", manufacturerRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("vendors", vendorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("car", new Car());
		return "cars";
	}
	
	public String submitCars(Car rs, Model model) {
		return submitCars(rs, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/cars")
	public String submitCars(@ModelAttribute Car rs, Model model, RedirectAttributes redirectAttributes) {
		if (rs.getManufacturer() == null || rs.getManufacturer().getId() == null || !manufacturerRepo.existsById(rs.getManufacturer().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Manufacturer is required.");
			return "redirect:/cars";
		}
		if (rs.getVendor() == null || rs.getVendor().getId() == null || !vendorRepo.existsById(rs.getVendor().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vendor is required.");
			return "redirect:/cars";
		}
		if (rs.getId() != null && (rs.getId() <= 0 || !carRepo.existsById(rs.getId()))) {
			rs.setId(null);
		}
		carRepo.save(rs);
		return "redirect:/cars";
	}
}

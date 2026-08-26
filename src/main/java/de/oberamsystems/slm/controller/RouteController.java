package de.oberamsystems.slm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Car;
import de.oberamsystems.slm.model.CarRepository;
import de.oberamsystems.slm.model.CarTrip;
import de.oberamsystems.slm.model.CarTripRepository;
import de.oberamsystems.slm.model.ManufacturerRepository;
import de.oberamsystems.slm.model.Point;
import de.oberamsystems.slm.model.PointRepository;
import de.oberamsystems.slm.model.Route;
import de.oberamsystems.slm.model.RouteRepository;
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
		model.addAttribute("routes", repo.findAll());
		model.addAttribute("points", pointRepo.findAll());
		Route r = new Route();
		model.addAttribute("route", r);
		return "routes";
	}
	
	@PostMapping("/routes")
	public String submitBooks(@ModelAttribute Route tt, Model model) {
		model.addAttribute("routes", repo.findAll());
		model.addAttribute("points", pointRepo.findAll());
		repo.save(tt);
		model.addAttribute("route", tt);
		return "redirect:/routes";
	}
	
	@GetMapping("/points")
	public String addPoints(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("points", pointRepo.findAll());
		model.addAttribute("point", new Point());
		return "points";
	}
	
	@PostMapping("/points")
	public String submitPoints(@ModelAttribute Point a, Model model) {
		model.addAttribute("points", pointRepo.findAll());
		pointRepo.save(a);
		model.addAttribute("point", a);
		return "redirect:/points";
	}
	
	
	@GetMapping("/car-trips")
	public String addCarTrips(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("cartrips", carTripRepo.findAll());
		model.addAttribute("routes", repo.findAll());
		model.addAttribute("cars", carRepo.findAll());
		model.addAttribute("cartrip", new CarTrip());
		return "car-trips";
	}
	
	@PostMapping("/car-trips")
	public String submitCarTrips(@ModelAttribute CarTrip rs, Model model) {
		model.addAttribute("cartrips", carTripRepo.findAll());
		model.addAttribute("routes", repo.findAll());
		model.addAttribute("cars", carRepo.findAll());
		carTripRepo.save(rs);
		model.addAttribute("cartrip", rs);
		return "redirect:/car-trips";
	}
	
	@GetMapping("/cars")
	public String addCars(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("cars", carRepo.findAll());
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		model.addAttribute("vendors", vendorRepo.findAll());
		model.addAttribute("car", new Car());
		return "cars";
	}
	
	@PostMapping("/cars")
	public String submitCars(@ModelAttribute Car rs, Model model) {
		model.addAttribute("cars", repo.findAll());
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		model.addAttribute("vendors", vendorRepo.findAll());
		carRepo.save(rs);
		model.addAttribute("car", rs);
		return "redirect:/cars";
	}
}

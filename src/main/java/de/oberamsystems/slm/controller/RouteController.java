package de.oberamsystems.slm.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
	
	@PostMapping("/routes")
	public String submitBooks(@ModelAttribute Route tt, Model model) {
		model.addAttribute("routes", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("points", pointRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (tt.getStart() == null || tt.getStart().getId() == null) {
			List<Point> points = pointRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
			if (!points.isEmpty()) {
				tt.setStart(points.getFirst());
			}
		}
		if (tt.getEnd() == null || tt.getEnd().getId() == null) {
			List<Point> points = pointRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
			if (!points.isEmpty()) {
				tt.setEnd(points.getFirst());
			}
		}
		if (tt.getStart() != null && tt.getEnd() != null && tt.getStart().getId() != null && tt.getEnd().getId() != null) {
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
		model.addAttribute("route", tt);
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
	
	@PostMapping("/car-trips")
	public String submitCarTrips(@ModelAttribute CarTrip rs, Model model) {
		model.addAttribute("cartrips", carTripRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("routes", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("cars", carRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (rs.getCar() == null || rs.getCar().getId() == null) {
			List<Car> cars = carRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
			if (!cars.isEmpty()) {
				rs.setCar(cars.getFirst());
			}
		}
		if (rs.getRoute() == null || rs.getRoute().getId() == null) {
			List<Route> routes = repo.findAll(Sort.by(Sort.Direction.ASC, "name"));
			if (!routes.isEmpty()) {
				rs.setRoute(routes.getFirst());
			}
		}
		if (rs.getId() != null && (rs.getId() <= 0 || !carTripRepo.existsById(rs.getId()))) {
			rs.setId(null);
		}
		carTripRepo.save(rs);
		model.addAttribute("cartrip", rs);
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
	
	@PostMapping("/cars")
	public String submitCars(@ModelAttribute Car rs, Model model) {
		model.addAttribute("cars", carRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("manufacturers", manufacturerRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("vendors", vendorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (rs.getManufacturer() == null || rs.getManufacturer().getId() == null) {
			List<Manufacturer> mans = manufacturerRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
			if (!mans.isEmpty()) {
				rs.setManufacturer(mans.getFirst());
			}
		}
		if (rs.getVendor() == null || rs.getVendor().getId() == null) {
			List<Vendor> vendors = vendorRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
			if (!vendors.isEmpty()) {
				rs.setVendor(vendors.getFirst());
			}
		}
		if (rs.getId() != null && (rs.getId() <= 0 || !carRepo.existsById(rs.getId()))) {
			rs.setId(null);
		}
		carRepo.save(rs);
		model.addAttribute("car", rs);
		return "redirect:/cars";
	}
}

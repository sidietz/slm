package de.oberamsystems.slm.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Device;
import de.oberamsystems.slm.model.DeviceRepository;
import de.oberamsystems.slm.model.DeviceTypeRepository;
import de.oberamsystems.slm.model.Game;
import de.oberamsystems.slm.model.GameRepository;
import de.oberamsystems.slm.model.GamingSession;
import de.oberamsystems.slm.model.GamingSessionRepository;
import de.oberamsystems.slm.model.Manufacturer;
import de.oberamsystems.slm.model.ManufacturerRepository;
import de.oberamsystems.slm.model.Publisher;
import de.oberamsystems.slm.model.PublisherRepository;
import de.oberamsystems.slm.model.Studio;
import de.oberamsystems.slm.model.StudioRepository;
import de.oberamsystems.slm.model.Vendor;
import de.oberamsystems.slm.model.VendorRepository;

@Controller
public class GameController {

	Logger log = LoggerFactory.getLogger(GameController.class);
	
	@Autowired
	private GameRepository repo;
	
	@Autowired
	private PublisherRepository publisherRepo;
	
	@Autowired
	private StudioRepository studioRepo;
	
	@Autowired
	private GamingSessionRepository sessionRepo;
	
	@Autowired
	private ManufacturerRepository manufacturerRepo;

	@Autowired
	private DeviceRepository deviceRepo;

	@Autowired
	private DeviceTypeRepository deviceTypeRepo;
	
	@Autowired
	private VendorRepository vendorRepo;
	
	@GetMapping("/gaming-sessions")
	public String addGamingSessions(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("gamingsessions", sessionRepo.findAll());
		GamingSession gs = new GamingSession();
		model.addAttribute("devices", deviceRepo.findAll());
		model.addAttribute("games", repo.findAll());
		model.addAttribute("gamingsession", gs);
		return "gaming-sessions";
	}
	
	@PostMapping("/gaming-sessions")
	public String submitGamingSessions(@ModelAttribute GamingSession gs, Model model) {
		model.addAttribute("gamingsessions", sessionRepo.findAll());
		model.addAttribute("devices", deviceRepo.findAll());
		model.addAttribute("games", repo.findAll());
		sessionRepo.save(gs);
		model.addAttribute("gamingsession", gs);
		return "redirect:/gaming-sessions";
	}
	
	@GetMapping("/games")
	public String addGames(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("games", repo.findAll());
		model.addAttribute("studios", studioRepo.findAll());
		model.addAttribute("publishers", publisherRepo.findAll());
		Game g = new Game();
		model.addAttribute("game", g);
		return "games";
	}
	
	@PostMapping("/games")
	public String submitGames(@ModelAttribute Game g, Model model) {
		model.addAttribute("games", repo.findAll());
		model.addAttribute("studios", studioRepo.findAll());
		model.addAttribute("publishers", publisherRepo.findAll());
		repo.save(g);
		model.addAttribute("game", g);
		return "redirect:/games";
	}
	
	@GetMapping("/devices")
	public String addDevices(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("devices", deviceRepo.findAll());
		Device d = new Device();
		model.addAttribute("deviceTypes", deviceTypeRepo.findAll());
		model.addAttribute("vendors", vendorRepo.findAll());
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		model.addAttribute("device",d);
		return "devices";
	}
	
	@PostMapping("/devices")
	public String submitDevices(@ModelAttribute Device device, Model model) {
		model.addAttribute("devices", deviceRepo.findAll());
		model.addAttribute("deviceTypes", deviceTypeRepo.findAll());
		model.addAttribute("vendors", vendorRepo.findAll());
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		deviceRepo.save(device);
		model.addAttribute("device", device);
		return "redirect:/devices";
	}
	
	@GetMapping("/publishers")
	public String addPublishers(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("publishers", publisherRepo.findAll());
		Publisher p = new Publisher();
		model.addAttribute("publisher",p);
		return "publishers";
	}
	
	@PostMapping("/publishers")
	public String submitPublishers(@ModelAttribute Publisher p, Model model) {
		model.addAttribute("publishers", publisherRepo.findAll());
		publisherRepo.save(p);
		model.addAttribute("publisher", p);
		return "redirect:/publishers";
	}
	
	@GetMapping("/studios")
	public String addStudios(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("studios", studioRepo.findAll());
		Studio s = new Studio();
		model.addAttribute("studio",s);
		return "studios";
	}
	
	@PostMapping("/studios")
	public String submitStudios(@ModelAttribute Studio s, Model model) {
		model.addAttribute("studios", studioRepo.findAll());
		studioRepo.save(s);
		model.addAttribute("studio", s);
		return "redirect:/studios";
	}
	
	@GetMapping("/vendors")
	public String addVendors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("vendors", vendorRepo.findAll());
		Vendor v = new Vendor();
		model.addAttribute("vendor",v);
		return "vendors";
	}
	
	@PostMapping("/vendors")
	public String submitVendors(@ModelAttribute Vendor v, Model model) {
		model.addAttribute("vendors", vendorRepo.findAll());
		vendorRepo.save(v);
		model.addAttribute("vendor", v);
		return "redirect:/vendors";
	}
	
	@GetMapping("/manufacturers")
	public String addManufacturers(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		Manufacturer m = new Manufacturer();
		model.addAttribute("manufacturer", m);
		return "manufacturers";
	}
	
	@PostMapping("/manufacturers")
	public String submitManufacturers(@ModelAttribute Manufacturer m, Model model) {
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		manufacturerRepo.save(m);
		model.addAttribute("manufacturer", m);
		return "redirect:/manufacturers";
	}
}

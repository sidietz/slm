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

import de.oberamsystems.slm.model.Device;
import de.oberamsystems.slm.model.DeviceRepository;
import de.oberamsystems.slm.model.DeviceType;
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
		model.addAttribute("gamingsessions", sessionRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		GamingSession gs = new GamingSession();
		model.addAttribute("devices", deviceRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("games", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("gamingsession", gs);
		return "gaming-sessions";
	}
	
	public String submitGamingSessions(GamingSession gs, Model model) {
		return submitGamingSessions(gs, 0L, model);
	}

	@PostMapping("/gaming-sessions")
	public String submitGamingSessions(@ModelAttribute GamingSession gs,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes,
			Model model) {
		model.addAttribute("gamingsessions", sessionRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("devices", deviceRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("games", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		if (breakMinutes != null && breakMinutes > 0) {
			gs.setBreakDuration(java.time.Duration.ofMinutes(breakMinutes));
		}
		sessionRepo.save(gs);
		model.addAttribute("gamingsession", gs);
		return "redirect:/gaming-sessions";
	}
	
	@GetMapping("/games")
	public String addGames(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("games", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("studios", studioRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("publishers", publisherRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		Game g = new Game();
		model.addAttribute("game", g);
		return "games";
	}
	
	public String submitGames(Game g, Model model) {
		return submitGames(g, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/games")
	public String submitGames(@ModelAttribute Game g, Model model, RedirectAttributes redirectAttributes) {
		if (g.getPublisher() == null || g.getPublisher().getId() == null || !publisherRepo.existsById(g.getPublisher().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Publisher is required.");
			return "redirect:/games";
		}
		if (g.getStudio() == null || g.getStudio().getId() == null || !studioRepo.existsById(g.getStudio().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Studio is required.");
			return "redirect:/games";
		}
		if (g.getId() != null && (g.getId() <= 0 || !repo.existsById(g.getId()))) {
			g.setId(null);
		}
		repo.save(g);
		return "redirect:/games";
	}
	
	@GetMapping("/devices")
	public String addDevices(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("devices", deviceRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		Device d = new Device();
		model.addAttribute("deviceTypes", deviceTypeRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("vendors", vendorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("manufacturers", manufacturerRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("device",d);
		return "devices";
	}
	
	public String submitDevices(Device device, Model model) {
		return submitDevices(device, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/devices")
	public String submitDevices(@ModelAttribute Device device, Model model, RedirectAttributes redirectAttributes) {
		if (device.getDeviceType() == null || device.getDeviceType().getId() == null || !deviceTypeRepo.existsById(device.getDeviceType().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Device type is required.");
			return "redirect:/devices";
		}
		if (device.getVendor() == null || device.getVendor().getId() == null || !vendorRepo.existsById(device.getVendor().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Vendor is required.");
			return "redirect:/devices";
		}
		if (device.getManufacturer() == null || device.getManufacturer().getId() == null || !manufacturerRepo.existsById(device.getManufacturer().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Manufacturer is required.");
			return "redirect:/devices";
		}
		if (device.getId() != null && (device.getId() <= 0 || !deviceRepo.existsById(device.getId()))) {
			device.setId(null);
		}
		deviceRepo.save(device);
		return "redirect:/devices";
	}
	
	@GetMapping("/publishers")
	public String addPublishers(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("publishers", publisherRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		Publisher p = new Publisher();
		model.addAttribute("publisher",p);
		return "publishers";
	}
	
	@PostMapping("/publishers")
	public String submitPublishers(@ModelAttribute Publisher p, Model model) {
		model.addAttribute("publishers", publisherRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (p.getId() != null && (p.getId() <= 0 || !publisherRepo.existsById(p.getId()))) {
			p.setId(null);
		}
		publisherRepo.save(p);
		model.addAttribute("publisher", p);
		return "redirect:/publishers";
	}
	
	@GetMapping("/studios")
	public String addStudios(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("studios", studioRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		Studio s = new Studio();
		model.addAttribute("studio",s);
		return "studios";
	}
	
	@PostMapping("/studios")
	public String submitStudios(@ModelAttribute Studio s, Model model) {
		model.addAttribute("studios", studioRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (s.getId() != null && (s.getId() <= 0 || !studioRepo.existsById(s.getId()))) {
			s.setId(null);
		}
		studioRepo.save(s);
		model.addAttribute("studio", s);
		return "redirect:/studios";
	}
	
	@GetMapping("/vendors")
	public String addVendors(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("vendors", vendorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		Vendor v = new Vendor();
		model.addAttribute("vendor",v);
		return "vendors";
	}
	
	@PostMapping("/vendors")
	public String submitVendors(@ModelAttribute Vendor v, Model model) {
		model.addAttribute("vendors", vendorRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (v.getId() != null && (v.getId() <= 0 || !vendorRepo.existsById(v.getId()))) {
			v.setId(null);
		}
		vendorRepo.save(v);
		model.addAttribute("vendor", v);
		return "redirect:/vendors";
	}
	
	@GetMapping("/manufacturers")
	public String addManufacturers(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("manufacturers", manufacturerRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		Manufacturer m = new Manufacturer();
		model.addAttribute("manufacturer", m);
		return "manufacturers";
	}
	
	@PostMapping("/manufacturers")
	public String submitManufacturers(@ModelAttribute Manufacturer m, Model model) {
		model.addAttribute("manufacturers", manufacturerRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (m.getId() != null && (m.getId() <= 0 || !manufacturerRepo.existsById(m.getId()))) {
			m.setId(null);
		}
		manufacturerRepo.save(m);
		model.addAttribute("manufacturer", m);
		return "redirect:/manufacturers";
	}
}

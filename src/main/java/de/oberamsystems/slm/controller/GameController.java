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

	@GetMapping("/gaming-session")
	public String getGamingSession(Model model) {
		model.addAttribute("gamingsessions", sessionRepo.findAll());
		return "gaming-session";
	}
	
	@GetMapping("/add-gaming-session")
	public String addGamingSession(@RequestParam(required = false) Long id, Model model) {
		GamingSession gs = new GamingSession();
		model.addAttribute("devices", deviceRepo.findAll());
		model.addAttribute("games", repo.findAll());
		model.addAttribute("gamingsession", gs);
		return "add-gaming-session";
	}
	
	@PostMapping("/add-gaming-session")
	public String submitGamingSession(@ModelAttribute GamingSession gs, Model model) {
		model.addAttribute("devices", deviceRepo.findAll());
		model.addAttribute("games", repo.findAll());
		sessionRepo.save(gs);
		model.addAttribute("gamingsession", gs);
		return "add-gaming-session";
	}

	@GetMapping("/game")
	public String getGame(Model model) {
		model.addAttribute("games", repo.findAll());
		return "game";
	}
	
	@GetMapping("/add-game")
	public String addGame(@RequestParam(required = false) Long id, Model model) {
		Game a = new Game();
		model.addAttribute("studios", studioRepo.findAll());
		model.addAttribute("publishers", publisherRepo.findAll());
		model.addAttribute("game", a);
		return "add-game";
	}
	
	@PostMapping("/add-game")
	public String submitGame(@ModelAttribute Game game, Model model) {
		List<Studio> studios = studioRepo.findAll();
		List<Publisher> publishers = publisherRepo.findAll();
		model.addAttribute("studios", studios);
		model.addAttribute("publishers", publishers);
		repo.save(game);
		model.addAttribute("game", game);
		return "add-game";
	}

	@GetMapping("/device")
	public String getDevices(Model model) {
		model.addAttribute("devices", deviceRepo.findAll());
		return "device";
	}
	
	@GetMapping("/add-device")
	public String addDevice(@RequestParam(required = false) Long id, Model model) {
		Device d = new Device();
		model.addAttribute("deviceTypes", deviceTypeRepo.findAll());
		model.addAttribute("vendors", vendorRepo.findAll());
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		model.addAttribute("device",d);
		return "add-device";
	}
	
	@PostMapping("/add-device")
	public String submitDevice(@ModelAttribute Device device, Model model) {
		model.addAttribute("deviceTypes", deviceTypeRepo.findAll());
		model.addAttribute("vendors", vendorRepo.findAll());
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		deviceRepo.save(device);
		model.addAttribute("device", device);
		return "add-device";
	}
	
	@GetMapping("/studio")
	public String getStudios(Model model) {
		model.addAttribute("studios", studioRepo.findAll());
		return "studio";
	}
	
	@GetMapping("/add-studio")
	public String addStudio(@RequestParam(required = false) Long id, Model model) {
		Studio s = new Studio();
		model.addAttribute("studio",s);
		return "add-studio";
	}
	
	@PostMapping("/add-studio")
	public String submitStudio(@ModelAttribute Studio studio, Model model) {
		studioRepo.save(studio);
		model.addAttribute("studio", studio);
		return "add-studio";
	}
	
	@GetMapping("/publisher")
	public String getPublishers(Model model) {
		model.addAttribute("publishers", publisherRepo.findAll());
		return "publisher";
	}
	
	@GetMapping("/add-publisher")
	public String addPublisher(@RequestParam(required = false) Long id, Model model) {
		Publisher p = new Publisher();
		model.addAttribute("publisher",p);
		return "add-publisher";
	}
	
	@PostMapping("/add-publisher")
	public String submitPublisher(@ModelAttribute Publisher pub, Model model) {
		publisherRepo.save(pub);
		model.addAttribute("publisher", pub);
		return "add-publisher";
	}

	@GetMapping("/manufacturer")
	public String getmanufacturers(Model model) {
		model.addAttribute("manufacturers", manufacturerRepo.findAll());
		return "manufacturer";
	}
	
	@GetMapping("/add-manufacturer")
	public String addmanufacturer(@RequestParam(required = false) Long id, Model model) {
		Manufacturer f = new Manufacturer();
		model.addAttribute("manufacturer", f);
		return "add-manufacturer";
	}
	
	@PostMapping("/add-manufacturer")
	public String submitmanufacturer(@ModelAttribute Manufacturer manufacturer, Model model) {
		manufacturerRepo.save(manufacturer);
		model.addAttribute("manufacturer", manufacturer);
		return "add-manufacturer";
	}
}

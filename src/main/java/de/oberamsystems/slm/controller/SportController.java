package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.SportSession;
import de.oberamsystems.slm.model.SportSessionRepository;
import de.oberamsystems.slm.model.SportTypeRepository;

@Controller
public class SportController {

	Logger log = LoggerFactory.getLogger(SportController.class);
	
	@Autowired
	private SportSessionRepository repo;
	
	@Autowired
	private SportTypeRepository typeRepo;
	
	@GetMapping("/add-sport")
	public String addSport(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("sport", new SportSession());
		model.addAttribute("sports", typeRepo.findAll());
		log.warn(String.format("%d", id));
		return "add-sport";
	}
	
	@PostMapping("/add-sport")
	public String submitSport(@ModelAttribute SportSession sport, Model model) {
		model.addAttribute("sports", typeRepo.findAll());
		log.warn(sport.toString());
		repo.save(sport);
		model.addAttribute("sport", sport);
		return "add-sport";
	}
	
	
	
	
}

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
import de.oberamsystems.slm.model.SportType;
import de.oberamsystems.slm.model.SportTypeRepository;

@Controller
public class SportController {

	Logger log = LoggerFactory.getLogger(SportController.class);
	
	@Autowired
	private SportSessionRepository repo;
	
	@Autowired
	private SportTypeRepository typeRepo;
	
	@GetMapping("/sports")
	public String addSports(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("sports", repo.findAll());
		SportSession s = new SportSession();
		s.setStart(LocalDateTime.now());
		s.setEnd(LocalDateTime.now());
		model.addAttribute("sport", s);
		model.addAttribute("sportTypes", typeRepo.findAll());
		return "sports";
	}
	
	@PostMapping("/sports")
	public String submitSports(@ModelAttribute SportSession sport, Model model) {
		model.addAttribute("sports", repo.findAll());
		model.addAttribute("sportTypes", typeRepo.findAll());
		repo.save(sport);
		model.addAttribute("sport", sport);
		return "redirect:/sports";
	}
	
	@GetMapping("/sport-types")
	public String addSportType(@RequestParam(required = false) Long id, Model model) {
		SportType t = new SportType();
		model.addAttribute("type", t);
		model.addAttribute("types", typeRepo.findAll());
		return "sport-types";
	}
	
	@PostMapping("/sport-types")
	public String submitSportType(@ModelAttribute SportType type, Model model) {
		model.addAttribute("sports", typeRepo.findAll());
		typeRepo.save(type);
		model.addAttribute("type", type);
		return "redirect:/sport-types";
	}
}

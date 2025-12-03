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
	
	@GetMapping("/sport")
	public String getSport(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusDays(28) : fromDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		LocalDateTime minDate = repo.findMinDate();
	    LocalDateTime maxDate = repo.findMaxDate();

	    minDate = minDate == null ? LocalDateTime.now().minusDays(7) : minDate.minusDays(1); //fixes off by one bug
	    maxDate = maxDate == null ? LocalDateTime.now(): maxDate;

		model.addAttribute("sports", repo.findByStartBetween(fromDate, toDate.plusDays(1))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "sport";
	}
	
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
	
	@GetMapping("/add-sport")
	public String addSport(@RequestParam(required = false) Long id, Model model) {
		SportSession s = new SportSession();
		s.setStart(LocalDateTime.now());
		s.setEnd(LocalDateTime.now());
		model.addAttribute("sport", s);
		model.addAttribute("sports", typeRepo.findAll());
		return "add-sport";
	}
	
	@PostMapping("/add-sport")
	public String submitSport(@ModelAttribute SportSession sport, Model model) {
		model.addAttribute("sports", typeRepo.findAll());
		repo.save(sport);
		model.addAttribute("sport", sport);
		return "add-sport";
	}
}

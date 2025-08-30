package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.MeditationSession;
import de.oberamsystems.slm.model.MeditationSessionRepository;

@Controller
public class MeditationController {

	Logger log = LoggerFactory.getLogger(MeditationController.class);
	
	@Autowired
	private MeditationSessionRepository repo;
	
	@GetMapping({"/meditation", "meditation.html"})
	public String getMeditation(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusDays(28) : fromDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		LocalDateTime minDate = repo.findMinDate();
	    LocalDateTime maxDate = repo.findMaxDate();
	    
	    minDate = minDate == null ? LocalDateTime.now().minusDays(1) : minDate;
	    maxDate = maxDate == null ? LocalDateTime.now() : maxDate;

		model.addAttribute("meditations", repo.findByStartBetween(fromDate, toDate.plusDays(1))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "meditation";
	}
	
	@GetMapping("/add-meditation")
	public String addSport(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("meditation", new MeditationSession());
		log.warn(String.format("%d", id));
		return "add-meditation";
	}
	
	@PostMapping("/add-meditation")
	public String submitSport(@ModelAttribute MeditationSession meditation, Model model) {
		log.warn(meditation.toString());
		repo.save(meditation);
		model.addAttribute("meditation", meditation);
		return "add-meditation";
	}
	
	
	
	
}

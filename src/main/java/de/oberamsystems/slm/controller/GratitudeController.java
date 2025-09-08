package de.oberamsystems.slm.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Gratitude;
import de.oberamsystems.slm.model.GratitudeRepository;

@Controller
public class GratitudeController {

	Logger log = LoggerFactory.getLogger(GratitudeController.class);
	
	@Autowired
	private GratitudeRepository repo;
	
	@GetMapping({"/gratitude", "gratitude.html"})
	public String getEvent(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDate.now().minusDays(28) : fromDate;
		toDate = toDate == null ? repo.findMaxDate() : toDate;
		toDate = toDate == null ? LocalDate.now() : toDate;

		LocalDate minDate = repo.findMinDate();
	    LocalDate maxDate = repo.findMaxDate();
	    
	    minDate = minDate == null ? LocalDate.now().minusDays(1) : minDate;
	    maxDate = maxDate == null ? LocalDate.now() : maxDate;

		model.addAttribute("gratitudes", repo.findByDateAfter(fromDate, Sort.by("date"))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "gratitude";
	}
	
	@GetMapping("/add-gratitude")
	public String addEvent(@RequestParam(required = false) Long id, Model model) {
		Gratitude g = new Gratitude();
		g.setDate(LocalDate.now());
		model.addAttribute("gratitude", g);
		return "add-gratitude";
	}
	
	@PostMapping("/add-gratitude")
	public String submitEvent(@ModelAttribute Gratitude gratitude, Model model) {
		log.warn(gratitude.toString());
		repo.save(gratitude);
		model.addAttribute("gratitude", gratitude);
		return "add-gratitude";
	}
}

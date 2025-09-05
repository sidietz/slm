package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Activity;
import de.oberamsystems.slm.model.ActivityRepository;
import de.oberamsystems.slm.model.ActivityTypeRepository;

@Controller
public class ActivityController {

	Logger log = LoggerFactory.getLogger(ActivityController.class);
	
	@Autowired
	private ActivityRepository repo;
	
	@Autowired
	private ActivityTypeRepository typeRepo;
	
	@GetMapping("/activity")
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

		model.addAttribute("activities", repo.findByStartBetween(fromDate, toDate.plusDays(1))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "activity";
	}
	
	@GetMapping("/add-activity")
	public String addSport(@RequestParam(required = false) Long id, Model model) {
		Activity a = new Activity();
		a.setStart(LocalDateTime.now());
		a.setEnd(LocalDateTime.now());
		model.addAttribute("activity", a);
		model.addAttribute("activities", typeRepo.findAll());
		return "add-activity";
	}
	
	@PostMapping("/add-activity")
	public String submitSport(@ModelAttribute Activity activity, Model model) {
		model.addAttribute("activities", typeRepo.findAll());
		repo.save(activity);
		model.addAttribute("activity", activity);
		return "add-activity";
	}
	
	
	
	
}

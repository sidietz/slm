package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.SleepTime;
import de.oberamsystems.slm.model.SleepTimeRepository;

@Controller
public class SleepTimeController {
	
	Logger log = LoggerFactory.getLogger(SleepTimeController.class);

	@Autowired
	private SleepTimeRepository repo;

	@GetMapping(value = "/sleeptimes-bb")
	public String sleepTimeC3(Model model) {
		model.addAttribute("sleeptimes", repo.findAll());
		return "sleeptimes-bb";
	}

	@GetMapping(value = "/sleeptimes")
	public String getSleepTimes(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {

		fromDate = fromDate == null ? LocalDateTime.now().minusDays(28) : fromDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		LocalDateTime minDate = repo.findMinDate();
	    LocalDateTime maxDate = repo.findMaxDate();

		model.addAttribute("sleeptimes", repo.findByGotobedBetween(fromDate, toDate.plusDays(1))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate);
		model.addAttribute("minDate", minDate.minusHours(22)); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "sleeptimes";
	}

	@GetMapping("/add-sleeptime")
	public String addSleepTime(Model model) {
		model.addAttribute("sleeptime", new SleepTime());
		return "add-sleeptime";
	}

	@PostMapping("/add-sleeptime")
	public String submitSleepTime(@ModelAttribute SleepTime sleepTime, Model model) {
		repo.save(sleepTime);
		model.addAttribute("sleeptime", sleepTime);
		return "add-sleeptime";
	}

}

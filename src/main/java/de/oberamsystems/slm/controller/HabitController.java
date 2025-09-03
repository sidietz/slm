package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Habit;
import de.oberamsystems.slm.model.HabitEntry;
import de.oberamsystems.slm.model.HabitRepository;
import de.oberamsystems.slm.model.HabitEntryRepository;

@Controller
public class HabitController {

	Logger log = LoggerFactory.getLogger(HabitController.class);
	
	@Autowired
	private HabitRepository repo;
	
	@Autowired
	private HabitEntryRepository entryRepo;
	
	@GetMapping("/habit")
	public String getSport(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusDays(28) : fromDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		//LocalDateTime minDate = repo.findMinDate();
	    //LocalDateTime maxDate = repo.findMaxDate();

	    //minDate = minDate == null ? LocalDateTime.now().minusDays(7) : minDate.minusDays(1); //fixes off by one bug
	    //maxDate = maxDate == null ? LocalDateTime.now(): maxDate;

		model.addAttribute("habits", repo.findAll());//(fromDate, toDate.plusDays(1))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		//model.addAttribute("minDate", minDate); //fixes off by one bug
	    //model.addAttribute("maxDate", maxDate);
		return "habit";
	}
	
	@GetMapping("/habitentry")
	public String getHabitEntries(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusDays(28) : fromDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		//LocalDateTime minDate = repo.findMinDate();
	    //LocalDateTime maxDate = repo.findMaxDate();

	    //minDate = minDate == null ? LocalDateTime.now().minusDays(7) : minDate.minusDays(1); //fixes off by one bug
	    //maxDate = maxDate == null ? LocalDateTime.now(): maxDate;

		model.addAttribute("habitentries", entryRepo.findAll());//(fromDate, toDate.plusDays(1))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		//model.addAttribute("minDate", minDate); //fixes off by one bug
	    //model.addAttribute("maxDate", maxDate);
		return "habitentry";
	}
	
	@GetMapping("/add-habitentry")
	public String addHabitEntry(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("habitentry", new HabitEntry());
		model.addAttribute("habits", repo.findAll());
		return "add-habitentry";
	}
	
	@PostMapping("/add-habitentry")
	public String submitHabitEntry(@ModelAttribute HabitEntry entry, Model model) {
		model.addAttribute("habits", repo.findAll());
		entryRepo.save(entry);
		model.addAttribute("habitentry", entry);
		return "add-habitentry";
	}
	
	@GetMapping("/add-habit")
	public String addHabit(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("habit", new Habit());
		return "add-habit";
	}
	
	@PostMapping("/add-habit")
	public String submitHabit(@ModelAttribute Habit ts, Model model) {
		model.addAttribute("habits", repo.findAll());
		repo.save(ts);
		model.addAttribute("habit", ts);
		return "add-habit";
	}
	
	
	
}

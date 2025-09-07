package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.CalendarEvent;
import de.oberamsystems.slm.model.CalendarEventRepository;

@Controller
public class CalendarEventController {

	Logger log = LoggerFactory.getLogger(CalendarEventController.class);
	
	@Autowired
	private CalendarEventRepository repo;
	
	@GetMapping({"/event", "event.html"})
	public String getEvent(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusDays(28) : fromDate;
		toDate = toDate == null ? repo.findMaxDate() : toDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		LocalDateTime minDate = repo.findMinDate();
	    LocalDateTime maxDate = repo.findMaxDate();
	    
	    minDate = minDate == null ? LocalDateTime.now().minusDays(1) : minDate;
	    maxDate = maxDate == null ? LocalDateTime.now() : maxDate;

		model.addAttribute("events", repo.findByStartAfter(LocalDateTime.now().minusDays(1), Sort.by("start"))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "event";
	}
	
	@GetMapping("/add-event")
	public String addEvent(@RequestParam(required = false) Long id, Model model) {
		CalendarEvent e = new CalendarEvent();
		e.setStart(LocalDateTime.now());
		e.setEnd(LocalDateTime.now());
		model.addAttribute("event", e);
		return "add-event";
	}
	
	@PostMapping("/add-event")
	public String submitEvent(@ModelAttribute CalendarEvent event, Model model) {
		repo.save(event);
		model.addAttribute("event", event);
		return "add-event";
	}
}

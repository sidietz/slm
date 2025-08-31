package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.TrainTrip;
import de.oberamsystems.slm.model.TrainTripRepository;


@Controller
public class TrainTripController {

	Logger log = LoggerFactory.getLogger(TrainTripController.class);
	
	@Autowired
	private TrainTripRepository repo;
	
	@Autowired
	private TrainStationRepository stationRepo;
	
	@GetMapping("/traintrip")
	public String getTrainTrip(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusDays(28) : fromDate;
		toDate = toDate == null ? LocalDateTime.now() : toDate;

		LocalDateTime minDate = repo.findMinDate();
	    LocalDateTime maxDate = repo.findMaxDate();

	    minDate = minDate == null ? LocalDateTime.now().minusDays(7) : minDate.minusDays(1); //fixes off by one bug
	    maxDate = maxDate == null ? LocalDateTime.now(): maxDate;

		model.addAttribute("traintrips", repo.findByStartBetween(fromDate, toDate.plusDays(1))); // find getup time instead of gotobed
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", toDate.plusDays(1));
		model.addAttribute("minDate", minDate); //fixes off by one bug
	    model.addAttribute("maxDate", maxDate);
		return "traintrip";
	}
	
	@GetMapping("/add-traintrip")
	public String addTrainTrip(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("traintrip", new TrainTrip());
		model.addAttribute("traintrips", stationRepo.findAll());
		return "add-traintrip";
	}
	
	@PostMapping("/add-traintrip")
	public String submitSport(@ModelAttribute TrainTrip tt, Model model) {
		model.addAttribute("stations", stationRepo.findAll());
		repo.save(tt);
		model.addAttribute("traintrip", tt);
		return "add-traintrip";
	}
	
	
	
	
}

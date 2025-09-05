package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.TrainLine;
import de.oberamsystems.slm.model.TrainLineRepository;
import de.oberamsystems.slm.model.TrainStation2;
import de.oberamsystems.slm.model.TrainStation2Dto;
import de.oberamsystems.slm.model.TrainStation2Repository;
import de.oberamsystems.slm.model.TrainTrip2;
import de.oberamsystems.slm.model.TrainTrip2Repository;


@Controller
public class TrainTripController2 {

	Logger log = LoggerFactory.getLogger(TrainTripController2.class);
	
	@Autowired
	private TrainTrip2Repository repo;
	
	@Autowired
	private TrainStation2Repository stationRepo;
	
	@Autowired
	private TrainLineRepository lineRepo;
	
	@GetMapping("/traintrip2")
	public String getTrainTrip2(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
			Model model) {
		fromDate = fromDate == null ? LocalDateTime.now().minusMonths(1) : fromDate; // TODO: fix by setting time to start of day
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
		return "traintrip2";
	}
	
	@GetMapping({"/trainstation2.html", "/trainstations2.html", "/trainstation2", "/trainstations2"})
	public String index2(Model model) {
		List<TrainStation2> tss = stationRepo.findAll(Sort.by("ds100"));
		model.addAttribute("trainstations", tss);
		return "trainstation2";
	}
	
	@GetMapping("/add-traintrip2")
	public String addTrainTrip2(@RequestParam(required = false) Long id, Model model) {
		TrainTrip2 tt = new TrainTrip2();
		id = id == null ? 0 : id;
		TrainLine l = lineRepo.getReferenceById(id);
		tt.setLine(l);
		model.addAttribute("lineId", id);
		tt.setStart(LocalDateTime.now());
		tt.setEnd(LocalDateTime.now());
		model.addAttribute("traintrip", tt);
		model.addAttribute("traintrips", stationRepo.findByLines_Id(l.getId()));
		model.addAttribute("lines", lineRepo.findAll());
		return "add-traintrip2";
	}
	
	@PostMapping("/add-traintrip2")
	public String submitTrainTrip2(@RequestParam(required = false) Long id, @ModelAttribute TrainTrip2 tt, Model model) {
		id = id == null ? 0 : id;
		tt.setLine(lineRepo.getReferenceById(id));
		model.addAttribute("lineId", id);
		model.addAttribute("traintrip", tt);
		model.addAttribute("traintrips", stationRepo.findAll());
		model.addAttribute("lines", lineRepo.findAll());
		model.addAttribute("stations", stationRepo.findAll());
		repo.save(tt);
		model.addAttribute("traintrip", tt);
		return "add-traintrip2";
	}
	
	@GetMapping("/add-trainstation2")
	public String addTrainStation2(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("trainstation", new TrainStation2Dto());
		model.addAttribute("lineId", 0);
		model.addAttribute("lines", lineRepo.findAll());
		return "add-trainstation2";
	}
	
	@PostMapping("/add-trainstation2")
	public String submitTrainStation2(@ModelAttribute TrainStation2Dto tsdto, Model model) {
		model.addAttribute("stations", stationRepo.findAll());
		model.addAttribute("lines", lineRepo.findAll());
		log.warn(String.format("lineId: %d", tsdto.getLineId()));
		TrainStation2 ts = new TrainStation2(tsdto.getDs100(), tsdto.getName());
		ts.addLine(lineRepo.getReferenceById(tsdto.getLineId()));
		stationRepo.save(ts);
		model.addAttribute("trainstation", new TrainStation2Dto());
		return "add-trainstation2";
	}
	
	@GetMapping("/add-trainline")
	public String addTrainLine(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("trainline", new TrainLine());
		return "add-trainline";
	}
	
	@PostMapping("/add-trainline")
	public String submitTrainLine(@ModelAttribute TrainLine line, Model model) {
		lineRepo.save(line);
		model.addAttribute("trainline", new TrainLine());
		return "add-trainline";
	}
	
}

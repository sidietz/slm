package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
	
	private List<TrainLine> getSortedLines() {
		List<TrainLine> lines = lineRepo.findAll();
		lines.sort(new de.oberamsystems.slm.util.TrainLineComparator());
		return lines;
	}

	@GetMapping("/train-trips")
	public String addTrainTrips(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("traintrips2", repo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		TrainTrip2 tt = new TrainTrip2();
		id = id == null ? 0 : id;
		TrainLine l = lineRepo.getReferenceById(id);
		tt.setLine(l);
		model.addAttribute("lineId", id);
		tt.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		tt.setEnd(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("traintrip", tt);
		model.addAttribute("traintrips", stationRepo.findByLines_Id(l.getId()));
		model.addAttribute("lines", getSortedLines());
		return "train-trips";
	}
	
	@PostMapping("/train-trips")
	public String submitTrainTrips(@RequestParam(required = false) Long id, @ModelAttribute TrainTrip2 tt, Model model) {
		id = id == null ? 0 : id;
		model.addAttribute("traintrips2", repo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		tt.setLine(lineRepo.getReferenceById(id));
		model.addAttribute("lineId", id);
		model.addAttribute("traintrip", tt);
		model.addAttribute("traintrips", stationRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("lines", getSortedLines());
		model.addAttribute("stations", stationRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		repo.save(tt);
		model.addAttribute("traintrip", tt);
		return "train-trips";
	}
	
	@GetMapping("/train-stations")
	public String addTrainStations(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("stations", stationRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("trainstation", new TrainStation2Dto());
		model.addAttribute("lineId", 0);
		model.addAttribute("lines", getSortedLines());
		return "train-stations";
	}
	
	@PostMapping("/train-stations")
	public String submitTrainStations(@ModelAttribute TrainStation2Dto tsdto, Model model) {
		model.addAttribute("stations", stationRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("lines", getSortedLines());
		log.warn(String.format("lineId: %d", tsdto.getLineId()));
		TrainStation2 ts = (tsdto.getId() != null)
				? stationRepo.findById(tsdto.getId()).orElseGet(() -> new TrainStation2(tsdto.getDs100(), tsdto.getName()))
				: new TrainStation2(tsdto.getDs100(), tsdto.getName());
		ts.setDs100(tsdto.getDs100());
		ts.setName(tsdto.getName());
		if (tsdto.getLineId() != null && tsdto.getLineId() > 0) {
			ts.addLine(lineRepo.getReferenceById(tsdto.getLineId()));
		}
		stationRepo.save(ts);
		model.addAttribute("trainstation", new TrainStation2Dto());
		return "redirect:/train-stations";
	}
	
	@GetMapping("/train-lines")
	public String addTrainLines(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("lines", getSortedLines());
		model.addAttribute("trainline", new TrainLine());
		return "train-lines";
	}
	
	@PostMapping("/train-lines")
	public String submitTrainLines(@ModelAttribute TrainLine line, Model model) {
		model.addAttribute("lines", getSortedLines());
		lineRepo.save(line);
		model.addAttribute("trainline", new TrainLine());
		return "redirect:/train-lines";
	}
}

package de.oberamsystems.slm.controller;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import de.oberamsystems.slm.config.TimeZoneConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
		List<TrainLine> lines = getSortedLines();
		model.addAttribute("lines", lines);

		TrainTrip2 tt = new TrainTrip2();
		Long lineId = (id != null && id > 0) ? id : (!lines.isEmpty() ? lines.getFirst().getId() : null);
		if (lineId != null) {
			try {
				tt.setLine(lineRepo.getReferenceById(lineId));
			} catch (Exception ignored) {
			}
		}
		model.addAttribute("lineId", lineId);
		tt.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		tt.setEnd(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("traintrip", tt);

		List<TrainStation2> allStations = stationRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
		List<TrainStation2> lineStations = (lineId != null) ? stationRepo.findByLines_Id(lineId) : List.of();
		model.addAttribute("traintrips", lineStations.isEmpty() ? allStations : lineStations);
		model.addAttribute("stations", allStations);
		return "train-trips";
	}
	
	@PostMapping("/train-trips")
	public String submitTrainTrips(@RequestParam(required = false) Long id,
								   @RequestParam(required = false) Long lineId,
								   @RequestParam(required = false) Long line,
								   @ModelAttribute TrainTrip2 tt,
								   Model model,
								   RedirectAttributes redirectAttributes) {
		Long effectiveLineId = null;
		if (lineId != null && lineId > 0) {
			effectiveLineId = lineId;
		} else if (line != null && line > 0) {
			effectiveLineId = line;
		} else if (tt.getLine() != null && tt.getLine().getId() != null && tt.getLine().getId() > 0) {
			effectiveLineId = tt.getLine().getId();
		}

		if (effectiveLineId != null && effectiveLineId > 0 && lineRepo.existsById(effectiveLineId)) {
			tt.setLine(lineRepo.getReferenceById(effectiveLineId));
		}

		if (tt.getLine() == null || tt.getLine().getId() == null || !lineRepo.existsById(tt.getLine().getId())) {
			if (redirectAttributes != null) {
				redirectAttributes.addFlashAttribute("errorMessage", "Train line is required.");
			}
			model.addAttribute("errorMessage", "Train line is required.");
			populateModel(model, effectiveLineId, tt);
			return "train-trips";
		}

		if (tt.getOrigin() == null || tt.getOrigin().getId() == null || !stationRepo.existsById(tt.getOrigin().getId())
				|| tt.getDestination() == null || tt.getDestination().getId() == null || !stationRepo.existsById(tt.getDestination().getId())) {
			if (redirectAttributes != null) {
				redirectAttributes.addFlashAttribute("errorMessage", "Origin and destination stations are required.");
			}
			model.addAttribute("errorMessage", "Origin and destination stations are required.");
			populateModel(model, effectiveLineId, tt);
			return "train-trips";
		}

		if (tt.getId() != null && (tt.getId() <= 0 || !repo.existsById(tt.getId()))) {
			tt.setId(null);
		}

		repo.save(tt);
		populateModel(model, effectiveLineId, tt);
		return "train-trips";
	}

	public String submitTrainTrips(Long id, TrainTrip2 tt, Model model) {
		return submitTrainTrips(id, null, null, tt, model, null);
	}

	private void populateModel(Model model, Long effectiveLineId, TrainTrip2 tt) {
		model.addAttribute("traintrips2", repo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("lineId", effectiveLineId);
		model.addAttribute("traintrip", tt);
		model.addAttribute("traintrips", stationRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("lines", getSortedLines());
		model.addAttribute("stations", stationRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
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
		TrainStation2 ts = (tsdto.getId() != null && tsdto.getId() > 0 && stationRepo.existsById(tsdto.getId()))
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
		if (line.getId() != null && (line.getId() <= 0 || !lineRepo.existsById(line.getId()))) {
			line.setId(null);
		}
		lineRepo.save(line);
		model.addAttribute("trainline", new TrainLine());
		return "redirect:/train-lines";
	}
}

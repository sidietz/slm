package de.oberamsystems.slm.controller;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.config.TimeZoneConfig;
import de.oberamsystems.slm.model.MeditationSession;
import de.oberamsystems.slm.model.MeditationSessionRepository;

import org.springframework.data.domain.Sort;

@Controller
public class MeditationController {

	Logger log = LoggerFactory.getLogger(MeditationController.class);
	
	@Autowired
	private MeditationSessionRepository repo;
	
	@GetMapping("/meditations")
	public String addMeditations(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("meditations", repo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		MeditationSession ms = new MeditationSession();
		ms.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		ms.setEnd(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("meditation", ms);
		log.warn(String.format("%d", id));
		return "meditations";
	}
	
	@PostMapping("/meditations")
	public String submitMeditations(@ModelAttribute MeditationSession meditation, Model model) {
		model.addAttribute("meditations", repo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		repo.save(meditation);
		model.addAttribute("meditation", meditation);
		return "redirect:/meditations";
	}
}

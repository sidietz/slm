package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.oberamsystems.slm.model.SportSession;
import de.oberamsystems.slm.model.SportSessionRepository;
import de.oberamsystems.utils.Utils;

@Controller
public class IndexController {
	
	Logger log = LoggerFactory.getLogger(IndexController.class);
	
	@Autowired
	private SportSessionRepository repo;
	
	
	@GetMapping({"/index.html", "index", "/"})
	public String index(Model model) {
		SportSession s = repo.findFirstByOrderByStartDesc();
		log.warn(s.toString());
		String kind = s.getType().getName();
		LocalDateTime ldt = s.getStart();
		Duration diff = Duration.between(ldt, LocalDateTime.now());
		
		String msg = String.format("Last sport activity was: %s at %s and was %s ago.", kind, Utils.LocalDateTimeToString(ldt), Utils.DurationToString(diff));
		
		model.addAttribute("msg", msg);
		return "index";
	}
}

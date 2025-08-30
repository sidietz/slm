package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.oberamsystems.slm.model.Human;
import de.oberamsystems.slm.model.HumanRepository;
import de.oberamsystems.slm.model.MeditationSession;
import de.oberamsystems.slm.model.MeditationSessionRepository;
import de.oberamsystems.slm.model.SportSession;
import de.oberamsystems.slm.model.SportSessionRepository;
import de.oberamsystems.utils.Utils;

@Controller
public class IndexController {
	
	Logger log = LoggerFactory.getLogger(IndexController.class);

	@Autowired
	private SportSessionRepository sportRepo;

	@Autowired
	private MeditationSessionRepository meditationRepo;

	@Autowired
	private HumanRepository humanRepo;
	
	
	@GetMapping({"/index.html", "index", "/"})
	public String index(Model model) {
		SportSession s = sportRepo.findFirstByOrderByStartDesc();
		Human h = humanRepo.findFirstByOrderByDaysUntilBirthdayAsc();
		MeditationSession m = meditationRepo.findFirstByOrderByStartDesc();
		log.warn(s.toString());
		String kind = s.getType().getName();
		LocalDateTime ldt = s.getStart();
		Duration diff = Duration.between(ldt, LocalDateTime.now());
		Duration diff2 = Duration.between(m.getStart(), LocalDateTime.now());

		List<String> msgs = new ArrayList<String>();

		msgs.add(String.format("Next birthday is %s's on %s in %s days.", h.getFirstname() + " " + h.getLastname(), Utils.LocalDateTimeToString(h.getBirthday()), String.format("%d", h.getDaysUntilBirthday())));
		msgs.add(String.format("Last sport activity was: %s at %s and was %s ago.", kind, Utils.LocalDateTimeToString(ldt), Utils.DurationToString(diff)));
		msgs.add(String.format("Last meditation was at %s and was %s ago.", Utils.LocalDateTimeToString(m.getStart()), Utils.DurationToString(diff2)));

		model.addAttribute("msgs", msgs);
		return "index";
	}
}

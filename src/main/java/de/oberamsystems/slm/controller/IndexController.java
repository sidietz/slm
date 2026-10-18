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
import de.oberamsystems.slm.model.HumanEvent;
import de.oberamsystems.slm.model.HumanEventRepository;
import de.oberamsystems.slm.model.HumanRepository;
import de.oberamsystems.slm.model.MeditationSession;
import de.oberamsystems.slm.model.MeditationSessionRepository;
import de.oberamsystems.slm.model.ReadingSession;
import de.oberamsystems.slm.model.ReadingSessionRepository;
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

	@Autowired
	private HumanEventRepository eventRepo;
	
	@Autowired
	private ReadingSessionRepository readingsessionRepo;

	@GetMapping({"/index.html", "index", "/"})
	public String index(Model model) {
		SportSession s = sportRepo.findFirstByOrderByStartDesc();
		Human h = humanRepo.findFirstByOrderByDaysUntilBirthdayAsc();
		MeditationSession m = meditationRepo.findFirstByOrderByStartDesc();
		ReadingSession rs = readingsessionRepo.findFirstByOrderByStartDesc();
		HumanEvent nextEvent = eventRepo.findFirstByOrderByDaysUntilNextAsc();
		List<HumanEvent> upcomingEvents = eventRepo.findUpcomingEventsWithinDays(30);

		List<String> msgs = new ArrayList<String>();

		if (nextEvent != null) {
			String daysText = nextEvent.getDaysUntilNext() != null ? String.valueOf(nextEvent.getDaysUntilNext()) : "0";
			String milestoneText = nextEvent.getYearsElapsed() != null && nextEvent.getYearsElapsed() > 0 
				? String.format(" (%d. %s)", nextEvent.getYearsElapsed(), nextEvent.getEventType().getDisplayName())
				: "";
			msgs.add(String.format("Next milestone is %s on %s in %s days%s.",
					nextEvent.getTitle(),
					nextEvent.getNextOccurrence() != null ? nextEvent.getNextOccurrence().toString() : nextEvent.getEventDate().toString(),
					daysText,
					milestoneText));
		} else if (h != null) {
			msgs.add(String.format("Next birthday is %s's on %s in %s days.", h.getFirstname() + " " + h.getLastname(), Utils.LocalDateTimeToString(h.getBirthday()), String.format("%d", h.getDaysUntilBirthday())));
		}

		if (s != null) {
			String kind = s.getType().getName();
			LocalDateTime ldt = s.getStart();
			Duration diff = Duration.between(ldt, LocalDateTime.now());
			msgs.add(String.format("Last sport activity was: %s at %s and was %s ago.", kind, Utils.LocalDateTimeToString(ldt), Utils.DurationToString(diff)));
		}

		if (m != null) {
			Duration diff2 = Duration.between(m.getEnd(), LocalDateTime.now());
			msgs.add(String.format("Last meditation was at %s and was %s ago.", Utils.LocalDateTimeToString(m.getStart()), Utils.DurationToString(diff2)));
		}

		if (rs != null) {
			Duration diff3 = Duration.between(rs.getEnd(), LocalDateTime.now());
			msgs.add(String.format("Last reading session was at %s and was %s ago.", Utils.LocalDateTimeToString(rs.getStart()), Utils.DurationToString(diff3)));
		}

		model.addAttribute("msgs", msgs);
		model.addAttribute("upcomingEvents", upcomingEvents);
		return "index";
	}
}

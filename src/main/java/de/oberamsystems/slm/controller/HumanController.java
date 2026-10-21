package de.oberamsystems.slm.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.oberamsystems.slm.model.EventType;
import de.oberamsystems.slm.model.Human;
import de.oberamsystems.slm.model.HumanCategory;
import de.oberamsystems.slm.model.HumanEvent;
import de.oberamsystems.slm.model.HumanEventRepository;
import de.oberamsystems.slm.model.HumanRepository;
import de.oberamsystems.utils.Utils;

@Controller
public class HumanController {
	
	private static final Logger log = LoggerFactory.getLogger(HumanController.class);
	
	@Autowired
	private HumanRepository repo;

	@Autowired
	private HumanEventRepository eventRepo;
	
	@GetMapping({"/humans"})
	public String addHumans(@RequestParam(required = false) Long id, Model model) {
		List<Human> humans = repo.findAllByOrderByDaysUntilBirthdayAsc();
		model.addAttribute("humans", humans);
		model.addAttribute("categories", HumanCategory.values());
		model.addAttribute("human", new Human());
		return "humans";
	}
	
	@PostMapping({"/humans"})
	public String submitHumans(@ModelAttribute Human human, Model model) {
		if (human.getId() != null && human.getId() <= 0) {
			human.setId(null);
		}
		Human saved = repo.save(human);
		syncHumanEvents(saved);

		List<Human> humans = repo.findAllByOrderByDaysUntilBirthdayAsc();
		model.addAttribute("humans", humans);
		model.addAttribute("categories", HumanCategory.values());
		model.addAttribute("human", new Human());
		return "humans";
	}

	private void syncHumanEvents(Human human) {
		if (human == null || human.getId() == null || human.getId() <= 0) {
			return;
		}

		if (human.getBirthday() != null) {
			LocalDate bDate = Utils.dateToLocalDate(human.getBirthday());
			List<HumanEvent> existingBirthdays = eventRepo.findByHumanIdOrderByDaysUntilNextAsc(human.getId())
					.stream().filter(e -> e.getEventType() == EventType.BIRTHDAY).toList();
			if (existingBirthdays.isEmpty()) {
				HumanEvent bEvent = new HumanEvent(human, "Birthday – " + human.getFullName(), EventType.BIRTHDAY, bDate);
				eventRepo.save(bEvent);
			} else {
				HumanEvent bEvent = existingBirthdays.get(0);
				bEvent.setEventDate(bDate);
				bEvent.setTitle("Birthday – " + human.getFullName());
				eventRepo.save(bEvent);
			}
		}

		if (human.getDeathDate() != null) {
			LocalDate dDate = Utils.dateToLocalDate(human.getDeathDate());
			List<HumanEvent> existingMemorials = eventRepo.findByHumanIdOrderByDaysUntilNextAsc(human.getId())
					.stream().filter(e -> e.getEventType() == EventType.MEMORIAL).toList();
			if (existingMemorials.isEmpty()) {
				HumanEvent mEvent = new HumanEvent(human, "Memorial – " + human.getFullName(), EventType.MEMORIAL, dDate);
				eventRepo.save(mEvent);
			} else {
				HumanEvent mEvent = existingMemorials.get(0);
				mEvent.setEventDate(dDate);
				mEvent.setTitle("Memorial – " + human.getFullName());
				eventRepo.save(mEvent);
			}
		}
	}
}

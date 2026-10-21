package de.oberamsystems.slm.controller;

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
import de.oberamsystems.slm.model.HumanEvent;
import de.oberamsystems.slm.model.HumanEventRepository;
import de.oberamsystems.slm.model.HumanRepository;

@Controller
public class HumanEventController {

	private static final Logger log = LoggerFactory.getLogger(HumanEventController.class);

	@Autowired
	private HumanEventRepository eventRepo;

	@Autowired
	private HumanRepository humanRepo;

	@GetMapping({"/human-events", "/events"})
	public String listEvents(@RequestParam(required = false) EventType type,
	                         @RequestParam(required = false) Long humanId,
	                         Model model) {
		List<HumanEvent> events;
		if (humanId != null) {
			events = eventRepo.findByHumanIdOrderByDaysUntilNextAsc(humanId);
		} else if (type != null) {
			events = eventRepo.findByEventTypeOrderByDaysUntilNextAsc(type);
		} else {
			events = eventRepo.findAllByOrderByDaysUntilNextAsc();
		}

		List<Human> humans = humanRepo.findAll();
		List<HumanEvent> allEvents = eventRepo.findAllByOrderByDaysUntilNextAsc();

		long birthdaysCount = allEvents.stream().filter(e -> e.getEventType() == EventType.BIRTHDAY).count();
		long memorialsCount = allEvents.stream().filter(e -> e.getEventType() == EventType.MEMORIAL).count();
		long anniversariesCount = allEvents.stream().filter(e -> e.getEventType() == EventType.WEDDING_ANNIVERSARY || e.getEventType() == EventType.RELATIONSHIP_ANNIVERSARY).count();
		long milestonesCount = allEvents.stream().filter(e -> e.getEventType() == EventType.WORK_ANNIVERSARY || e.getEventType() == EventType.CUSTOM).count();

		model.addAttribute("events", events);
		model.addAttribute("activeType", type != null ? type.name() : "ALL");
		model.addAttribute("types", EventType.values());
		model.addAttribute("humans", humans);
		model.addAttribute("newEvent", new HumanEvent());
		model.addAttribute("totalCount", allEvents.size());
		model.addAttribute("birthdaysCount", birthdaysCount);
		model.addAttribute("memorialsCount", memorialsCount);
		model.addAttribute("anniversariesCount", anniversariesCount);
		model.addAttribute("milestonesCount", milestonesCount);

		return "human-events";
	}

	@PostMapping({"/human-events", "/events"})
	public String saveEvent(@ModelAttribute HumanEvent event,
	                         @RequestParam(required = false) Long humanId,
	                         @RequestParam(required = false) Long secondaryHumanId) {
		if (humanId != null && humanId > 0) {
			humanRepo.findById(humanId).ifPresent(event::setHuman);
		}
		if (secondaryHumanId != null && secondaryHumanId > 0) {
			humanRepo.findById(secondaryHumanId).ifPresent(event::setSecondaryHuman);
		}
		if (event.getHuman() == null) {
			humanRepo.findAll().stream().findFirst().ifPresent(event::setHuman);
		}
		if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
			String contactName = event.getHuman() != null ? event.getHuman().getFullName() : "Contact";
			event.setTitle(event.getEventType().getDisplayName() + " – " + contactName);
		}
		if (event.getId() != null && (event.getId() <= 0 || !eventRepo.existsById(event.getId()))) {
			event.setId(null);
		}
		eventRepo.save(event);
		log.info("Saved HumanEvent id={}, type={}, date={}", event.getId(), event.getEventType(), event.getEventDate());
		return "redirect:/human-events";
	}

	@PostMapping({"/human-events/delete", "/events/delete"})
	public String deleteEvent(@RequestParam Long id) {
		eventRepo.deleteById(id);
		log.info("Deleted HumanEvent id={}", id);
		return "redirect:/human-events";
	}
}

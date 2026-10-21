package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Activity;
import de.oberamsystems.slm.model.ActivityRepository;
import de.oberamsystems.slm.model.ActivityType;
import de.oberamsystems.slm.model.ActivityTypeRepository;

@Controller
public class ActivityController {

	Logger log = LoggerFactory.getLogger(ActivityController.class);
	
	@Autowired
	private ActivityRepository repo;
	
	@Autowired
	private ActivityTypeRepository typeRepo;
	
	@GetMapping("/activities")
	public String addActivity(@RequestParam(required = false) Long id, Model model) {
		Activity a = new Activity();
		a.setStart(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		a.setEnd(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("activities", repo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("activity", a);
		model.addAttribute("activityTypes", typeRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		return "activities";
	}
	
	@PostMapping("/activities")
	public String submitActivity(@ModelAttribute Activity activity, Model model) {
		model.addAttribute("activities", repo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("activityTypes", typeRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (activity.getType() == null || activity.getType().getId() == null) {
			java.util.List<ActivityType> types = typeRepo.findAll(Sort.by(Sort.Direction.ASC, "name"));
			if (!types.isEmpty()) {
				activity.setType(types.getFirst());
			}
		}
		if (activity.getId() != null && (activity.getId() <= 0 || !repo.existsById(activity.getId()))) {
			activity.setId(null);
		}
		repo.save(activity);
		model.addAttribute("activity", activity);
		return "redirect:/activities";
	}
	
	@GetMapping("/activity-types")
	public String addActivityType(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("activityTypes", typeRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		ActivityType type = new ActivityType();
		model.addAttribute("type", type);
		return "activity-types";
	}
	
	@PostMapping("/activity-types")
	public String submitActivityType(@ModelAttribute ActivityType type, Model model) {
		model.addAttribute("activityTypes", typeRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (type.getId() != null && (type.getId() <= 0 || !typeRepo.existsById(type.getId()))) {
			type.setId(null);
		}
		typeRepo.save(type);
		model.addAttribute("type", type);
		return "redirect:/activity-types";
	}
}

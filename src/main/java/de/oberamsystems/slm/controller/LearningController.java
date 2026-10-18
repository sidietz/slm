package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.LearningItem;
import de.oberamsystems.slm.model.LearningItemRepository;
import de.oberamsystems.slm.model.LearningSession;
import de.oberamsystems.slm.model.LearningSessionRepository;
import de.oberamsystems.slm.model.SourceEnum;
import de.oberamsystems.slm.model.StatusEnum;

@Controller
public class LearningController {

	Logger log = LoggerFactory.getLogger(LearningController.class);
	
	@Autowired
	private LearningItemRepository repo;
	
	@Autowired
	private LearningSessionRepository learningSessionRepo;
	
	@GetMapping("/learning-sessions")
	public String addLearningSessions(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("learningsessions", learningSessionRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		model.addAttribute("learningsession", new LearningSession());
		model.addAttribute("items", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		return "learning-sessions";
	}
	
	public String submitLearningSessions(LearningSession rs, Model model) {
		return submitLearningSessions(rs, 0L, model);
	}

	@PostMapping("/learning-sessions")
	public String submitLearningSessions(@ModelAttribute LearningSession rs,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes,
			Model model) {
		model.addAttribute("learningsessions", learningSessionRepo.findAll(Sort.by(Sort.Direction.DESC, "start")));
		LearningItem li = rs.getLearningItem();
		if (li != null) {
			li.setLastUpdated(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
			repo.save(li);
		}
		model.addAttribute("items", repo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		if (breakMinutes != null && breakMinutes > 0) {
			rs.setBreakDuration(Duration.ofMinutes(breakMinutes));
		}
		learningSessionRepo.save(rs);
		model.addAttribute("learningsession", rs);
		return "redirect:/learning-sessions";
	}
	
	@GetMapping("/learning-items")
	public String addLearningItems(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("items", repo.findAll(Sort.by(Sort.Direction.DESC, "lastUpdated")));
		model.addAttribute("sources", SourceEnum.values());
		model.addAttribute("statuss", StatusEnum.values());
		LearningItem i = new LearningItem();
		i.setLastUpdated(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("learningitem", i);
		return "learning-items";
	}
	
	@PostMapping("/learning-items")
	public String submitLearningItems(@ModelAttribute LearningItem i, Model model) {
		model.addAttribute("items", repo.findAll(Sort.by(Sort.Direction.DESC, "lastUpdated")));
		model.addAttribute("sources", SourceEnum.values());
		model.addAttribute("statuss", StatusEnum.values());
		repo.save(i);
		model.addAttribute("learningitem", i);
		return "redirect:/learning-items";
	}
}

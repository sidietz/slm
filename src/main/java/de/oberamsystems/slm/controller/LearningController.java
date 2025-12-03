package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@GetMapping("/learningitem")
	public String getbook(Model model) {
		List<LearningItem> items = repo.findAll();
		model.addAttribute("items", items);
		return "learningitem";
	}

	@GetMapping("/learning-session")
	public String getLearningSession(Model model) {
		model.addAttribute("learningsessions", learningSessionRepo.findAll());
		return "learning-session";
	}
	
	@GetMapping("/learning-sessions")
	public String addLearningSessions(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("learningsessions", learningSessionRepo.findAll());
		model.addAttribute("learningsession", new LearningSession());
		model.addAttribute("items", repo.findAll());
		return "learning-sessions";
	}
	
	@PostMapping("/learning-sessions")
	public String submitLearningSessions(@ModelAttribute LearningSession rs, Model model) {
		model.addAttribute("learningsessions", learningSessionRepo.findAll());
		LearningItem li = rs.getLearningItem();
		li.setLastUpdated(LocalDateTime.now());
		model.addAttribute("items", repo.findAll());
		Duration dur = Duration.between(rs.getStart(), rs.getEnd());
		rs.setDuration(dur);
		learningSessionRepo.save(rs);
		repo.save(li);
		model.addAttribute("learningsession", rs);
		return "redirect:/learning-sessions";
	}
	
	@GetMapping("/learning-items")
	public String addLearningItems(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("items", repo.findAll());
		model.addAttribute("sources", SourceEnum.values());
		model.addAttribute("statuss", StatusEnum.values());
		LearningItem i = new LearningItem();
		i.setLastUpdated(LocalDateTime.now());
		model.addAttribute("learningitem", i);
		return "learning-items";
	}
	
	@PostMapping("/learning-items")
	public String submitLearningItems(@ModelAttribute LearningItem i, Model model) {
		model.addAttribute("items", repo.findAll());
		model.addAttribute("sources", SourceEnum.values());
		model.addAttribute("statuss", StatusEnum.values());
		repo.save(i);
		model.addAttribute("learningitem", i);
		return "redirect:/learning-items";
	}
	
	@GetMapping("/add-learningitem")
	public String addLearningItem(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("sources", SourceEnum.values());
		model.addAttribute("statuss", StatusEnum.values());
		LearningItem i = new LearningItem();
		i.setLastUpdated(LocalDateTime.now());
		model.addAttribute("learningitem", i);
		return "add-learningitem";
	}
	
	@PostMapping("/add-learningitem")
	public String submitLearningItem(@ModelAttribute LearningItem i, Model model) {
		model.addAttribute("sources", SourceEnum.values());
		model.addAttribute("statuss", StatusEnum.values());
		repo.save(i);
		model.addAttribute("learningitem", i);
		return "add-learningitem";
	}
	
	@GetMapping("/add-learning-session")
	public String addLearningSession(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("learningsession", new LearningSession());
		model.addAttribute("items", repo.findAll());
		return "add-learning-session";
	}
	
	@PostMapping("/add-learning-session")
	public String submitLearningSession(@ModelAttribute LearningSession rs, Model model) {
		LearningItem li = rs.getLearningItem();
		li.setLastUpdated(LocalDateTime.now());
		model.addAttribute("items", repo.findAll());
		Duration dur = Duration.between(rs.getStart(), rs.getEnd());
		rs.setDuration(dur);
		learningSessionRepo.save(rs);
		repo.save(li);
		model.addAttribute("learningsession", rs);
		return "add-learning-session";
	}
}

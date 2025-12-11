package de.oberamsystems.slm.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Mood;
import de.oberamsystems.slm.model.MoodRepository;


@Controller
public class MoodController {

	Logger log = LoggerFactory.getLogger(MoodController.class);

	@Autowired
	private MoodRepository repo;
	
	@GetMapping("/moods")
	public String addMoods(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("moods", repo.findAll());
		Mood m = new Mood();
		m.setDate(LocalDate.now());
		model.addAttribute("mood", m);
		return "moods";
	}

	@PostMapping("/moods")
	public String submitMoods(@ModelAttribute Mood m, Model model) {
		model.addAttribute("moods", repo.findAll());
		repo.save(m);
		model.addAttribute("mood", m);
		return "redirect:/moods";
	}

	@GetMapping("/mood-bb")
	public String moodBb(Model model) {
		model.addAttribute("moods", repo.findAll());
		return "mood-bb";
	}
}

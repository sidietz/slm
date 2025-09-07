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

	@GetMapping({"/mood", "/mood.html"})
	public String getmood(Model model) {
		List<Mood> moods = repo.findAll();
		model.addAttribute("moods", moods);
		return "mood";
	}

	@GetMapping("/add-mood")
	public String addMood(@RequestParam(required = false) Long id, Model model) {
		Mood m = new Mood();
		m.setDate(LocalDate.now());
		model.addAttribute("mood", m);
		return "add-mood";
	}

	@PostMapping("/add-mood")
	public String submitMood(@ModelAttribute Mood m, Model model) {
		repo.save(m);
		model.addAttribute("mood", m);
		return "add-mood";
	}
	
	@GetMapping("/mood-bb")
	public String moodBb(Model model) {
		model.addAttribute("moods", repo.findAll());
		return "mood-bb";
	}
}

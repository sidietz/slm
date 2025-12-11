package de.oberamsystems.slm.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Gratitude;
import de.oberamsystems.slm.model.GratitudeRepository;

@Controller
public class GratitudeController {

	Logger log = LoggerFactory.getLogger(GratitudeController.class);
	
	@Autowired
	private GratitudeRepository repo;
	
	@GetMapping("/gratitudes")
	public String addGratitude(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("gratitudes", repo.findAll());
		Gratitude g = new Gratitude();
		g.setDate(LocalDate.now());
		model.addAttribute("gratitude", g);
		return "gratitudes";
	}
	
	@PostMapping("/gratitudes")
	public String submitGratitude(@ModelAttribute Gratitude gratitude, Model model) {
		model.addAttribute("gratitudes", repo.findAll());
		repo.save(gratitude);
		model.addAttribute("gratitude", gratitude);
		return "redirect:/gratitudes";
	}
}

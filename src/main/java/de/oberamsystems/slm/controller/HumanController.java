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

import de.oberamsystems.slm.model.Human;
import de.oberamsystems.slm.model.HumanRepository;

@Controller
public class HumanController {
	
	Logger log = LoggerFactory.getLogger(HumanController.class);
	
	@Autowired
	private HumanRepository repo;
	
	
	@GetMapping({"/human.html", "/humans.html", "/human", "/humans"})
	public String index(Model model) {
		List<Human> humans = repo.findAllByOrderByDaysUntilBirthdayAsc();
		model.addAttribute("humans", humans);
		return "human";
	}
	
	@GetMapping({"/add-human", "/add-human.html"})
	public String addHuman(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("human", new Human());
		return "add-human";
	}
	
	@PostMapping({"/add-human", "/add-human.html"})
	public String submitHuman(@ModelAttribute Human human, Model model) {
		repo.save(human);
		model.addAttribute("human", human);
		return "add-human";
	}
	
}

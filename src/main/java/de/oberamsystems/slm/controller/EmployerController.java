package de.oberamsystems.slm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.oberamsystems.slm.model.Employer;
import de.oberamsystems.slm.model.EmployerRepository;

@Controller
public class EmployerController {

	@Autowired
	private EmployerRepository employerRepo;

	@GetMapping("/employers")
	public String addEmployers(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("employers", employerRepo.findAll());
		model.addAttribute("employer", new Employer());
		return "employers";
	}

	@PostMapping("/employers")
	public String submitEmployers(@ModelAttribute Employer employer, Model model) {
		employerRepo.save(employer);
		return "redirect:/employers";
	}
}

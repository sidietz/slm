package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.SleepTime;
import de.oberamsystems.slm.model.SleepTimeRepository;

@Controller
public class SleepTimeController {
	
	Logger log = LoggerFactory.getLogger(SleepTimeController.class);

	@Autowired
	private SleepTimeRepository repo;

	@GetMapping("/sleeptime-bb")
	public String sleepTimeC3(Model model) {
		model.addAttribute("sleeptimes", repo.findAll());
		return "sleeptime-bb";
	}
	
	@GetMapping("/sleep-times")
	public String addSleepTimes(Model model) {
		model.addAttribute("sleeptimes", repo.findAll());
		SleepTime st = new SleepTime();
		st.setGotobed(LocalDateTime.now().minusHours(9));
		st.setGetup(LocalDateTime.now());
		model.addAttribute("sleeptime", st);
		return "sleep-times";
	}
	
	@PostMapping("/sleep-times")
	public String submitSleepTimes(@ModelAttribute SleepTime sleepTime, Model model) {
		model.addAttribute("sleeptimes", repo.findAll());
		repo.save(sleepTime);
		model.addAttribute("sleeptime", sleepTime);
		return "redirect:/sleep-times";
	}
}

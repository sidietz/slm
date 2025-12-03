package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.Habit;
import de.oberamsystems.slm.model.HabitEntry;
import de.oberamsystems.slm.model.HabitRepository;
import de.oberamsystems.slm.model.HabitEntryRepository;

@Controller
public class HabitController {

	Logger log = LoggerFactory.getLogger(HabitController.class);
	
	@Autowired
	private HabitRepository repo;
	
	@Autowired
	private HabitEntryRepository entryRepo;
	
	@GetMapping("/habits")
	public String addHabits(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("habits", repo.findAll());
		model.addAttribute("habit", new Habit());
		return "habits";
	}
	
	@PostMapping("/habits")
	public String submitHabits(@ModelAttribute Habit ts, Model model) {
		model.addAttribute("habits", repo.findAll());
		repo.save(ts);
		model.addAttribute("habit", ts);
		return "redirect:/habits";
	}
	
	@GetMapping("/habit-entries")
	public String addHabitEntries(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("habitentries", entryRepo.findAll());
		HabitEntry e = new HabitEntry();
		e.setLastDone(LocalDateTime.now());
		model.addAttribute("habitentry", e);
		model.addAttribute("habits", repo.findAll());
		return "habit-entries";
	}
	
	@PostMapping("/habit-entries")
	public String submitHabitEntries(@ModelAttribute HabitEntry entry, Model model) {
		model.addAttribute("habitentries", entryRepo.findAll());
		model.addAttribute("habits", repo.findAll());
		entryRepo.save(entry);
		model.addAttribute("habitentry", entry);
		return "redirect:/habit-entries";
	}
	
	@GetMapping("/habit")
	public String getSport(Model model) {
		model.addAttribute("habits", repo.findAll());
		return "habit";
	}
	
	@GetMapping("/habitentry")
	public String getHabitEntries(Model model) {
		model.addAttribute("habitentries", entryRepo.findAll());
		return "habitentry";
	}
	
	@GetMapping("/last-done-habitentry")
	public String getLastDoneHabitEntries(
			@RequestParam(required = false) Long id, Model model) {
		id = id == null ? 1 : id;
		model.addAttribute("habitId", id);
		model.addAttribute("habits", repo.findAll());
		model.addAttribute("lastdonehabitentries", entryRepo.lastDoneHabitEntryById(id));
		return "last-done-habitentry";
	}

	@GetMapping("/add-habitentry")
	public String addHabitEntry(@RequestParam(required = false) Long id, Model model) {
		HabitEntry e = new HabitEntry();
		e.setLastDone(LocalDateTime.now());
		model.addAttribute("habitentry", e);
		model.addAttribute("habits", repo.findAll());
		return "add-habitentry";
	}
	
	@PostMapping("/add-habitentry")
	public String submitHabitEntry(@ModelAttribute HabitEntry entry, Model model) {
		model.addAttribute("habits", repo.findAll());
		entryRepo.save(entry);
		model.addAttribute("habitentry", entry);
		return "add-habitentry";
	}
	
	@GetMapping("/add-habit")
	public String addHabit(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("habits", repo.findAll());
		model.addAttribute("habit", new Habit());
		return "add-habit";
	}
	
	@PostMapping("/add-habit")
	public String submitHabit(@ModelAttribute Habit ts, Model model) {
		model.addAttribute("habits", repo.findAll());
		repo.save(ts);
		model.addAttribute("habit", ts);
		return "add-habit";
	}
}

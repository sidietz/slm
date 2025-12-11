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

	@GetMapping("/last-done-habit-entries")
	public String getLastDoneHabitEntries(
			@RequestParam(required = false) Long id, Model model) {
		id = id == null ? 1 : id;
		Habit selected = repo.getById(id);
		model.addAttribute("habit", selected);
		model.addAttribute("habitId", id);
		model.addAttribute("habits", repo.findAll());
		model.addAttribute("lastdonehabitentries", entryRepo.lastDoneHabitEntryById(id));
		HabitEntry e = new HabitEntry();
		e.setHabit(selected);
		e.setLastDone(LocalDateTime.now());
		model.addAttribute("habitentry", e);
		return "last-done-habit-entries";
	}

	@PostMapping("/last-done-habit-entries")
	public String submitLastDoneHabitEntries(@RequestParam(required = false) Long id, @ModelAttribute HabitEntry entry, Model model) {
		id = id == null ? 1 : id;
		model.addAttribute("habitId", id);
		Habit selected = repo.getById(id);
		entry.setHabit(selected);
		model.addAttribute("habit", selected);
		model.addAttribute("habits", repo.findAll());
		model.addAttribute("lastdonehabitentries", entryRepo.lastDoneHabitEntryById(id));
		model.addAttribute("habitentries", entryRepo.findAll());
		entryRepo.save(entry);
		model.addAttribute("habitentry", entry);
		return "last-done-habit-entries";
	}
}

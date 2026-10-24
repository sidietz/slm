package de.oberamsystems.slm.controller;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import de.oberamsystems.slm.config.TimeZoneConfig;

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

import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HabitController {

	Logger log = LoggerFactory.getLogger(HabitController.class);
	
	@Autowired
	private HabitRepository repo;
	
	@Autowired
	private HabitEntryRepository entryRepo;
	
	@GetMapping("/habits")
	public String addHabits(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("habits", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("habit", new Habit());
		return "habits";
	}
	
	@PostMapping("/habits")
	public String submitHabits(@ModelAttribute Habit ts, Model model) {
		model.addAttribute("habits", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		if (ts.getId() != null && (ts.getId() <= 0 || !repo.existsById(ts.getId()))) {
			ts.setId(null);
		}
		repo.save(ts);
		model.addAttribute("habit", ts);
		return "redirect:/habits";
	}
	
	@GetMapping("/habit-entries")
	public String addHabitEntries(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("habitentries", entryRepo.findAll(Sort.by(Sort.Direction.DESC, "lastDone")));
		HabitEntry e = new HabitEntry();
		e.setLastDone(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("habitentry", e);
		model.addAttribute("habits", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		return "habit-entries";
	}

	public String submitHabitEntries(HabitEntry entry, Model model) {
		return submitHabitEntries(entry, model, new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap());
	}

	@PostMapping("/habit-entries")
	public String submitHabitEntries(@ModelAttribute HabitEntry entry, Model model, RedirectAttributes redirectAttributes) {
		if (entry.getHabit() == null || entry.getHabit().getId() == null || !repo.existsById(entry.getHabit().getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Habit is required.");
			return "redirect:/habit-entries";
		}
		if (entry.getId() != null && (entry.getId() <= 0 || !entryRepo.existsById(entry.getId()))) {
			entry.setId(null);
		}
		entryRepo.save(entry);
		return "redirect:/habit-entries";
	}

	@GetMapping("/last-done-habit-entries")
	public String getLastDoneHabitEntries(
			@RequestParam(required = false) Long id, Model model) {
		id = id == null ? 1 : id;
		Habit selected = repo.getById(id);
		model.addAttribute("habit", selected);
		model.addAttribute("habitId", id);
		model.addAttribute("habits", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("lastdonehabitentries", entryRepo.lastDoneHabitEntryById(id));
		HabitEntry e = new HabitEntry();
		e.setHabit(selected);
		e.setLastDone(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("habitentry", e);
		return "last-done-habit-entries";
	}

	@PostMapping("/last-done-habit-entries")
	public String submitLastDoneHabitEntries(@RequestParam(required = false) Long id, @ModelAttribute HabitEntry entry, Model model) {
		id = id == null ? 1 : id;
		model.addAttribute("habitId", id);
		Habit selected = repo.getById(id);
		entry.setHabit(selected);
		entry.setId(null);
		model.addAttribute("habit", selected);
		model.addAttribute("habits", repo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("lastdonehabitentries", entryRepo.lastDoneHabitEntryById(id));
		model.addAttribute("habitentries", entryRepo.findAll(Sort.by(Sort.Direction.DESC, "lastDone")));
		entryRepo.save(entry);
		model.addAttribute("habitentry", entry);
		return "redirect:/last-done-habit-entries";
	}
}

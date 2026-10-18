package de.oberamsystems.slm.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.DiaryEntry;
import de.oberamsystems.slm.model.DiaryEntryRepository;

import org.springframework.data.domain.Sort;

@Controller
public class DiaryEntryController {

	Logger log = LoggerFactory.getLogger(DiaryEntryController.class);
	
	@Autowired
	private DiaryEntryRepository repo;
	
	@GetMapping("/diary")
	public String addDiaryEntry(@RequestParam(required = false) Long id, Model model) {
		model.addAttribute("diaryentries", repo.findAll(Sort.by(Sort.Direction.DESC, "date")));
		DiaryEntry g = new DiaryEntry();
		g.setDate(LocalDate.now());
		model.addAttribute("diaryentry", g);
		return "diary";
	}
	
	@PostMapping("/diary")
	public String submitDiaryEntry(@ModelAttribute DiaryEntry DiaryEntry, Model model) {
		model.addAttribute("diaryentries", repo.findAll(Sort.by(Sort.Direction.DESC, "date")));
		repo.save(DiaryEntry);
		model.addAttribute("diaryentries", DiaryEntry);
		return "redirect:/diary";
	}
}

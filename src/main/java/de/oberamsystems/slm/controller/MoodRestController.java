package de.oberamsystems.slm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.oberamsystems.slm.model.Mood;
import de.oberamsystems.slm.model.MoodRepository;

@RestController
public class MoodRestController {
	
	@Autowired
	private MoodRepository repo;
	
	@GetMapping("/api/moods")
	public List<Mood> getMoods() {
		return repo.findAll(Sort.by(Sort.Direction.ASC, "date"));
	}
}

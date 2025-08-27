package de.oberamsystems.slm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.oberamsystems.slm.model.SleepTimeRepository;
import de.oberamsystems.slm.model.SleepTime;

@RestController
public class SleepTimeRestController {
	
	@Autowired
	private SleepTimeRepository repo;
	
	@GetMapping("/api/sleeptimes")
	public List<SleepTime> getSleepTimes() {
		return repo.findAll();
	}
}

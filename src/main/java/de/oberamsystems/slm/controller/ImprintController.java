package de.oberamsystems.slm.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImprintController {
	
	Logger log = LoggerFactory.getLogger(ImprintController.class);
	
	@Value("${version}")
	public String version;
	
	
	@GetMapping({"/imprint.html", "imprint"})
	public String index(Model model) {
		
		List<String> msgs = new ArrayList<String>();
		msgs.add("SLM - simon's life manager");
		msgs.add(String.format("version: %s", version));
		msgs.add("This software is licensed under AGPL-3.");

		model.addAttribute("msgs", msgs);
		return "imprint";
	}
}

package de.oberamsystems.slm.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ImprintController {
	
	Logger log = LoggerFactory.getLogger(ImprintController.class);
	
	private final BuildProperties buildProperties;
	
	public String version;

	@Autowired
	public ImprintController(@Autowired(required = false) BuildProperties buildProperties) {
		this.buildProperties = buildProperties;
	}

	public ImprintController() {
		this(null);
	}
	
	@GetMapping({"/imprint.html", "imprint"})
	public String index(Model model) {
		String ver = (buildProperties != null && buildProperties.getVersion() != null)
				? buildProperties.getVersion()
				: (version != null ? version : "dev");
		
		List<String> msgs = new ArrayList<String>();
		msgs.add("SLM - simon's life manager");
		msgs.add(String.format("version: %s", ver));
		msgs.add("This software is licensed under AGPL-3.");

		model.addAttribute("msgs", msgs);
		return "imprint";
	}
}


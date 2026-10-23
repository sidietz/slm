package de.oberamsystems.slm.controller;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.SleepTime;
import de.oberamsystems.slm.model.SleepTimeRepository;

import org.springframework.data.domain.Sort;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for legacy SleepTime tracking.
 *
 * @deprecated since 0.0.3-rc9, scheduled for removal in future major release.
 *             All ongoing sleep tracking is consolidated into {@link de.oberamsystems.slm.model.SleepSession}
 *             and {@link SessionController} at {@code /sessions?type=SLEEP}.
 */
@Deprecated(since = "0.0.3-rc9", forRemoval = true)
@Controller
public class SleepTimeController {
	
	Logger log = LoggerFactory.getLogger(SleepTimeController.class);

	@Autowired
	private SleepTimeRepository repo;

	@GetMapping("/sleeptime-bb")
	public String sleepTimeC3(Model model) {
		model.addAttribute("sleeptimes", repo.findAll(Sort.by(Sort.Direction.DESC, "gotobed")));
		return "sleeptime-bb";
	}
	
	@GetMapping("/sleep-times")
	public String addSleepTimes(Model model) {
		model.addAttribute("sleeptimes", repo.findAll(Sort.by(Sort.Direction.DESC, "gotobed")));
		SleepTime st = new SleepTime();
		st.setGotobed(LocalDateTime.now().minusHours(9).truncatedTo(ChronoUnit.MINUTES));
		st.setGetup(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("sleeptime", st);
		return "sleep-times";
	}
	
	@PostMapping("/sleep-times")
	public String submitSleepTimes(@ModelAttribute SleepTime sleepTime, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("errorMessage",
				"Logging legacy sleep times is disabled. Please log sleep sessions via the Sessions Hub.");
		return "redirect:/sleep-times";
	}
}

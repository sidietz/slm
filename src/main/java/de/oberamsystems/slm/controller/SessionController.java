package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import de.oberamsystems.slm.model.*;
import de.oberamsystems.slm.util.DurationFormatter;

@Controller
public class SessionController {

	private static final Logger log = LoggerFactory.getLogger(SessionController.class);

	@Autowired
	private SessionRepository sessionRepo;

	@Autowired
	private CodingSessionRepository codingSessionRepo;

	@Autowired
	private WorkingSessionRepository workingSessionRepo;

	@Autowired
	private LearningSessionRepository learningSessionRepo;

	@Autowired
	private GamingSessionRepository gamingSessionRepo;

	@Autowired
	private ReadingSessionRepository readingSessionRepo;

	@Autowired
	private MeditationSessionRepository meditationSessionRepo;

	@Autowired
	private SportSessionRepository sportSessionRepo;

	@Autowired
	private LearningItemRepository learningItemRepo;

	@Autowired
	private GameRepository gameRepo;

	@Autowired
	private DeviceRepository deviceRepo;

	@Autowired
	private BookRepository bookRepo;

	@Autowired
	private SportTypeRepository sportTypeRepo;

	@Autowired
	private EmployerRepository employerRepo;

	@GetMapping("/sessions")
	public String viewSessions(@RequestParam(required = false, defaultValue = "ALL") String type, Model model) {
		List<Session> allSessions = sessionRepo.findAll();
		// Sort chronologically descending
		allSessions.sort(Comparator.comparing(Session::getStart, Comparator.nullsLast(Comparator.reverseOrder())));

		List<Session> filteredSessions;
		if ("ALL".equalsIgnoreCase(type)) {
			filteredSessions = allSessions;
		} else {
			filteredSessions = allSessions.stream()
					.filter(s -> {
						String simpleName = s.getClass().getSimpleName().replace("Session", "").toUpperCase();
						return simpleName.equalsIgnoreCase(type);
					})
					.collect(Collectors.toList());
		}

		// Calculate statistics
		Duration totalNetDuration = Duration.ZERO;
		Duration totalBreakDuration = Duration.ZERO;
		double focusRatingSum = 0;
		int ratedCount = 0;

		for (Session s : filteredSessions) {
			if (s.getNetDuration() != null) {
				totalNetDuration = totalNetDuration.plus(s.getNetDuration());
			} else if (s.getDuration() != null) {
				totalNetDuration = totalNetDuration.plus(s.getDuration());
			}
			if (s.getBreakDuration() != null) {
				totalBreakDuration = totalBreakDuration.plus(s.getBreakDuration());
			}
			if (s.getFocusRating() != null && s.getFocusRating() > 0) {
				focusRatingSum += s.getFocusRating();
				ratedCount++;
			}
		}

		double avgFocusRating = ratedCount > 0 ? (focusRatingSum / ratedCount) : 0.0;

		model.addAttribute("sessions", filteredSessions);
		model.addAttribute("activeType", type.toUpperCase());
		model.addAttribute("totalSessionsCount", filteredSessions.size());
		model.addAttribute("totalNetDuration", DurationFormatter.format(totalNetDuration));
		model.addAttribute("totalBreakDuration", DurationFormatter.format(totalBreakDuration));
		model.addAttribute("avgFocusRating", String.format(java.util.Locale.US, "%.1f", avgFocusRating));

		// Reference items for the modal forms
		populateReferenceData(model);

		// Blank session models for form binding
		model.addAttribute("codingSession", new CodingSession());
		model.addAttribute("workingSession", new WorkingSession());
		model.addAttribute("learningSession", new LearningSession());
		model.addAttribute("gamingSession", new GamingSession());
		model.addAttribute("readingSession", new ReadingSession());
		model.addAttribute("meditationSession", new MeditationSession());
		model.addAttribute("sportSession", new SportSession());

		return "sessions";
	}

	@GetMapping("/coding-sessions")
	public String viewCodingSessions(Model model) {
		List<CodingSession> list = codingSessionRepo.findAll();
		list.sort(Comparator.comparing(CodingSession::getStart, Comparator.nullsLast(Comparator.reverseOrder())));
		model.addAttribute("sessions", list);
		CodingSession cs = new CodingSession();
		cs.setStart(LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.MINUTES));
		cs.setEnd(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		model.addAttribute("codingSession", cs);
		return "coding-sessions";
	}

	@PostMapping("/coding-sessions")
	public String submitCodingSessionDirect(@ModelAttribute CodingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		codingSessionRepo.save(session);
		return "redirect:/coding-sessions";
	}

	@GetMapping("/working-sessions")
	public String viewWorkingSessions(Model model) {
		List<WorkingSession> list = workingSessionRepo.findAll();
		list.sort(Comparator.comparing(WorkingSession::getStart, Comparator.nullsLast(Comparator.reverseOrder())));
		model.addAttribute("sessions", list);
		model.addAttribute("employers", employerRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name")));
		WorkingSession ws = new WorkingSession();
		ws.setStart(LocalDateTime.now().minusHours(2).truncatedTo(ChronoUnit.MINUTES));
		ws.setEnd(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
		ws.setBillable(true);
		model.addAttribute("workingSession", ws);
		return "working-sessions";
	}

	@PostMapping("/working-sessions")
	public String submitWorkingSessionDirect(@ModelAttribute WorkingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		workingSessionRepo.save(session);
		return "redirect:/working-sessions";
	}

	@PostMapping("/sessions/coding")
	public String submitCodingSession(@ModelAttribute CodingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		codingSessionRepo.save(session);
		return "redirect:/sessions?type=CODING";
	}

	@PostMapping("/sessions/working")
	public String submitWorkingSession(@ModelAttribute WorkingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		workingSessionRepo.save(session);
		return "redirect:/sessions?type=WORKING";
	}

	@PostMapping("/sessions/learning")
	public String submitLearningSession(@ModelAttribute LearningSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		if (session.getLearningItem() != null && session.getLearningItem().getId() != null) {
			learningItemRepo.findById(session.getLearningItem().getId()).ifPresent(item -> {
				item.setLastUpdated(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
				learningItemRepo.save(item);
			});
		}
		learningSessionRepo.save(session);
		return "redirect:/sessions?type=LEARNING";
	}

	@PostMapping("/sessions/gaming")
	public String submitGamingSession(@ModelAttribute GamingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		gamingSessionRepo.save(session);
		return "redirect:/sessions?type=GAMING";
	}

	@PostMapping("/sessions/reading")
	public String submitReadingSession(@ModelAttribute ReadingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		if (session.getStart() != null && session.getEnd() != null) {
			long minutes = Math.max(1, Duration.between(session.getStart(), session.getEnd()).toMinutes() - breakMinutes);
			long pages = session.getEndPageCount() - session.getStartPageCount();
			if (pages > 0) {
				session.setReadingSpeed(((float) pages / (float) minutes) * 60.0F);
			}
		}
		readingSessionRepo.save(session);
		return "redirect:/sessions?type=READING";
	}

	@PostMapping("/sessions/meditation")
	public String submitMeditationSession(@ModelAttribute MeditationSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		meditationSessionRepo.save(session);
		return "redirect:/sessions?type=MEDITATION";
	}

	@PostMapping("/sessions/sport")
	public String submitSportSession(@ModelAttribute SportSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		sportSessionRepo.save(session);
		return "redirect:/sessions?type=SPORT";
	}

	@PostMapping("/sessions/delete/{id}")
	public String deleteSession(@PathVariable String id,
			@RequestParam(required = false, defaultValue = "ALL") String returnType) {
		sessionRepo.deleteById(id);
		return "redirect:/sessions?type=" + returnType;
	}

	private void populateReferenceData(Model model) {
		model.addAttribute("learningItems", learningItemRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "title")));
		model.addAttribute("games", gameRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "title")));
		model.addAttribute("devices", deviceRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name")));
		model.addAttribute("books", bookRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "title")));
		model.addAttribute("sportTypes", sportTypeRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name")));
		model.addAttribute("employers", employerRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "name")));
	}
}

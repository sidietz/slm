package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
	private SleepSessionRepository sleepSessionRepo;

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
		} else if ("PRODUCTIVITY".equalsIgnoreCase(type)) {
			filteredSessions = allSessions.stream()
					.filter(s -> s instanceof CodingSession || s instanceof WorkingSession
							|| s instanceof LearningSession || s instanceof ReadingSession)
					.collect(Collectors.toList());
		} else if ("SPORT".equalsIgnoreCase(type)) {
			filteredSessions = allSessions.stream()
					.filter(s -> s instanceof SportSession)
					.collect(Collectors.toList());
		} else if ("WELLNESS".equalsIgnoreCase(type)) {
			filteredSessions = allSessions.stream()
					.filter(s -> s instanceof MeditationSession || s instanceof SleepSession)
					.collect(Collectors.toList());
		} else if ("LEISURE".equalsIgnoreCase(type)) {
			filteredSessions = allSessions.stream()
					.filter(s -> s instanceof GamingSession)
					.collect(Collectors.toList());
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

		// Calculate four pillars category balances across all sessions
		Duration productivityDuration = Duration.ZERO;
		Duration sportDuration = Duration.ZERO;
		Duration wellnessDuration = Duration.ZERO;
		Duration leisureDuration = Duration.ZERO;

		for (Session s : allSessions) {
			Duration d = s.getNetDuration() != null ? s.getNetDuration()
					: (s.getDuration() != null ? s.getDuration() : Duration.ZERO);
			if (s instanceof CodingSession || s instanceof WorkingSession
					|| s instanceof LearningSession || s instanceof ReadingSession) {
				productivityDuration = productivityDuration.plus(d);
			} else if (s instanceof SportSession) {
				sportDuration = sportDuration.plus(d);
			} else if (s instanceof MeditationSession || s instanceof SleepSession) {
				wellnessDuration = wellnessDuration.plus(d);
			} else if (s instanceof GamingSession) {
				leisureDuration = leisureDuration.plus(d);
			}
		}

		model.addAttribute("sessions", filteredSessions);
		model.addAttribute("activeType", type.toUpperCase());
		model.addAttribute("totalSessionsCount", filteredSessions.size());
		model.addAttribute("totalNetDuration", DurationFormatter.format(totalNetDuration));
		model.addAttribute("totalBreakDuration", DurationFormatter.format(totalBreakDuration));
		model.addAttribute("avgFocusRating", String.format(Locale.US, "%.1f", avgFocusRating));

		model.addAttribute("productivityDuration", DurationFormatter.format(productivityDuration));
		model.addAttribute("sportDuration", DurationFormatter.format(sportDuration));
		model.addAttribute("wellnessDuration", DurationFormatter.format(wellnessDuration));
		model.addAttribute("leisureDuration", DurationFormatter.format(leisureDuration));

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
		model.addAttribute("sleepSession", new SleepSession());

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
		model.addAttribute("employers", employerRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
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

	@PostMapping("/sessions/sleep")
	public String submitSleepSession(@ModelAttribute SleepSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes,
			@RequestParam(required = false, defaultValue = "0") Long deepSleepMinutes,
			@RequestParam(required = false, defaultValue = "0") Long remSleepMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		if (deepSleepMinutes != null && deepSleepMinutes > 0) {
			session.setDeepSleepDuration(DurationFormatter.fromMinutes(deepSleepMinutes));
		}
		if (remSleepMinutes != null && remSleepMinutes > 0) {
			session.setRemSleepDuration(DurationFormatter.fromMinutes(remSleepMinutes));
		}
		sleepSessionRepo.save(session);
		return "redirect:/sessions?type=SLEEP";
	}

	@PostMapping("/sessions/delete/{id}")
	public String deleteSession(@PathVariable String id,
			@RequestParam(required = false, defaultValue = "ALL") String returnType) {
		sessionRepo.deleteById(id);
		return "redirect:/sessions?type=" + returnType;
	}

	private void populateReferenceData(Model model) {
		model.addAttribute("learningItems", learningItemRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("games", gameRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("devices", deviceRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("books", bookRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("sportTypes", sportTypeRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
		model.addAttribute("employers", employerRepo.findAll(Sort.by(Sort.Direction.ASC, "name")));
	}
}

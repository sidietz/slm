package de.oberamsystems.slm.controller;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import de.oberamsystems.slm.config.TimeZoneConfig;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

	@Autowired
	private LectureSessionRepository lectureSessionRepo;

	@Autowired
	private CourseRepository courseRepo;

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
							|| s instanceof LearningSession || s instanceof ReadingSession
							|| s instanceof LectureSession)
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

		// Determine contextual labels for KPI header cards
		String kpiDurationTitle;
		String kpiRatingTitle;
		String normalizedType = type.toUpperCase();

		if ("SPORT".equals(normalizedType)) {
			kpiDurationTitle = "Active Training Time";
			kpiRatingTitle = "Avg Workout Intensity";
		} else if ("WELLNESS".equals(normalizedType) || "SLEEP".equals(normalizedType)) {
			kpiDurationTitle = "Rest & Recovery Time";
			kpiRatingTitle = "Avg Sleep Quality";
		} else if ("MEDITATION".equals(normalizedType)) {
			kpiDurationTitle = "Mindfulness Time";
			kpiRatingTitle = "Avg Mindfulness Rating";
		} else if ("LEISURE".equals(normalizedType) || "GAMING".equals(normalizedType)) {
			kpiDurationTitle = "Recreation Time";
			kpiRatingTitle = "Avg Game Rating";
		} else {
			// ALL, PRODUCTIVITY, CODING, WORKING, LEARNING, READING
			kpiDurationTitle = "Net Focused Time";
			kpiRatingTitle = "Avg Focus Rating";
		}

		// Calculate statistics
		Duration totalNetDuration = Duration.ZERO;
		Duration totalBreakDuration = Duration.ZERO;
		double focusRatingSum = 0;
		int ratedCount = 0;

		// When type=ALL, cognitive "Net Focused Time", breaks, and "Avg Focus Rating" are strictly computed
		// across Productivity sessions (Coding, Working, Learning, Reading, Lecture), excluding Sleep and Wellness.
		List<Session> statsSource = "ALL".equalsIgnoreCase(type)
				? allSessions.stream()
						.filter(s -> s instanceof CodingSession || s instanceof WorkingSession
								|| s instanceof LearningSession || s instanceof ReadingSession
								|| s instanceof LectureSession)
						.collect(Collectors.toList())
				: filteredSessions;

		for (Session s : statsSource) {
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
		Duration totalLifeDuration = Duration.ZERO;

		for (Session s : allSessions) {
			Duration d = s.getNetDuration() != null ? s.getNetDuration()
					: (s.getDuration() != null ? s.getDuration() : Duration.ZERO);
			totalLifeDuration = totalLifeDuration.plus(d);
			if (s instanceof CodingSession || s instanceof WorkingSession
					|| s instanceof LearningSession || s instanceof ReadingSession
					|| s instanceof LectureSession) {
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
		model.addAttribute("kpiDurationTitle", kpiDurationTitle);
		model.addAttribute("kpiRatingTitle", kpiRatingTitle);
		model.addAttribute("totalSessionsCount", filteredSessions.size());
		model.addAttribute("totalNetDuration", DurationFormatter.format(totalNetDuration));
		model.addAttribute("totalBreakDuration", DurationFormatter.format(totalBreakDuration));
		model.addAttribute("avgFocusRating", String.format(Locale.US, "%.1f", avgFocusRating));
		model.addAttribute("totalLifeTracked", DurationFormatter.format(totalLifeDuration));

		model.addAttribute("productivityDuration", DurationFormatter.format(productivityDuration));
		model.addAttribute("sportDuration", DurationFormatter.format(sportDuration));
		model.addAttribute("wellnessDuration", DurationFormatter.format(wellnessDuration));
		model.addAttribute("leisureDuration", DurationFormatter.format(leisureDuration));

		List<Session> activeSessions = sessionRepo.findByEndIsNullOrderByStartDesc();
		model.addAttribute("activeSessions", activeSessions);
		model.addAttribute("hasActiveSession", !activeSessions.isEmpty());

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
		model.addAttribute("lectureSession", new LectureSession());

		return "sessions";
	}

	@GetMapping("/coding-sessions")
	public String viewCodingSessions(Model model) {
		List<CodingSession> list = codingSessionRepo.findAll();
		list.sort(Comparator.comparing(CodingSession::getStart, Comparator.nullsLast(Comparator.reverseOrder())));
		model.addAttribute("sessions", list);
		CodingSession cs = new CodingSession();
		cs.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		cs.setEnd(null);
		model.addAttribute("codingSession", cs);
		return "coding-sessions";
	}

	private void initializeSessionStatus(Session session, Long breakMinutes) {
		session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
		if (session.getStart() == null) {
			session.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		}
		if (session.getEnd() == null) {
			session.setStatus("IN_PROGRESS");
		} else {
			session.setStatus("COMPLETED");
		}
	}

	@PostMapping("/coding-sessions")
	public String submitCodingSessionDirect(@ModelAttribute CodingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
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
		ws.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		ws.setEnd(null);
		ws.setBillable(true);
		model.addAttribute("workingSession", ws);
		return "working-sessions";
	}

	@PostMapping("/working-sessions")
	public String submitWorkingSessionDirect(@ModelAttribute WorkingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
		workingSessionRepo.save(session);
		return "redirect:/working-sessions";
	}

	@PostMapping("/sessions/coding")
	public String submitCodingSession(@ModelAttribute CodingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
		codingSessionRepo.save(session);
		return "redirect:/sessions?type=CODING";
	}

	@PostMapping("/sessions/working")
	public String submitWorkingSession(@ModelAttribute WorkingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
		workingSessionRepo.save(session);
		return "redirect:/sessions?type=WORKING";
	}

	@PostMapping("/sessions/learning")
	public String submitLearningSession(@ModelAttribute LearningSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
		if (session.getLearningItem() != null && session.getLearningItem().getId() != null) {
			learningItemRepo.findById(session.getLearningItem().getId()).ifPresent(item -> {
				item.setLastUpdated(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
				learningItemRepo.save(item);
			});
		}
		learningSessionRepo.save(session);
		return "redirect:/sessions?type=LEARNING";
	}

	@PostMapping("/sessions/gaming")
	public String submitGamingSession(@ModelAttribute GamingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
		gamingSessionRepo.save(session);
		return "redirect:/sessions?type=GAMING";
	}

	@PostMapping("/sessions/reading")
	public String submitReadingSession(@ModelAttribute ReadingSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
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
		initializeSessionStatus(session, breakMinutes);
		meditationSessionRepo.save(session);
		return "redirect:/sessions?type=MEDITATION";
	}

	@PostMapping("/sessions/sport")
	public String submitSportSession(@ModelAttribute SportSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes) {
		initializeSessionStatus(session, breakMinutes);
		sportSessionRepo.save(session);
		return "redirect:/sessions?type=SPORT";
	}

	@PostMapping("/sessions/sleep")
	public String submitSleepSession(@ModelAttribute SleepSession session,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes,
			@RequestParam(required = false) Long deepSleepMinutes,
			@RequestParam(required = false) Long remSleepMinutes,
			@RequestParam(required = false) Long lightSleepMinutes,
			org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

		if (session.getStart() == null) {
			session.setStart(OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES));
		}

		if (session.getEnd() == null) {
			session.setStatus("IN_PROGRESS");
			session.setBreakDuration(DurationFormatter.fromMinutes(breakMinutes));
			session.setDeepSleepDuration(Duration.ZERO);
			session.setRemSleepDuration(Duration.ZERO);
			session.setLightSleepDuration(Duration.ZERO);
			sleepSessionRepo.save(session);
			return "redirect:/sessions?type=SLEEP";
		}

		long totalMinutes = Duration.between(session.getStart(), session.getEnd()).toMinutes();
		if (totalMinutes <= 0) {
			redirectAttributes.addFlashAttribute("errorMessage", "Wake-up time must be after bedtime.");
			return "redirect:/sessions?type=SLEEP";
		}

		long awakeMinutes = (breakMinutes != null && breakMinutes > 0) ? breakMinutes : 0;
		if (awakeMinutes >= totalMinutes) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Awake disruptions (" + awakeMinutes + "m) cannot equal or exceed total bedtime duration (" + totalMinutes + "m).");
			return "redirect:/sessions?type=SLEEP";
		}

		long sleepMinutes = totalMinutes - awakeMinutes;

		int providedCount = 0;
		if (deepSleepMinutes != null) providedCount++;
		if (remSleepMinutes != null) providedCount++;
		if (lightSleepMinutes != null) providedCount++;

		long deep = 0;
		long rem = 0;
		long light = 0;

		if (providedCount == 0) {
			// All 3 stages empty -> whole sleep duration in minutes set to light sleep
			light = sleepMinutes;
			deep = 0;
			rem = 0;
		} else if (providedCount == 2) {
			// Exactly one sleep duration is missing -> calculate from wake up - bedtime and other sleep times
			if (deepSleepMinutes == null) {
				rem = Math.max(0, remSleepMinutes);
				light = Math.max(0, lightSleepMinutes);
				deep = sleepMinutes - rem - light;
				if (deep < 0) {
					redirectAttributes.addFlashAttribute("errorMessage",
							"Specified sleep stages (" + (rem + light) + "m) exceed net sleep duration (" + sleepMinutes + "m).");
					return "redirect:/sessions?type=SLEEP";
				}
			} else if (remSleepMinutes == null) {
				deep = Math.max(0, deepSleepMinutes);
				light = Math.max(0, lightSleepMinutes);
				rem = sleepMinutes - deep - light;
				if (rem < 0) {
					redirectAttributes.addFlashAttribute("errorMessage",
							"Specified sleep stages (" + (deep + light) + "m) exceed net sleep duration (" + sleepMinutes + "m).");
					return "redirect:/sessions?type=SLEEP";
				}
			} else { // lightSleepMinutes == null
				deep = Math.max(0, deepSleepMinutes);
				rem = Math.max(0, remSleepMinutes);
				light = sleepMinutes - deep - rem;
				if (light < 0) {
					redirectAttributes.addFlashAttribute("errorMessage",
							"Specified sleep stages (" + (deep + rem) + "m) exceed net sleep duration (" + sleepMinutes + "m).");
					return "redirect:/sessions?type=SLEEP";
				}
			}
		} else if (providedCount == 3) {
			// All 3 stages provided -> validate that deep + rem + light + awake == total bedtime
			deep = Math.max(0, deepSleepMinutes);
			rem = Math.max(0, remSleepMinutes);
			light = Math.max(0, lightSleepMinutes);

			long stageSum = deep + rem + light + awakeMinutes;
			if (stageSum != totalMinutes) {
				redirectAttributes.addFlashAttribute("errorMessage",
						"Sleep stage durations (Deep: " + deep + "m, REM: " + rem + "m, Light: " + light + "m, Awake: " + awakeMinutes
								+ "m = " + stageSum + "m) do not add up to total bedtime duration (" + totalMinutes + "m).");
				return "redirect:/sessions?type=SLEEP";
			}
		} else { // providedCount == 1
			redirectAttributes.addFlashAttribute("errorMessage",
					"Please either leave all sleep stages empty (to default to light sleep) or specify at least two sleep stages so the missing duration can be calculated.");
			return "redirect:/sessions?type=SLEEP";
		}

		session.setStatus("COMPLETED");
		session.setBreakDuration(Duration.ofMinutes(awakeMinutes));
		session.setDeepSleepDuration(Duration.ofMinutes(deep));
		session.setRemSleepDuration(Duration.ofMinutes(rem));
		session.setLightSleepDuration(Duration.ofMinutes(light));

		sleepSessionRepo.save(session);
		return "redirect:/sessions?type=SLEEP";
	}

	@PostMapping("/sessions/lecture")
	public String submitLectureSession(@ModelAttribute LectureSession session,
			@RequestParam(required = false) Long courseId,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes,
			@RequestParam(required = false) LectureActivityType activityType) {
		initializeSessionStatus(session, breakMinutes);
		if (activityType != null) {
			session.setActivityType(activityType);
		} else if (session.getActivityType() == null) {
			session.setActivityType(LectureActivityType.LECTURE);
		}
		Long cid = courseId != null ? courseId : (session.getCourse() != null ? session.getCourse().getId() : null);
		if (cid != null) {
			courseRepo.findById(cid).ifPresent(session::setCourse);
		}
		lectureSessionRepo.save(session);
		return "redirect:/sessions?type=LECTURE";
	}

	@PostMapping("/sessions/{id}/stop")
	public String stopSession(
			@PathVariable UUID id,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
			@RequestParam(required = false, defaultValue = "0") Long breakMinutes,
			@RequestParam(required = false) Integer focusRating,
			@RequestParam(required = false) String outcomeSummary,
			@RequestParam(required = false) String comment,
			@RequestParam(required = false, defaultValue = "ALL") String returnType) {

		Session session = sessionRepo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Session not found: " + id));

		OffsetDateTime stopTime = (end != null) ? end : OffsetDateTime.now(TimeZoneConfig.getZoneId()).truncatedTo(ChronoUnit.MINUTES);
		Duration brk = DurationFormatter.fromMinutes(breakMinutes);
		session.stop(stopTime, brk, focusRating, outcomeSummary, comment);

		if (session instanceof SleepSession ss) {
			Duration net = Duration.between(session.getStart(), stopTime).minus(brk);
			Duration deep = ss.getDeepSleepDuration() != null ? ss.getDeepSleepDuration() : Duration.ZERO;
			Duration rem = ss.getRemSleepDuration() != null ? ss.getRemSleepDuration() : Duration.ZERO;
			Duration light = ss.getLightSleepDuration() != null ? ss.getLightSleepDuration() : Duration.ZERO;
			if (deep.isZero() && rem.isZero() && light.isZero()) {
				ss.setLightSleepDuration(net.isNegative() ? Duration.ZERO : net);
			} else if (light.isZero() && !deep.plus(rem).equals(net)) {
				Duration remaining = net.minus(deep).minus(rem);
				if (!remaining.isNegative()) {
					ss.setLightSleepDuration(remaining);
				}
			}
		}

		sessionRepo.save(session);
		return "redirect:/sessions?type=" + returnType;
	}

	@PostMapping("/sessions/delete/{id}")
	public String deleteSession(@PathVariable UUID id,
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
		model.addAttribute("courses", courseRepo.findAll(Sort.by(Sort.Direction.ASC, "title")));
		model.addAttribute("lectureActivityTypes", LectureActivityType.values());
	}
}

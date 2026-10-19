package de.oberamsystems.slm.mcp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.oberamsystems.slm.model.CodingSession;
import de.oberamsystems.slm.model.CodingSessionRepository;
import de.oberamsystems.slm.model.GamingSession;
import de.oberamsystems.slm.model.LearningSession;
import de.oberamsystems.slm.model.LectureSession;
import de.oberamsystems.slm.model.MeditationSession;
import de.oberamsystems.slm.model.ReadingSession;
import de.oberamsystems.slm.model.Session;
import de.oberamsystems.slm.model.SessionRepository;
import de.oberamsystems.slm.model.SleepSession;
import de.oberamsystems.slm.model.SleepSessionRepository;
import de.oberamsystems.slm.model.SportSession;
import de.oberamsystems.slm.model.WorkingSession;

@Service
public class McpSessionTools {

	@Autowired
	private SessionRepository sessionRepo;

	@Autowired
	private CodingSessionRepository codingRepo;

	@Autowired
	private SleepSessionRepository sleepRepo;

	@Tool(description = "Calculate daily Four Pillars life balance breakdown (Productivity focus, Sport training, Wellness sleep/meditation, and Leisure gaming)")
	public Map<String, Object> getDailyLifeBalance(
			@ToolParam(description = "Date in format YYYY-MM-DD (e.g. 2026-10-18)") String dateStr) {

		LocalDate date = LocalDate.parse(dateStr);
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

		List<Session> daySessions = sessionRepo.findByStartBetween(startOfDay, endOfDay);

		long productivityMinutes = 0;
		long sportMinutes = 0;
		long wellnessMinutes = 0;
		long leisureMinutes = 0;

		for (Session s : daySessions) {
			Duration d = s.getNetDuration() != null ? s.getNetDuration() : s.getDuration();
			if (d == null && s.getStart() != null && s.getEnd() != null) {
				Duration raw = Duration.between(s.getStart(), s.getEnd());
				Duration brk = s.getBreakDuration() != null ? s.getBreakDuration() : Duration.ZERO;
				d = raw.minus(brk);
			}
			long minutes = (d != null && !d.isNegative()) ? d.toMinutes() : 0;

			if (s instanceof CodingSession || s instanceof WorkingSession || s instanceof LearningSession || s instanceof ReadingSession || s instanceof LectureSession) {
				productivityMinutes += minutes;
			} else if (s instanceof SportSession) {
				sportMinutes += minutes;
			} else if (s instanceof MeditationSession || s instanceof SleepSession) {
				wellnessMinutes += minutes;
			} else if (s instanceof GamingSession) {
				leisureMinutes += minutes;
			}
		}

		Map<String, Object> balance = new HashMap<>();
		balance.put("date", dateStr);
		balance.put("totalSessionsCount", daySessions.size());
		balance.put("productivityMinutes", productivityMinutes);
		balance.put("productivityHours", String.format("%.2f", productivityMinutes / 60.0));
		balance.put("sportMinutes", sportMinutes);
		balance.put("sportHours", String.format("%.2f", sportMinutes / 60.0));
		balance.put("wellnessMinutes", wellnessMinutes);
		balance.put("wellnessHours", String.format("%.2f", wellnessMinutes / 60.0));
		balance.put("leisureMinutes", leisureMinutes);
		balance.put("leisureHours", String.format("%.2f", leisureMinutes / 60.0));

		return balance;
	}

	@Tool(description = "Retrieve recent time tracking sessions filtered by category (PRODUCTIVITY, SPORT, WELLNESS, LEISURE, or ALL)")
	public List<Map<String, Object>> getRecentSessions(
			@ToolParam(description = "Category: PRODUCTIVITY, SPORT, WELLNESS, LEISURE, or ALL (default ALL)", required = false) String category,
			@ToolParam(description = "Maximum number of sessions to return (default 10)", required = false) Integer limit) {

		int max = (limit != null && limit > 0) ? limit : 10;
		String cat = (category != null && !category.trim().isEmpty()) ? category.trim().toUpperCase() : "ALL";

		List<Session> all = sessionRepo.findAll();
		all.sort(Comparator.comparing(Session::getStart, Comparator.nullsLast(Comparator.reverseOrder())));

		List<Session> filtered = all.stream().filter(s -> {
			if ("ALL".equals(cat)) return true;
			if ("PRODUCTIVITY".equals(cat)) return s instanceof CodingSession || s instanceof WorkingSession || s instanceof LearningSession || s instanceof ReadingSession || s instanceof LectureSession;
			if ("SPORT".equals(cat)) return s instanceof SportSession;
			if ("WELLNESS".equals(cat)) return s instanceof MeditationSession || s instanceof SleepSession;
			if ("LEISURE".equals(cat)) return s instanceof GamingSession;
			return true;
		}).limit(max).collect(Collectors.toList());

		List<Map<String, Object>> result = new ArrayList<>();
		for (Session s : filtered) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", s.getId());
			m.put("type", getSessionTypeName(s));
			m.put("start", s.getStart() != null ? s.getStart().toString() : null);
			m.put("end", s.getEnd() != null ? s.getEnd().toString() : null);
			Duration net = s.getNetDuration() != null ? s.getNetDuration() : s.getDuration();
			m.put("netDurationMinutes", net != null ? net.toMinutes() : null);
			m.put("focusRating", s.getFocusRating());
			m.put("comment", s.getComment());
			m.put("outcomeSummary", s.getOutcomeSummary());
			result.add(m);
		}

		return result;
	}

	@Tool(description = "Log a completed coding session")
	public Map<String, Object> logCodingSession(
			@ToolParam(description = "Project name (e.g. SLM, Antigravity)") String project,
			@ToolParam(description = "Start datetime in format YYYY-MM-DDTHH:mm (e.g. 2026-10-18T14:00)") String startStr,
			@ToolParam(description = "End datetime in format YYYY-MM-DDTHH:mm (e.g. 2026-10-18T16:30)") String endStr,
			@ToolParam(description = "Break duration in minutes (default 0)", required = false) Integer breakMinutes,
			@ToolParam(description = "Focus rating from 1 to 5", required = false) Integer focusRating,
			@ToolParam(description = "Outcome summary of what was accomplished", required = false) String outcome,
			@ToolParam(description = "Notes or reflection comment", required = false) String comment,
			@ToolParam(description = "Programming language (e.g. Java, TypeScript)", required = false) String language,
			@ToolParam(description = "Git branch", required = false) String branch) {

		LocalDateTime start = LocalDateTime.parse(startStr);
		LocalDateTime end = LocalDateTime.parse(endStr);
		int bMinutes = (breakMinutes != null && breakMinutes >= 0) ? breakMinutes : 0;

		CodingSession cs = new CodingSession();
		cs.setProject(project);
		cs.setStart(start);
		cs.setEnd(end);
		cs.setBreakDuration(Duration.ofMinutes(bMinutes));
		cs.setFocusRating(focusRating);
		cs.setOutcomeSummary(outcome);
		cs.setComment(comment);
		cs.setLanguage(language);
		cs.setBranch(branch);
		cs.setStatus("COMPLETED");

		cs = codingRepo.save(cs);

		Map<String, Object> res = new HashMap<>();
		res.put("id", cs.getId());
		res.put("project", cs.getProject());
		res.put("status", "SUCCESS");
		return res;
	}

	@Tool(description = "Log a sleep session (bedtime, wake-up time, quality rating, disruptions)")
	public Map<String, Object> logSleepSession(
			@ToolParam(description = "Bedtime in format YYYY-MM-DDTHH:mm (e.g. 2026-10-18T23:30)") String bedtimeStr,
			@ToolParam(description = "Wake-up time in format YYYY-MM-DDTHH:mm (e.g. 2026-10-19T07:15)") String wakeStr,
			@ToolParam(description = "Number of awake disruptions (default 0)", required = false) Integer awakeCount,
			@ToolParam(description = "Awake disruption minutes (default 0)", required = false) Integer breakMinutes,
			@ToolParam(description = "Sleep quality rating from 1 (poor) to 5 (excellent)", required = false) Integer qualityRating,
			@ToolParam(description = "Notes on dreams or restfulness", required = false) String comment) {

		LocalDateTime start = LocalDateTime.parse(bedtimeStr);
		LocalDateTime end = LocalDateTime.parse(wakeStr);
		int bMinutes = (breakMinutes != null && breakMinutes >= 0) ? breakMinutes : 0;

		long totalMinutes = Duration.between(start, end).toMinutes();
		long sleepMinutes = Math.max(0, totalMinutes - bMinutes);

		SleepSession ss = new SleepSession();
		ss.setStart(start);
		ss.setEnd(end);
		ss.setBreakDuration(Duration.ofMinutes(bMinutes));
		ss.setLightSleepDuration(Duration.ofMinutes(sleepMinutes));
		ss.setDeepSleepDuration(Duration.ZERO);
		ss.setRemSleepDuration(Duration.ZERO);
		ss.setAwakeCount(awakeCount != null ? awakeCount : 0);
		ss.setFocusRating(qualityRating);
		ss.setComment(comment);
		ss.setStatus("COMPLETED");

		ss = sleepRepo.save(ss);

		Map<String, Object> res = new HashMap<>();
		res.put("id", ss.getId());
		res.put("start", ss.getStart().toString());
		res.put("end", ss.getEnd().toString());
		res.put("status", "SUCCESS");
		return res;
	}

	private String getSessionTypeName(Session s) {
		if (s instanceof CodingSession) return "CODING";
		if (s instanceof WorkingSession) return "WORKING";
		if (s instanceof LearningSession) return "LEARNING";
		if (s instanceof ReadingSession) return "READING";
		if (s instanceof SportSession) return "SPORT";
		if (s instanceof LectureSession) return "LECTURE";
		if (s instanceof MeditationSession) return "MEDITATION";
		if (s instanceof SleepSession) return "SLEEP";
		if (s instanceof GamingSession) return "GAMING";
		return "OTHER";
	}
}

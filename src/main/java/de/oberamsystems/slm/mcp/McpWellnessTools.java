package de.oberamsystems.slm.mcp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.oberamsystems.slm.model.Gratitude;
import de.oberamsystems.slm.model.GratitudeRepository;
import de.oberamsystems.slm.model.Habit;
import de.oberamsystems.slm.model.HabitEntry;
import de.oberamsystems.slm.model.HabitEntryRepository;
import de.oberamsystems.slm.model.HabitRepository;
import de.oberamsystems.slm.model.Mood;
import de.oberamsystems.slm.model.MoodRepository;

@Service
public class McpWellnessTools {

	@Autowired
	private HabitRepository habitRepo;

	@Autowired
	private HabitEntryRepository habitEntryRepo;

	@Autowired
	private MoodRepository moodRepo;

	@Autowired
	private GratitudeRepository gratitudeRepo;

	@Tool(description = "Retrieve all tracked habits and their last-done completion timestamp")
	public List<Map<String, Object>> getHabitsStatus() {
		List<Habit> habits = habitRepo.findAll();
		List<Map<String, Object>> result = new ArrayList<>();

		for (Habit h : habits) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", h.getId());
			m.put("name", h.getName());
			m.put("description", h.getDescription());

			List<HabitEntry> entries = h.getEntries();
			if (entries != null && !entries.isEmpty()) {
				entries.stream()
						.map(HabitEntry::getLastDone)
						.filter(d -> d != null)
						.max(LocalDateTime::compareTo)
						.ifPresentOrElse(
								last -> m.put("lastDone", last.toString()),
								() -> m.put("lastDone", null)
						);
			} else {
				m.put("lastDone", null);
			}
			result.add(m);
		}

		return result;
	}

	@Tool(description = "Record a habit as completed")
	public Map<String, Object> recordHabitCompleted(
			@ToolParam(description = "Name or ID of the habit (e.g. 'Gym', 'Floss', '1')") String habitIdentifier,
			@ToolParam(description = "Completion timestamp in format YYYY-MM-DDTHH:mm (optional, defaults to now)", required = false) String timestampStr) {

		LocalDateTime timestamp = (timestampStr != null && !timestampStr.trim().isEmpty())
				? LocalDateTime.parse(timestampStr)
				: LocalDateTime.now();

		Habit targetHabit = null;
		try {
			Long id = Long.parseLong(habitIdentifier);
			targetHabit = habitRepo.findById(id).orElse(null);
		} catch (NumberFormatException ignored) {
		}

		if (targetHabit == null) {
			List<Habit> matches = habitRepo.findAll().stream()
					.filter(h -> h.getName() != null && h.getName().equalsIgnoreCase(habitIdentifier.trim()))
					.toList();
			if (!matches.isEmpty()) {
				targetHabit = matches.get(0);
			}
		}

		if (targetHabit == null) {
			throw new IllegalArgumentException("Habit not found matching: " + habitIdentifier);
		}

		HabitEntry entry = new HabitEntry();
		entry.setHabit(targetHabit);
		entry.setLastDone(timestamp);
		entry = habitEntryRepo.save(entry);

		Map<String, Object> res = new HashMap<>();
		res.put("entryId", entry.getId());
		res.put("habitName", targetHabit.getName());
		res.put("lastDone", entry.getLastDone().toString());
		res.put("status", "SUCCESS");
		return res;
	}

	@Tool(description = "Log a daily mood rating with happiness, impetus/energy, and stress levels")
	public Map<String, Object> logDailyMood(
			@ToolParam(description = "Happiness score from 1 (lowest) to 5 (highest)") int happiness,
			@ToolParam(description = "Impetus / drive / energy score from 1 to 5") int impetus,
			@ToolParam(description = "Stress level from 1 (relaxed) to 5 (very stressed)") int stress,
			@ToolParam(description = "Date in format YYYY-MM-DD (optional, defaults to today)", required = false) String dateStr) {

		LocalDate date = (dateStr != null && !dateStr.trim().isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();

		Mood mood = new Mood();
		mood.setDate(date);
		mood.setHappiness(happiness);
		mood.setImpetus(impetus);
		mood.setStress(stress);
		mood = moodRepo.save(mood);

		Map<String, Object> res = new HashMap<>();
		res.put("id", mood.getId());
		res.put("date", mood.getDate().toString());
		res.put("happiness", mood.getHappiness());
		res.put("impetus", mood.getImpetus());
		res.put("stress", mood.getStress());
		res.put("status", "SUCCESS");
		return res;
	}

	@Tool(description = "Log a daily gratitude reflection note")
	public Map<String, Object> logGratitude(
			@ToolParam(description = "Description of what you are grateful for") String description,
			@ToolParam(description = "Date in format YYYY-MM-DD (optional, defaults to today)", required = false) String dateStr) {

		LocalDate date = (dateStr != null && !dateStr.trim().isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();

		Gratitude gratitude = new Gratitude();
		gratitude.setDate(date);
		gratitude.setDescription(description);
		gratitude = gratitudeRepo.save(gratitude);

		Map<String, Object> res = new HashMap<>();
		res.put("id", gratitude.getId());
		res.put("date", gratitude.getDate().toString());
		res.put("description", gratitude.getDescription());
		res.put("status", "SUCCESS");
		return res;
	}
}

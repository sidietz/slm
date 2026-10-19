package de.oberamsystems.slm.mcp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.oberamsystems.slm.model.EventType;
import de.oberamsystems.slm.model.Human;
import de.oberamsystems.slm.model.HumanEvent;
import de.oberamsystems.slm.model.HumanEventRepository;
import de.oberamsystems.slm.model.HumanRepository;

@Service
public class McpLifeEventTools {

	@Autowired
	private HumanEventRepository eventRepo;

	@Autowired
	private HumanRepository humanRepo;

	@Tool(description = "Retrieve upcoming life events, anniversaries, birthdays, memorials, and milestones for contacts sorted by days until occurrence")
	public List<Map<String, Object>> getUpcomingEvents(
			@ToolParam(description = "Maximum days ahead to look for upcoming events (default 365)", required = false) Integer daysAhead) {

		int maxDays = (daysAhead != null && daysAhead > 0) ? daysAhead : 365;
		List<HumanEvent> allEvents = eventRepo.findAllByOrderByDaysUntilNextAsc();
		List<Map<String, Object>> result = new ArrayList<>();

		for (HumanEvent event : allEvents) {
			Integer days = event.getDaysUntilNext();
			if (days != null && days <= maxDays) {
				Map<String, Object> m = new HashMap<>();
				m.put("id", event.getId());
				m.put("title", event.getTitle());
				m.put("type", event.getEventType() != null ? event.getEventType().name() : null);
				m.put("typeDisplay", event.getEventType() != null ? event.getEventType().getDisplayName() : null);
				m.put("contactName", event.getHuman() != null ? event.getHuman().getFullName() : null);
				m.put("contactId", event.getHuman() != null ? event.getHuman().getId() : null);
				m.put("secondaryContactName", event.getSecondaryHuman() != null ? event.getSecondaryHuman().getFullName() : null);
				m.put("eventDate", event.getEventDate() != null ? event.getEventDate().toString() : null);
				m.put("nextOccurrence", event.getNextOccurrence() != null ? event.getNextOccurrence().toString() : null);
				m.put("daysUntilNext", event.getDaysUntilNext());
				m.put("yearsElapsed", event.getYearsElapsed());
				m.put("notes", event.getNotes());
				result.add(m);
			}
		}

		return result;
	}

	@Tool(description = "Retrieve all life events and anniversaries recorded for a specific contact")
	public List<Map<String, Object>> getEventsForContact(
			@ToolParam(description = "ID of the contact/human") Long contactId) {

		List<HumanEvent> events = eventRepo.findByHumanIdOrderByDaysUntilNextAsc(contactId);
		List<Map<String, Object>> result = new ArrayList<>();

		for (HumanEvent event : events) {
			Map<String, Object> m = new HashMap<>();
			m.put("id", event.getId());
			m.put("title", event.getTitle());
			m.put("type", event.getEventType() != null ? event.getEventType().name() : null);
			m.put("eventDate", event.getEventDate() != null ? event.getEventDate().toString() : null);
			m.put("nextOccurrence", event.getNextOccurrence() != null ? event.getNextOccurrence().toString() : null);
			m.put("daysUntilNext", event.getDaysUntilNext());
			m.put("yearsElapsed", event.getYearsElapsed());
			m.put("notes", event.getNotes());
			result.add(m);
		}

		return result;
	}

	@Tool(description = "Create a new life event, anniversary, memorial, or milestone for a contact")
	public Map<String, Object> createLifeEvent(
			@ToolParam(description = "ID of primary contact/human") Long contactId,
			@ToolParam(description = "Event type: BIRTHDAY, MEMORIAL, WEDDING_ANNIVERSARY, RELATIONSHIP_ANNIVERSARY, WORK_ANNIVERSARY, or CUSTOM") String type,
			@ToolParam(description = "Original date in format YYYY-MM-DD") String dateStr,
			@ToolParam(description = "Optional custom title for the milestone", required = false) String title,
			@ToolParam(description = "Optional notes or memories", required = false) String notes,
			@ToolParam(description = "Optional secondary contact ID for couple/wedding anniversaries", required = false) Long secondaryContactId) {

		Human human = humanRepo.findById(contactId)
				.orElseThrow(() -> new IllegalArgumentException("Contact not found with ID: " + contactId));

		EventType eventType = EventType.valueOf(type.toUpperCase());
		LocalDate date = LocalDate.parse(dateStr);

		HumanEvent event = new HumanEvent(human, title, eventType, date);
		event.setNotes(notes);

		if (secondaryContactId != null) {
			humanRepo.findById(secondaryContactId).ifPresent(event::setSecondaryHuman);
		}

		if (title == null || title.trim().isEmpty()) {
			event.setTitle(eventType.getDisplayName() + " – " + human.getFullName());
		}

		event = eventRepo.save(event);

		Map<String, Object> res = new HashMap<>();
		res.put("id", event.getId());
		res.put("title", event.getTitle());
		res.put("type", event.getEventType().name());
		res.put("eventDate", event.getEventDate().toString());
		res.put("status", "SUCCESS");
		return res;
	}
}

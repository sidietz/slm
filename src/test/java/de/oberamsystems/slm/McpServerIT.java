package de.oberamsystems.slm;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import de.oberamsystems.slm.mcp.McpLifeEventTools;
import de.oberamsystems.slm.mcp.McpMedicalTools;
import de.oberamsystems.slm.mcp.McpSessionTools;
import de.oberamsystems.slm.mcp.McpWellnessTools;
import de.oberamsystems.slm.model.CodingSession;
import de.oberamsystems.slm.model.CodingSessionRepository;
import de.oberamsystems.slm.model.Doctor;
import de.oberamsystems.slm.model.DoctorRepository;
import de.oberamsystems.slm.model.EventType;
import de.oberamsystems.slm.model.Habit;
import de.oberamsystems.slm.model.HabitRepository;
import de.oberamsystems.slm.model.Human;
import de.oberamsystems.slm.model.HumanCategory;
import de.oberamsystems.slm.model.HumanEvent;
import de.oberamsystems.slm.model.HumanEventRepository;
import de.oberamsystems.slm.model.HumanRepository;
import de.oberamsystems.slm.model.Speciality;
import de.oberamsystems.slm.model.SpecialityRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class McpServerIT extends AbstractIntegrationTest {

	private static final String API_KEY = "slm-mcp-secret-key-2026";

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private HumanRepository humanRepo;

	@Autowired
	private HumanEventRepository eventRepo;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private CodingSessionRepository codingRepo;

	@Autowired
	private HabitRepository habitRepo;

	@Autowired
	private DoctorRepository doctorRepo;

	@Autowired
	private SpecialityRepository specialityRepo;

	@Autowired
	private McpLifeEventTools lifeEventTools;

	@Autowired
	private McpSessionTools sessionTools;

	@Autowired
	private McpWellnessTools wellnessTools;

	@Autowired
	private McpMedicalTools medicalTools;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = webAppContextSetup(context)
				.apply(springSecurity())
				.build();
	}

	@Test
	void testMcpSecurityApiKeyAuthentication() throws Exception {
		// 1. Unauthenticated request to /sse should return 401 Unauthorized
		mockMvc.perform(get("/sse"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().json("{\"error\":\"Unauthorized: Invalid or missing MCP API key\"}"));

		// 2. Invalid API key should return 401 Unauthorized
		mockMvc.perform(get("/sse").header("X-API-KEY", "wrong-key"))
				.andExpect(status().isUnauthorized());

		// 3. Valid API key via header should connect cleanly to SSE stream
		mockMvc.perform(get("/sse").header("X-API-KEY", API_KEY))
				.andExpect(status().isOk());

		// 4. Valid API key via query parameter (?apiKey=...) should also connect cleanly
		mockMvc.perform(get("/sse").param("apiKey", API_KEY))
				.andExpect(status().isOk());

		// 5. Unauthenticated request to /mcp/message should return 401
		mockMvc.perform(post("/mcp/message"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@Transactional
	void testLifeEventsToolsExecution() {
		Human contact = new Human();
		contact.setFirstname("Max");
		contact.setLastname("Mustermann");
		contact.setCategory(HumanCategory.FAMILY);
		contact.setBirthday(Date.valueOf("1990-06-15"));
		contact = humanRepo.saveAndFlush(contact);

		HumanEvent birthday = new HumanEvent(contact, "Max Birthday", EventType.BIRTHDAY, LocalDate.of(1990, 6, 15));
		eventRepo.saveAndFlush(birthday);

		entityManager.clear();

		List<Map<String, Object>> upcoming = lifeEventTools.getUpcomingEvents(365);
		assertFalse(upcoming.isEmpty(), "Should return upcoming life events");
		assertTrue(upcoming.stream().anyMatch(e -> "Max Birthday".equals(e.get("title"))));

		List<Map<String, Object>> forContact = lifeEventTools.getEventsForContact(contact.getId());
		assertEquals(1, forContact.size());

		Map<String, Object> created = lifeEventTools.createLifeEvent(
				contact.getId(), "WORK_ANNIVERSARY", "2020-09-01", "Joined Company", "5 years milestone", null);
		assertNotNull(created.get("id"));
		assertEquals("SUCCESS", created.get("status"));
	}

	@Test
	@Transactional
	void testSessionToolsExecution() {
		CodingSession cs = new CodingSession();
		cs.setProject("SLM");
		cs.setStart(LocalDateTime.of(2026, 10, 18, 10, 0));
		cs.setEnd(LocalDateTime.of(2026, 10, 18, 12, 30));
		codingRepo.saveAndFlush(cs);

		Map<String, Object> balance = sessionTools.getDailyLifeBalance("2026-10-18");
		assertNotNull(balance);
		assertTrue((Long) balance.get("productivityMinutes") >= 150);

		List<Map<String, Object>> recent = sessionTools.getRecentSessions("PRODUCTIVITY", 5);
		assertFalse(recent.isEmpty());

		Map<String, Object> logged = sessionTools.logCodingSession(
				"Antigravity", "2026-10-18T14:00", "2026-10-18T16:00", 15, 5, "Completed MCP server", "Productive", "Java", "develop");
		assertNotNull(logged.get("id"));
		assertEquals("SUCCESS", logged.get("status"));
	}

	@Test
	@Transactional
	void testWellnessToolsExecution() {
		Habit h = new Habit();
		h.setName("Meditate");
		h.setDescription("Morning mindfulness session");
		h = habitRepo.saveAndFlush(h);

		List<Map<String, Object>> habits = wellnessTools.getHabitsStatus();
		assertFalse(habits.isEmpty());

		Map<String, Object> done = wellnessTools.recordHabitCompleted("Meditate", "2026-10-18T08:00");
		assertEquals("SUCCESS", done.get("status"));

		Map<String, Object> mood = wellnessTools.logDailyMood(5, 4, 1, "2026-10-18");
		assertEquals("SUCCESS", mood.get("status"));
		assertEquals(5, mood.get("happiness"));

		Map<String, Object> gratitude = wellnessTools.logGratitude("Great progress on MCP integration", "2026-10-18");
		assertEquals("SUCCESS", gratitude.get("status"));
	}

	@Test
	@Transactional
	void testMedicalToolsExecution() {
		Speciality spec = new Speciality();
		spec.setTitle("Cardiology");
		spec = specialityRepo.saveAndFlush(spec);

		Doctor doc = new Doctor();
		doc.setFirstname("Dr. Hans");
		doc.setLastname("Schmidt");
		doc.setCity("Munich");
		doc.setSpeciality(spec);
		doctorRepo.saveAndFlush(doc);

		List<Map<String, Object>> doctors = medicalTools.searchDoctors("Cardiology");
		assertFalse(doctors.isEmpty());
		assertEquals("Dr. Hans Schmidt", doctors.get(0).get("name"));
		assertEquals("Cardiology", doctors.get(0).get("speciality"));
	}
}

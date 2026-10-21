package de.oberamsystems.slm;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import de.oberamsystems.slm.model.EventType;
import de.oberamsystems.slm.model.Human;
import de.oberamsystems.slm.model.HumanCategory;
import de.oberamsystems.slm.model.HumanEvent;
import de.oberamsystems.slm.model.HumanEventRepository;
import de.oberamsystems.slm.model.HumanRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class HumanEventIT extends AbstractIntegrationTest {

	@Autowired
	private HumanRepository humanRepo;

	@Autowired
	private HumanEventRepository eventRepo;

	@Autowired
	private EntityManager entityManager;

	@Test
	@Transactional
	void testHumanDeceasedAndEventFormulas() {
		// 1. Create deceased contact
		Human grandpa = new Human();
		grandpa.setFirstname("Hans");
		grandpa.setLastname("Mustermann");
		grandpa.setCategory(HumanCategory.FAMILY);
		grandpa.setBirthday(Date.valueOf("1940-05-10"));
		grandpa.setDeathDate(Date.valueOf("2020-11-15"));
		grandpa = humanRepo.saveAndFlush(grandpa);

		entityManager.clear();
		grandpa = humanRepo.findById(grandpa.getId()).orElseThrow();

		assertTrue(grandpa.isDeceased());
		assertEquals(80, grandpa.getAgeAtDeath());

		// 2. Create Memorial event
		HumanEvent memorial = new HumanEvent(grandpa, "Grandpa Memorial", EventType.MEMORIAL, LocalDate.of(2020, 11, 15));
		memorial = eventRepo.saveAndFlush(memorial);

		entityManager.clear();
		memorial = eventRepo.findById(memorial.getId()).orElseThrow();

		assertNotNull(memorial.getId());
		assertNotNull(memorial.getNextOccurrence());
		assertNotNull(memorial.getDaysUntilNext());
		assertTrue(memorial.getDaysUntilNext() >= 0);
		assertTrue(memorial.getYearsElapsed() >= 4);

		// 3. Create couple anniversary (Wedding)
		Human partner = new Human();
		partner.setFirstname("Erika");
		partner.setLastname("Mustermann");
		partner.setCategory(HumanCategory.FAMILY);
		partner.setBirthday(Date.valueOf("1945-08-20"));
		partner = humanRepo.saveAndFlush(partner);

		grandpa = humanRepo.findById(grandpa.getId()).orElseThrow();

		HumanEvent wedding = new HumanEvent(grandpa, "Golden Wedding", EventType.WEDDING_ANNIVERSARY, LocalDate.of(1965, 6, 20));
		wedding.setSecondaryHuman(partner);
		wedding = eventRepo.saveAndFlush(wedding);

		entityManager.clear();
		wedding = eventRepo.findById(wedding.getId()).orElseThrow();

		assertNotNull(wedding.getId());
		assertEquals("Erika", wedding.getSecondaryHuman().getFirstname());
		assertTrue(wedding.getYearsElapsed() >= 60);

		// 4. Query upcoming events sorted
		List<HumanEvent> events = eventRepo.findAllByOrderByDaysUntilNextAsc();
		assertFalse(events.isEmpty());
	}

	@Autowired
	private WebApplicationContext context;

	@Test
	void testSecurityPermitsHumanEvents() throws Exception {
		MockMvc mockMvc = webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		// /human-events should be publicly accessible (HTTP 200 OK)
		mockMvc.perform(get("/human-events"))
				.andExpect(status().isOk());

		// /events should be publicly accessible (HTTP 200 OK)
		mockMvc.perform(get("/events"))
				.andExpect(status().isOk());

		// Non-permitted endpoints should trigger authentication challenge (HTTP 401 Unauthorized)
		mockMvc.perform(get("/unauthorized-test-endpoint"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void testAddLecturerInHumansSection() throws Exception {
		MockMvc mockMvc = webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		// 1. GET /humans should be accessible and return HTTP 200 OK
		mockMvc.perform(get("/humans"))
				.andExpect(status().isOk());

		// 2. Submit new lecturer contact via POST /humans (including empty id param from modal form)
		mockMvc.perform(post("/humans")
				.with(csrf())
				.param("id", "")
				.param("firstname", "Donald")
				.param("lastname", "Knuth")
				.param("birthday", "1938-01-10")
				.param("category", "LECTURER"))
				.andExpect(status().isOk());

		// 3. Verify Donald Knuth was persisted with category LECTURER
		List<Human> lecturers = humanRepo.findByCategory(HumanCategory.LECTURER);
		assertFalse(lecturers.isEmpty(), "Lecturer list should contain at least 1 lecturer");
		boolean found = lecturers.stream()
				.anyMatch(h -> "Donald".equals(h.getFirstname()) && "Knuth".equals(h.getLastname()) && h.getCategory() == HumanCategory.LECTURER);
		assertTrue(found, "Lecturer Donald Knuth should be persisted with category LECTURER");
	}

	@Test
	void testModalCreateHumanEventWithEmptyIdParam() throws Exception {
		MockMvc mockMvc = webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		Human contact = humanRepo.saveAndFlush(new Human(null, "Grace", "Hopper", Date.valueOf("1906-12-09"), HumanCategory.FRIEND));

		// 1. Submit event via modal form with empty id and explicit humanId
		mockMvc.perform(post("/human-events")
						.with(csrf())
						.param("id", "")
						.param("eventType", "BIRTHDAY")
						.param("humanId", contact.getId().toString())
						.param("eventDate", "1906-12-09")
						.param("notes", "Pioneer in computer science"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/human-events"));

		assertEquals(1, eventRepo.count());
		HumanEvent event = eventRepo.findAll().getFirst();
		assertEquals("Grace", event.getHuman().getFirstname());
		assertNotNull(event.getId());

		// 2. Submit event via modal form with empty id and omitted humanId (tests fallback)
		mockMvc.perform(post("/human-events")
						.with(csrf())
						.param("id", "")
						.param("eventType", "WORK_ANNIVERSARY")
						.param("eventDate", "2020-05-01"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/human-events"));

		assertEquals(2, eventRepo.count());
	}
}


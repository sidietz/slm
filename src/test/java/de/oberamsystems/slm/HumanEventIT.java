package de.oberamsystems.slm;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

public class HumanEventIT extends AbstractIntegrationTest {

	@Autowired
	private HumanRepository humanRepo;

	@Autowired
	private HumanEventRepository eventRepo;

	@Autowired
	private jakarta.persistence.EntityManager entityManager;

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
}

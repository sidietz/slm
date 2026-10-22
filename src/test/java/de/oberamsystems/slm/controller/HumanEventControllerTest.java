package de.oberamsystems.slm.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

import de.oberamsystems.slm.model.EventType;
import de.oberamsystems.slm.model.Human;
import de.oberamsystems.slm.model.HumanCategory;
import de.oberamsystems.slm.model.HumanEvent;
import de.oberamsystems.slm.model.HumanEventRepository;
import de.oberamsystems.slm.model.HumanRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HumanEventControllerTest {

	private HumanEventController controller;
	private HumanEventRepository eventRepo;
	private HumanRepository humanRepo;

	@BeforeEach
	void setUp() throws Exception {
		controller = new HumanEventController();
		eventRepo = mock(HumanEventRepository.class);
		humanRepo = mock(HumanRepository.class);

		java.lang.reflect.Field f1 = HumanEventController.class.getDeclaredField("eventRepo");
		f1.setAccessible(true);
		f1.set(controller, eventRepo);

		java.lang.reflect.Field f2 = HumanEventController.class.getDeclaredField("humanRepo");
		f2.setAccessible(true);
		f2.set(controller, humanRepo);
	}

	@Test
	void testListEventsAll() {
		when(eventRepo.findAllByOrderByDaysUntilNextAsc()).thenReturn(Collections.emptyList());
		when(humanRepo.findAll()).thenReturn(Collections.emptyList());

		ConcurrentModel model = new ConcurrentModel();
		String view = controller.listEvents(null, null, model);

		assertEquals("human-events", view);
		assertNotNull(model.getAttribute("events"));
		assertEquals("ALL", model.getAttribute("activeType"));
	}

	@Test
	void testListEventsFilterByType() {
		when(eventRepo.findByEventTypeOrderByDaysUntilNextAsc(EventType.MEMORIAL)).thenReturn(Collections.emptyList());
		when(eventRepo.findAllByOrderByDaysUntilNextAsc()).thenReturn(Collections.emptyList());
		when(humanRepo.findAll()).thenReturn(Collections.emptyList());

		ConcurrentModel model = new ConcurrentModel();
		String view = controller.listEvents(EventType.MEMORIAL, null, model);

		assertEquals("human-events", view);
		assertEquals("MEMORIAL", model.getAttribute("activeType"));
	}

	@Test
	void testSaveEvent() {
		Human h = new Human(1L, "Max", "Mustermann", new java.util.Date(), HumanCategory.FAMILY);
		when(humanRepo.existsById(1L)).thenReturn(true);
		HumanEvent event = new HumanEvent(h, "25th Anniversary", EventType.WEDDING_ANNIVERSARY, LocalDate.of(2001, 6, 15));

		String view = controller.saveEvent(event, null, null);

		assertEquals("redirect:/human-events", view);
		verify(eventRepo).save(event);
	}

	@Test
	void testDeleteEvent() {
		String view = controller.deleteEvent(42L);

		assertEquals("redirect:/human-events", view);
		verify(eventRepo).deleteById(42L);
	}
}

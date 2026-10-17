package de.oberamsystems.slm.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class WorkingSessionTest {

	@Test
	public void testWorkingSessionGettersAndSetters() {
		WorkingSession ws = new WorkingSession();
		Contractor client = new Contractor(1L, "Acme Corp");
		ws.setClient(client);
		ws.setProject("Enterprise Cloud");
		ws.setBillable(true);
		ws.setHourlyRate(new BigDecimal("125.50"));
		ws.setTaskCategory("Backend Development");
		ws.setStart(LocalDateTime.now().minusHours(2));
		ws.setEnd(LocalDateTime.now());
		ws.prePersist();

		assertNotNull(ws.getId());
		assertEquals(client, ws.getClient());
		assertEquals("Enterprise Cloud", ws.getProject());
		assertTrue(ws.getBillable());
		assertEquals(new BigDecimal("125.50"), ws.getHourlyRate());
		assertEquals("Backend Development", ws.getTaskCategory());
		assertTrue(ws.toString().contains("Enterprise Cloud"));
	}

	@Test
	public void testWorkingSessionConstructor() {
		Contractor client = new Contractor(2L, "Globex");
		WorkingSession ws = new WorkingSession(client, "Core API", false, new BigDecimal("95.00"), "Consulting");
		assertEquals(client, ws.getClient());
		assertEquals("Core API", ws.getProject());
		assertFalse(ws.getBillable());
		assertEquals(new BigDecimal("95.00"), ws.getHourlyRate());
		assertEquals("Consulting", ws.getTaskCategory());
	}
}

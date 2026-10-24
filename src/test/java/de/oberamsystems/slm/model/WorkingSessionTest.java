package de.oberamsystems.slm.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class WorkingSessionTest {

	@Test
	public void testWorkingSessionGettersAndSetters() {
		WorkingSession ws = new WorkingSession();
		Employer employer = new Employer(1L, "Acme Corp");
		ws.setEmployer(employer);
		ws.setProject("Enterprise Cloud");
		ws.setBillable(true);
		ws.setHourlyRate(new BigDecimal("125.50"));
		ws.setTaskCategory("Backend Development");
		ws.setStart(OffsetDateTime.now().minusHours(2));
		ws.setEnd(OffsetDateTime.now());
		ws.prePersist();

		assertNotNull(ws.getId());
		assertEquals(employer, ws.getEmployer());
		assertEquals("Enterprise Cloud", ws.getProject());
		assertTrue(ws.getBillable());
		assertEquals(new BigDecimal("125.50"), ws.getHourlyRate());
		assertEquals("Backend Development", ws.getTaskCategory());
		assertTrue(ws.toString().contains("Enterprise Cloud"));
		assertTrue(ws.toString().contains("Acme Corp"));
	}

	@Test
	public void testWorkingSessionConstructor() {
		Employer employer = new Employer(2L, "Globex");
		WorkingSession ws = new WorkingSession(employer, "Core API", false, new BigDecimal("95.00"), "Consulting");
		assertEquals(employer, ws.getEmployer());
		assertEquals("Core API", ws.getProject());
		assertFalse(ws.getBillable());
		assertEquals(new BigDecimal("95.00"), ws.getHourlyRate());
		assertEquals("Consulting", ws.getTaskCategory());
	}
}

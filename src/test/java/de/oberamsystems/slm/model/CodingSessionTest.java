package de.oberamsystems.slm.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CodingSessionTest {

	@Test
	public void testCodingSessionGettersAndSetters() {
		CodingSession cs = new CodingSession();
		cs.setProject("slm");
		cs.setRepository("github.com/oberamsystems/slm");
		cs.setBranch("feat/generalized-sessions");
		cs.setTicket("SLM-42");
		cs.setLanguage("Java 27");
		cs.setStart(LocalDateTime.now().minusHours(1));
		cs.setEnd(LocalDateTime.now());
		cs.prePersist();

		assertNotNull(cs.getId());
		assertEquals("slm", cs.getProject());
		assertEquals("github.com/oberamsystems/slm", cs.getRepository());
		assertEquals("feat/generalized-sessions", cs.getBranch());
		assertEquals("SLM-42", cs.getTicket());
		assertEquals("Java 27", cs.getLanguage());
		assertTrue(cs.toString().contains("slm"));
	}

	@Test
	public void testCodingSessionConstructor() {
		CodingSession cs = new CodingSession("slm", "repo", "main", "TASK-1", "Java");
		assertEquals("slm", cs.getProject());
		assertEquals("repo", cs.getRepository());
		assertEquals("main", cs.getBranch());
		assertEquals("TASK-1", cs.getTicket());
		assertEquals("Java", cs.getLanguage());
	}
}

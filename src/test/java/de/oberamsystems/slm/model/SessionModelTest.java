package de.oberamsystems.slm.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class SessionModelTest {

	@Test
	public void testPrePersistGeneratesUuidV7() {
		// Anonymous subclass of abstract Session for testing
		Session session = new Session() {
		};

		assertNull(session.getId());
		session.prePersist();

		assertNotNull(session.getId());
		assertInstanceOf(UUID.class, session.getId());
		assertEquals(7, session.getId().version(), "Generated UUID must be RFC 9562 Version 7");

		// If ID is already set, prePersist should not overwrite it
		UUID existingId = UUID.fromString("0192e200-0000-7000-8000-000000000001");
		session.setId(existingId);
		session.prePersist();
		assertEquals(existingId, session.getId());
	}

	@Test
	public void testSessionFieldsAndDefaults() {
		Session session = new Session() {
		};

		assertEquals("COMPLETED", session.getStatus());
		assertEquals(Duration.ZERO, session.getBreakDuration());

		LocalDateTime start = LocalDateTime.of(2026, 10, 17, 10, 0);
		LocalDateTime end = LocalDateTime.of(2026, 10, 17, 12, 0);
		Duration breakDur = Duration.ofMinutes(15);
		Duration grossDur = Duration.ofHours(2);
		Duration netDur = Duration.ofMinutes(105);
		UUID testUuid = UUID.fromString("0192e200-0000-7000-8000-000000000002");

		session.setId(testUuid);
		session.setStatus("IN_PROGRESS");
		session.setStart(start);
		session.setEnd(end);
		session.setBreakDuration(breakDur);
		session.setDuration(grossDur);
		session.setNetDuration(netDur);
		session.setFocusRating(4);
		session.setComment("Deep focus work");
		session.setOutcomeSummary("Implemented feature X");

		assertEquals(testUuid, session.getId());
		assertEquals("IN_PROGRESS", session.getStatus());
		assertEquals(start, session.getStart());
		assertEquals(end, session.getEnd());
		assertEquals(breakDur, session.getBreakDuration());
		assertEquals(grossDur, session.getDuration());
		assertEquals(netDur, session.getNetDuration());
		assertEquals(4, session.getFocusRating());
		assertEquals("Deep focus work", session.getComment());
		assertEquals("Implemented feature X", session.getOutcomeSummary());
		assertTrue(session.toString().contains(testUuid.toString()));
	}

	@Test
	public void testDefaultStartTimeInitializedToNow() {
		Session session = new Session() {
		};
		assertNotNull(session.getStart(), "Start time must be initialized when created");
		assertNull(session.getEnd(), "End time must be null for ongoing sessions by default");
		LocalDateTime now = LocalDateTime.now();
		assertEquals(0, session.getStart().getSecond());
		assertEquals(0, session.getStart().getNano());
		assertTrue(Duration.between(session.getStart(), now).abs().toMinutes() <= 1);
	}

	@Test
	public void testLectureSessionActivityTypeDefaults() {
		LectureSession ls = new LectureSession();
		assertEquals(LectureActivityType.LECTURE, ls.getActivityType());

		for (LectureActivityType type : LectureActivityType.values()) {
			ls.setActivityType(type);
			assertEquals(type, ls.getActivityType());
			assertNotNull(type.getDisplayName());
			assertNotNull(type.getBootstrapIcon());
			assertNotNull(type.getBadgeCssClass());
		}

		assertEquals(7, LectureActivityType.values().length);
	}
}

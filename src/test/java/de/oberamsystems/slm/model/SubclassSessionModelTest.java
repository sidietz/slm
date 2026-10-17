package de.oberamsystems.slm.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SubclassSessionModelTest {

	@Test
	public void testLearningSession() {
		LearningItem item = new LearningItem();
		item.setTitle("Modern Java");
		LearningSession ls = new LearningSession(item, 5);

		assertEquals(item, ls.getLearningItem());
		assertEquals(5, ls.getUnitsCompleted());

		ls.setStart(LocalDateTime.now().minusHours(1));
		ls.setEnd(LocalDateTime.now());
		ls.prePersist();
		assertNotNull(ls.getId());
		assertTrue(ls.toString().contains("Modern Java"));
	}

	@Test
	public void testGamingSession() {
		Game game = new Game();
		Device device = new Device();
		GamingSession gs = new GamingSession(game, device, "Multiplayer");

		assertEquals(game, gs.getGame());
		assertEquals(device, gs.getDevice());
		assertEquals("Multiplayer", gs.getGameMode());

		gs.prePersist();
		assertNotNull(gs.getId());
		assertTrue(gs.toString().contains("Multiplayer"));
	}

	@Test
	public void testReadingSession() {
		Book book = new Book();
		Set<Book> bookSet = new HashSet<>();
		bookSet.add(book);
		ReadingSession rs = new ReadingSession(book, 10, 50, 40.0f, bookSet);

		assertEquals(book, rs.getBook());
		assertEquals(10, rs.getStartPageCount());
		assertEquals(50, rs.getEndPageCount());
		assertEquals(40.0f, rs.getReadingSpeed());
		assertEquals(bookSet, rs.getBooks());

		rs.prePersist();
		assertNotNull(rs.getId());
	}

	@Test
	public void testMeditationSession() {
		MeditationSession ms = new MeditationSession();
		ms.setComment("Mindfulness session");
		ms.prePersist();

		assertNotNull(ms.getId());
		assertEquals("Mindfulness session", ms.getComment());
		assertTrue(ms.toString().contains("MeditationSession"));
	}

	@Test
	public void testSportSession() {
		SportType type = new SportType();
		type.setName("Running");
		SportSession ss = new SportSession(type);

		assertEquals(type, ss.getType());
		ss.prePersist();
		assertNotNull(ss.getId());
		assertTrue(ss.toString().contains("SportSession"));
	}
}

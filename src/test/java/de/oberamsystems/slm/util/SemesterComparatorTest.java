package de.oberamsystems.slm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SemesterComparatorTest {

	@Test
	public void testSemesterChronologicalDescendingOrder() {
		List<String> semesters = new ArrayList<>(Arrays.asList(
				"SS 2025",
				"WiSe 2026/27",
				"SoSe 2027",
				"WS 2025/26",
				"Wintersemester 2027/28",
				"SoSe 2026"
		));

		semesters.sort(SemesterComparator.DESCENDING);

		List<String> expected = Arrays.asList(
				"Wintersemester 2027/28",
				"SoSe 2027",
				"WiSe 2026/27",
				"SoSe 2026",
				"WS 2025/26",
				"SS 2025"
		);

		assertEquals(expected, semesters);
	}

	@Test
	public void testVariousNotationFormats() {
		// Verify winter notation
		assertTrue(SemesterComparator.DESCENDING.compare("WiSe 2026/27", "SoSe 2026") < 0);
		assertTrue(SemesterComparator.DESCENDING.compare("WS 2026/27", "SoSe 2026") < 0);
		assertTrue(SemesterComparator.DESCENDING.compare("Wintersemester 2026/27", "Sommersemester 2026") < 0);
		assertTrue(SemesterComparator.DESCENDING.compare("Summer 2027", "WiSe 2026/27") < 0);
		assertTrue(SemesterComparator.DESCENDING.compare("SS 2027", "WS 2026/27") < 0);

		// Two-digit year notations
		assertTrue(SemesterComparator.DESCENDING.compare("WiSe 26/27", "SS 26") < 0);
	}

	@Test
	public void testNullAndBlankAndUnparsableHandling() {
		List<String> semesters = new ArrayList<>(Arrays.asList(
				null,
				"Unknown",
				"WiSe 2026/27",
				"",
				"SoSe 2026",
				"   "
		));

		semesters.sort(SemesterComparator.DESCENDING);

		assertEquals("WiSe 2026/27", semesters.get(0));
		assertEquals("SoSe 2026", semesters.get(1));
		assertEquals("Unknown", semesters.get(2));
		// blanks and null at the bottom
		assertTrue(semesters.subList(3, 6).contains(null));
		assertTrue(semesters.subList(3, 6).contains(""));
		assertTrue(semesters.subList(3, 6).contains("   "));
	}

	@Test
	public void testTieBreakingIdenticalRank() {
		List<String> semesters = new ArrayList<>(Arrays.asList(
				"WS 2026/27",
				"WiSe 2026/27"
		));

		semesters.sort(SemesterComparator.DESCENDING);
		// WiSe vs WS case-insensitive tie break
		assertEquals("WiSe 2026/27", semesters.get(0));
		assertEquals("WS 2026/27", semesters.get(1));
	}
}

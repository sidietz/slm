package de.oberamsystems.slm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.oberamsystems.slm.model.Course;

public class CourseComparatorTest {

	private Course createCourse(Long id, String title, String semester) {
		Course c = new Course();
		try {
			Field idField = Course.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(c, id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		c.setTitle(title);
		c.setSemester(semester);
		return c;
	}

	@Test
	public void testCourseOrderingWithinSameSemester() {
		// When courses belong to the same semester, they must sort strictly alphabetically ascending by title
		Course c1 = createCourse(1L, "Distributed Systems", "WiSe 2026/27");
		Course c2 = createCourse(2L, "Algorithms and Data Structures", "WiSe 2026/27");
		Course c3 = createCourse(3L, "Cloud Architecture", "WiSe 2026/27");

		List<Course> courses = new ArrayList<>(Arrays.asList(c1, c2, c3));
		courses.sort(CourseComparator.INSTANCE);

		assertEquals("Algorithms and Data Structures", courses.get(0).getTitle());
		assertEquals("Cloud Architecture", courses.get(1).getTitle());
		assertEquals("Distributed Systems", courses.get(2).getTitle());
	}

	@Test
	public void testCourseOrderingAcrossDifferentSemesters() {
		// Most recent semester first, and within each semester alphabetically ascending
		Course c1 = createCourse(1L, "Linear Algebra", "SoSe 2026");
		Course c2 = createCourse(2L, "Distributed Systems", "WiSe 2026/27");
		Course c3 = createCourse(3L, "Applied Cryptography", "WiSe 2026/27");
		Course c4 = createCourse(4L, "Machine Learning", "SoSe 2027");

		List<Course> courses = new ArrayList<>(Arrays.asList(c1, c2, c3, c4));
		courses.sort(CourseComparator.INSTANCE);

		// SoSe 2027 (newest)
		assertEquals("Machine Learning", courses.get(0).getTitle());
		assertEquals("SoSe 2027", courses.get(0).getSemester());

		// WiSe 2026/27: Applied Cryptography before Distributed Systems
		assertEquals("Applied Cryptography", courses.get(1).getTitle());
		assertEquals("WiSe 2026/27", courses.get(1).getSemester());

		assertEquals("Distributed Systems", courses.get(2).getTitle());
		assertEquals("WiSe 2026/27", courses.get(2).getSemester());

		// SoSe 2026 (oldest)
		assertEquals("Linear Algebra", courses.get(3).getTitle());
		assertEquals("SoSe 2026", courses.get(3).getSemester());
	}

	@Test
	public void testCourseOrderingFallbackToId() {
		Course c1 = createCourse(10L, "Databases", "WiSe 2026/27");
		Course c2 = createCourse(5L, "Databases", "WiSe 2026/27");

		List<Course> courses = new ArrayList<>(Arrays.asList(c1, c2));
		courses.sort(CourseComparator.INSTANCE);

		assertEquals(5L, courses.get(0).getId());
		assertEquals(10L, courses.get(1).getId());
	}
}

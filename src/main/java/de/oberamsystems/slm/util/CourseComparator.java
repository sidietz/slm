package de.oberamsystems.slm.util;

import java.util.Comparator;

import de.oberamsystems.slm.model.Course;

/**
 * Deterministic domain-specific comparator for {@link Course} entities.
 * Ordering rules:
 * 1. Semester DESCENDING (most recent semester first via {@link SemesterComparator#DESCENDING})
 * 2. Course Title ASCENDING (case-insensitive)
 * 3. Course ID ASCENDING (deterministic fallback)
 */
public class CourseComparator implements Comparator<Course> {

	public static final CourseComparator INSTANCE = new CourseComparator();

	@Override
	public int compare(Course c1, Course c2) {
		if (c1 == null && c2 == null) return 0;
		if (c1 == null) return 1;
		if (c2 == null) return -1;

		// 1. Primary: Semester DESCENDING (most recent first)
		int semCmp = SemesterComparator.DESCENDING.compare(c1.getSemester(), c2.getSemester());
		if (semCmp != 0) {
			return semCmp;
		}

		// 2. Secondary: Title ASCENDING (case-insensitive)
		String t1 = c1.getTitle() != null ? c1.getTitle() : "";
		String t2 = c2.getTitle() != null ? c2.getTitle() : "";
		int titleCmp = t1.compareToIgnoreCase(t2);
		if (titleCmp != 0) {
			return titleCmp;
		}

		// 3. Tertiary: Course ID ASCENDING (deterministic fallback)
		Long id1 = c1.getId();
		Long id2 = c2.getId();
		if (id1 != null && id2 != null) {
			return id1.compareTo(id2);
		} else if (id1 != null) {
			return -1;
		} else if (id2 != null) {
			return 1;
		}

		return 0;
	}
}

package de.oberamsystems.slm.util;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares academic semester strings chronologically in descending order (most recent first).
 * Supports German and European semester notations such as:
 * - "WiSe 2026/27", "WS 2026/27", "Wintersemester 2026/27"
 * - "SoSe 2027", "SS 2027", "Sommersemester 2027", "Summer 2027"
 * Null or blank semester strings are ordered last.
 */
public class SemesterComparator implements Comparator<String> {

	public static final SemesterComparator DESCENDING = new SemesterComparator();
	public static final Comparator<String> ASCENDING = DESCENDING.reversed();

	private static final Pattern YEAR_4DIGIT_PATTERN = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");
	private static final Pattern YEAR_2DIGIT_PATTERN = Pattern.compile("\\b(\\d{2})[/\\\\](\\d{2})\\b");
	private static final Pattern WINTER_PATTERN = Pattern.compile("(?i)\\b(wise|ws|winter|wintersemester)\\b");
	private static final Pattern SUMMER_PATTERN = Pattern.compile("(?i)\\b(sose|ss|sommer|sommersemester|summer)\\b");

	@Override
	public int compare(String s1, String s2) {
		if (s1 == null && s2 == null) return 0;
		if (s1 == null || s1.isBlank()) return 1;
		if (s2 == null || s2.isBlank()) return -1;

		Long rank1 = parseSemesterRank(s1);
		Long rank2 = parseSemesterRank(s2);

		if (rank1 != null && rank2 != null) {
			int cmp = Long.compare(rank2, rank1); // descending: higher rank (more recent) comes first
			if (cmp != 0) {
				return cmp;
			}
			return s1.compareToIgnoreCase(s2);
		} else if (rank1 != null) {
			return -1; // valid semester comes before unparsable text
		} else if (rank2 != null) {
			return 1;
		}

		return s1.compareToIgnoreCase(s2);
	}

	public static Long parseSemesterRank(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String s = raw.trim();

		// 1. Extract base year
		Integer year = null;
		Matcher m4 = YEAR_4DIGIT_PATTERN.matcher(s);
		if (m4.find()) {
			year = Integer.parseInt(m4.group(1));
		} else {
			Matcher m2 = YEAR_2DIGIT_PATTERN.matcher(s);
			if (m2.find()) {
				int y2 = Integer.parseInt(m2.group(1));
				year = (y2 >= 70) ? (1900 + y2) : (2000 + y2);
			}
		}

		if (year == null) {
			return null;
		}

		// 2. Determine term: Winter vs Summer vs default
		boolean isWinter = WINTER_PATTERN.matcher(s).find();
		boolean isSummer = SUMMER_PATTERN.matcher(s).find();

		// If neither matched explicitly, slash or dash notation (e.g. "2026/27") indicates winter semester
		if (!isWinter && !isSummer) {
			if (s.contains("/") || s.contains("-")) {
				isWinter = true;
			}
		}

		long termOffset = isWinter ? 1 : 0;
		return ((long) year * 2) + termOffset;
	}
}

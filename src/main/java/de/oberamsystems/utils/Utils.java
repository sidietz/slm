package de.oberamsystems.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {

	public static String LocalDateTimeToString(LocalDateTime ldt) {
		return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
	
	public static String DurationToString(Duration dur) {
		return String.format("%s day(s) %s hour(s) and %s minute(s)",dur.toDaysPart(), dur.toHoursPart(), dur.toMinutesPart());
	}
}

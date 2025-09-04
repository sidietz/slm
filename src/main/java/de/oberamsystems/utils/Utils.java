package de.oberamsystems.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Utils {

	private static String PATTERN = "dd.MM.yyyy HH:mm";

	public static String LocalDateTimeToString(LocalDateTime ldt) {
		return ldt.format(DateTimeFormatter.ofPattern(PATTERN));
	}

	public static String LocalDateTimeToString(Date ldt) {
		return new SimpleDateFormat(PATTERN).format(ldt);
	}

	public static String LocalDateTimeToString(LocalDate ldt) {
		return new SimpleDateFormat(PATTERN).format(ldt);
	}

	
	public static String DurationToString(Duration dur) {
		return String.format("%s day(s) %s hour(s) and %s minute(s)",dur.toDaysPart(), dur.toHoursPart(), dur.toMinutesPart());
	}
}

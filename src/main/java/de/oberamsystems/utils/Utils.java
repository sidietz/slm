package de.oberamsystems.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import de.oberamsystems.slm.config.TimeZoneConfig;

public class Utils {

	private static String PATTERN = "dd.MM.yyyy HH:mm";

	public static OffsetDateTime toOffset(int year, int month, int day, int hour, int minute, ZoneId zoneId) {
		return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zoneId).toOffsetDateTime();
	}

	public static OffsetDateTime toOffset(int year, int month, int day, int hour, int minute, int second, ZoneId zoneId) {
		return ZonedDateTime.of(year, month, day, hour, minute, second, 0, zoneId).toOffsetDateTime();
	}

	public static String OffsetDateTimeToString(OffsetDateTime odt) {
		if (odt == null) {
			return "";
		}
		return odt.atZoneSameInstant(TimeZoneConfig.getZoneId()).format(DateTimeFormatter.ofPattern(PATTERN));
	}

	public static String LocalDateTimeToString(OffsetDateTime odt) {
		return OffsetDateTimeToString(odt);
	}

	public static String LocalDateTimeToString(LocalDateTime ldt) {
		if (ldt == null) {
			return "";
		}
		return ldt.format(DateTimeFormatter.ofPattern(PATTERN));
	}

	public static String LocalDateTimeToString(Date ldt) {
		if (ldt == null) {
			return "";
		}
		return new SimpleDateFormat(PATTERN).format(ldt);
	}

	public static String LocalDateTimeToString(LocalDate ldt) {
		if (ldt == null) {
			return "";
		}
		return new SimpleDateFormat(PATTERN).format(ldt);
	}

	
	public static String DurationToString(Duration dur) {
		return String.format("%s day(s) %s hour(s) and %s minute(s)",dur.toDaysPart(), dur.toHoursPart(), dur.toMinutesPart());
	}

	public static LocalDate dateToLocalDate(Date date) {
		if (date == null) {
			return null;
		}
		if (date instanceof java.sql.Date sqlDate) {
			return sqlDate.toLocalDate();
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
}

package de.oberamsystems.slm.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.core.convert.converter.Converter;

import de.oberamsystems.slm.config.TimeZoneConfig;

public class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

	@Override
	public OffsetDateTime convert(String source) {
		return parse(source);
	}

	public static OffsetDateTime parse(String source) {
		if (source == null || source.trim().isEmpty()) {
			return null;
		}
		String trimmed = source.trim();
		// 1. Try standard ISO OffsetDateTime parse (e.g. 2026-10-24T14:30:00+02:00 or 2026-10-24T14:30:00Z)
		try {
			return OffsetDateTime.parse(trimmed);
		} catch (DateTimeParseException ignored) {
		}

		// 2. Try LocalDateTime parse (e.g. HTML5 datetime-local "2026-10-24T14:30" or "2026-10-24T14:30:00")
		try {
			LocalDateTime ldt = LocalDateTime.parse(trimmed);
			return ldt.atZone(TimeZoneConfig.getZoneId()).toOffsetDateTime();
		} catch (DateTimeParseException ignored) {
		}

		// 3. Fallback for pure LocalDate string "2026-10-24"
		try {
			LocalDate ld = LocalDate.parse(trimmed);
			return ld.atStartOfDay(TimeZoneConfig.getZoneId()).toOffsetDateTime();
		} catch (DateTimeParseException ignored) {
		}

		throw new IllegalArgumentException("Cannot parse '" + source + "' into OffsetDateTime");
	}
}

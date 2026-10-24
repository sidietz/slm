package de.oberamsystems.slm.converter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;

import de.oberamsystems.slm.config.TimeZoneConfig;

public class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	@Override
	public String convert(OffsetDateTime source) {
		if (source == null) {
			return "";
		}
		OffsetDateTime inZone = source.atZoneSameInstant(TimeZoneConfig.getZoneId()).toOffsetDateTime();
		return FORMATTER.format(inZone);
	}
}

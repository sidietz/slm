package de.oberamsystems.slm;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;

public class DurationConverter implements Converter<Duration, String> {

	Logger log = LoggerFactory.getLogger(DurationConverter.class);
	
	@Override
	public String convert(Duration source) {
		if (source == null) {
			return "";
		}
		log.trace(source.toString());
		Duration abs = source.abs();
		String str = source.isNegative() ? "-" : "";
		long days = abs.toDaysPart();
		int hours = abs.toHoursPart();
		int minutes = abs.toMinutesPart();
		
		if (days != 0) {
			str = str + days + "D ";
		}
		
		str = str + String.format("%02d:%02d", hours, minutes);
		return str;
	}

	@Bean
	public DurationConverter ldtConverter() {
		return new DurationConverter();
	}
}

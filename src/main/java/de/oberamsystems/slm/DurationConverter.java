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
		log.trace(source.toString());
		String str = "";
		long days = source.toDaysPart();
		int hours = source.toHoursPart();
		int minutes = source.toMinutesPart();
		int seconds = source.toSecondsPart();
		
		if (days != 0) {
			str = str + String.valueOf(days) + "D ";
		}
		
		str = str + String.format("%02d", hours) + ":" + String.format("%02d", minutes)+ ":" + String.format("%02d", seconds);
		return str;
	}

	@Bean
	public DurationConverter ldtConverter() {
		return new DurationConverter();
	}
}

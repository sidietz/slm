package de.oberamsystems.slm.spring;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class DurationToStringConverter implements Converter<Duration, String> {

	@Override
	public String convert(Duration source) {
		return String.format("%d", source.toHours());
	}
}

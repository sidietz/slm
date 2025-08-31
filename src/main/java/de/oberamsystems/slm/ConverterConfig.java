package de.oberamsystems.slm;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class ConverterConfig {

	@Bean
	public Converter<Duration, String> durationConverter() {
		return new DurationConverter();
	}

}

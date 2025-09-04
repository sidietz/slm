package de.oberamsystems.slm.mail;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class ConversionConfiguration {

	@Bean
	public Converter<LocalDateTime, String> localDatetimeConverter() {
		return new LocalDateTimeConverter();
	}
}

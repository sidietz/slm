package de.oberamsystems.slm;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.oberamsystems.slm.converter.OffsetDateTimeToStringConverter;
import de.oberamsystems.slm.converter.StringToOffsetDateTimeConverter;

@Configuration
public class ConverterConfig implements WebMvcConfigurer {

	@Bean
	public Converter<Duration, String> durationConverter() {
		return new DurationConverter();
	}

	@Bean
	public Converter<String, OffsetDateTime> stringToOffsetDateTimeConverter() {
		return new StringToOffsetDateTimeConverter();
	}

	@Bean
	public Converter<OffsetDateTime, String> offsetDateTimeToStringConverter() {
		return new OffsetDateTimeToStringConverter();
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(durationConverter());
		registry.addConverter(stringToOffsetDateTimeConverter());
		registry.addConverter(offsetDateTimeToStringConverter());
	}
}

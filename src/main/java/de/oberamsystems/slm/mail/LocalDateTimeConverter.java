package de.oberamsystems.slm.mail;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LocalDateTimeConverter implements Converter<LocalDateTime, String>, ConditionalConverter {
	
	@Bean
	public LocalDateTimeConverter ldtConverter() {	
		return new LocalDateTimeConverter();
	}

	@Override
	public String convert(LocalDateTime source) {
		return source.format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss"));
	}
	
	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.getType() == LocalDateTime.class && targetType.getType() == String.class ) {
			return true;
		} else {
			return false;
		}
	}
}

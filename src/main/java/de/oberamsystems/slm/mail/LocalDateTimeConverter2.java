package de.oberamsystems.slm.mail;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.GenericConversionService;

public class LocalDateTimeConverter2 extends GenericConversionService
		implements ConditionalConverter, ConfigurableConversionService, ConversionService {

	LocalDateTimeConverter2() {
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source instanceof OffsetDateTime odt) {
			return (Object) odt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
		}
		if (source instanceof LocalDateTime ldt) {
			return (Object) ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
		}
		return source != null ? source.toString() : "";
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if ((sourceType.getType() == LocalDateTime.class || sourceType.getType() == OffsetDateTime.class)
				&& targetType.getType() == String.class) {
			return true;
		} else {
			return false;
		}
	}

	public static <t> boolean matches(Class<? extends Object> class1, Class<t> targetClass) {
		if ((class1.equals(LocalDateTime.class) || class1.equals(OffsetDateTime.class))
				&& targetClass.equals(String.class)) {
			return true;
		} else {
			return false;
		}
	}
}

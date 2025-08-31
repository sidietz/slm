package de.oberamsystems.slm.mail;

import java.time.LocalDateTime;
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
		return (Object) (((LocalDateTime) source).format(DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss")));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.getType() == LocalDateTime.class && targetType.getType() == String.class) {
			return true;
		} else {
			return false;
		}
	}

	public static <t> boolean matches(Class<? extends Object> class1, Class<t> targetClass) {
		if (class1.equals(LocalDateTime.class) && targetClass.equals(String.class)) {
			return true;
		} else {
			return false;
		}
	}
}

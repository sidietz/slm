package de.oberamsystems.slm.mail;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.GenericConversionService;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.standard.expression.IStandardConversionService;
import org.thymeleaf.standard.expression.StandardConversionService;

@Configuration
public class MyLocalDateTimeConversionService implements IStandardConversionService {

	GenericConversionService myConverter = LocalDateTimeConverter2();
	StandardConversionService standardConversionService = new StandardConversionService();

	@Override
	public <T> T convert(IExpressionContext context, Object object, Class<T> targetClass) {
		if (LocalDateTimeConverter2.matches(object.getClass(), targetClass)) {
			return myConverter.convert(object.getClass(), targetClass);
		} else {
			return standardConversionService.convert(context, object, targetClass);
		}
	}

	private GenericConversionService LocalDateTimeConverter2() {
		return new LocalDateTimeConverter2();
	}
}

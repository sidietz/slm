package de.oberamsystems.slm.spring;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

@Configuration
public class ConversionServiceProvider {

	@Autowired
	private DurationToStringConverter converter;

	@Bean
	public ConversionService getConversionService() {
		ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
		bean.setConverters(getConverters());
		bean.afterPropertiesSet();
		ConversionService object = bean.getObject();
		return object;
	}

	private Set<Converter<?, ?>> getConverters() {
		Set<Converter<?, ?>> converters = new HashSet<Converter<?, ?>>();

		converters.add(converter);
		// add here more custom converters, either as spring bean references or directly
		// instantiated

		return converters;
	}

}

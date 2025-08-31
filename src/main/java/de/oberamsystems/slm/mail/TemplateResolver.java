package de.oberamsystems.slm.mail;

import java.util.Collections;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardConversionService;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class TemplateResolver {

	@Bean
	public ITemplateResolver thymeleafTemplateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setResolvablePatterns(Collections.singleton("html/*"));
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("HTML");
		templateResolver.setCharacterEncoding("UTF-8");
		templateResolver.setCacheable(false);
		return templateResolver;
	}

	@Bean
	public SpringTemplateEngine thymeleafTemplateEngine(ITemplateResolver templateResolver) {

		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		Set<IDialect> dialects = templateEngine.getDialects();
		StandardDialect standardDialect = (StandardDialect) dialects.iterator().next();
		IStandardConversionService conversionService = new MyLocalDateTimeConversionService();
		standardDialect.setConversionService(conversionService);
		templateEngine.setTemplateResolver(templateResolver);
		return templateEngine;
	}
	
}

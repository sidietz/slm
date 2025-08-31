package de.oberamsystems.slm.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:mail.properties")
public class EmailServiceConfig {
	
	@Value("${from}")
	private String from;

	@Value("${to}")
	private String to;

	public String getFrom() {
		return this.from;
	}

	public String getTo() {
		return this.to;
	}

}

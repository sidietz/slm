package de.oberamsystems.slm.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeZoneConfig {

	private static ZoneId applicationZoneId = ZoneId.of(System.getProperty("spring.jpa.properties.hibernate.jdbc.time_zone", "Europe/Berlin"));

	@Value("${spring.jpa.properties.hibernate.jdbc.time_zone:Europe/Berlin}")
	private String configuredTimeZone;

	@PostConstruct
	public void init() {
		applicationZoneId = ZoneId.of(configuredTimeZone);
		TimeZone.setDefault(TimeZone.getTimeZone(applicationZoneId));
	}

	public static ZoneId getZoneId() {
		return applicationZoneId;
	}
}

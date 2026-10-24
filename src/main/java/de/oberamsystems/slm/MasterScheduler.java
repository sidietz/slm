package de.oberamsystems.slm;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.oberamsystems.slm.config.TimeZoneConfig;
import de.oberamsystems.slm.mail.EmailService;
import de.oberamsystems.slm.model.CalendarEvent;
import de.oberamsystems.slm.model.CalendarEventRepository;

@Component
@EnableScheduling
public class MasterScheduler {

	private static final Logger log = LoggerFactory.getLogger(MasterScheduler.class);

	@Autowired
	private CalendarEventRepository repo;

	@Autowired
	private EmailService mailer;

	@Scheduled(cron = "0 0 6 * * *")
	public int scheduleTest() {

		OffsetDateTime now = OffsetDateTime.now(TimeZoneConfig.getZoneId());
		List<CalendarEvent> events = repo.findByStartBetween(now, now.plusDays(1));

		log.debug(String.format("%d events sent via email", events.size()));
		sendNrMail(events);

		return 0;
	}

	private void sendNrMail(List<CalendarEvent> nrs) {

		if (nrs.isEmpty()) {
			return;
		}

		String subject = "[SLM] 📅 upcoming events on " + OffsetDateTime.now(TimeZoneConfig.getZoneId()).format(DateTimeFormatter.ofPattern("E, dd.MM.yyy"));

		mailer.sendHtmlEmail(subject, nrs);
	}
}

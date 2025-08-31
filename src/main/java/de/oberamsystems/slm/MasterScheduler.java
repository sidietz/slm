package de.oberamsystems.slm;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

	@Scheduled(fixedRate = 15 * 1000)
	public int scheduleTest() {

		List<CalendarEvent> events = repo.findByStartBetween(LocalDateTime.now(), LocalDateTime.now().plusDays(1));

		log.debug(String.format("%d events sent via email", events.size()));
		//sendNrMail(events);

		return 0;
	}

	private void sendNrMail(List<CalendarEvent> nrs) {

		if (nrs.isEmpty()) {
			return;
		}

		String subject = "[SLM] upcoming events";

		mailer.sendHtmlEmail(subject, nrs);
	}

	private static String humanReadableFormat(Duration duration) {
		return duration.toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase();
	}

}

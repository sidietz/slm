package de.oberamsystems.slm.mail;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import de.oberamsystems.slm.model.CalendarEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private EmailServiceConfig emailServiceConfig;

	@Autowired
	TemplateEngine templateEngine;

	public void sendEmail(String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(emailServiceConfig.getFrom());
		message.setTo(emailServiceConfig.getTo());
		message.setSubject(subject);
		message.setText(body);

		mailSender.send(message);
	}

	public void sendHtmlEmail(String subject, List<CalendarEvent> events) {

		Context ctx = new Context();
		ctx.setVariable("events", events);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
		try {
			message.setFrom(emailServiceConfig.getFrom());
			message.setTo(emailServiceConfig.getTo());
			message.setSubject(subject);
			String htmlContent = this.templateEngine.process("mail/default.html", ctx);

			message.setText(htmlContent, true);

			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

	}
}

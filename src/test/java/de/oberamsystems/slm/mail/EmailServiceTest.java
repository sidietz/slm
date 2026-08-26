package de.oberamsystems.slm.mail;

import de.oberamsystems.slm.model.CalendarEvent;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EmailServiceTest {

    @Test
    public void testSendEmail() throws Exception {
        EmailService service = new EmailService();

        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailServiceConfig config = mock(EmailServiceConfig.class);
        TemplateEngine engine = mock(TemplateEngine.class);

        java.lang.reflect.Field f1 = EmailService.class.getDeclaredField("mailSender");
        f1.setAccessible(true);
        f1.set(service, mailSender);

        java.lang.reflect.Field f2 = EmailService.class.getDeclaredField("emailServiceConfig");
        f2.setAccessible(true);
        f2.set(service, config);

        java.lang.reflect.Field f3 = EmailService.class.getDeclaredField("templateEngine");
        f3.setAccessible(true);
        f3.set(service, engine);

        when(config.getFrom()).thenReturn("from@example.com");
        when(config.getTo()).thenReturn("to@example.com");

        service.sendEmail("Subject", "Body");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(engine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        service.sendHtmlEmail("Subject", new ArrayList<CalendarEvent>());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}

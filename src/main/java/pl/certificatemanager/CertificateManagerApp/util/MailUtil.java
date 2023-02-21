package pl.certificatemanager.CertificateManagerApp.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailUtil {
    @Value("${email.username}")
    private String fromEmail;

    @Value("${email.password}")
    private String password;

    @Value("${email.smtp.host}")
    private String smtpHost;

    @Value("${email.smtp.port}")
    private String smtpPort;

    public void sendMail(String recipientEmail, String subject, String body) {
        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

        try {
            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            log.error("Something went wrong with sending an email. Error: ", e);
        }
    }
}

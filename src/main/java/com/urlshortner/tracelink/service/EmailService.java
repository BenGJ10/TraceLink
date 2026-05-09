package com.urlshortner.tracelink.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${contact.team.email:${spring.mail.username}}")
    private String contactTeamEmail;

    public void sendPasswordResetEmail(String toAddress, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(contactTeamEmail);
            message.setTo(toAddress);
            message.setSubject("TraceLink Account Password Reset Request");
            message.setText(buildPasswordResetMessage(resetUrl));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Email sending failed", e);
        }
    }

    public void sendContactMessage(String senderName, String senderEmail, String contactMessage) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(contactTeamEmail);
            message.setTo(contactTeamEmail);
            message.setReplyTo(senderEmail);
            message.setSubject("TraceLink Contact Us - Message from " + senderName);
            message.setText(buildContactMessage(senderName, senderEmail, contactMessage));
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Contact email sending failed", e);
        }
    }

    private String buildPasswordResetMessage(String resetUrl) {
        return """
                Hello,

                We received a request to reset your password.

                Click the link below to reset your password:
                %s

                If you did not request this, please ignore this email.

                Regards,
                TraceLink Team
                """.formatted(resetUrl);
    }

    private String buildContactMessage(String senderName, String senderEmail, String contactMessage) {
        return """
                Hello Team,

                You received a new message from the Contact Us page.

                Name: %s
                Email: %s

                Message:
                %s

                Regards,
                TraceLink System
                """.formatted(senderName, senderEmail, contactMessage);
    }
}

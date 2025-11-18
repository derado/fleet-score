package com.fleetscore.common.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpMailService implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@fleetscore.local}")
    private String from;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify your FleetScore email";
        String verifyLink = baseUrl + "/api/auth/verify?token=" + token;
        String text = "Welcome to FleetScore!\n\n" +
                "Please verify your email by clicking the link below:\n" +
                verifyLink + "\n\n" +
                "If you didn't sign up, ignore this email.";
        send(to, subject, text);
    }

    @Override
    public void sendInvitationEmail(String to, String token) {
        String subject = "You have been invited to FleetScore";
        String text = "You've been invited to join an organisation on FleetScore.\n\n" +
                "Use this invitation token to complete your account setup via the app: \n" +
                token + "\n\n" +
                "API endpoint: POST /api/auth/accept-invitation { token, password }";
        send(to, subject, text);
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}

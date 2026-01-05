package com.fleetscore.mail;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
    MailService noopMailService() {
        return new NoopMailService();
    }

    @Bean
    @ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
    MailService smtpMailService(JavaMailSender mailSender) {
        return new SmtpMailService(mailSender);
    }

    @Bean
    MailEventListener mailEventListener(MailService mailService) {
        return new MailEventListener(mailService);
    }
}

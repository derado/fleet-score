package com.fleetscore.common.mail;

public interface MailService {
    void sendVerificationEmail(String to, String token);
    void sendInvitationEmail(String to, String token);
    void sendPasswordResetEmail(String to, String token);
}

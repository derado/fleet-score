package com.fleetscore.mail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class NoopMailService implements MailService {
    @Override
    public void sendVerificationEmail(String to, String token) {
        log.info("[NOOP MAIL] Verification email to={} token={}", to, token);
    }

    @Override
    public void sendInvitationEmail(String to, String token) {
        log.info("[NOOP MAIL] Invitation email to={} token={}", to, token);
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        log.info("[NOOP MAIL] Password reset email to={} token={}", to, token);
    }
}

package com.fleetscore.mail;

import com.fleetscore.user.events.InvitationEmailRequested;
import com.fleetscore.user.events.PasswordResetEmailRequested;
import com.fleetscore.user.events.VerificationEmailRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
public class MailEventListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerification(VerificationEmailRequested event) {
        mailService.sendVerificationEmail(event.email(), event.token());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitation(InvitationEmailRequested event) {
        mailService.sendInvitationEmail(event.email(), event.token());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordReset(PasswordResetEmailRequested event) {
        mailService.sendPasswordResetEmail(event.email(), event.token());
    }
}

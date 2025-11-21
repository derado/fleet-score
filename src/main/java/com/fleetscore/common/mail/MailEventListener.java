package com.fleetscore.common.mail;

import com.fleetscore.common.events.InvitationEmailRequested;
import com.fleetscore.common.events.VerificationEmailRequested;
import com.fleetscore.common.events.PasswordResetEmailRequested;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
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

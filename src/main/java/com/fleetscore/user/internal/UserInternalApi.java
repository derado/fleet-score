package com.fleetscore.user.internal;

import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import com.fleetscore.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInternalApi {

    private final UserAccountRepository userAccountRepository;

    @Transactional(readOnly = true)
    public UserAccount findByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByEmailOptional(String email) {
        return userAccountRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public UserAccount findById(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> findByIdOptional(Long id) {
        return userAccountRepository.findById(id);
    }
}

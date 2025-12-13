package com.fleetscore.user.repository;

import com.fleetscore.user.domain.Profile;
import com.fleetscore.user.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUser(UserAccount user);
}

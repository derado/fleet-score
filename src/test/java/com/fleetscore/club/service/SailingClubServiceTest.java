package com.fleetscore.club.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.organisation.repository.OrganisationRepository;
import com.fleetscore.organisation.service.OrganisationService;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class SailingClubServiceTest {

    @Autowired SailingClubService sailingClubService;
    @Autowired SailingClubRepository sailingClubRepository;
    @Autowired OrganisationService organisationService;
    @Autowired OrganisationRepository organisationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void createClub_withoutOrganisation_isAllowed() {
        // given
        UserAccount user = new UserAccount();
        user.setEmail("clubber@example.com");
        user.setPasswordHash(passwordEncoder.encode("Secret123!"));
        user.setEmailVerified(true);
        userAccountRepository.save(user);

        // when
        var resp = sailingClubService.createClub("clubber@example.com", "Local Club", "Split", null);

        // then
        assertThat(resp.id()).isNotNull();
        assertThat(resp.organisationId()).isNull();
        assertThat(sailingClubRepository.findById(resp.id())).isPresent();
    }

    @Test
    void createClub_withOrganisation_asAdmin_isAllowed() {
        // given
        UserAccount owner = new UserAccount();
        owner.setEmail("ownerclub@example.com");
        owner.setPasswordHash(passwordEncoder.encode("Secret123!"));
        owner.setEmailVerified(true);
        userAccountRepository.save(owner);

        var org = organisationService.createOrganisation("ownerclub@example.com", "Org For Club");

        // when
        var resp = sailingClubService.createClub("ownerclub@example.com", "Org Club", "Zadar", org.id());

        // then
        assertThat(resp.organisationId()).isEqualTo(org.id());
        var savedClub = sailingClubRepository.findById(resp.id()).orElseThrow();
        assertThat(savedClub.getOrganisation()).isNotNull();
        assertThat(savedClub.getOrganisation().getId()).isEqualTo(org.id());
    }

    @Test
    void createClub_withOrganisation_asNonAdmin_isForbidden() {
        // given
        UserAccount owner = new UserAccount();
        owner.setEmail("ownerclub2@example.com");
        owner.setPasswordHash(passwordEncoder.encode("Secret123!"));
        owner.setEmailVerified(true);
        userAccountRepository.save(owner);

        UserAccount other = new UserAccount();
        other.setEmail("nonadmin@example.com");
        other.setPasswordHash(passwordEncoder.encode("Secret123!"));
        other.setEmailVerified(true);
        userAccountRepository.save(other);

        var org = organisationService.createOrganisation("ownerclub2@example.com", "Org For Club 2");

        // when / then
        assertThrows(AccessDeniedException.class,
                () -> sailingClubService.createClub("nonadmin@example.com", "Forbidden Club", "Rijeka", org.id()));

        assertThat(organisationRepository.findById(org.id())).isPresent();
    }
}

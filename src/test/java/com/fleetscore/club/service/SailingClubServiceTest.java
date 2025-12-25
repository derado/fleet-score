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
        var resp = sailingClubService.createClub(user, "Local Club", "Split", null);

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

        var org = organisationService.createOrganisation(owner, "Org For Club");

        // when
        var resp = sailingClubService.createClub(owner, "Org Club", "Zadar", org.id());

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

        var org = organisationService.createOrganisation(owner, "Org For Club 2");

        // when / then
        assertThrows(AccessDeniedException.class,
                () -> sailingClubService.createClub(other, "Forbidden Club", "Rijeka", org.id()));

        assertThat(organisationRepository.findById(org.id())).isPresent();
    }

    @Test
    void createClub_creatorBecomesAdmin() {
        UserAccount user = new UserAccount();
        user.setEmail("clubcreator@example.com");
        user.setPasswordHash(passwordEncoder.encode("Secret123!"));
        user.setEmailVerified(true);
        userAccountRepository.save(user);

        var clubResponse = sailingClubService.createClub(user, "Admin Club", "Dubrovnik", null);

        var club = sailingClubRepository.findById(clubResponse.id()).orElseThrow();
        assertThat(club.getAdmins()).extracting(UserAccount::getEmail).contains("clubcreator@example.com");
    }

    @Test
    void promoteAdmin_adminCanPromoteAnotherUser() {
        UserAccount creator = new UserAccount();
        creator.setEmail("clubadmin@example.com");
        creator.setPasswordHash(passwordEncoder.encode("Secret123!"));
        creator.setEmailVerified(true);
        userAccountRepository.save(creator);

        UserAccount other = new UserAccount();
        other.setEmail("newadmin@example.com");
        other.setPasswordHash(passwordEncoder.encode("Secret123!"));
        other.setEmailVerified(true);
        userAccountRepository.save(other);

        var clubResponse = sailingClubService.createClub(creator, "Promo Club", "Pula", null);

        sailingClubService.promoteAdmin(clubResponse.id(), other.getId());

        var club = sailingClubRepository.findById(clubResponse.id()).orElseThrow();
        assertThat(club.getAdmins()).extracting(UserAccount::getEmail)
                .contains("clubadmin@example.com", "newadmin@example.com");
    }

    @Test
    void promoteAdmin_existingAdminIsNotAddedAgain() {
        UserAccount admin = new UserAccount();
        admin.setEmail("dupeadmin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("Secret123!"));
        admin.setEmailVerified(true);
        userAccountRepository.save(admin);

        var clubResponse = sailingClubService.createClub(admin, "Dupe Club", "Trogir", null);

        var clubBefore = sailingClubRepository.findById(clubResponse.id()).orElseThrow();
        int adminCountBefore = clubBefore.getAdmins().size();

        sailingClubService.promoteAdmin(clubResponse.id(), admin.getId());

        var clubAfter = sailingClubRepository.findById(clubResponse.id()).orElseThrow();
        assertThat(clubAfter.getAdmins()).hasSize(adminCountBefore);
    }
}

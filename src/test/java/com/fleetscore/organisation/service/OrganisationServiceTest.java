package com.fleetscore.organisation.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.organisation.repository.OrganisationRepository;
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
@Transactional
class OrganisationServiceTest {

    @Autowired OrganisationService organisationService;
    @Autowired OrganisationRepository organisationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void createOrganisation_creatorBecomesAdmin() {
        UserAccount creator = new UserAccount();
        creator.setEmail("owner@example.com");
        creator.setPasswordHash(passwordEncoder.encode("Secret123!"));
        creator.setEmailVerified(true);
        userAccountRepository.save(creator);

        var organisationResponse = organisationService.createOrganisation("owner@example.com", "Sailing Association");
        assertThat(organisationResponse.id()).isNotNull();

        var org = organisationRepository.findById(organisationResponse.id()).orElseThrow();
        assertThat(org.getName()).isEqualTo("Sailing Association");
        assertThat(org.getAdmins()).extracting(UserAccount::getEmail).contains("owner@example.com");
    }

    @Test
    void promoteAdmin_adminCanPromoteAnotherUser() {
        UserAccount owner = new UserAccount();
        owner.setEmail("owner2@example.com");
        owner.setPasswordHash(passwordEncoder.encode("Secret123!"));
        owner.setEmailVerified(true);
        userAccountRepository.save(owner);

        UserAccount other = new UserAccount();
        other.setEmail("member@example.com");
        other.setPasswordHash(passwordEncoder.encode("Secret123!"));
        other.setEmailVerified(true);
        userAccountRepository.save(other);

        var organisationResponse = organisationService.createOrganisation("owner2@example.com", "National Sailing Org");

        organisationService.promoteAdmin("owner2@example.com", organisationResponse.id(), other.getId());

        var org = organisationRepository.findById(organisationResponse.id()).orElseThrow();
        assertThat(org.getAdmins()).extracting(UserAccount::getEmail)
                .contains("owner2@example.com", "member@example.com");
    }

    @Test
    void promoteAdmin_anyAdminCanPromoteAdmins() {
        UserAccount creator = new UserAccount();
        creator.setEmail("creator@example.com");
        creator.setPasswordHash(passwordEncoder.encode("Secret123!"));
        creator.setEmailVerified(true);
        userAccountRepository.save(creator);

        UserAccount firstAdmin = new UserAccount();
        firstAdmin.setEmail("admin@example.com");
        firstAdmin.setPasswordHash(passwordEncoder.encode("Secret123!"));
        firstAdmin.setEmailVerified(true);
        userAccountRepository.save(firstAdmin);

        UserAccount target = new UserAccount();
        target.setEmail("target2@example.com");
        target.setPasswordHash(passwordEncoder.encode("Secret123!"));
        target.setEmailVerified(true);
        userAccountRepository.save(target);

        var organisationResponse = organisationService.createOrganisation("creator@example.com", "Org X");
        organisationService.promoteAdmin("creator@example.com", organisationResponse.id(), firstAdmin.getId());

        organisationService.promoteAdmin("admin@example.com", organisationResponse.id(), target.getId());

        var org = organisationRepository.findById(organisationResponse.id()).orElseThrow();
        assertThat(org.getAdmins()).extracting(UserAccount::getEmail)
                .contains("creator@example.com", "admin@example.com", "target2@example.com");
    }

    @Test
    void promoteAdmin_nonAdminIsForbidden() {
        UserAccount owner = new UserAccount();
        owner.setEmail("owner3@example.com");
        owner.setPasswordHash(passwordEncoder.encode("Secret123!"));
        owner.setEmailVerified(true);
        userAccountRepository.save(owner);

        UserAccount nonOwner = new UserAccount();
        nonOwner.setEmail("nonowner@example.com");
        nonOwner.setPasswordHash(passwordEncoder.encode("Secret123!"));
        nonOwner.setEmailVerified(true);
        userAccountRepository.save(nonOwner);

        UserAccount target = new UserAccount();
        target.setEmail("target@example.com");
        target.setPasswordHash(passwordEncoder.encode("Secret123!"));
        target.setEmailVerified(true);
        userAccountRepository.save(target);

        var created = organisationService.createOrganisation("owner3@example.com", "Club Org");

        assertThrows(AccessDeniedException.class,
                () -> organisationService.promoteAdmin("nonowner@example.com", created.id(), target.getId()));
    }

    @Test
    void promoteAdmin_existingAdminIsNotAddedAgain() {
        UserAccount admin = new UserAccount();
        admin.setEmail("admin@example.com");
        admin.setPasswordHash(passwordEncoder.encode("Secret123!"));
        admin.setEmailVerified(true);
        userAccountRepository.save(admin);

        var organisationResponse = organisationService.createOrganisation("admin@example.com", "Test Org");

        var orgBefore = organisationRepository.findById(organisationResponse.id()).orElseThrow();
        int adminCountBefore = orgBefore.getAdmins().size();

        organisationService.promoteAdmin("admin@example.com", organisationResponse.id(), admin.getId());

        var orgAfter = organisationRepository.findById(organisationResponse.id()).orElseThrow();
        assertThat(orgAfter.getAdmins()).hasSize(adminCountBefore);
    }
}

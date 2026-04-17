package com.fleetscore.club.internal;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class ClubInternalApiTest {

    @Autowired ClubInternalApi clubInternalApi;
    @Autowired SailingClubRepository sailingClubRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(60000L);

    @Test
    void findUserClubAssociations_userIsOwnerOfSingleClub_returnsSingleOwnerAssociation() {
        UserAccount user = createUser();
        SailingClub club = createClub("Owner Club", user);

        List<UserClubAssociation> associations = clubInternalApi.findUserClubAssociations(user.getId());

        assertThat(associations).hasSize(1);
        assertThat(associations.get(0).id()).isEqualTo(club.getId());
        assertThat(associations.get(0).name()).isEqualTo("Owner Club");
        assertThat(associations.get(0).roles())
                .containsExactly(UserClubAssociation.Role.OWNER);
    }

    @Test
    void findUserClubAssociations_userIsAdminOfOneClubAndMemberOfAnother_returnsTwoAssociations() {
        UserAccount owner = createUser();
        UserAccount target = createUser();

        SailingClub adminClub = createClub("Admin Club", owner);
        adminClub.getAdmins().add(target);
        sailingClubRepository.save(adminClub);

        SailingClub memberClub = createClub("Member Club", owner);
        memberClub.getMembers().add(target);
        sailingClubRepository.save(memberClub);

        List<UserClubAssociation> associations = clubInternalApi.findUserClubAssociations(target.getId());

        assertThat(associations).hasSize(2);
        UserClubAssociation adminAssoc = findById(associations, adminClub.getId());
        UserClubAssociation memberAssoc = findById(associations, memberClub.getId());

        assertThat(adminAssoc.roles()).containsExactly(UserClubAssociation.Role.ADMIN);
        assertThat(memberAssoc.roles()).containsExactly(UserClubAssociation.Role.MEMBER);
    }

    @Test
    void findUserClubAssociations_userIsOwnerAdminAndMemberOfSameClub_returnsSingleAssociationWithAllRoles() {
        UserAccount user = createUser();
        SailingClub club = createClub("Triple Role Club", user);
        club.getAdmins().add(user);
        club.getMembers().add(user);
        sailingClubRepository.save(club);

        List<UserClubAssociation> associations = clubInternalApi.findUserClubAssociations(user.getId());

        assertThat(associations).hasSize(1);
        assertThat(associations.get(0).id()).isEqualTo(club.getId());
        assertThat(associations.get(0).roles()).containsExactlyInAnyOrder(
                UserClubAssociation.Role.OWNER,
                UserClubAssociation.Role.ADMIN,
                UserClubAssociation.Role.MEMBER
        );
    }

    @Test
    void findUserClubAssociations_userHasNoClub_returnsEmptyList() {
        UserAccount user = createUser();

        List<UserClubAssociation> associations = clubInternalApi.findUserClubAssociations(user.getId());

        assertThat(associations).isEmpty();
    }

    @Test
    void findUserClubAssociations_filtersByUserId_acrossMultipleUsers() {
        UserAccount userA = createUser();
        UserAccount userB = createUser();

        SailingClub clubA = createClub("A Club", userA);
        SailingClub clubB = createClub("B Club", userB);

        List<UserClubAssociation> resultA = clubInternalApi.findUserClubAssociations(userA.getId());
        List<UserClubAssociation> resultB = clubInternalApi.findUserClubAssociations(userB.getId());

        assertThat(resultA).hasSize(1);
        assertThat(resultA.get(0).id()).isEqualTo(clubA.getId());

        assertThat(resultB).hasSize(1);
        assertThat(resultB.get(0).id()).isEqualTo(clubB.getId());
    }

    @Test
    void findSummariesByIds_emptyInput_returnsEmptyList() {
        List<ClubSummary> result = clubInternalApi.findSummariesByIds(Set.of());

        assertThat(result).isEmpty();
    }

    @Test
    void findSummariesByIds_validIds_returnsClubSummaries() {
        UserAccount owner = createUser();
        SailingClub clubA = createClub("Summary Club A", owner);
        SailingClub clubB = createClub("Summary Club B", owner);

        List<ClubSummary> result = clubInternalApi.findSummariesByIds(List.of(clubA.getId(), clubB.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ClubSummary::id)
                .containsExactlyInAnyOrder(clubA.getId(), clubB.getId());
        assertThat(result).extracting(ClubSummary::name)
                .containsExactlyInAnyOrder("Summary Club A", "Summary Club B");
        assertThat(result).extracting(ClubSummary::place)
                .containsOnly("Split");
    }

    @Test
    void findSummariesByIds_nonExistentId_isSilentlySkipped() {
        UserAccount owner = createUser();
        SailingClub club = createClub("Existing Club", owner);

        List<ClubSummary> result = clubInternalApi.findSummariesByIds(List.of(club.getId(), 9_999_999L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(club.getId());
    }

    // --- helpers ---

    private UserAccount createUser() {
        UserAccount u = new UserAccount();
        u.setEmail("clubassoc-" + idSequence.incrementAndGet() + "@example.com");
        u.setPasswordHash(passwordEncoder.encode("Secret123!"));
        u.setEmailVerified(true);
        return userAccountRepository.save(u);
    }

    private SailingClub createClub(String name, UserAccount owner) {
        SailingClub club = new SailingClub();
        club.setName(name);
        club.setPlace("Split");
        club.setOwner(owner);
        return sailingClubRepository.save(club);
    }

    private UserClubAssociation findById(List<UserClubAssociation> list, Long id) {
        return list.stream()
                .filter(a -> a.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}

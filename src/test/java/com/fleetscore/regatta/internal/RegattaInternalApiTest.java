package com.fleetscore.regatta.internal;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.regatta.service.RegattaService;
import com.fleetscore.sailingclass.domain.HullType;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.domain.WorldSailingStatus;
import com.fleetscore.sailingclass.repository.SailingClassRepository;
import com.fleetscore.sailingnation.domain.SailingNation;
import com.fleetscore.sailingnation.repository.SailingNationRepository;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class RegattaInternalApiTest {

    @Autowired RegattaInternalApi regattaInternalApi;
    @Autowired RegattaService regattaService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired SailingNationRepository sailingNationRepository;
    @Autowired SailingClubRepository sailingClubRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(90000L);

    private UserAccount owner;
    private UserAccount target;
    private SailingClass sailingClass;
    private SailingNation sailingNation;
    private Long regattaId;

    @BeforeEach
    void setUp() {
        owner = createUser();
        target = createUser();
        sailingClass = createSailingClass(idSequence.incrementAndGet(), "Laser " + idSequence.get());
        sailingNation = createSailingNation(idSequence.incrementAndGet(), "T" + (idSequence.get() % 1000), "Testland " + idSequence.get());

        var regattaResponse = regattaService.createRegatta(owner, new RegattaRequest(
                "Test Regatta " + idSequence.incrementAndGet(),
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5),
                "Test Venue", "Croatia", "Split", "21000", "Address 1",
                "test@example.com", "+385 21 123456",
                0, 0,
                Set.of(sailingClass.getId()),
                Set.of(),
                null
        ));
        regattaId = regattaResponse.id();
    }

    @Test
    void findClubIdsByRegistrantUserId_userRegisteredForTwoDifferentClubs_returnsBothIds() {
        SailingClub clubA = createClub("Club A");
        SailingClub clubB = createClub("Club B");

        saveRegistration(201, target, clubA);
        saveRegistration(202, target, clubB);

        Set<Long> ids = regattaInternalApi.findClubIdsByRegistrantUserId(target.getId());

        assertThat(ids).containsExactlyInAnyOrder(clubA.getId(), clubB.getId());
    }

    @Test
    void findClubIdsByRegistrantUserId_userRegisteredMultipleTimesForSameClub_returnsDistinctSingleId() {
        SailingClub club = createClub("Repeat Club");

        // Two distinct regattas to avoid unique constraints; both point to same club.
        saveRegistration(203, target, club);

        Long secondRegattaId = createAdditionalRegatta();
        saveRegistrationInRegatta(secondRegattaId, 204, target, club);

        Set<Long> ids = regattaInternalApi.findClubIdsByRegistrantUserId(target.getId());

        assertThat(ids).containsExactly(club.getId());
    }

    @Test
    void findClubIdsByRegistrantUserId_registrationWithoutClub_isExcluded() {
        saveRegistration(205, target, null);

        Set<Long> ids = regattaInternalApi.findClubIdsByRegistrantUserId(target.getId());

        assertThat(ids).isEmpty();
    }

    @Test
    void findClubIdsByRegistrantUserId_differentUserHasNoMatches() {
        SailingClub club = createClub("Other Club");
        saveRegistration(206, target, club);

        UserAccount stranger = createUser();

        Set<Long> ids = regattaInternalApi.findClubIdsByRegistrantUserId(stranger.getId());

        assertThat(ids).isEmpty();
    }

    @Test
    void findExternalClubNamesByUserId_freeTextClubsWithoutFk_returnsDistinctNames() {
        saveRegistrationWithExternalName(301, target, "External Yacht Club");

        Long secondRegattaId = createAdditionalRegatta();
        saveRegistrationInRegattaWithExternalName(secondRegattaId, 302, target, "Harbour Sailing Association");

        List<String> names = regattaInternalApi.findExternalClubNamesByUserId(target.getId());

        assertThat(names).containsExactlyInAnyOrder("External Yacht Club", "Harbour Sailing Association");
    }

    @Test
    void findExternalClubNamesByUserId_onlyLinkedClubRegistrations_returnsEmptyList() {
        SailingClub club = createClub("Linked Club");
        saveRegistration(303, target, club);

        List<String> names = regattaInternalApi.findExternalClubNamesByUserId(target.getId());

        assertThat(names).isEmpty();
    }

    @Test
    void findExternalClubNamesByUserId_duplicateNamesAcrossRegistrations_returnsOneCopy() {
        saveRegistrationWithExternalName(304, target, "Duplicate Club");

        Long secondRegattaId = createAdditionalRegatta();
        saveRegistrationInRegattaWithExternalName(secondRegattaId, 305, target, "Duplicate Club");

        List<String> names = regattaInternalApi.findExternalClubNamesByUserId(target.getId());

        assertThat(names).containsExactly("Duplicate Club");
    }

    @Test
    void findExternalClubNamesByUserId_otherUsersExternalRegistrations_areNotIncluded() {
        saveRegistrationWithExternalName(306, target, "Target External Club");

        UserAccount stranger = createUser();

        List<String> names = regattaInternalApi.findExternalClubNamesByUserId(stranger.getId());

        assertThat(names).isEmpty();
    }

    // --- helpers ---

    private UserAccount createUser() {
        UserAccount u = new UserAccount();
        u.setEmail("regint-" + idSequence.incrementAndGet() + "@example.com");
        u.setPasswordHash(passwordEncoder.encode("Secret123!"));
        u.setEmailVerified(true);
        return userAccountRepository.save(u);
    }

    private SailingClass createSailingClass(Long id, String name) {
        SailingClass sc = new SailingClass();
        sc.setId(id);
        sc.setName(name);
        sc.setHullType(HullType.CENTREBOARD);
        sc.setWorldSailingStatus(WorldSailingStatus.OLYMPIC);
        return sailingClassRepository.save(sc);
    }

    private SailingNation createSailingNation(Long id, String code, String country) {
        SailingNation sn = new SailingNation();
        sn.setId(id);
        sn.setCode(code);
        sn.setCountry(country);
        return sailingNationRepository.save(sn);
    }

    private SailingClub createClub(String name) {
        SailingClub club = new SailingClub();
        club.setName(name);
        club.setPlace("Split");
        club.setOwner(owner);
        return sailingClubRepository.save(club);
    }

    private Registration saveRegistration(int sailNumber, UserAccount registrationUser, SailingClub club) {
        return saveRegistrationInRegatta(regattaId, sailNumber, registrationUser, club);
    }

    private Registration saveRegistrationInRegatta(Long regattaIdToUse, int sailNumber, UserAccount registrationUser, SailingClub club) {
        Regatta regatta = regattaRepository.findById(regattaIdToUse).orElseThrow();
        Registration reg = new Registration();
        reg.setRegatta(regatta);
        reg.setSailorName("Sailor " + sailNumber);
        reg.setEmail("sailor" + sailNumber + "@example.com");
        reg.setDateOfBirth(LocalDate.of(1990, 1, 1));
        reg.setGender(Gender.M);
        reg.setSailingClubName(club != null ? club.getName() : "No Club");
        reg.setSailingClub(club);
        reg.setSailingClass(sailingClass);
        reg.setSailingNation(sailingNation);
        reg.setSailNumber(sailNumber);
        reg.setUser(registrationUser);
        return registrationRepository.save(reg);
    }

    private Registration saveRegistrationWithExternalName(int sailNumber, UserAccount registrationUser, String externalName) {
        return saveRegistrationInRegattaWithExternalName(regattaId, sailNumber, registrationUser, externalName);
    }

    private Registration saveRegistrationInRegattaWithExternalName(Long regattaIdToUse, int sailNumber, UserAccount registrationUser, String externalName) {
        Regatta regatta = regattaRepository.findById(regattaIdToUse).orElseThrow();
        Registration reg = new Registration();
        reg.setRegatta(regatta);
        reg.setSailorName("Sailor " + sailNumber);
        reg.setEmail("sailor" + sailNumber + "@example.com");
        reg.setDateOfBirth(LocalDate.of(1990, 1, 1));
        reg.setGender(Gender.M);
        reg.setSailingClubName(externalName);
        reg.setSailingClub(null);
        reg.setSailingClass(sailingClass);
        reg.setSailingNation(sailingNation);
        reg.setSailNumber(sailNumber);
        reg.setUser(registrationUser);
        return registrationRepository.save(reg);
    }

    private Long createAdditionalRegatta() {
        var response = regattaService.createRegatta(owner, new RegattaRequest(
                "Extra Regatta " + idSequence.incrementAndGet(),
                LocalDate.of(2027, 8, 1),
                LocalDate.of(2027, 8, 5),
                "Extra Venue", "Croatia", "Split", "21000", "Address 1",
                "test@example.com", "+385 21 123456",
                0, 0,
                Set.of(sailingClass.getId()),
                Set.of(),
                null
        ));
        return response.id();
    }
}

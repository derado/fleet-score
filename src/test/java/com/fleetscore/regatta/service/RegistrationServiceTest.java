package com.fleetscore.regatta.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.common.exception.RegistrationInUseException;
import com.fleetscore.regatta.api.dto.CreateRegistrationRequest;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.domain.Race;
import com.fleetscore.regatta.domain.RaceResult;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.repository.RaceRepository;
import com.fleetscore.regatta.repository.RaceResultRepository;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class RegistrationServiceTest {

    @Autowired RegistrationService registrationService;
    @Autowired RegattaService regattaService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired RaceRepository raceRepository;
    @Autowired RaceResultRepository raceResultRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired SailingNationRepository sailingNationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(80000L);

    private UserAccount owner;
    private UserAccount admin;
    private UserAccount stranger;
    private SailingClass sailingClass;
    private SailingNation sailingNation;
    private Long regattaId;

    @BeforeEach
    void setUp() {
        owner = createUser("reg-owner-" + idSequence.incrementAndGet() + "@example.com");
        admin = createUser("reg-admin-" + idSequence.incrementAndGet() + "@example.com");
        stranger = createUser("reg-stranger-" + idSequence.incrementAndGet() + "@example.com");
        sailingClass = createSailingClass(idSequence.incrementAndGet(), "Laser " + idSequence.get());
        sailingNation = createSailingNation(idSequence.incrementAndGet(), "T" + idSequence.get() % 1000, "Testland " + idSequence.get());

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

        // Promote admin user — requires authenticated owner in SecurityContext
        authenticateAs(owner);
        regattaService.promoteAdmin(regattaId, admin.getId());
        clearAuthentication();
    }

    // --- updateRegistration ---

    @Test
    void updateRegistration_ownerOfRegistration_canUpdate() {
        Registration reg = saveRegistration(101, owner);

        CreateRegistrationRequest updateRequest = buildUpdateRequest(102);
        var result = registrationService.updateRegistration(reg.getId(), updateRequest, owner);

        assertThat(result.sailNumber()).isEqualTo(102);
    }

    @Test
    void updateRegistration_regattaAdmin_canUpdateAnyRegistration() {
        Registration reg = saveRegistration(103, stranger);

        CreateRegistrationRequest updateRequest = buildUpdateRequest(104);
        var result = registrationService.updateRegistration(reg.getId(), updateRequest, admin);

        assertThat(result.sailNumber()).isEqualTo(104);
    }

    @Test
    void updateRegistration_unrelatedUser_isDenied() {
        Registration reg = saveRegistration(105, owner);

        CreateRegistrationRequest updateRequest = buildUpdateRequest(106);
        assertThrows(AccessDeniedException.class,
                () -> registrationService.updateRegistration(reg.getId(), updateRequest, stranger));
    }

    @Test
    void updateRegistration_nullUserRegistration_regattaAdminCanUpdate() {
        Registration reg = saveRegistration(107, null);

        CreateRegistrationRequest updateRequest = buildUpdateRequest(108);
        var result = registrationService.updateRegistration(reg.getId(), updateRequest, admin);

        assertThat(result.sailNumber()).isEqualTo(108);
    }

    @Test
    void updateRegistration_nullUserRegistration_nonAdminIsDenied() {
        Registration reg = saveRegistration(109, null);

        CreateRegistrationRequest updateRequest = buildUpdateRequest(110);
        assertThrows(AccessDeniedException.class,
                () -> registrationService.updateRegistration(reg.getId(), updateRequest, stranger));
    }

    // --- deleteRegistration ---

    @Test
    void deleteRegistration_ownerCanDelete_withNoResults() {
        Registration reg = saveRegistration(111, owner);
        Long regId = reg.getId();

        registrationService.deleteRegistration(regId, owner);

        assertThat(registrationRepository.findById(regId)).isEmpty();
    }

    @Test
    void deleteRegistration_throwsRegistrationInUse_whenResultsExist() {
        Registration reg = saveRegistration(112, owner);
        attachRaceResult(reg);

        assertThrows(RegistrationInUseException.class,
                () -> registrationService.deleteRegistration(reg.getId(), owner));
    }

    @Test
    void deleteRegistration_nonOwnerNonAdmin_isDenied() {
        Registration reg = saveRegistration(113, owner);

        assertThrows(AccessDeniedException.class,
                () -> registrationService.deleteRegistration(reg.getId(), stranger));
    }

    // --- helpers ---

    private Registration saveRegistration(int sailNumber, UserAccount registrationUser) {
        Regatta regatta = regattaRepository.findById(regattaId).orElseThrow();
        Registration reg = new Registration();
        reg.setRegatta(regatta);
        reg.setSailorName("Sailor " + sailNumber);
        reg.setEmail("sailor" + sailNumber + "@example.com");
        reg.setDateOfBirth(LocalDate.of(1990, 1, 1));
        reg.setGender(Gender.M);
        reg.setSailingClubName("Club");
        reg.setSailingClass(sailingClass);
        reg.setSailingNation(sailingNation);
        reg.setSailNumber(sailNumber);
        reg.setUser(registrationUser);
        return registrationRepository.save(reg);
    }

    private void attachRaceResult(Registration registration) {
        Regatta regatta = regattaRepository.findById(regattaId).orElseThrow();
        Race race = new Race();
        race.setRegatta(regatta);
        race.setRaceNumber(1);
        race.setSailingClass(sailingClass);
        race = raceRepository.save(race);

        RaceResult result = new RaceResult();
        result.setRace(race);
        result.setRegistration(registration);
        result.setPosition(1);
        result.setPoints(1);
        raceResultRepository.save(result);
    }

    private CreateRegistrationRequest buildUpdateRequest(int sailNumber) {
        return new CreateRegistrationRequest(
                "Updated Sailor",
                "updated@example.com",
                LocalDate.of(1990, 6, 15),
                Gender.M,
                "Updated Club",
                null,
                null,
                sailingClass.getId(),
                sailingNation.getId(),
                sailNumber
        );
    }

    private UserAccount createUser(String email) {
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("Secret123!"));
        user.setEmailVerified(true);
        return userAccountRepository.save(user);
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

    private static void authenticateAs(UserAccount user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList())
        );
    }

    private static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}

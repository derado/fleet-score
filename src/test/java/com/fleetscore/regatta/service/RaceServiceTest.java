package com.fleetscore.regatta.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.api.dto.CreateRaceRequest;
import com.fleetscore.regatta.api.dto.RaceResponse;
import com.fleetscore.regatta.api.dto.RaceResultRequest;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.domain.Regatta;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class RaceServiceTest {

    @Autowired RaceService raceService;
    @Autowired RegattaService regattaService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired SailingNationRepository sailingNationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(90000L);

    private SailingClass sailingClass;
    private SailingNation sailingNation;
    private Long regattaId;

    @BeforeEach
    void setUp() {
        UserAccount owner = createUser("race-owner-" + idSequence.incrementAndGet() + "@example.com");
        sailingClass = createSailingClass(idSequence.incrementAndGet(), "Laser " + idSequence.get());
        sailingNation = createSailingNation(idSequence.incrementAndGet(), "T" + idSequence.get() % 1000, "Testland " + idSequence.get());

        var regattaResponse = regattaService.createRegatta(owner, new RegattaRequest(
                "Race Test Regatta " + idSequence.incrementAndGet(),
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
        authenticateAs(owner);
    }

    @Test
    void updateRace_replacesResults_withoutDuplicateKeyViolation() {
        Registration reg1 = saveRegistration(1);
        Registration reg2 = saveRegistration(2);

        RaceResponse created = raceService.createRace(regattaId, new CreateRaceRequest(
                sailingClass.getId(), 1, LocalDate.of(2026, 8, 1),
                List.of(
                        new RaceResultRequest(reg1.getId(), 1, null),
                        new RaceResultRequest(reg2.getId(), 2, null)
                )
        ));

        // Update with the same registrations in reverse order — triggers the duplicate key if flush is missing
        RaceResponse updated = raceService.updateRace(created.id(), new CreateRaceRequest(
                sailingClass.getId(), 1, LocalDate.of(2026, 8, 1),
                List.of(
                        new RaceResultRequest(reg1.getId(), 2, null),
                        new RaceResultRequest(reg2.getId(), 1, null)
                )
        ));

        assertThat(updated.results()).hasSize(2);
        var resultByReg1 = updated.results().stream()
                .filter(r -> r.registration().id().equals(reg1.getId()))
                .findFirst().orElseThrow();
        assertThat(resultByReg1.position()).isEqualTo(2);
    }

    // --- helpers ---

    private Registration saveRegistration(int sailNumber) {
        Regatta regatta = regattaRepository.findById(regattaId).orElseThrow();
        Registration reg = new Registration();
        reg.setRegatta(regatta);
        reg.setSailorName("Sailor " + sailNumber + "-" + idSequence.incrementAndGet());
        reg.setEmail("sailor" + idSequence.get() + "@example.com");
        reg.setDateOfBirth(LocalDate.of(1990, 1, 1));
        reg.setGender(Gender.M);
        reg.setSailingClubName("Club");
        reg.setSailingClass(sailingClass);
        reg.setSailingNation(sailingNation);
        reg.setSailNumber(sailNumber);
        return registrationRepository.save(reg);
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

    private void authenticateAs(UserAccount user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList())
        );
    }
}

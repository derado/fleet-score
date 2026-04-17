package com.fleetscore.regatta.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.api.dto.CreateRegistrationRequest;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.api.dto.RegistrationResponse;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.sailor.repository.SailorRepository;
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

/**
 * Regression test for: two children sharing a parent's contact email must both be
 * registerable in the same regatta as separate sailors.
 *
 * <p>Email is a contact address, not a unique identity for a sailor. Identity is
 * determined by (name, dateOfBirth) in SailorResolver.findOrCreate.
 */
@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class RegistrationServiceMultiChildTest {

    @Autowired RegistrationService registrationService;
    @Autowired RegattaService regattaService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired SailorRepository sailorRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired SailingNationRepository sailingNationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(90000L);
    private static final String SHARED_EMAIL = "parent@example.com";

    private UserAccount parent;
    private SailingClass sailingClass;
    private SailingNation sailingNation;
    private Long regattaId;

    @BeforeEach
    void setUp() {
        parent = createUser("parent-user-" + idSequence.incrementAndGet() + "@example.com");
        sailingClass = createSailingClass(idSequence.incrementAndGet(), "Optimist " + idSequence.get());
        sailingNation = createSailingNation(idSequence.incrementAndGet(),
                "T" + idSequence.get() % 1000, "Testland " + idSequence.get());

        var regattaResponse = regattaService.createRegatta(parent, new RegattaRequest(
                "Youth Regatta " + idSequence.incrementAndGet(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 3),
                "Test Venue", "Croatia", "Split", "21000", "Address 1",
                "info@example.com", "+385 21 000000",
                0, 0,
                Set.of(sailingClass.getId()),
                Set.of(),
                null
        ));
        regattaId = regattaResponse.id();
    }

    @Test
    void twoChildrenWithSameContactEmail_canBothBeRegistered() {
        var firstChildRequest = new CreateRegistrationRequest(
                "Alice Smith",
                SHARED_EMAIL,
                LocalDate.of(2012, 3, 10),
                Gender.F,
                "Yacht Club A",
                null,
                null,
                sailingClass.getId(),
                sailingNation.getId(),
                1
        );

        var secondChildRequest = new CreateRegistrationRequest(
                "Bob Smith",
                SHARED_EMAIL,
                LocalDate.of(2014, 7, 22),
                Gender.M,
                "Yacht Club A",
                null,
                null,
                sailingClass.getId(),
                sailingNation.getId(),
                2
        );

        RegistrationResponse firstResult = registrationService.createRegistration(regattaId, firstChildRequest, parent);
        RegistrationResponse secondResult = registrationService.createRegistration(regattaId, secondChildRequest, parent);

        assertThat(firstResult.id()).isNotNull();
        assertThat(secondResult.id()).isNotNull();
        assertThat(firstResult.id()).isNotEqualTo(secondResult.id());

        assertThat(registrationRepository.count()).isGreaterThanOrEqualTo(2);

        var registrationsInRegatta = registrationRepository
                .findByRegattaIdWithFilters(regattaId, null, null, null, null, null, null);
        assertThat(registrationsInRegatta).hasSize(2);

        List<Long> sailorIds = registrationsInRegatta.stream()
                .map(r -> r.getSailor().getId())
                .toList();
        assertThat(sailorIds).doesNotHaveDuplicates();

        long sailorsWithSharedEmail = sailorRepository.findAll().stream()
                .filter(s -> SHARED_EMAIL.equals(s.getEmail()))
                .count();
        assertThat(sailorsWithSharedEmail).isEqualTo(2);
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
}

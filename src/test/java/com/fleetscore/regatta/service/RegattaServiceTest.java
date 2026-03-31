package com.fleetscore.regatta.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.domain.Registration;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class RegattaServiceTest {

    @Autowired RegattaService regattaService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired SailingNationRepository sailingNationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void deleteRegatta_emptyRegatta_isDeleted() {
        UserAccount owner = createUser("owner-delete@example.com");
        SailingClass sailingClass = createSailingClass(9000L, "Test Class");

        var regattaResponse = regattaService.createRegatta(owner, new RegattaRequest(
                "Delete Me Regatta",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 5),
                "Test Venue", "Croatia", "Split", "21000", "Address 1",
                "test@example.com", "+385 21 123456",
                0, 0,
                Set.of(sailingClass.getId()),
                Set.of(),
                null
        ));

        authenticateAs(owner);
        regattaService.deleteRegatta(regattaResponse.id());
        clearAuthentication();

        assertThat(regattaRepository.findById(regattaResponse.id())).isEmpty();
    }

    @Test
    void deleteRegatta_withRegistrations_throwsIllegalState() {
        UserAccount owner = createUser("owner-nodelete@example.com");
        SailingClass sailingClass = createSailingClass(9001L, "Test Class 2");
        SailingNation sailingNation = createSailingNation(9000L, "TST", "Testland");

        var regattaResponse = regattaService.createRegatta(owner, new RegattaRequest(
                "Cannot Delete Regatta",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 5),
                "Test Venue", "Croatia", "Split", "21000", "Address 1",
                "test@example.com", "+385 21 123456",
                0, 0,
                Set.of(sailingClass.getId()),
                Set.of(),
                null
        ));

        var regatta = regattaRepository.findById(regattaResponse.id()).orElseThrow();
        Registration registration = new Registration();
        registration.setRegatta(regatta);
        registration.setSailorName("John Doe");
        registration.setEmail("john@example.com");
        registration.setDateOfBirth(LocalDate.of(1990, 1, 1));
        registration.setGender(Gender.M);
        registration.setSailingClubName("Local Club");
        registration.setSailingClass(sailingClass);
        registration.setSailingNation(sailingNation);
        registration.setSailNumber(42);
        registrationRepository.save(registration);

        authenticateAs(owner);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> regattaService.deleteRegatta(regattaResponse.id()));
        clearAuthentication();

        assertThat(ex.getMessage()).isEqualTo("Cannot delete regatta that has registrations or races");
        assertThat(regattaRepository.findById(regattaResponse.id())).isPresent();
    }

    @Test
    void deleteRegatta_nonExistent_throwsAccessDenied() {
        UserAccount owner = createUser("owner-notfound@example.com");
        authenticateAs(owner);
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> regattaService.deleteRegatta(999999L));
        clearAuthentication();
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

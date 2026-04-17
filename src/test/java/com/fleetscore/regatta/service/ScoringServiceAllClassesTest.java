package com.fleetscore.regatta.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.api.dto.RegattaRequest;
import com.fleetscore.regatta.api.dto.RegattaScoreResponse;
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
class ScoringServiceAllClassesTest {

    @Autowired ScoringService scoringService;
    @Autowired RegattaService regattaService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired RaceRepository raceRepository;
    @Autowired RaceResultRepository raceResultRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired SailingNationRepository sailingNationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(90000L);

    private Long regattaId;
    private SailingClass laser;
    private SailingClass optimist;
    private SailingNation nation;

    @BeforeEach
    void setUp() {
        UserAccount owner = createUser("allscores-owner-" + idSequence.incrementAndGet() + "@example.com");

        laser = createSailingClass(idSequence.incrementAndGet(), "Laser-" + idSequence.get());
        optimist = createSailingClass(idSequence.incrementAndGet(), "Optimist-" + idSequence.get());
        nation = createSailingNation(idSequence.incrementAndGet(), "T" + (idSequence.get() % 1000), "Testland-" + idSequence.get());

        var regattaResponse = regattaService.createRegatta(owner, new RegattaRequest(
                "All-Classes Regatta " + idSequence.incrementAndGet(),
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 5),
                "Test Venue", "Croatia", "Split", "21000", "Address 1",
                "test@example.com", "+385 21 123456",
                0, 0,
                Set.of(laser.getId(), optimist.getId()),
                Set.of(),
                null
        ));
        regattaId = regattaResponse.id();
    }

    @Test
    void calculateAllClassScores_returnOneEntryPerSailingClass() {
        Regatta regatta = regattaRepository.findById(regattaId).orElseThrow();

        Registration laserReg = saveRegistration(regatta, laser, 101, "Laser Sailor One");
        Registration optimistReg = saveRegistration(regatta, optimist, 201, "Optimist Sailor One");

        Race laserRace = saveRace(regatta, laser, 1);
        Race optimistRace = saveRace(regatta, optimist, 1);

        saveRaceResult(laserRace, laserReg, 1, 1);
        saveRaceResult(optimistRace, optimistReg, 1, 1);

        List<RegattaScoreResponse> results = scoringService.calculateAllClassScores(regattaId);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(RegattaScoreResponse::sailingClassId)
                .containsExactlyInAnyOrder(laser.getId(), optimist.getId());
    }

    @Test
    void calculateAllClassScores_eachEntryContainsCorrectStandings() {
        Regatta regatta = regattaRepository.findById(regattaId).orElseThrow();

        Registration laserReg1 = saveRegistration(regatta, laser, 102, "Laser Sailor A");
        Registration laserReg2 = saveRegistration(regatta, laser, 103, "Laser Sailor B");
        Registration optimistReg1 = saveRegistration(regatta, optimist, 202, "Optimist Sailor X");

        Race laserRace = saveRace(regatta, laser, 1);
        Race optimistRace = saveRace(regatta, optimist, 1);

        saveRaceResult(laserRace, laserReg1, 1, 1);
        saveRaceResult(laserRace, laserReg2, 2, 2);
        saveRaceResult(optimistRace, optimistReg1, 1, 1);

        List<RegattaScoreResponse> results = scoringService.calculateAllClassScores(regattaId);

        var laserScore = results.stream()
                .filter(r -> r.sailingClassId().equals(laser.getId()))
                .findFirst()
                .orElseThrow();

        var optimistScore = results.stream()
                .filter(r -> r.sailingClassId().equals(optimist.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(laserScore.standings()).hasSize(2);
        assertThat(laserScore.standings())
                .extracting(RegattaScoreResponse.SailorStanding::sailorName)
                .containsExactlyInAnyOrder("Laser Sailor A", "Laser Sailor B");

        assertThat(optimistScore.standings()).hasSize(1);
        assertThat(optimistScore.standings().getFirst().sailorName()).isEqualTo("Optimist Sailor X");
    }

    @Test
    void calculateAllClassScores_emptyRegatta_returnsEmptyStandingsPerClass() {
        List<RegattaScoreResponse> results = scoringService.calculateAllClassScores(regattaId);

        assertThat(results).hasSize(2);
        results.forEach(r -> assertThat(r.standings()).isEmpty());
    }

    // --- helpers ---

    private Registration saveRegistration(Regatta regatta, SailingClass sailingClass, int sailNumber, String name) {
        Registration reg = new Registration();
        reg.setRegatta(regatta);
        reg.setSailorName(name);
        reg.setEmail("sailor" + sailNumber + "-" + idSequence.incrementAndGet() + "@example.com");
        reg.setDateOfBirth(LocalDate.of(1990, 1, 1));
        reg.setGender(Gender.M);
        reg.setSailingClubName("Test Club");
        reg.setSailingClass(sailingClass);
        reg.setSailingNation(nation);
        reg.setSailNumber(sailNumber);
        return registrationRepository.save(reg);
    }

    private Race saveRace(Regatta regatta, SailingClass sailingClass, int raceNumber) {
        Race race = new Race();
        race.setRegatta(regatta);
        race.setSailingClass(sailingClass);
        race.setRaceNumber(raceNumber);
        return raceRepository.save(race);
    }

    private void saveRaceResult(Race race, Registration registration, int position, int points) {
        RaceResult result = new RaceResult();
        result.setRace(race);
        result.setRegistration(registration);
        result.setPosition(position);
        result.setPoints(points);
        raceResultRepository.save(result);
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

package com.fleetscore.regatta.service;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.api.dto.CreateSeriesRequest;
import com.fleetscore.regatta.api.dto.SeriesScoreResponse;
import com.fleetscore.regatta.domain.Race;
import com.fleetscore.regatta.domain.RaceResult;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.domain.SeriesScoringType;
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
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class SeriesScoringServiceTest {

    @Autowired SeriesScoringService seriesScoringService;
    @Autowired SeriesService seriesService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired RaceRepository raceRepository;
    @Autowired RaceResultRepository raceResultRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired SailingNationRepository sailingNationRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(70000L);

    private UserAccount owner;
    private SailingClass sailingClass;
    private SailingNation sailingNation;
    private Regatta regatta1;
    private Regatta regatta2;
    private Registration reg1; // Alice in regatta1
    private Registration reg2; // Bob in regatta1
    private Registration reg3; // Alice in regatta2
    private Registration reg4; // Bob in regatta2

    @BeforeEach
    void setUp() {
        owner = createUser("series-scoring-" + idSequence.incrementAndGet() + "@example.com");
        sailingClass = createSailingClass(idSequence.incrementAndGet(), "Laser SC " + idSequence.get());
        sailingNation = createSailingNation(idSequence.incrementAndGet(), "T" + (idSequence.get() % 1000), "Testland " + idSequence.get());

        regatta1 = createRegatta("Regatta One " + idSequence.incrementAndGet(), sailingClass);
        regatta2 = createRegatta("Regatta Two " + idSequence.incrementAndGet(), sailingClass);

        reg1 = saveRegistration(regatta1, "Alice", "alice-" + idSequence.get() + "@test.com", 1);
        reg2 = saveRegistration(regatta1, "Bob", "bob-" + idSequence.get() + "@test.com", 2);
        reg3 = saveRegistration(regatta2, "Alice", "alice-" + idSequence.get() + "@test.com", 1);
        reg4 = saveRegistration(regatta2, "Bob", "bob-" + idSequence.get() + "@test.com", 2);

        // regatta1: Alice 1st (1 pt), Bob 2nd (2 pts)
        Race race1 = saveRace(regatta1, 1);
        saveRaceResult(race1, reg1, 1, 1);
        saveRaceResult(race1, reg2, 2, 2);

        // regatta2: Bob 1st (1 pt), Alice 2nd (2 pts)
        Race race2 = saveRace(regatta2, 1);
        saveRaceResult(race2, reg3, 2, 2);
        saveRaceResult(race2, reg4, 1, 1);
    }

    @Test
    void calculateSeriesScores_regattaPlaces_sumsRanks() {
        Long seriesId = createSeries(SeriesScoringType.REGATTA_PLACES, 0, 0, 1.0, 1.0);

        SeriesScoreResponse response = seriesScoringService.calculateSeriesScores(seriesId, sailingClass.getId());

        assertThat(response.standings()).hasSize(2);
        assertThat(response.standings()).extracting(SeriesScoreResponse.SailorStanding::netScore)
                .allMatch(score -> score == 3.0);
    }

    @Test
    void calculateSeriesScores_sumRacePoints_poolsAllRaces() {
        Long seriesId = createSeries(SeriesScoringType.SUM_RACE_POINTS, 0, 0, 1.0, 1.0);

        SeriesScoreResponse response = seriesScoringService.calculateSeriesScores(seriesId, sailingClass.getId());

        assertThat(response.standings()).hasSize(2);
        assertThat(response.standings()).extracting(SeriesScoreResponse.SailorStanding::netScore)
                .allMatch(score -> score == 3.0);
        // Each sailor has 2 individual race scores, not regatta-level scores
        assertThat(response.standings().get(0).raceScores()).hasSize(2);
        assertThat(response.standings().get(0).regattaScores()).isEmpty();
    }

    @Test
    void calculateSeriesScores_sumRacePoints_ignoresPerRegattaThrowouts() {
        // Add a second bad race to regatta1 so per-regatta throwout would apply
        Race race1b = saveRace(regatta1, 2);
        saveRaceResult(race1b, reg1, 5, 5); // Alice: bad race
        saveRaceResult(race1b, reg2, 1, 1); // Bob: good race

        // Configure regatta1 with per-regatta throwout (throwoutAfter=2)
        regatta1.setThrowoutAfter(2);
        regatta1.setThrowoutLimit(1);
        regattaRepository.save(regatta1);

        // SUM_RACE_POINTS must NOT apply per-regatta throwouts
        // Alice: 1 + 5 (regatta1) + 2 (regatta2) = 8 total, no series throwouts → netScore=8
        Long seriesId = createSeries(SeriesScoringType.SUM_RACE_POINTS, 0, 0, 1.0, 1.0);
        SeriesScoreResponse response = seriesScoringService.calculateSeriesScores(seriesId, sailingClass.getId());

        SeriesScoreResponse.SailorStanding alice = response.standings().stream()
                .filter(s -> s.sailorName().equals("Alice"))
                .findFirst().orElseThrow();

        assertThat(alice.netScore()).isEqualTo(8.0);
        assertThat(alice.raceScores()).hasSize(3); // 2 from regatta1, 1 from regatta2
    }

    @Test
    void calculateSeriesScores_normalized_scoresAreFractions() {
        Long seriesId = createSeries(SeriesScoringType.NORMALIZED, 0, 0, 1.0, 1.0);

        SeriesScoreResponse response = seriesScoringService.calculateSeriesScores(seriesId, sailingClass.getId());

        // fleetSize=2, worst possible = fleetSize+1 = 3
        // Alice: regatta1 score=1/3, regatta2 score=2/3 → total ≈ 1.0
        // Bob:   regatta1 score=2/3, regatta2 score=1/3 → total ≈ 1.0
        assertThat(response.standings()).hasSize(2);
        assertThat(response.standings()).extracting(SeriesScoreResponse.SailorStanding::netScore)
                .allSatisfy(score -> assertThat(score).isCloseTo(1.0, within(0.001)));
    }

    @Test
    void calculateSeriesScores_withThrowout_excludesWorstRegatta() {
        Long seriesId = createSeries(SeriesScoringType.REGATTA_PLACES, 2, 1, 1.0, 1.0);

        SeriesScoreResponse response = seriesScoringService.calculateSeriesScores(seriesId, sailingClass.getId());

        // 2 regattas, throwoutAfter=2 → 1 throwout
        // Alice: scores [1.0, 2.0], worst=2.0 excluded → netScore=1.0
        // Bob:   scores [2.0, 1.0], worst=2.0 excluded → netScore=1.0
        assertThat(response.standings()).hasSize(2);
        assertThat(response.standings()).extracting(SeriesScoreResponse.SailorStanding::netScore)
                .allMatch(score -> score == 1.0);

        for (SeriesScoreResponse.SailorStanding standing : response.standings()) {
            long excludedCount = standing.regattaScores().stream()
                    .filter(SeriesScoreResponse.RegattaScore::excluded)
                    .count();
            assertThat(excludedCount).isEqualTo(1);
        }
    }

    @Test
    void calculateSeriesScores_normalizedWithCoefficient_appliesWeight() {
        // regatta1 coefficient=2.0, regatta2 coefficient=1.0
        Long seriesId = createSeries(SeriesScoringType.NORMALIZED, 0, 0, 2.0, 1.0);

        SeriesScoreResponse response = seriesScoringService.calculateSeriesScores(seriesId, sailingClass.getId());

        // Alice regatta1: (1/3)*2.0 ≈ 0.6667
        SeriesScoreResponse.SailorStanding alice = response.standings().stream()
                .filter(s -> s.sailorName().equals("Alice"))
                .findFirst()
                .orElseThrow();

        SeriesScoreResponse.RegattaScore aliceRegatta1Score = alice.regattaScores().stream()
                .filter(rs -> rs.regattaId().equals(regatta1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(aliceRegatta1Score.score()).isCloseTo(2.0 / 3.0, within(0.001));
    }

    // --- helpers ---

    private Long createSeries(SeriesScoringType type, int throwoutAfter, int throwoutLimit,
                               double coeff1, double coeff2) {
        var request = new CreateSeriesRequest(
                "Test Series " + idSequence.incrementAndGet(),
                null,
                type,
                List.of(
                        new CreateSeriesRequest.RegattaEntry(regatta1.getId(), coeff1),
                        new CreateSeriesRequest.RegattaEntry(regatta2.getId(), coeff2)
                ),
                throwoutAfter,
                throwoutLimit
        );
        return seriesService.createSeries(owner, request).id();
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

    private Regatta createRegatta(String name, SailingClass sc) {
        Regatta regatta = new Regatta();
        regatta.setName(name);
        regatta.setStartDate(LocalDate.of(2026, 8, 1));
        regatta.setEndDate(LocalDate.of(2026, 8, 5));
        regatta.setVenue("Test Venue");
        regatta.setThrowoutAfter(0);
        regatta.setThrowoutLimit(0);
        regatta.setOwner(owner);
        regatta.getSailingClasses().add(sc);
        return regattaRepository.save(regatta);
    }

    private Registration saveRegistration(Regatta regatta, String name, String email, int sailNumber) {
        Registration reg = new Registration();
        reg.setRegatta(regatta);
        reg.setSailorName(name);
        reg.setEmail(email);
        reg.setDateOfBirth(LocalDate.of(1990, 1, 1));
        reg.setGender(Gender.M);
        reg.setSailingClubName("Test Club");
        reg.setSailingClass(sailingClass);
        reg.setSailingNation(sailingNation);
        reg.setSailNumber(sailNumber);
        return registrationRepository.save(reg);
    }

    private Race saveRace(Regatta regatta, int raceNumber) {
        Race race = new Race();
        race.setRegatta(regatta);
        race.setRaceNumber(raceNumber);
        race.setSailingClass(sailingClass);
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
}

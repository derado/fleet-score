package com.fleetscore.regatta.service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.fleetscore.FleetScoreApplication;
import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.api.dto.CreateSeriesRequest;
import com.fleetscore.regatta.api.dto.SeriesResponse;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.SeriesScoringType;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.sailingclass.domain.HullType;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.domain.WorldSailingStatus;
import com.fleetscore.sailingclass.repository.SailingClassRepository;
import com.fleetscore.user.domain.UserAccount;
import com.fleetscore.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = FleetScoreApplication.class)
@ActiveProfiles("test")
@Transactional
class SeriesServiceTest {

    @Autowired SeriesService seriesService;
    @Autowired RegattaRepository regattaRepository;
    @Autowired SailingClassRepository sailingClassRepository;
    @Autowired UserAccountRepository userAccountRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private static final AtomicLong idSequence = new AtomicLong(90000L);

    private UserAccount userA;
    private UserAccount userB;
    private Long regattaId1;
    private Long regattaId2;

    @BeforeEach
    void setUp() {
        userA = createUser("series-user-a-" + idSequence.incrementAndGet() + "@example.com");
        userB = createUser("series-user-b-" + idSequence.incrementAndGet() + "@example.com");

        SailingClass sailingClass = createSailingClass(idSequence.incrementAndGet(), "Laser Series " + idSequence.get());

        regattaId1 = createRegatta("Regatta One " + idSequence.incrementAndGet(), userA, sailingClass).getId();
        regattaId2 = createRegatta("Regatta Two " + idSequence.incrementAndGet(), userA, sailingClass).getId();
    }

    @Test
    void createSeries_authenticated_persistsSeriesWithRegattas() {
        var request = new CreateSeriesRequest(
                "Autumn Cup Series",
                "A three-regatta autumn series",
                SeriesScoringType.REGATTA_PLACES,
                List.of(entry(regattaId1, null), entry(regattaId2, null)),
                null, null
        );

        SeriesResponse response = seriesService.createSeries(userA, request);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Autumn Cup Series");
        assertThat(response.description()).isEqualTo("A three-regatta autumn series");
        assertThat(response.scoringType()).isEqualTo(SeriesScoringType.REGATTA_PLACES);
        assertThat(response.ownerEmail()).isEqualTo(userA.getEmail());
        assertThat(response.regattas()).extracting(SeriesResponse.RegattaEntry::regattaId)
                .containsExactlyInAnyOrder(regattaId1, regattaId2);
        assertThat(response.regattas()).extracting(SeriesResponse.RegattaEntry::coefficient)
                .containsOnly(1.0);
        assertThat(response.throwoutAfter()).isEqualTo(0);
        assertThat(response.throwoutLimit()).isEqualTo(0);
    }

    @Test
    void createSeries_normalizedWithCoefficients_persistsCoefficients() {
        var request = new CreateSeriesRequest(
                "Weighted Series",
                null,
                SeriesScoringType.NORMALIZED,
                List.of(entry(regattaId1, 1.5), entry(regattaId2, 2.0)),
                null, null
        );

        SeriesResponse response = seriesService.createSeries(userA, request);

        assertThat(response.regattas()).extracting(SeriesResponse.RegattaEntry::coefficient)
                .containsExactlyInAnyOrder(1.5, 2.0);
    }

    @Test
    void createSeries_withThrowouts_persistsThrowoutConfig() {
        var request = new CreateSeriesRequest(
                "Winter Series",
                null,
                SeriesScoringType.REGATTA_PLACES,
                List.of(entry(regattaId1, null), entry(regattaId2, null)),
                5, 1
        );

        SeriesResponse response = seriesService.createSeries(userA, request);

        assertThat(response.throwoutAfter()).isEqualTo(5);
        assertThat(response.throwoutLimit()).isEqualTo(1);
    }

    @Test
    void createSeries_unknownRegattaId_throwsResourceNotFoundException() {
        long unknownId = 999_999L;
        var request = new CreateSeriesRequest(
                "Bad Series",
                null,
                SeriesScoringType.REGATTA_PLACES,
                List.of(entry(regattaId1, null), entry(unknownId, null)),
                null, null
        );

        assertThrows(ResourceNotFoundException.class,
                () -> seriesService.createSeries(userA, request));
    }

    @Test
    void findAllByOwner_returnsOnlyOwnSeries() {
        var requestA1 = new CreateSeriesRequest("Series A1", null, SeriesScoringType.REGATTA_PLACES, List.of(entry(regattaId1, null)), null, null);
        var requestA2 = new CreateSeriesRequest("Series A2", null, SeriesScoringType.REGATTA_PLACES, List.of(entry(regattaId2, null)), null, null);
        var requestB1 = new CreateSeriesRequest("Series B1", null, SeriesScoringType.REGATTA_PLACES, List.of(entry(regattaId1, null)), null, null);

        seriesService.createSeries(userA, requestA1);
        seriesService.createSeries(userA, requestA2);
        seriesService.createSeries(userB, requestB1);

        List<SeriesResponse> results = seriesService.findAllByOwner(userA);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(SeriesResponse::ownerEmail)
                .allMatch(email -> email.equals(userA.getEmail()));
    }

    private static CreateSeriesRequest.RegattaEntry entry(Long regattaId, Double coefficient) {
        return new CreateSeriesRequest.RegattaEntry(regattaId, coefficient);
    }

    // --- helpers ---

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

    private Regatta createRegatta(String name, UserAccount owner, SailingClass sailingClass) {
        Regatta regatta = new Regatta();
        regatta.setName(name);
        regatta.setStartDate(LocalDate.of(2026, 8, 1));
        regatta.setEndDate(LocalDate.of(2026, 8, 5));
        regatta.setVenue("Test Venue");
        regatta.setThrowoutAfter(0);
        regatta.setThrowoutLimit(0);
        regatta.setOwner(owner);
        regatta.getSailingClasses().add(sailingClass);
        return regattaRepository.save(regatta);
    }
}

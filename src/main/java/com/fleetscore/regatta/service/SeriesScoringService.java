package com.fleetscore.regatta.service;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.api.dto.RegattaScoreResponse;
import com.fleetscore.regatta.api.dto.SeriesScoreResponse;
import com.fleetscore.regatta.domain.Circumstance;
import com.fleetscore.regatta.domain.Race;
import com.fleetscore.regatta.domain.RaceResult;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.domain.Series;
import com.fleetscore.regatta.domain.SeriesRegatta;
import com.fleetscore.regatta.domain.SeriesScoringType;
import com.fleetscore.regatta.repository.RaceRepository;
import com.fleetscore.regatta.repository.RaceResultRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.regatta.repository.SeriesRepository;
import com.fleetscore.regatta.scoring.LowPointScoringCalculator;
import com.fleetscore.regatta.scoring.SeriesScoringCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SeriesScoringService {

    private final SeriesRepository seriesRepository;
    private final ScoringService scoringService;
    private final RegistrationRepository registrationRepository;
    private final SeriesScoringCalculator seriesScoringCalculator;
    private final RaceRepository raceRepository;
    private final RaceResultRepository raceResultRepository;
    private final LowPointScoringCalculator lowPointScoringCalculator;

    @Transactional(readOnly = true)
    public SeriesScoreResponse calculateSeriesScores(Long seriesId, Long sailingClassId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId));

        if (series.getScoringType() == SeriesScoringType.SUM_RACE_POINTS) {
            return calculateSumRacePoints(series, sailingClassId);
        }

        return calculatePerRegattaAggregation(series, sailingClassId);
    }

    private SeriesScoreResponse calculatePerRegattaAggregation(Series series, Long sailingClassId) {
        List<SeriesScoringCalculator.RegattaInput> regattaInputs = new ArrayList<>();
        List<SeriesScoreResponse.RegattaInfo> regattaInfos = new ArrayList<>();
        String sailingClassName = null;

        for (SeriesRegatta seriesRegatta : series.getRegattas()) {
            RegattaScoreResponse scoreResponse = fetchScoresQuietly(seriesRegatta.getRegatta().getId(), sailingClassId);
            if (scoreResponse == null) {
                continue;
            }
            if (sailingClassName == null) {
                sailingClassName = scoreResponse.sailingClassName();
            }
            regattaInfos.add(new SeriesScoreResponse.RegattaInfo(
                    seriesRegatta.getRegatta().getId(),
                    seriesRegatta.getRegatta().getName()
            ));
            regattaInputs.add(buildRegattaInput(seriesRegatta, scoreResponse));
        }

        if (regattaInputs.isEmpty()) {
            return emptyResponse(series, sailingClassId, sailingClassName);
        }

        List<SeriesScoringCalculator.SailorSeriesScore> scores = seriesScoringCalculator.calculate(
                series.getScoringType(), regattaInputs, series.getThrowoutAfter(), series.getThrowoutLimit());

        return new SeriesScoreResponse(
                series.getId(), series.getName(), series.getScoringType(),
                sailingClassId, sailingClassName,
                series.getThrowoutAfter(), series.getThrowoutLimit(),
                regattaInfos, toStandings(scores));
    }

    private SeriesScoreResponse calculateSumRacePoints(Series series, Long sailingClassId) {
        List<SeriesScoreResponse.RegattaInfo> regattaInfos = new ArrayList<>();
        List<LowPointScoringCalculator.RaceResultInput> allInputs = new ArrayList<>();
        Map<Long, Long> raceToRegattaId = new HashMap<>();
        Map<String, Long> keyToSyntheticId = new HashMap<>();
        Map<Long, SailorMetadata> metadataById = new HashMap<>();
        String sailingClassName = null;
        long nextId = 1L;
        int globalRaceNumber = 0;

        for (SeriesRegatta seriesRegatta : series.getRegattas()) {
            Regatta regatta = seriesRegatta.getRegatta();
            List<Race> races = raceRepository.findByRegattaIdAndSailingClassId(regatta.getId(), sailingClassId);
            if (races.isEmpty()) {
                continue;
            }
            regattaInfos.add(new SeriesScoreResponse.RegattaInfo(regatta.getId(), regatta.getName()));

            List<Long> raceIds = races.stream().map(Race::getId).toList();
            List<RaceResult> results = raceResultRepository.findByRaceIdIn(raceIds);

            List<Race> sortedRaces = races.stream()
                    .sorted(Comparator.comparingInt(Race::getRaceNumber))
                    .toList();

            for (Race race : sortedRaces) {
                globalRaceNumber++;
                final int gn = globalRaceNumber;
                raceToRegattaId.put(race.getId(), regatta.getId());

                for (RaceResult result : results) {
                    if (!result.getRace().getId().equals(race.getId())) continue;

                    Registration reg = result.getRegistration();
                    String sailorKey = resolveSailorKey(reg);

                    if (!keyToSyntheticId.containsKey(sailorKey)) {
                        long sid = nextId++;
                        keyToSyntheticId.put(sailorKey, sid);
                        metadataById.put(sid, new SailorMetadata(
                                sailorKey, reg.getSailorName(),
                                reg.getSailingNation().getCode(), reg.getSailingClubName()));
                        if (sailingClassName == null) {
                            sailingClassName = reg.getSailingClass().getName();
                        }
                    }

                    allInputs.add(new LowPointScoringCalculator.RaceResultInput(
                            keyToSyntheticId.get(sailorKey), race.getId(), gn,
                            result.getPoints(), result.getCircumstance() == Circumstance.DNE));
                }
            }
        }

        if (allInputs.isEmpty()) {
            return emptyResponse(series, sailingClassId, sailingClassName);
        }

        List<LowPointScoringCalculator.SailorScore> scores = lowPointScoringCalculator.calculateScores(
                allInputs, series.getThrowoutAfter(), series.getThrowoutLimit());

        List<SeriesScoreResponse.SailorStanding> standings = scores.stream()
                .map(score -> toSumRaceStanding(score, metadataById, raceToRegattaId))
                .toList();

        return new SeriesScoreResponse(
                series.getId(), series.getName(), series.getScoringType(),
                sailingClassId, sailingClassName,
                series.getThrowoutAfter(), series.getThrowoutLimit(),
                regattaInfos, standings);
    }

    private SeriesScoreResponse.SailorStanding toSumRaceStanding(
            LowPointScoringCalculator.SailorScore score,
            Map<Long, SailorMetadata> metadataById,
            Map<Long, Long> raceToRegattaId
    ) {
        SailorMetadata meta = metadataById.get(score.registrationId());
        List<SeriesScoreResponse.RaceScore> raceScores = score.raceScores().stream()
                .map(rs -> new SeriesScoreResponse.RaceScore(
                        rs.raceId(), rs.raceNumber(),
                        raceToRegattaId.get(rs.raceId()),
                        rs.points(), rs.excluded()))
                .toList();
        return new SeriesScoreResponse.SailorStanding(
                score.rank(), meta.sailorKey(), meta.sailorName(),
                meta.nationCode(), meta.clubName(),
                List.of(), raceScores,
                score.totalPoints(), score.netPoints());
    }

    private RegattaScoreResponse fetchScoresQuietly(Long regattaId, Long sailingClassId) {
        try {
            return scoringService.calculateScores(regattaId, sailingClassId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SeriesScoringCalculator.RegattaInput buildRegattaInput(
            SeriesRegatta seriesRegatta, RegattaScoreResponse scoreResponse) {
        List<Long> registrationIds = scoreResponse.standings().stream()
                .map(RegattaScoreResponse.SailorStanding::registrationId)
                .toList();
        Map<Long, Registration> registrationMap = registrationRepository.findAllById(registrationIds)
                .stream()
                .collect(Collectors.toMap(Registration::getId, r -> r));
        List<SeriesScoringCalculator.SailorInput> sailorInputs = scoreResponse.standings().stream()
                .map(standing -> toSailorInput(standing, registrationMap))
                .toList();
        return new SeriesScoringCalculator.RegattaInput(
                seriesRegatta.getRegatta().getId(),
                seriesRegatta.getCoefficient(),
                scoreResponse.standings().size(),
                sailorInputs);
    }

    private SeriesScoringCalculator.SailorInput toSailorInput(
            RegattaScoreResponse.SailorStanding standing,
            Map<Long, Registration> registrationMap
    ) {
        Registration registration = registrationMap.get(standing.registrationId());
        return new SeriesScoringCalculator.SailorInput(
                resolveSailorKey(registration),
                standing.sailorName(),
                standing.nationCode(),
                standing.sailingClubName(),
                standing.netPoints(),
                standing.rank());
    }

    private String resolveSailorKey(Registration registration) {
        if (registration != null && registration.getSailor() != null) {
            return "sailor:" + registration.getSailor().getId();
        }
        if (registration != null) {
            return "email:" + registration.getEmail();
        }
        return "unknown";
    }

    private List<SeriesScoreResponse.SailorStanding> toStandings(
            List<SeriesScoringCalculator.SailorSeriesScore> scores) {
        return scores.stream().map(this::toStanding).toList();
    }

    private SeriesScoreResponse.SailorStanding toStanding(SeriesScoringCalculator.SailorSeriesScore score) {
        List<SeriesScoreResponse.RegattaScore> regattaScores = score.regattaScores().stream()
                .map(rs -> new SeriesScoreResponse.RegattaScore(rs.regattaId(), rs.score(), rs.excluded()))
                .toList();
        return new SeriesScoreResponse.SailorStanding(
                score.rank(), score.sailorKey(), score.sailorName(),
                score.nationCode(), score.clubName(),
                regattaScores, List.of(),
                score.totalScore(), score.netScore());
    }

    private SeriesScoreResponse emptyResponse(Series series, Long sailingClassId, String sailingClassName) {
        return new SeriesScoreResponse(
                series.getId(), series.getName(), series.getScoringType(),
                sailingClassId, sailingClassName,
                series.getThrowoutAfter(), series.getThrowoutLimit(),
                List.of(), List.of());
    }

    private record SailorMetadata(String sailorKey, String sailorName, String nationCode, String clubName) {}
}

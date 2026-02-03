package com.fleetscore.regatta.service;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.api.dto.RegattaScoreResponse;
import com.fleetscore.regatta.domain.Circumstance;
import com.fleetscore.regatta.domain.Race;
import com.fleetscore.regatta.domain.RaceResult;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.repository.RaceRepository;
import com.fleetscore.regatta.repository.RaceResultRepository;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.regatta.scoring.LowPointScoringCalculator;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.internal.SailingClassInternalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class ScoringService {

    private final RegattaRepository regattaRepository;
    private final RaceRepository raceRepository;
    private final RaceResultRepository raceResultRepository;
    private final RegistrationRepository registrationRepository;
    private final SailingClassInternalApi sailingClassApi;
    private final LowPointScoringCalculator scoringCalculator;

    @Transactional(readOnly = true)
    public RegattaScoreResponse calculateScores(Long regattaId, Long sailingClassId) {
        Regatta regatta = regattaRepository.findById(regattaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));

        SailingClass sailingClass = sailingClassApi.findById(sailingClassId);

        if (!regatta.getSailingClasses().contains(sailingClass)) {
            throw new IllegalArgumentException("Sailing class is not part of this regatta");
        }

        List<Race> races = raceRepository.findByRegattaIdAndSailingClassId(regattaId, sailingClassId);

        List<RegattaScoreResponse.RaceInfo> raceInfos = races.stream()
                .sorted(Comparator.comparingInt(Race::getRaceNumber))
                .map(race -> new RegattaScoreResponse.RaceInfo(race.getId(), race.getRaceNumber()))
                .toList();

        Map<Long, Map<Long, RaceResult>> resultsByRegistrationAndRace = new HashMap<>();
        Map<Long, Registration> registrationMap = new HashMap<>();

        for (Race race : races) {
            List<RaceResult> raceResults = raceResultRepository.findByRaceId(race.getId());
            for (RaceResult result : raceResults) {
                Registration reg = result.getRegistration();
                registrationMap.putIfAbsent(reg.getId(), reg);

                resultsByRegistrationAndRace
                        .computeIfAbsent(reg.getId(), k -> new HashMap<>())
                        .put(race.getId(), result);
            }
        }

        List<LowPointScoringCalculator.RaceResultInput> inputs = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, RaceResult>> entry : resultsByRegistrationAndRace.entrySet()) {
            Long registrationId = entry.getKey();
            for (RaceResult result : entry.getValue().values()) {
                boolean isNonExcludable = isNonExcludableCircumstance(result.getCircumstance());
                inputs.add(new LowPointScoringCalculator.RaceResultInput(
                        registrationId,
                        result.getRace().getId(),
                        result.getRace().getRaceNumber(),
                        result.getPoints(),
                        isNonExcludable
                ));
            }
        }

        int throwoutAfter = regatta.getThrowoutAfter();
        int throwoutLimit = regatta.getThrowoutLimit();
        int appliedThrowouts = scoringCalculator.calculateThrowouts(races.size(), throwoutAfter, throwoutLimit);

        List<LowPointScoringCalculator.SailorScore> calculatedScores =
                scoringCalculator.calculateScores(inputs, throwoutAfter, throwoutLimit);

        List<RegattaScoreResponse.SailorStanding> standings = new ArrayList<>();
        for (LowPointScoringCalculator.SailorScore score : calculatedScores) {
            Registration registration = registrationMap.get(score.registrationId());
            if (registration == null) continue;

            List<RegattaScoreResponse.RaceResult> raceResults = new ArrayList<>();
            Map<Long, RaceResult> regResults = resultsByRegistrationAndRace.get(score.registrationId());

            for (LowPointScoringCalculator.RaceScore raceScore : score.raceScores()) {
                RaceResult result = regResults != null ? regResults.get(raceScore.raceId()) : null;
                raceResults.add(new RegattaScoreResponse.RaceResult(
                        raceScore.raceId(),
                        raceScore.raceNumber(),
                        result != null ? result.getPosition() : null,
                        result != null ? result.getCircumstance() : null,
                        raceScore.points(),
                        raceScore.excluded()
                ));
            }

            standings.add(new RegattaScoreResponse.SailorStanding(
                    score.rank(),
                    registration.getId(),
                    registration.getSailorName(),
                    registration.getSailNumber(),
                    registration.getSailingNation().getCode(),
                    score.netPoints(),
                    score.totalPoints(),
                    raceResults
            ));
        }

        return new RegattaScoreResponse(
                regattaId,
                sailingClassId,
                sailingClass.getName(),
                throwoutAfter,
                throwoutLimit,
                appliedThrowouts,
                raceInfos,
                standings
        );
    }

    private boolean isNonExcludableCircumstance(Circumstance circumstance) {
        if (circumstance == null) {
            return false;
        }
        return circumstance == Circumstance.DNE || circumstance == Circumstance.DPI;
    }
}

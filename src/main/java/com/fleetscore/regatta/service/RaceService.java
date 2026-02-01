package com.fleetscore.regatta.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.api.dto.CreateRaceRequest;
import com.fleetscore.regatta.api.dto.RaceResponse;
import com.fleetscore.regatta.api.dto.RaceResultRequest;
import com.fleetscore.regatta.domain.Race;
import com.fleetscore.regatta.domain.RaceResult;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.repository.RaceRepository;
import com.fleetscore.regatta.repository.RaceResultRepository;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.internal.SailingClassInternalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;
    private final RaceResultRepository raceResultRepository;
    private final RegattaRepository regattaRepository;
    private final RegistrationRepository registrationRepository;
    private final SailingClassInternalApi sailingClassApi;

    @Transactional
    @PreAuthorize("isAuthenticated() and @regattaAuthz.isAdmin(principal?.id, #regattaId)")
    public RaceResponse createRace(Long regattaId, CreateRaceRequest request) {
        Regatta regatta = regattaRepository.findById(regattaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));

        SailingClass sailingClass = sailingClassApi.findById(request.sailingClassId());

        if (!regatta.getSailingClasses().contains(sailingClass)) {
            throw new IllegalArgumentException("Sailing class is not part of this regatta");
        }

        if (raceRepository.existsByRegattaIdAndRaceNumberAndSailingClassId(
                regattaId, request.raceNumber(), request.sailingClassId())) {
            throw new IllegalArgumentException("Race with this number already exists for this class");
        }

        Race race = new Race();
        race.setRegatta(regatta);
        race.setSailingClass(sailingClass);
        race.setRaceNumber(request.raceNumber());
        race.setRaceDate(request.raceDate());

        Race savedRace = raceRepository.save(race);

        int totalRegistered = raceResultRepository.countRegistrationsByRegattaAndClass(regattaId, request.sailingClassId());
        int dnfPoints = totalRegistered + 1;

        Set<Long> registrationIds = request.results().stream()
                .map(RaceResultRequest::registrationId)
                .collect(Collectors.toSet());

        List<Registration> registrations = registrationRepository.findAllById(registrationIds);
        Map<Long, Registration> registrationMap = registrations.stream()
                .collect(Collectors.toMap(Registration::getId, Function.identity()));

        for (RaceResultRequest resultRequest : request.results()) {
            Registration registration = registrationMap.get(resultRequest.registrationId());
            if (registration == null) {
                throw new ResourceNotFoundException("Registration not found: " + resultRequest.registrationId());
            }
            if (!registration.getSailingClass().getId().equals(request.sailingClassId())) {
                throw new IllegalArgumentException(
                        "Registration " + resultRequest.registrationId() + " is not in the same sailing class as the race");
            }
            if (!registration.getRegatta().getId().equals(regattaId)) {
                throw new IllegalArgumentException(
                        "Registration " + resultRequest.registrationId() + " is not for the same regatta");
            }
        }

        List<RaceResult> results = new ArrayList<>();
        for (RaceResultRequest resultRequest : request.results()) {
            Registration registration = registrationMap.get(resultRequest.registrationId());

            RaceResult result = new RaceResult();
            result.setRace(savedRace);
            result.setRegistration(registration);
            result.setPosition(resultRequest.position());
            result.setCircumstance(resultRequest.circumstance());

            if (resultRequest.circumstance() != null) {
                result.setPoints(dnfPoints);
            } else if (resultRequest.position() != null) {
                result.setPoints(resultRequest.position());
            } else {
                result.setPoints(dnfPoints);
            }

            results.add(result);
        }

        raceResultRepository.saveAll(results);

        List<RaceResult> savedResults = raceResultRepository.findByRaceId(savedRace.getId());
        return toRaceResponse(savedRace, savedResults);
    }

    @Transactional(readOnly = true)
    public List<RaceResponse> findRacesByRegatta(Long regattaId, Long sailingClassId) {
        if (!regattaRepository.existsById(regattaId)) {
            throw new ResourceNotFoundException("Regatta not found");
        }

        List<Race> races;
        if (sailingClassId != null) {
            races = raceRepository.findByRegattaIdAndSailingClassId(regattaId, sailingClassId);
        } else {
            races = raceRepository.findByRegattaId(regattaId);
        }

        return races.stream()
                .map(race -> {
                    List<RaceResult> results = raceResultRepository.findByRaceId(race.getId());
                    return toRaceResponse(race, results);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public RaceResponse findRaceById(Long raceId) {
        Race race = raceRepository.findByIdWithSailingClass(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
        List<RaceResult> results = raceResultRepository.findByRaceId(raceId);
        return toRaceResponse(race, results);
    }

    @Transactional
    @PreAuthorize("isAuthenticated() and @regattaAuthz.isAdmin(principal?.id, @raceService.findRegattaIdByRaceId(#raceId))")
    public RaceResponse updateRace(Long raceId, CreateRaceRequest request) {
        Race race = raceRepository.findByIdWithSailingClass(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));

        Long regattaId = race.getRegatta().getId();
        Long currentSailingClassId = race.getSailingClass().getId();

        if (!request.sailingClassId().equals(currentSailingClassId)) {
            throw new IllegalArgumentException("Sailing class cannot be changed");
        }

        if (!request.raceNumber().equals(race.getRaceNumber()) &&
                raceRepository.existsByRegattaIdAndRaceNumberAndSailingClassId(
                        regattaId, request.raceNumber(), request.sailingClassId())) {
            throw new IllegalArgumentException("Race with this number already exists for this class");
        }

        race.setRaceNumber(request.raceNumber());
        race.setRaceDate(request.raceDate());

        int totalRegistered = raceResultRepository.countRegistrationsByRegattaAndClass(regattaId, request.sailingClassId());
        int dnfPoints = totalRegistered + 1;

        Set<Long> registrationIds = request.results().stream()
                .map(RaceResultRequest::registrationId)
                .collect(Collectors.toSet());

        List<Registration> registrations = registrationRepository.findAllById(registrationIds);
        Map<Long, Registration> registrationMap = registrations.stream()
                .collect(Collectors.toMap(Registration::getId, Function.identity()));

        for (RaceResultRequest resultRequest : request.results()) {
            Registration registration = registrationMap.get(resultRequest.registrationId());
            if (registration == null) {
                throw new ResourceNotFoundException("Registration not found: " + resultRequest.registrationId());
            }
            if (!registration.getSailingClass().getId().equals(request.sailingClassId())) {
                throw new IllegalArgumentException(
                        "Registration " + resultRequest.registrationId() + " is not in the same sailing class as the race");
            }
            if (!registration.getRegatta().getId().equals(regattaId)) {
                throw new IllegalArgumentException(
                        "Registration " + resultRequest.registrationId() + " is not for the same regatta");
            }
        }

        raceResultRepository.deleteByRaceId(raceId);

        List<RaceResult> results = new ArrayList<>();
        for (RaceResultRequest resultRequest : request.results()) {
            Registration registration = registrationMap.get(resultRequest.registrationId());

            RaceResult result = new RaceResult();
            result.setRace(race);
            result.setRegistration(registration);
            result.setPosition(resultRequest.position());
            result.setCircumstance(resultRequest.circumstance());

            if (resultRequest.circumstance() != null) {
                result.setPoints(dnfPoints);
            } else if (resultRequest.position() != null) {
                result.setPoints(resultRequest.position());
            } else {
                result.setPoints(dnfPoints);
            }

            results.add(result);
        }

        raceResultRepository.saveAll(results);

        List<RaceResult> savedResults = raceResultRepository.findByRaceId(raceId);
        return toRaceResponse(race, savedResults);
    }

    private RaceResponse toRaceResponse(Race race, List<RaceResult> results) {
        List<RaceResponse.RaceResultItem> resultItems = results.stream()
                .map(this::toRaceResultItem)
                .toList();

        return new RaceResponse(
                race.getId(),
                race.getRegatta().getId(),
                new RaceResponse.SailingClassSummary(
                        race.getSailingClass().getId(),
                        race.getSailingClass().getName()
                ),
                race.getRaceNumber(),
                race.getRaceDate(),
                resultItems
        );
    }

    private RaceResponse.RaceResultItem toRaceResultItem(RaceResult result) {
        Registration reg = result.getRegistration();
        return new RaceResponse.RaceResultItem(
                result.getId(),
                new RaceResponse.RegistrationSummary(
                        reg.getId(),
                        reg.getSailorName(),
                        reg.getSailNumber(),
                        reg.getSailingClass().getName(),
                        reg.getSailingNation().getCode()
                ),
                result.getPosition(),
                result.getCircumstance(),
                result.getPoints()
        );
    }

    @Transactional(readOnly = true)
    public Long findRegattaIdByRaceId(Long raceId) {
        Race race = raceRepository.findById(raceId)
                .orElseThrow(() -> new ResourceNotFoundException("Race not found"));
        return race.getRegatta().getId();
    }
}

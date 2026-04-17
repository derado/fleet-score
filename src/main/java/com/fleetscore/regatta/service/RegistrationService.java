package com.fleetscore.regatta.service;

import java.util.List;

import com.fleetscore.club.domain.SailingClub;
import com.fleetscore.club.internal.ClubInternalApi;
import com.fleetscore.common.exception.DuplicateSailNumberException;
import com.fleetscore.common.exception.RegistrationInUseException;
import com.fleetscore.common.exception.ResourceNotFoundException;
import com.fleetscore.regatta.api.dto.CreateRegistrationRequest;
import com.fleetscore.regatta.api.dto.RegistrationResponse;
import com.fleetscore.common.domain.Gender;
import com.fleetscore.regatta.domain.Regatta;
import com.fleetscore.regatta.domain.Registration;
import com.fleetscore.regatta.repository.RaceResultRepository;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.sailor.domain.Sailor;
import com.fleetscore.sailingclass.domain.SailingClass;
import com.fleetscore.sailingclass.internal.SailingClassInternalApi;
import com.fleetscore.sailingnation.domain.SailingNation;
import com.fleetscore.sailingnation.internal.SailingNationInternalApi;
import com.fleetscore.user.domain.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final RegattaRepository regattaRepository;
    private final SailingClassInternalApi sailingClassApi;
    private final SailingNationInternalApi sailingNationApi;
    private final ClubInternalApi clubApi;
    private final SailorResolver sailorResolver;
    private final RegattaAuthorizationService regattaAuthz;
    private final RaceResultRepository raceResultRepository;

    @Transactional
    public RegistrationResponse createRegistration(Long regattaId, CreateRegistrationRequest request, UserAccount currentUser) {
        Regatta regatta = regattaRepository.findById(regattaId)
                .orElseThrow(() -> new ResourceNotFoundException("Regatta not found"));

        SailingClass sailingClass = sailingClassApi.findById(request.sailingClassId());

        if (!regatta.getSailingClasses().contains(sailingClass)) {
            throw new IllegalArgumentException("Sailing class is not part of this regatta");
        }

        SailingNation sailingNation = sailingNationApi.findById(request.sailingNationId());

        if (registrationRepository.existsByRegattaIdAndSailNumberAndSailingClassId(
                regattaId, request.sailNumber(), request.sailingClassId())) {
            throw new DuplicateSailNumberException(request.sailNumber());
        }

        SailingClub sailingClub = null;
        String sailingClubName = request.sailingClubName();
        if (request.sailingClubId() != null) {
            sailingClub = clubApi.findById(request.sailingClubId());
            sailingClubName = sailingClub.getName();
        }

        Sailor sailor = sailorResolver.findOrCreate(
                request.sailorId(),
                request.email(),
                request.sailorName(),
                request.dateOfBirth(),
                request.gender()
        );

        Registration registration = new Registration();
        registration.setRegatta(regatta);
        registration.setSailorName(request.sailorName());
        registration.setEmail(request.email());
        registration.setDateOfBirth(request.dateOfBirth());
        registration.setGender(request.gender());
        registration.setSailor(sailor);
        registration.setSailingClubName(sailingClubName);
        registration.setSailingClub(sailingClub);
        registration.setUser(currentUser);
        registration.setSailingClass(sailingClass);
        registration.setSailingNation(sailingNation);
        registration.setSailNumber(request.sailNumber());

        Registration saved = registrationRepository.save(registration);
        return toResponse(saved);
    }

    @Transactional
    public RegistrationResponse updateRegistration(Long registrationId, CreateRegistrationRequest request, UserAccount currentUser) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        checkUpdateOrDeleteAuthorization(registration, currentUser);

        SailingClass sailingClass = sailingClassApi.findById(request.sailingClassId());

        if (!registration.getRegatta().getSailingClasses().contains(sailingClass)) {
            throw new IllegalArgumentException("Sailing class is not part of this regatta");
        }

        SailingNation sailingNation = sailingNationApi.findById(request.sailingNationId());

        if (registrationRepository.existsByRegattaIdAndSailNumberAndSailingClassIdAndIdNot(
                registration.getRegatta().getId(), request.sailNumber(), request.sailingClassId(), registrationId)) {
            throw new DuplicateSailNumberException(request.sailNumber());
        }

        SailingClub sailingClub = null;
        if (request.sailingClubId() != null) {
            sailingClub = clubApi.findById(request.sailingClubId());
        }

        Sailor sailor = sailorResolver.findOrCreate(
                request.sailorId(),
                request.email(),
                request.sailorName(),
                request.dateOfBirth(),
                request.gender()
        );

        registration.setSailorName(request.sailorName());
        registration.setEmail(request.email());
        registration.setDateOfBirth(request.dateOfBirth());
        registration.setGender(request.gender());
        registration.setSailor(sailor);
        registration.setSailingClubName(request.sailingClubName());
        registration.setSailingClub(sailingClub);
        registration.setSailingClass(sailingClass);
        registration.setSailingNation(sailingNation);
        registration.setSailNumber(request.sailNumber());

        return toResponse(registration);
    }

    @Transactional
    public void deleteRegistration(Long registrationId, UserAccount currentUser) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        checkUpdateOrDeleteAuthorization(registration, currentUser);

        if (raceResultRepository.existsByRegistrationId(registrationId)) {
            throw new RegistrationInUseException();
        }

        registrationRepository.delete(registration);
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> findRegistrationsByRegatta(
            Long regattaId,
            Long sailingClassId,
            Long sailingNationId,
            String sailorName,
            String sailingClubName,
            Integer sailNumber,
            Gender gender) {
        if (!regattaRepository.existsById(regattaId)) {
            throw new ResourceNotFoundException("Regatta not found");
        }

        return registrationRepository
                .findByRegattaIdWithFilters(regattaId, sailingClassId, sailingNationId, sailorName, sailingClubName, sailNumber, gender)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void checkUpdateOrDeleteAuthorization(Registration registration, UserAccount currentUser) {
        boolean isOwner = registration.getUser() != null
                && registration.getUser().getId().equals(currentUser.getId());
        boolean isRegattaAdmin = regattaAuthz.isAdmin(currentUser.getId(), registration.getRegatta().getId())
                || regattaAuthz.isOwner(currentUser.getId(), registration.getRegatta().getId());
        if (!isOwner && !isRegattaAdmin) {
            throw new AccessDeniedException("Not authorised to update this registration");
        }
    }

    private RegistrationResponse toResponse(Registration reg) {
        Long sailingClubId = reg.getSailingClub() != null ? reg.getSailingClub().getId() : null;
        Long userId = reg.getUser() != null ? reg.getUser().getId() : null;
        Long sailorId = reg.getSailor() != null ? reg.getSailor().getId() : null;

        return new RegistrationResponse(
                reg.getId(),
                reg.getSailorName(),
                reg.getEmail(),
                reg.getDateOfBirth(),
                reg.getGender(),
                sailorId,
                reg.getSailingClubName(),
                sailingClubId,
                userId,
                new RegistrationResponse.SailingClassSummary(
                        reg.getSailingClass().getId(),
                        reg.getSailingClass().getName()
                ),
                new RegistrationResponse.NationSummary(
                        reg.getSailingNation().getId(),
                        reg.getSailingNation().getCode(),
                        reg.getSailingNation().getCountry()
                ),
                reg.getSailNumber()
        );
    }
}

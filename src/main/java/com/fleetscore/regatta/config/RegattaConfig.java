package com.fleetscore.regatta.config;

import com.fleetscore.club.internal.ClubInternalApi;
import com.fleetscore.organisation.internal.OrganisationInternalApi;
import com.fleetscore.regatta.internal.RegattaInternalApi;
import com.fleetscore.regatta.repository.RaceRepository;
import com.fleetscore.regatta.repository.RaceResultRepository;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.repository.RegistrationRepository;
import com.fleetscore.regatta.service.RaceService;
import com.fleetscore.regatta.service.RegattaAuthorizationService;
import com.fleetscore.regatta.service.RegattaService;
import com.fleetscore.regatta.service.RegistrationService;
import com.fleetscore.sailingclass.internal.SailingClassInternalApi;
import com.fleetscore.sailingnation.internal.SailingNationInternalApi;
import com.fleetscore.user.internal.UserInternalApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegattaConfig {

    @Bean
    RegattaInternalApi regattaInternalApi(RegattaRepository regattaRepository) {
        return new RegattaInternalApi(regattaRepository);
    }

    @Bean("regattaAuthz")
    RegattaAuthorizationService regattaAuthorizationService(RegattaRepository regattaRepository) {
        return new RegattaAuthorizationService(regattaRepository);
    }

    @Bean
    RegattaService regattaService(
            RegattaRepository regattaRepository,
            SailingClassInternalApi sailingClassApi,
            ClubInternalApi clubApi,
            OrganisationInternalApi organisationApi,
            UserInternalApi userApi) {
        return new RegattaService(regattaRepository, sailingClassApi, clubApi, organisationApi, userApi);
    }

    @Bean
    RegistrationService registrationService(
            RegistrationRepository registrationRepository,
            RegattaRepository regattaRepository,
            SailingClassInternalApi sailingClassApi,
            SailingNationInternalApi sailingNationApi,
            ClubInternalApi clubApi) {
        return new RegistrationService(registrationRepository, regattaRepository, sailingClassApi, sailingNationApi, clubApi);
    }

    @Bean
    RaceService raceService(
            RaceRepository raceRepository,
            RaceResultRepository raceResultRepository,
            RegattaRepository regattaRepository,
            RegistrationRepository registrationRepository,
            SailingClassInternalApi sailingClassApi) {
        return new RaceService(raceRepository, raceResultRepository, regattaRepository, registrationRepository, sailingClassApi);
    }
}

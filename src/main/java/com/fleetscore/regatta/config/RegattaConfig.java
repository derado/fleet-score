package com.fleetscore.regatta.config;

import com.fleetscore.club.internal.ClubInternalApi;
import com.fleetscore.organisation.internal.OrganisationInternalApi;
import com.fleetscore.regatta.internal.RegattaInternalApi;
import com.fleetscore.regatta.repository.RegattaRepository;
import com.fleetscore.regatta.service.RegattaAuthorizationService;
import com.fleetscore.regatta.service.RegattaService;
import com.fleetscore.sailingclass.internal.SailingClassInternalApi;
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
}

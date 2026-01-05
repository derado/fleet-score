package com.fleetscore.organisation.config;

import com.fleetscore.organisation.internal.OrganisationInternalApi;
import com.fleetscore.organisation.repository.OrganisationRepository;
import com.fleetscore.organisation.service.OrganisationAuthorizationService;
import com.fleetscore.organisation.service.OrganisationService;
import com.fleetscore.user.internal.UserInternalApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrganisationConfig {

    @Bean
    OrganisationInternalApi organisationInternalApi(OrganisationRepository organisationRepository) {
        return new OrganisationInternalApi(organisationRepository);
    }

    @Bean("orgAuthz")
    OrganisationAuthorizationService organisationAuthorizationService(OrganisationRepository organisationRepository) {
        return new OrganisationAuthorizationService(organisationRepository);
    }

    @Bean
    OrganisationService organisationService(OrganisationRepository organisationRepository, UserInternalApi userApi) {
        return new OrganisationService(organisationRepository, userApi);
    }
}

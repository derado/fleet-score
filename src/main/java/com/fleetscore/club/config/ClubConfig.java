package com.fleetscore.club.config;

import com.fleetscore.club.repository.SailingClubRepository;
import com.fleetscore.club.service.ClubAuthorizationService;
import com.fleetscore.club.service.SailingClubService;
import com.fleetscore.organisation.internal.OrganisationInternalApi;
import com.fleetscore.user.internal.UserInternalApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClubConfig {

    @Bean("clubAuthz")
    ClubAuthorizationService clubAuthorizationService(SailingClubRepository sailingClubRepository) {
        return new ClubAuthorizationService(sailingClubRepository);
    }

    @Bean
    SailingClubService sailingClubService(
            SailingClubRepository sailingClubRepository,
            OrganisationInternalApi organisationApi,
            UserInternalApi userApi) {
        return new SailingClubService(sailingClubRepository, organisationApi, userApi);
    }
}

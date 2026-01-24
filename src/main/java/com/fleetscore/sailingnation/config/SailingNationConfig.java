package com.fleetscore.sailingnation.config;

import com.fleetscore.sailingnation.internal.SailingNationInternalApi;
import com.fleetscore.sailingnation.repository.SailingNationRepository;
import com.fleetscore.sailingnation.service.SailingNationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SailingNationConfig {

    @Bean
    SailingNationInternalApi sailingNationInternalApi(SailingNationRepository sailingNationRepository) {
        return new SailingNationInternalApi(sailingNationRepository);
    }

    @Bean
    SailingNationService sailingNationService(SailingNationRepository sailingNationRepository) {
        return new SailingNationService(sailingNationRepository);
    }
}

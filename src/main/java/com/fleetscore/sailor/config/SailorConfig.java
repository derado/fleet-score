package com.fleetscore.sailor.config;

import com.fleetscore.sailor.internal.SailorInternalApi;
import com.fleetscore.sailor.repository.SailorRepository;
import com.fleetscore.sailor.service.SailorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SailorConfig {

    @Bean
    public SailorService sailorService(SailorRepository sailorRepository) {
        return new SailorService(sailorRepository);
    }

    @Bean
    public SailorInternalApi sailorInternalApi(SailorRepository sailorRepository) {
        return new SailorInternalApi(sailorRepository);
    }
}

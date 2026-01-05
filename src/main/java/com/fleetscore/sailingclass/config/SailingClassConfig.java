package com.fleetscore.sailingclass.config;

import com.fleetscore.sailingclass.internal.SailingClassInternalApi;
import com.fleetscore.sailingclass.repository.SailingClassRepository;
import com.fleetscore.sailingclass.service.SailingClassService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SailingClassConfig {

    @Bean
    SailingClassInternalApi sailingClassInternalApi(SailingClassRepository sailingClassRepository) {
        return new SailingClassInternalApi(sailingClassRepository);
    }

    @Bean
    SailingClassService sailingClassService(SailingClassRepository sailingClassRepository) {
        return new SailingClassService(sailingClassRepository);
    }
}

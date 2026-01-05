package com.fleetscore.common.config;

import com.fleetscore.common.util.TokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonConfig {

    @Bean
    TokenGenerator tokenGenerator() {
        return new TokenGenerator();
    }
}

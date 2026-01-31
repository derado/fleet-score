package com.fleetscore.common.config;

import com.fleetscore.common.logging.ControllerLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {

    private final ControllerLoggingInterceptor controllerLoggingInterceptor;

    public ApiVersioningConfig(ControllerLoggingInterceptor controllerLoggingInterceptor) {
        this.controllerLoggingInterceptor = controllerLoggingInterceptor;
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.useRequestHeader("Api-Version");
        configurer.setVersionRequired(false);
        configurer.addSupportedVersions("1");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(controllerLoggingInterceptor);
    }
}

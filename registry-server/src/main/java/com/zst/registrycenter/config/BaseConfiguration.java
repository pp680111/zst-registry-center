package com.zst.registrycenter.config;

import com.zst.registrycenter.health.DefaultHealthChecker;
import com.zst.registrycenter.health.HealthChecker;
import com.zst.registrycenter.service.DefaultRegistryService;
import com.zst.registrycenter.service.RegistryService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@EnableConfigurationProperties(RegistryProperties.class)
public class BaseConfiguration {
    @Bean
    public RegistryService registryService() {
        return new DefaultRegistryService();
    }

    @Bean
    public HealthChecker healthChecker() {
        return new DefaultHealthChecker();
    }
}

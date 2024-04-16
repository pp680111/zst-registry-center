package com.zst.registrycenter.config;

import com.zst.registrycenter.service.DefaultRegistryService;
import com.zst.registrycenter.service.RegistryService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class BaseConfiguration {
    @Bean
    public RegistryService registryService() {
        return new DefaultRegistryService();
    }
}

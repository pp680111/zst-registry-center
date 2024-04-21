package com.zst.registrycenter.config;

import com.zst.registrycenter.cluster.Cluster;
import com.zst.registrycenter.config.properties.RegistryProperties;
import com.zst.registrycenter.health.cluster.ClusterHealthChecker;
import com.zst.registrycenter.health.cluster.DefaultClusterHealthChecker;
import com.zst.registrycenter.health.registry.DefaultRegistryHealthChecker;
import com.zst.registrycenter.health.registry.RegistryHealthChecker;
import com.zst.registrycenter.service.DefaultRegistryService;
import com.zst.registrycenter.service.RegistryService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RegistryProperties.class)
public class BaseConfiguration {
    @Bean
    public RegistryService registryService() {
        return new DefaultRegistryService();
    }

    @Bean
    public RegistryHealthChecker healthChecker() {
        return new DefaultRegistryHealthChecker();
    }

    @Bean
    public Cluster cluster(RegistryProperties properties) {
        return new Cluster(properties);
    }

    @Bean
    public ClusterHealthChecker registryHealthChecker() {
        return new DefaultClusterHealthChecker();
    }
}

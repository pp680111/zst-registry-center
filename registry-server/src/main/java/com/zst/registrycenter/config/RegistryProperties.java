package com.zst.registrycenter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "registry")
public class RegistryProperties {
    private List<String> servers = new ArrayList<>();
}

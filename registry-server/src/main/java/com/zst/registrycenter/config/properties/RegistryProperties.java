package com.zst.registrycenter.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "registry.cluster")
public class RegistryProperties {
    /**
     * 集群节点列表
     */
    private List<String> servers = new ArrayList<>();
    /**
     * 当前实例的ip地址
     */
    private String instanceIp;
}

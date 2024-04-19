package com.zst.registrycenter.cluster;

import com.zst.registrycenter.config.RegistryProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注册服务器集群化机制相关代码
 */
@Slf4j
public class Cluster {
    RegistryProperties properties;

    List<Server> serverList;

    public Cluster(RegistryProperties properties) {
        if (null == properties) {
            throw new IllegalArgumentException("RegistryProperties is null");
        }

        this.properties = properties;
    }

    public void init() {
        parseServerListFromProperties();
    }

    public void parseServerListFromProperties() {
        List<String> propertiesServerList = properties.getServerList();
        serverList = propertiesServerList.stream().map(serverUrl -> {
            Server server = new Server();
            server.setAddress(serverUrl);
            return server;
        }).collect(Collectors.toList());
    }
}

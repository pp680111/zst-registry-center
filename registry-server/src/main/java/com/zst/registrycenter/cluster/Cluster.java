package com.zst.registrycenter.cluster;

import com.zst.registrycenter.config.RegistryProperties;
import com.zst.registrycenter.service.RegistryService;
import com.zst.registrycenter.utils.InetUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 注册服务器集群化机制相关代码
 */
@Slf4j
public class Cluster {
    @Value("${server.port:8444}")
    private int port;
    private RegistryProperties properties;
    @Autowired
    private RegistryService registryService;
    private List<Server> serverList;
    /**
     * 当前节点
     */
    private Server currentServer;

    public Cluster(RegistryProperties properties) {
        if (null == properties) {
            throw new IllegalArgumentException("RegistryProperties is null");
        }

        this.properties = properties;
    }

    public List<Server> getServerList() {
        return serverList;
    }

    public Server getCurrentServer() {
        // TODO 这里可以改成事件通知的形式，由RegistryService主动通知其他监听着版本号变更的组件去更新版本号
        currentServer.setVersion(Math.toIntExact(Optional.ofNullable(registryService.getVersion()).orElse(-1L)));
        return currentServer;
    }

    public void refreshServers(List<Server> serverList) {

    }

    private void init() {
        parseServerListFromProperties();
    }

    private void parseServerListFromProperties() {
        List<String> propertiesServerList = properties.getServerList();
        serverList = propertiesServerList.stream().map(serverUrl -> {
            Server server = new Server();
            server.setAddress(serverUrl);
            return server;
        }).collect(Collectors.toList());
    }

    private void prepareCurrentServer() {
        Server server = new Server();
        server.setIp(InetUtils.I.findFirstNonLoopbackHostInfo().getIpAddress());
        server.setPort(port);
        server.setStatus(true);
        server.setLeader(false);
        this.currentServer = server;
    }
}

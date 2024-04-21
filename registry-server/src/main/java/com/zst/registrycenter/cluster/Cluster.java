package com.zst.registrycenter.cluster;

import com.zst.registrycenter.config.properties.RegistryProperties;
import com.zst.registrycenter.service.RegistryService;
import com.zst.registrycenter.utils.InetUtils;
import com.zst.registrycenter.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

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

    @PostConstruct
    public void postConstruct() {
        init();
    }

    public List<Server> getServerList() {
        return serverList;
    }

    public Server getCurrentServer() {
        // TODO 这里可以改成事件通知的形式，由RegistryService主动通知其他监听着版本号变更的组件去更新版本号
        currentServer.setVersion(Math.toIntExact(Optional.ofNullable(registryService.getVersion()).orElse(-1L)));
        return currentServer;
    }

    private void init() {
        parseServerListFromProperties();
        prepareCurrentServer();
    }

    private void parseServerListFromProperties() {
        List<String> propertiesServerList = properties.getServers();
        serverList = propertiesServerList.stream().map(serverUrl -> {
            Server server = new Server();
            server.setAddress(serverUrl);
            return server;
        }).collect(Collectors.toList());
    }

    private void prepareCurrentServer() {
        String instanceIp = StringUtils.isNotEmpty(properties.getInstanceIp())
                ? properties.getInstanceIp()
                : InetUtils.I.findFirstNonLoopbackHostInfo().getIpAddress();

        // 如果配置的server列表中已经有当前节点的话，那就直接复用
        for (Server server : serverList) {
            if (server.getIp().equals(instanceIp) && server.getPort() == port) {
                this.currentServer = server;
                return;
            }
        }

        // 没有的话就新建一个，然后加入到serverList中
        Server server = new Server();
        server.setIp(instanceIp);
        server.setPort(port);
        server.setStatus(true);
        server.setLeader(false);
        this.currentServer = server;
        serverList.add(server);
    }
}

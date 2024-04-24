package com.zst.registrycenter.cluster;

import com.alibaba.fastjson2.JSON;
import com.zst.registrycenter.model.ServerInstanceSnapshot;
import com.zst.registrycenter.service.RegistryService;
import com.zst.registrycenter.transport.http.HttpInvoker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 集群数据同步工具
 */
@Slf4j
public class ClusterSynchronizer {
    private static final long SYNCHRONIZE_INTERVAL_MS = 5000;

    private ScheduledExecutorService scheduler;
    private HttpInvoker httpInvoker;

    @Autowired
    private RegistryService registryService;
    @Autowired
    private Cluster cluster;

    @PostConstruct
    public void postConstruct() {
        httpInvoker = new HttpInvoker();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::doSynchronize, 0, SYNCHRONIZE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void preDestroy() {
        try {
            scheduler.shutdown();
            scheduler.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Failed to shutdown cluster synchronizer scheduler", e);
        }
    }

    private void doSynchronize() {
        log.debug("start synchronize leader registry data");
        List<Server> serverList = cluster.getServerList();
        Optional<Server> leader = serverList.stream()
                .filter(server -> server.isStatus() && server.isLeader())
                .findAny();

        if (leader.isEmpty()) {
            log.debug("no avaliable leader found, skip running synchronize");
            return;
        }

        Server leaderServer = leader.get();

        if (leaderServer.equals(cluster.getCurrentServer())) {
            log.debug("im cluster leader, skip running synchronize");
            return;
        }

        ServerInstanceSnapshot snapshot = getLeaderSnapshot(leaderServer);
        if (snapshot == null) {
            log.error("get leader snapshot failed");
        }

        registryService.restoreFromSnapshot(snapshot);
    }

    private ServerInstanceSnapshot getLeaderSnapshot(Server leaderServer) {
        String url = String.format("http://%s/snapshot", leaderServer.getAddress());
        ServerInstanceSnapshot snapshot = null;
        try {
            CompletableFuture<HttpResponse> responseFuture = httpInvoker.doGet(url, null, null);
            HttpResponse response = responseFuture.get(5_000, TimeUnit.MILLISECONDS);

            String rawResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            snapshot = JSON.parseObject(rawResponse, ServerInstanceSnapshot.class);
        } catch (Exception e) {
            throw new RuntimeException("get leader server snapshot failed", e);
        }

        return snapshot;
    }
}

package com.zst.registrycenter.health.cluster;

import com.alibaba.fastjson2.JSON;
import com.zst.registrycenter.cluster.Cluster;
import com.zst.registrycenter.cluster.Server;
import com.zst.registrycenter.transport.http.HttpInvoker;
import com.zst.registrycenter.utils.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 默认集群健康检查
 */
@Slf4j
public class DefaultClusterHealthChecker implements ClusterHealthChecker {
    private static final long HEALTH_CHECK_INTERVAL_MS = Duration.ofSeconds(10).toMillis();
    private static final long HTTP_HEALTH_CHECK_TIMEOUT_MS = Duration.ofSeconds(5).toMillis();
    private static final int DEFAULT_HEALTH_CHECK_THREAD_NUM = 10;
    private HttpInvoker httpInvoker;
    private ScheduledExecutorService healthCheckExecutor;
    private ExecutorService healthCheckExecutorPool;

    @Autowired
    private Cluster cluster;

    @PostConstruct
    public void postConstruct() {
        start();
    }

    @PreDestroy
    public void preDestroy() {
        end();
    }

    @Override
    public void start() {
        httpInvoker = new HttpInvoker();
        healthCheckExecutorPool = new ThreadPoolExecutor(DEFAULT_HEALTH_CHECK_THREAD_NUM,
                DEFAULT_HEALTH_CHECK_THREAD_NUM,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.AbortPolicy());

        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        healthCheckExecutor.scheduleAtFixedRate(this::runCheckHealth, 2 * HEALTH_CHECK_INTERVAL_MS,
                HEALTH_CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void end() {
        try {
            healthCheckExecutorPool.shutdown();
            healthCheckExecutorPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("shutdown health check executor pool error", e);
        }

        try {
            healthCheckExecutor.shutdown();
            healthCheckExecutor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("shutdown health check executor error", e);
        }
    }

    private void runCheckHealth() {
        try {
            List<Server> targetServerList = cluster.getServerList();
            targetServerList.forEach(this::runCheckHealth);

            electLeader();
        } catch (Exception e) {
            log.error("run check health error", e);
        }
    }

    private void runCheckHealth(Server server) {
        if (cluster.getCurrentServer().equals(server)) {
            return;
        }

        String address = server.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw new RuntimeException("Server address invalid");
        }

        log.debug("start running server {} health check", address);
        try {
            CompletableFuture<Server> remoteServerFuture = getRemoteServerInfo(address);
            remoteServerFuture.thenAccept(responseServerInfo -> {
                if (responseServerInfo != null) {
                    server.setStatus(true);
                    server.setVersion(responseServerInfo.getVersion());
                    server.setLeader(responseServerInfo.isLeader());

                    log.debug(MessageFormat.format("finish refresh server {0} info, {1}", address, server.toString()));
                }
            });
            remoteServerFuture.get(HTTP_HEALTH_CHECK_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            if (e instanceof ExecutionException && e.getCause() != null) {
                e = (Exception) e.getCause();
            }
            log.error(MessageFormat.format("run server {0} health check error, set server status to disable", address), e);
            server.setStatus(false);
        }
    }

    private CompletableFuture<Server> getRemoteServerInfo(String address) {
        String url = MessageFormat.format("http://{0}/info", address);
        CompletableFuture<HttpResponse> responseFuture = httpInvoker.doGet(url, null, null);

        return responseFuture.thenApplyAsync(response -> {
            try {
                String rawResponseContext = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return JSON.parseObject(rawResponseContext, Server.class);
            } catch (Exception e) {
                throw new RuntimeException(MessageFormat.format("解析远端服务｛0｝返回结果时发生错误", address), e);
            }
        }, healthCheckExecutorPool);
    }

    private void electLeader() {
        List<Server> existLeaders = cluster.getServerList()
                .stream().filter(server -> server.isStatus() && server.isLeader()).toList();
        if (existLeaders.isEmpty()) {
            log.info("no available leader found, execute leader election");
            doLeaderElection(cluster.getCurrentServer(), cluster.getServerList());
        } else if (existLeaders.size() > 1) {
            log.info("multi available leader found, reset leader status, execute leader election");
            doLeaderElection(cluster.getCurrentServer(), cluster.getServerList());
        }
    }

    private void doLeaderElection(Server self, List<Server> allServerList) {
        Server newLeader = null;
        List<Server> candidateServers = allServerList.stream().filter(Server::isStatus).toList();
        for (Server candidate : candidateServers) {
            candidate.setLeader(false);

            if (newLeader == null) {
                newLeader = candidate;
            } else {
                if (candidate.getVersion() > newLeader.getVersion()) {
                    newLeader = candidate;
                }
            }
        }

        if (newLeader == null) {
            newLeader = self;
        }

        newLeader.setLeader(true);
        log.info("leader election complete, new leader is {}", newLeader.toString());
    }
}

package com.zst.registrycenter.health.cluster;

import com.zst.registrycenter.cluster.Cluster;
import com.zst.registrycenter.cluster.Server;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DefaultHealthChecker implements HealthChecker {
    private Cluster cluster;
    private ScheduledExecutorService healthCheckExecutor;
    private static final long HEALTH_CHECK_INTERVAL = Duration.ofSeconds(10).toMillis();

    @Override
    public void start() {
        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        healthCheckExecutor.scheduleAtFixedRate(this::runCheckHealth, 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void end() {

    }

    private void runCheckHealth() {
        List<Server> targetServerList = cluster.getServerList();
    }
}

package com.zst.registrycenter.health;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultHealthChecker implements HealthChecker {

    private ScheduledExecutorService healthCheckExecutor;
    private static final long HEALTH_CHECK_INTERVAL = Duration.ofSeconds(10).toMillis();

    @Override
    public void start() {
        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        healthCheckExecutor.scheduleAtFixedRate(this::runCheckHealth, 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        healthCheckExecutor.shutdown();
    }

    private void runCheckHealth() {
        // TODO 检查DefaultRegistryService中记录的timestampMap，标记无效实例
        // 超出一段短的时间把实例标记为false，再超出一段长的时间就当作故障
    }
}

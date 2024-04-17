package com.zst.registrycenter.health;

import com.zst.registrycenter.service.RegistryService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DefaultHealthChecker implements HealthChecker {
    @Autowired
    private RegistryService registryService;
    private ScheduledExecutorService healthCheckExecutor;
    /**
     * 记录每个服务实例的探活时间
     */
    private final Map<String, Long> timestampMap = new ConcurrentHashMap<>();
    private static final long HEALTH_CHECK_INTERVAL = Duration.ofSeconds(10).toMillis();
    private static final long MAX_INSTANCE_HEARTBEAT_INTERVAL = Duration.ofSeconds(30).toMillis();

    @PostConstruct
    public void init() {
        start();
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    @Override
    public void start() {
        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        healthCheckExecutor.scheduleAtFixedRate(this::runCheckHealth, 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        healthCheckExecutor.shutdown();
    }

    @Override
    public void renew(String serviceId, String instanceId) {
        timestampMap.put(MessageFormat.format("{0}@{1}", serviceId, instanceId), System.currentTimeMillis());
    }

    private void runCheckHealth() {
        timestampMap.forEach((id, timestamp) -> {
            if (System.currentTimeMillis() - timestamp > MAX_INSTANCE_HEARTBEAT_INTERVAL) {
                String[] instanceIdArr = id.split("@");

                log.info("instance {} is unhealthy, unregister", instanceIdArr[1]);

                registryService.unregister(instanceIdArr[0], instanceIdArr[1]);
                this.timestampMap.remove(id);
            }
        });
    }
}

package com.zst.registrycenter.health;

import com.zst.registrycenter.model.InstanceMetadata;

public interface HealthChecker {
    void start();

    void stop();

    void renew(String serviceId, String instanceId);
}

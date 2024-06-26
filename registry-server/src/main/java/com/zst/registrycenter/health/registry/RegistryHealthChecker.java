package com.zst.registrycenter.health.registry;

/**
 * 用于处理注册到当前实例的所有服务实例的状态
 *
 */
public interface RegistryHealthChecker {
    void start();

    void stop();

    void renew(String serviceId, String instanceId);
}

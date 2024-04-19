package com.zst.registrycenter.health.cluster;

/**
 * 集群实例的健康检查逻辑
 */
public interface HealthChecker {
    void start();

    void end();
}

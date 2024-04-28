package com.zst.registrycenter.service;

import com.zst.registrycenter.model.InstanceMetadata;
import com.zst.registrycenter.model.ServerInstanceSnapshot;

import java.util.List;
import java.util.Map;

/**
 * Interface for registry service
 *
 * @Author zst
 */
public interface RegistryService {
    void register(String serviceId, InstanceMetadata instanceMeta);

    void unregister(String serviceId, InstanceMetadata instanceMeta);
    void unregister(String serviceId, String instanceIdentifier);

    List<InstanceMetadata> getAllInstances(String serviceId);

    void renew(String serviceId, InstanceMetadata instanceMeta);

    /**
     * 获取当前实例的版本号
     *
     */
    Long getVersion();

    /**
     * 获取当前各服务的最新版本号
     * @param serviceIds
     * @return
     */
    Map<String, Long> getVersions(List<String> serviceIds);

    /**
     * 获取指定服务的最新版本号
     * @param serviceId
     * @return
     */
    Long getVersion(String serviceId);

    /**
     * 生成当前注册数据的快照
     * @return
     */
    ServerInstanceSnapshot generateSnapshot();

    /**
     * 从快照中恢复注册数据
     * @param snapshot
     */
    void restoreFromSnapshot(ServerInstanceSnapshot snapshot);
}

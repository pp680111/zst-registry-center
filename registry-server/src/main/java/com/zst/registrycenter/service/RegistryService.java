package com.zst.registrycenter.service;

import com.zst.registrycenter.model.InstanceMetadata;

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

    void renew(List<String> serviceId, InstanceMetadata instanceMeta);

    Map<String, Long> getVersions(List<String> serviceIds);

    Long getVersion(String serviceId);
}

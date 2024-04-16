package com.zst.registrycenter.service;

import com.zst.registrycenter.model.InstanceMetadata;

import java.util.List;

/**
 * Interface for registry service
 *
 * @Author zst
 */
public interface RegistryService {
    void register(String serviceId, InstanceMetadata instanceMeta);

    void unregister(String serviceId, InstanceMetadata instanceMeta);

    List<InstanceMetadata> getAllInstances(String serviceId);

    void renew(List<String> serviceId, InstanceMetadata instanceMeta);
}

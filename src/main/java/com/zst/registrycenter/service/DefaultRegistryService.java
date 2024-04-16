package com.zst.registrycenter.service;

import com.zst.registrycenter.model.InstanceMetadata;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultRegistryService implements RegistryService {
    private final Map<String, List<InstanceMetadata>> instanceMap = new ConcurrentHashMap<>();


    @Override
    public void register(String serviceId, InstanceMetadata instanceMeta) {
        // todo: 还需要补充对service的创建和检测

        List<InstanceMetadata> instances = instanceMap.computeIfAbsent(serviceId, k -> new ArrayList<>());
        if (!isInstanceExists(instances, instanceMeta)) {
            instances.add(instanceMeta);
            instanceMeta.setStatus(true);

            log.info("register instance, {}", instanceMeta);
        } else {
            log.info("instance already exists, {}", instanceMeta);
        }
    }

    @Override
    public void unregister(String serviceId, InstanceMetadata instanceMeta) {
        if (!instanceMap.containsKey(serviceId)) {
            return;
        }

        List<InstanceMetadata> instances = instanceMap.get(serviceId);
        if (isInstanceExists(instances, instanceMeta)) {
            instances.remove(instanceMeta);
            instanceMeta.setStatus(false);
        }
    }

    @Override
    public List<InstanceMetadata> getAllInstances(String serviceId) {
        return instanceMap.getOrDefault(serviceId, Collections.emptyList());
    }

    private boolean isInstanceExists(List<InstanceMetadata> currentInstances, InstanceMetadata newInstance) {
        return currentInstances.stream().anyMatch(i -> currentInstances.equals(newInstance));
    }
}

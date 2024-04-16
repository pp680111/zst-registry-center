package com.zst.registrycenter.service;

import com.zst.registrycenter.model.InstanceMetadata;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class DefaultRegistryService implements RegistryService {
    private final Map<String, List<InstanceMetadata>> instanceMap = new ConcurrentHashMap<>();

    /**
     * 以服务为单位记录版本号
     */
    private final Map<String, Long> versionMap = new ConcurrentHashMap<>();
    /**
     * 记录每个服务实例的探活时间
     */
    private final Map<String, Long> timestampMap = new ConcurrentHashMap<>();
    private final AtomicLong versionCounter = new AtomicLong(1);


    @Override
    public void register(String serviceId, InstanceMetadata instanceMeta) {
        // todo: 还需要补充对service的创建和检测

        List<InstanceMetadata> instances = instanceMap.computeIfAbsent(serviceId, k -> new ArrayList<>());
        if (!isInstanceExists(instances, instanceMeta)) {
            instances.add(instanceMeta);
            instanceMeta.setStatus(true);

            versionMap.put(serviceId, versionCounter.getAndIncrement());
            renew(Collections.singletonList(serviceId), instanceMeta);

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

            versionMap.put(serviceId, versionCounter.getAndIncrement());
        }
    }

    @Override
    public List<InstanceMetadata> getAllInstances(String serviceId) {
        return instanceMap.getOrDefault(serviceId, Collections.emptyList());
    }

    @Override
    public void renew(List<String> serviceIds, InstanceMetadata instanceMeta) {
        serviceIds.forEach(serviceId -> {
            if (instanceMap.containsKey(serviceId) && isInstanceExists(getAllInstances(serviceId), instanceMeta)) {
                log.debug(MessageFormat.format("renew instance, serviceId = {0}, instanceId={1}", serviceId, instanceMeta.getIdentifier()));
                timestampMap.put(MessageFormat.format("{0}@{1}", serviceId, instanceMeta.getIdentifier()), System.currentTimeMillis());
            }
        });
    }

    private boolean isInstanceExists(List<InstanceMetadata> currentInstances, InstanceMetadata newInstance) {
        return currentInstances.stream().anyMatch(i -> i.equals(newInstance));
    }
}

package com.zst.registrycenter.service;

import com.zst.registrycenter.health.registry.RegistryHealthChecker;
import com.zst.registrycenter.model.InstanceMetadata;
import com.zst.registrycenter.model.ServerInstanceSnapshot;
import com.zst.registrycenter.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class DefaultRegistryService implements RegistryService {
    private final static Duration SNAPSHOT_MAX_WAIT_TIME_MS = Duration.ofMillis(10_000L);
    private final ReentrantLock snapshotLock = new ReentrantLock();
    private final Map<String, List<InstanceMetadata>> instanceMap = new ConcurrentHashMap<>();
    /**
     * 以服务为单位记录版本号
     */
    private final Map<String, Long> versionMap = new ConcurrentHashMap<>();
    private final AtomicLong versionCounter = new AtomicLong(1);

    @Autowired
    private RegistryHealthChecker registryHealthChecker;


    @Override
    public void register(String serviceId, InstanceMetadata instanceMeta) {
        // todo: 还需要补充对service的创建和检测
        waitForSnapshotFinished(SNAPSHOT_MAX_WAIT_TIME_MS);

        List<InstanceMetadata> instances = instanceMap.computeIfAbsent(serviceId, k -> new ArrayList<>());
        if (!isInstanceExists(instances, instanceMeta)) {
            instances.add(instanceMeta);
            instanceMeta.setStatus(true);

            versionMap.put(serviceId, versionCounter.incrementAndGet());
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

        waitForSnapshotFinished(SNAPSHOT_MAX_WAIT_TIME_MS);

        List<InstanceMetadata> instances = instanceMap.get(serviceId);
        if (isInstanceExists(instances, instanceMeta)) {
            instances.remove(instanceMeta);
            instanceMeta.setStatus(false);

            versionMap.put(serviceId, versionCounter.incrementAndGet());
        }
    }

    @Override
    public void unregister(String serviceId, String instanceIdentifier) {
        if (!instanceMap.containsKey(serviceId)) {
            return;
        }

        waitForSnapshotFinished(SNAPSHOT_MAX_WAIT_TIME_MS);

        List<InstanceMetadata> instances = instanceMap.get(serviceId);
        instances.removeIf(instance -> StringUtils.equals(instance.getIdentifier(), instanceIdentifier));

        versionMap.put(serviceId, versionCounter.incrementAndGet());
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
                registryHealthChecker.renew(serviceId, instanceMeta.getIdentifier());
            }
        });
    }

    @Override
    public Long getVersion() {
        return versionCounter.get();
    }

    @Override
    public Map<String, Long> getVersions(List<String> serviceIds) {
        Map<String, Long> result = new HashMap<>();
        serviceIds.forEach(serviceId -> {
            if (versionMap.containsKey(serviceId)) {
                result.put(serviceId, versionMap.get(serviceId));
            }
        });

        return result;
    }

    @Override
    public Long getVersion(String serviceId) {
        return versionMap.get(serviceId);
    }

    @Override
    public ServerInstanceSnapshot generateSnapshot() {
        if (!snapshotLock.tryLock()) {
            throw new IllegalStateException("有快照正在生成中，无法重复执行");
        }

        try {
            ServerInstanceSnapshot snapshot = new ServerInstanceSnapshot();

            Map<String, List<InstanceMetadata>> ssInstanceMap = new HashMap<>();
            instanceMap.forEach((serviceId, instances) -> {
                ssInstanceMap.put(serviceId,
                        instances.stream().map(InstanceMetadata::new).collect(Collectors.toList()));
            });

            snapshot.setInstanceMap(ssInstanceMap);
            snapshot.setVersion(versionCounter.get());
            snapshot.setVersionMap(new HashMap<>(versionMap));
            return snapshot;
        } catch (Exception e) {
            throw new RuntimeException("生成快照时发生错误", e);
        } finally {
            snapshotLock.unlock();
        }
    }

    @Override
    public void restoreFromSnapshot(ServerInstanceSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException();
        }

        try {
            if (!snapshotLock.tryLock(SNAPSHOT_MAX_WAIT_TIME_MS.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("获取快照锁失败");
            }
        } catch (InterruptedException e) {
            if (!snapshotLock.isHeldByCurrentThread()) {
                throw new IllegalStateException("获取快照锁失败");
            }
        }

        try {
            this.instanceMap.clear();
            if (snapshot.getInstanceMap() != null) {
                this.instanceMap.putAll(snapshot.getInstanceMap());
            }

            this.versionMap.clear();
            if (snapshot.getVersionMap() != null) {
                this.versionMap.putAll(snapshot.getVersionMap());
            }

            this.versionCounter.set(snapshot.getVersion());
        } catch (Exception e) {
            throw new RuntimeException("恢复快照时发生错误", e);
        } finally {
            snapshotLock.unlock();
        }
    }

    private boolean isInstanceExists(List<InstanceMetadata> currentInstances, InstanceMetadata newInstance) {
        return currentInstances.stream().anyMatch(i -> i.equals(newInstance));
    }

    private void waitForSnapshotFinished(Duration maxWaitDuration) {
        if (this.snapshotLock.isLocked()) {
            try {
                this.snapshotLock.wait(maxWaitDuration.toMillis());
            } catch (Exception e) {
            }
        }
    }
}

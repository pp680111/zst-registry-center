package com.zst.registrycenter.service;

import com.zst.registrycenter.utils.PrivateAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootTest
public class DefaultRegistryServiceTest {
    @Autowired
    private DefaultRegistryService registryService;

    @Test
    public void waitForSnapshotFinished_testIfValid() {
        ReentrantLock lock = PrivateAccessor.get(registryService, "snapshotLock");
        lock.lock();

        LocalDateTime start = LocalDateTime.now();
        PrivateAccessor.invoke(registryService, "waitForSnapshotFinished", Duration.ofSeconds(5));
        System.err.println(Duration.between(start, LocalDateTime.now()).toMillis());
    }
}

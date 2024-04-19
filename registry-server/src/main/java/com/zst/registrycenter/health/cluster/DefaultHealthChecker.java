package com.zst.registrycenter.health.cluster;

import com.alibaba.fastjson2.JSON;
import com.zst.registrycenter.cluster.Cluster;
import com.zst.registrycenter.cluster.Server;
import com.zst.registrycenter.transport.http.HttpInvoker;
import com.zst.registrycenter.utils.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DefaultHealthChecker implements HealthChecker {
    private static final long HEALTH_CHECK_INTERVAL = Duration.ofSeconds(10).toMillis();
    private static final long HTTP_HEALTH_CHECK_TIMEOUT = Duration.ofSeconds(5).toMillis();
    private HttpInvoker httpInvoker;
    private ScheduledExecutorService healthCheckExecutor;

    @Autowired
    private Cluster cluster;

    @Override
    public void start() {
        httpInvoker = new HttpInvoker();
        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        healthCheckExecutor.scheduleAtFixedRate(this::runCheckHealth, 0, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void end() {
        healthCheckExecutor.shutdown();
    }

    private void runCheckHealth() {
        List<Server> targetServerList = cluster.getServerList();
        targetServerList.forEach(this::runCheckHealth);
    }

    private void runCheckHealth(Server server) {
        String address = server.getAddress();
        if (StringUtils.isEmpty(address)) {
            throw new RuntimeException("Server address invalid");
        }


        Server responseServerInfo = getRemoteServerInfo(address);
        if (responseServerInfo != null) {
            server.setStatus(true);
            server.setVersion(responseServerInfo.getVersion());
            server.setLeader(responseServerInfo.isLeader());
        }
    }

    private Server getRemoteServerInfo(String address) {
        try {
            String url = MessageFormat.format("http://{0}/info", address);
            CompletableFuture<HttpResponse> responseFuture = httpInvoker.doGet(url, null, null);
            HttpResponse response = responseFuture.get(HTTP_HEALTH_CHECK_TIMEOUT, TimeUnit.MILLISECONDS);

            String rawResponseContext = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return JSON.parseObject(rawResponseContext, Server.class);
        } catch (Exception e) {
            throw new RuntimeException(MessageFormat.format("调用远端服务｛0｝时发生错误", address), e);
        }
    }
}

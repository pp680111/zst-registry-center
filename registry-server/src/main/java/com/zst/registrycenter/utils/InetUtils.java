package com.zst.registrycenter.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.Closeable;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 复制自spring cloud common库的InetUtils类，为了少一个依赖库
 */
public class InetUtils implements Closeable {
    public static final InetUtils I = new InetUtils();

    // TODO: maybe shutdown the thread pool if it isn't being used?
    private final ExecutorService executorService;

    private final InetUtilsProperties properties;

    private final Log log = LogFactory.getLog(InetUtils.class);

    public InetUtils() {
        this.properties = new InetUtilsProperties();
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName(InetUtilsProperties.PREFIX);
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void close() {
        this.executorService.shutdown();
    }

    public HostInfo findFirstNonLoopbackHostInfo() {
        InetAddress address = findFirstNonLoopbackAddress();
        if (address != null) {
            return convertAddress(address);
        }
        HostInfo hostInfo = new HostInfo();
        hostInfo.setHostname(this.properties.getDefaultHostname());
        hostInfo.setIpAddress(this.properties.getDefaultIpAddress());
        return hostInfo;
    }

    public InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
                    .hasMoreElements();) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    this.log.trace("Testing interface: " + ifc.getDisplayName());
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    }
                    else if (result != null) {
                        continue;
                    }

                    // @formatter:off
                    if (!ignoreInterface(ifc.getDisplayName())) {
                        for (Enumeration<InetAddress> addrs = ifc
                                .getInetAddresses(); addrs.hasMoreElements();) {
                            InetAddress address = addrs.nextElement();
                            if (address instanceof Inet4Address
                                    && !address.isLoopbackAddress()
                                    && isPreferredAddress(address)) {
                                this.log.trace("Found non-loopback interface: "
                                        + ifc.getDisplayName());
                                result = address;
                            }
                        }
                    }
                    // @formatter:on
                }
            }
        }
        catch (IOException ex) {
            this.log.error("Cannot get first non-loopback address", ex);
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            this.log.warn("Unable to retrieve localhost");
        }

        return null;
    }

    // For testing.
    boolean isPreferredAddress(InetAddress address) {

        if (this.properties.isUseOnlySiteLocalInterfaces()) {
            final boolean siteLocalAddress = address.isSiteLocalAddress();
            if (!siteLocalAddress) {
                this.log.trace("Ignoring address: " + address.getHostAddress());
            }
            return siteLocalAddress;
        }
        final List<String> preferredNetworks = this.properties.getPreferredNetworks();
        if (preferredNetworks.isEmpty()) {
            return true;
        }
        for (String regex : preferredNetworks) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }
        this.log.trace("Ignoring address: " + address.getHostAddress());
        return false;
    }

    // For testing
    boolean ignoreInterface(String interfaceName) {
        for (String regex : this.properties.getIgnoredInterfaces()) {
            if (interfaceName.matches(regex)) {
                this.log.trace("Ignoring interface: " + interfaceName);
                return true;
            }
        }
        return false;
    }

    public HostInfo convertAddress(final InetAddress address) {
        HostInfo hostInfo = new HostInfo();
        Future<String> result = this.executorService.submit(address::getHostName);

        String hostname;
        try {
            hostname = result.get(this.properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        }
        catch (Exception e) {
            this.log.info("Cannot determine local hostname");
            hostname = "localhost";
        }
        hostInfo.setHostname(hostname);
        hostInfo.setIpAddress(address.getHostAddress());
        return hostInfo;
    }

    /**
     * Host information pojo.
     */
    public static class HostInfo {

        /**
         * Should override the host info.
         */
        public boolean override;

        private String ipAddress;

        private String hostname;

        public HostInfo(String hostname) {
            this.hostname = hostname;
        }

        public HostInfo() {
        }

        public int getIpAddressAsInt() {
            InetAddress inetAddress = null;
            String host = this.ipAddress;
            if (host == null) {
                host = this.hostname;
            }
            try {
                inetAddress = InetAddress.getByName(host);
            }
            catch (final UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
            return ByteBuffer.wrap(inetAddress.getAddress()).getInt();
        }

        public boolean isOverride() {
            return this.override;
        }

        public void setOverride(boolean override) {
            this.override = override;
        }

        public String getIpAddress() {
            return this.ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getHostname() {
            return this.hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

    }

    public static class InetUtilsProperties {

        /**
         * Prefix for the Inet Utils properties.
         */
        public static final String PREFIX = "spring.cloud.inetutils";

        /**
         * The default hostname. Used in case of errors.
         */
        private String defaultHostname = "localhost";

        /**
         * The default IP address. Used in case of errors.
         */
        private String defaultIpAddress = "127.0.0.1";

        /**
         * Timeout, in seconds, for calculating hostname.
         */
        @Value("${spring.util.timeout.sec:${SPRING_UTIL_TIMEOUT_SEC:1}}")
        private int timeoutSeconds = 1;

        /**
         * List of Java regular expressions for network interfaces that will be ignored.
         */
        private List<String> ignoredInterfaces = new ArrayList<>();

        /**
         * Whether to use only interfaces with site local addresses. See
         * {@link InetAddress#isSiteLocalAddress()} for more details.
         */
        private boolean useOnlySiteLocalInterfaces = false;

        /**
         * List of Java regular expressions for network addresses that will be preferred.
         */
        private List<String> preferredNetworks = new ArrayList<>();

        public static String getPREFIX() {
            return PREFIX;
        }

        public String getDefaultHostname() {
            return this.defaultHostname;
        }

        public void setDefaultHostname(String defaultHostname) {
            this.defaultHostname = defaultHostname;
        }

        public String getDefaultIpAddress() {
            return this.defaultIpAddress;
        }

        public void setDefaultIpAddress(String defaultIpAddress) {
            this.defaultIpAddress = defaultIpAddress;
        }

        public int getTimeoutSeconds() {
            return this.timeoutSeconds;
        }

        public void setTimeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }

        public List<String> getIgnoredInterfaces() {
            return this.ignoredInterfaces;
        }

        public void setIgnoredInterfaces(List<String> ignoredInterfaces) {
            this.ignoredInterfaces = ignoredInterfaces;
        }

        public boolean isUseOnlySiteLocalInterfaces() {
            return this.useOnlySiteLocalInterfaces;
        }

        public void setUseOnlySiteLocalInterfaces(boolean useOnlySiteLocalInterfaces) {
            this.useOnlySiteLocalInterfaces = useOnlySiteLocalInterfaces;
        }

        public List<String> getPreferredNetworks() {
            return this.preferredNetworks;
        }

        public void setPreferredNetworks(List<String> preferredNetworks) {
            this.preferredNetworks = preferredNetworks;
        }

    }
}

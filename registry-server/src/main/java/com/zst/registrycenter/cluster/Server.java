package com.zst.registrycenter.cluster;

import com.zst.registrycenter.utils.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"ip", "port", "address"})
public class Server {
    private String ip;
    private int port;
    private String address;
    private boolean status = false;
    private boolean isLeader = false;
    private int version = -1;

    public String getIp() {
        if (StringUtils.isNotEmpty(ip)) {
            return ip;
        }

        if (address != null) {
            return address.split(":")[0];
        }

        throw new IllegalArgumentException("server ip cannot be null");
    }

    public int getPort() {
        if (port > 0) {
            return port;
        }

        if (StringUtils.isNotEmpty(address)) {
            return Integer.parseInt(address.split(":")[1]);
        }

        throw new IllegalArgumentException("server port cannot be null");
    }

    public String getAddress() {
        if (StringUtils.isNotEmpty(address)) {
            return address;
        }

        if (StringUtils.isNotEmpty(ip) && port > 0) {
            return ip + ":" + port;
        }

        return null;
    }

    public boolean equals(Server o) {
        if (o == null) {
            return false;
        }

        String otherAddress = o.getAddress();
        String thisAddress = this.getAddress();
        return thisAddress != null && thisAddress.equals(otherAddress);
    }
}

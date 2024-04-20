package com.zst.registrycenter.cluster;

import com.zst.registrycenter.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Server {
    private String ip;
    private int port;
    private String address;
    private boolean status = false;
    private boolean isLeader = false;
    private int version = -1;

    public String getAddress() {
        if (StringUtils.isNotEmpty(address)) {
            return address;
        }

        if (StringUtils.isNotEmpty(ip) && port > 0) {
            return ip + ":" + port;
        }

        return null;
    }
}

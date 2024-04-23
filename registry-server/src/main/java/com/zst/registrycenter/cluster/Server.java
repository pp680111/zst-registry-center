package com.zst.registrycenter.cluster;

import com.zst.registrycenter.utils.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 表示注册中心集群的单个实例节点信息的类
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"address"}) // lombok使用address的getter方法（自己重写后的）来生成hashcode和equals
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
}

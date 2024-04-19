package com.zst.registrycenter.cluster;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Server {
    private String ip;
    private int port;
    private String address;
    private boolean status = false;
    private boolean isLeader = false;
    private int version = -1;
}

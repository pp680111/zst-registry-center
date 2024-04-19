package com.zst.registrycenter.transport.http;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpInvokerProperties {
    /**
     * 连接超时时间
     */
    private int connectTimeoutMs = 2000;
}

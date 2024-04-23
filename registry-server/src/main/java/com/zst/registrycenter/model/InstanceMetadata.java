package com.zst.registrycenter.model;

import com.zst.registrycenter.utils.StringUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * service instance metadata
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class InstanceMetadata {
    private String host;
    private int port;
    private String context = "/";
    private boolean status;

    /**
     * 用于复制实例数据的构造函数
     * @param source
     */
    public InstanceMetadata(InstanceMetadata source) {
        this.host = source.host;
        this.port = source.port;
        this.context = source.context;
        this.status = source.status;
        this.attributes.putAll(source.attributes);
    }

    private Map<String, String> attributes = new HashMap<>();

    /**
     * 获取服务实例的标识符
     */
    public String getIdentifier() {
        return String.format("%s_%d_%s", host, port, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InstanceMetadata that = (InstanceMetadata) o;
        return port == that.port && StringUtils.equals(host, that.host) && StringUtils.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, context, status, attributes);
    }
}

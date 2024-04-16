package com.zst.registrycenter.model;

import com.zst.registrycenter.utils.StringUtils;
import lombok.Getter;
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
public class InstanceMetadata {
    private String host;
    private int port;
    private String context = "/";
    private boolean status;
    private Map<String, String> attributes = new HashMap<>();

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

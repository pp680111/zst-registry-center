package com.zst.registrycenter.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册服务节点的完整快照
 */
@Getter
@Setter
public class ServerInstanceSnapshot {
    private Map<String, List<InstanceMetadata>> instanceMap = new HashMap<>();
    private Map<String, Long> versionMap;
    private long version;
    // 示例项目的代码里买呢还同步了timestamp，其实有这个必要吗，从节点只要负责从主节点同步节点实例列表就好了应该
}

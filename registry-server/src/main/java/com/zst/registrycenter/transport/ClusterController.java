package com.zst.registrycenter.transport;

import com.zst.registrycenter.cluster.Cluster;
import com.zst.registrycenter.cluster.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class ClusterController {
    @Autowired
    private Cluster cluster;

    /**
     * 提供给其他集群节点查询当前节点信息的接口
     *
     * @return
     */
    @GetMapping("/info")
    public Server info() {
        return cluster.getCurrentServer();
    }
}

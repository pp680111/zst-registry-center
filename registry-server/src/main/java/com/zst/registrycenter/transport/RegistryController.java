package com.zst.registrycenter.transport;

import com.zst.registrycenter.model.InstanceMetadata;
import com.zst.registrycenter.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/")
public class RegistryController {
    @Autowired
    private RegistryService registryService;

    @GetMapping("/getInstances")
    public List<InstanceMetadata> getInstances(@RequestParam("serviceId") String serviceId) {
        return registryService.getAllInstances(serviceId);
    }

    @GetMapping("/version")
    public Long getVersion(@RequestParam("serviceId") String serviceId) {
        return registryService.getVersion(serviceId);
    }

    @PutMapping("/register")
    public void register(@RequestParam("serviceId") String serviceId,
                         @RequestBody InstanceMetadata instanceMeta) {
        registryService.register(serviceId, instanceMeta);
    }

    @DeleteMapping("/unregister")
    public void unregister(@RequestParam("serviceId") String serviceId,
                           @RequestBody InstanceMetadata instanceMeta) {
        registryService.unregister(serviceId, instanceMeta);
    }

}

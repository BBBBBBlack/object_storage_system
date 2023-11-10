package org.example.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ServicesUtil {

    @Autowired
    private DiscoveryClient discoveryClient;

    public List<ServiceInstance> getInstance(String serviceName) {
        return discoveryClient.getInstances(serviceName);
    }

    public Integer getSlot(ServiceInstance instance) {
        return Integer.parseInt(instance.getMetadata().get("slot"));
    }

}

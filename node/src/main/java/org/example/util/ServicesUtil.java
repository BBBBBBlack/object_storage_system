package org.example.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Component
public class ServicesUtil {

    @Value("${server.port}")
    private String port;

    @Value("${spring.cloud.nacos.discovery.metadata.slot}")
    Integer slot;

    @Value("${base-url.windows}")
    private String baseUrl;

    @Autowired
    private DiscoveryClient discoveryClient;

    public List<ServiceInstance> getInstance(String serviceName) {
        return discoveryClient.getInstances(serviceName);
    }

    public Integer getSlot(ServiceInstance instance) {
        return Integer.parseInt(instance.getMetadata().get("slot"));
    }

    public String distributeURI(String str) {
        int hashCode = str.hashCode();
        hashCode = Math.abs(hashCode);
        hashCode %= 150;
//        if (hashCode >= slot && hashCode < slot + 50) {
//            return "localhost";
//        }
        List<ServiceInstance> instances = getInstance("node01-service");
        for (ServiceInstance instance : instances) {
            Integer slot1 = getSlot(instance);
            if (hashCode >= slot1 && hashCode < slot1 + 50) {
                return "http://" + instance.getHost() + ":" + instance.getPort();
            }
        }
        return null;
    }
}
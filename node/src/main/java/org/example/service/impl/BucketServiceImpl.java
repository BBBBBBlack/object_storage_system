package org.example.service.impl;

import org.example.context.UserContextHolder;
import org.example.feign.TestFeign;
import org.example.mapper.BucketMapper;
import org.example.pojo.Bucket;
import org.example.pojo.ResponseResult;
import org.example.service.BucketService;
import org.example.util.ServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Service
public class BucketServiceImpl implements BucketService {

    @Autowired
    private BucketMapper bucketMapper;

    @Autowired
    private TestFeign testFeign;

    @Autowired
    private ServicesUtil servicesUtil;


    @Value("${server.port}")
    private String port;

    @Value("${spring.cloud.nacos.discovery.metadata.slot}")
    Integer slot;

    @Override
    public ResponseResult<Bucket> createBucket(String bucketName, int acl, int lockEnable) {
        Bucket bucket = new Bucket(null, bucketName,
                UserContextHolder.getContext().getProperty("userEmail").toString(), acl, lockEnable);
        bucketMapper.createBucket(bucket);
        return new ResponseResult<>(200, "创建桶", bucket);
    }

    @Override
    public String test(String str) {
        int hashCode = str.hashCode();
        hashCode %= 150;
        if (hashCode >= slot && hashCode < slot + 50) {
            return port;
        }
        List<ServiceInstance> instances = servicesUtil.getInstance("node01-service");
        for (ServiceInstance instance : instances) {
            Integer slot1 = servicesUtil.getSlot(instance);
            if (hashCode >= slot1 && hashCode < slot1 + 50) {
                try {
                    String uri = "http://" + instance.getHost() + ":" + instance.getPort();
                    return testFeign.test(new URI(uri), str);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }
}

package org.example.service.impl;


import org.example.cache.BucketCache;
import org.example.feign.TestFeign;
import org.example.pojo.ResponseResult;
import org.example.service.DeleteService;
import org.example.util.AsyncTask;
import org.example.util.ServicesUtil;
import org.example.service.ClusterDeleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@Service
public class ClusterDeleteServiceImpl implements ClusterDeleteService {
    @Autowired
    private DeleteService deleteService;

    @Autowired
    private BucketCache bucketCache;

    @Autowired
    private AsyncTask asyncTask;

    @Autowired
    private ServicesUtil servicesUtil;

    @Autowired
    private TestFeign testFeign;

    @Override
    public ResponseResult delFile(String bucketId, String fileName, Boolean isForever) {
        String uri = servicesUtil.distributeURI(fileName);
        try {
            return testFeign.delFile(new URI(uri), bucketId, fileName, isForever, 1);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
//        return new ResponseResult<>(200, "找不到路径");
    }

    @Override
    public ResponseResult recoverFile(String bucketId, String fileName) {
        String uri = servicesUtil.distributeURI(fileName);
        try {
            return testFeign.recoverFile(new URI(uri), bucketId, fileName, 1);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
//        String socket = ClusterUtil.distributeSlot(bucketId, fileName);
//        if ("localhost".equals(socket)) {
//            return deleteService.recoverFile(bucketId, fileName);
//        } else if (socket != null) {
//            return clientFactory
//                    .getFeignClient(TestFeign.class, "http://" + socket)
//                    .recoverFile(bucketId, fileName);
//        }
//        return new ResponseResult(200, "找不到路径");
    }

    @Override
    public void delBucket(String bucketId) {
        Map<String, Integer> fileSet = bucketCache.getFileSetByName(bucketId, 1);
        fileSet.keySet().forEach(key -> {
            String fileName = key.substring(key.lastIndexOf('/') + 1);
            asyncTask.freshFileMessage(String.valueOf(bucketId), fileName, 3);
        });
    }
}

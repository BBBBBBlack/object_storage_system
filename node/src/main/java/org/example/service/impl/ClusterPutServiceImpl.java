package org.example.service.impl;


import org.example.cache.BucketCache;
import org.example.feign.TestFeign;
import org.example.pojo.ResponseResult;
import org.example.pojo.ShardFile;
import org.example.pojo.ShardMessage;
import org.example.pojo.SimpleFile;
import org.example.service.ClusterPutService;
import org.example.service.PutService;
import org.example.util.AsyncTask;
import org.example.util.JwtUtil;
import org.example.util.ServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Service
public class ClusterPutServiceImpl implements ClusterPutService {

    @Autowired
    PutService putService;

    @Autowired
    BucketCache bucketCache;

    @Autowired
    private ServicesUtil servicesUtil;

    @Autowired
    private TestFeign testFeign;

    @Value("${spring.cloud.nacos.discovery.metadata.slot}")
    private Integer slot;

    //    @Autowired
//    FeignClientFactory clientFactory;
//
//    @Autowired
//    ReedSolomonEncoder solomonEncoder;
//    @Autowired
//    ReedSolomonDecoder solomonDecoder;
    @Autowired
    AsyncTask asyncTask;

    @Override
    public ResponseResult<String> uploadSimple(SimpleFile simpleFile) {
        String bucketId = simpleFile.getBucketId();
        String filename = simpleFile.getMultipartFile().getOriginalFilename();
        assert filename != null;
        String uri = servicesUtil.distributeURI(filename);
        try {
            return testFeign.uploadSimple(new URI(uri), simpleFile.getMultipartFile(), simpleFile.getOriginMD5(),
                    bucketId, simpleFile.getIsZip(), 1);
        } catch (URISyntaxException e) {
            return new ResponseResult<>(500, "上传错误", e.getMessage());
        }
    }

    @Override
    public ResponseResult shardPreparation(ShardMessage message, String originMd5) {
        String bucketId = message.getBucketId();
        String fileName = message.getFileName();
        String uri = servicesUtil.distributeURI(fileName);
        String jwt = JwtUtil.createJWT(uri);
//        if ("localhost".equals(uri)) {
//            return putService.shardPreparation(message, originMd5);
//        } else {
        try {
            ResponseResult res = testFeign.shardPreparation(new URI(uri), fileName,
                    message.getShardNum(), message.getShardSize(), bucketId,
                    message.getIsZip(), originMd5, 1);
            return new ResponseResult(res.getCode(), res.getMsg(), jwt);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
//        }
    }

    @Override
    public ResponseResult<Integer> uploadShard(ShardFile shardFile, String key) {
        try {
            String uri = JwtUtil.parseJWT(key).getSubject();
            return testFeign.uploadShard(new URI(uri), shardFile.getNo(), shardFile.getTotalMD5(),
                    shardFile.getOwnMD5(), shardFile.getFile(), key, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(200, "key解析失败");
        }
    }

    @Override
    public ResponseResult<Set<Integer>> checkShard(String md5, String key) {
        try {
            String uri = JwtUtil.parseJWT(key).getSubject();
            return testFeign.checkShard(new URI(uri), md5, key, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult<>(200, "key解析失败");
        }
    }

    @Override
    public ResponseResult coldStore(String bucketId, String fileName) {
        Integer res = asyncTask.coldStore(bucketId, fileName);
        if (res == -1) {
            return new ResponseResult(200, "存储失败");
        } else if (res == -2) {
            return new ResponseResult(200, "节点数量不足");
        } else {
            return new ResponseResult(200, "开启");
        }
    }
}

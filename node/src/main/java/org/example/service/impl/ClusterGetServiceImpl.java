package org.example.service.impl;


import org.example.cache.BucketCache;
import org.example.feign.TestFeign;
import org.example.mapper.BucketMapper;
import org.example.pojo.Bucket;
import org.example.pojo.ResponseResult;
import org.example.pojo.vo.BucketVo;
import org.example.util.ServicesUtil;
import feign.Response;
import org.example.service.ClusterGetService;
import org.example.service.GetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ClusterGetServiceImpl implements ClusterGetService {
    //
//    @Autowired
//    FeignClientFactory clientFactory;
//
    @Autowired
    private GetService getService;

    @Autowired
    private ServicesUtil servicesUtil;

    @Autowired
    private TestFeign testFeign;

    @Autowired
    private BucketMapper bucketMapper;

    @Autowired
    private BucketCache bucketCache;

    public ResponseResult getFile(String bucketId, String fileName, Integer version, boolean isBase64, HttpServletResponse response) {
        String uri = servicesUtil.distributeURI(fileName);
        Response response1;
        try {
            response1 = testFeign.getFile(new URI(uri), bucketId, fileName, version, isBase64, 1);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            InputStream inputStream = response1.body().asInputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            response.setHeader("Content-Disposition", response1.headers().get("Content-Disposition").toString().replace("[", "").replace("]", ""));
            byte[] temp = new byte[1024 * 10];
            int length;
            if (isBase64) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                while ((length = bufferedInputStream.read(temp)) != -1) {
                    byteArrayOutputStream.write(temp, 0, length);
                }
                byte[] bytes = byteArrayOutputStream.toByteArray();
                String base64 = Base64.getEncoder().encodeToString(bytes);
                byteArrayOutputStream.close();
                bufferedInputStream.close();
                inputStream.close();
                return new ResponseResult(200, "下载文件", base64);
            } else {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
                while ((length = bufferedInputStream.read(temp)) != -1) {
                    bufferedOutputStream.write(temp, 0, length);
                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                bufferedInputStream.close();
                inputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ResponseResult(500, "下载失败");
    }


//    @Override
//    public void getFile(String bucketId, String fileName, Integer version, HttpServletResponse response) {
//        String uri = servicesUtil.distributeURI(fileName);
//        Response response1;
//        try {
//            response1 = testFeign.getFile(new URI(uri), bucketId, fileName, version, 1);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        try {
//            InputStream inputStream = response1.body().asInputStream();
//            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//            response.setHeader("Content-Disposition",
//                    response1.headers().get("Content-Disposition").toString().replace("[", "").replace("]", ""));
//            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
//            int length = 0;
//            byte[] temp = new byte[1024 * 10];
//            while ((length = bufferedInputStream.read(temp)) != -1) {
//                bufferedOutputStream.write(temp, 0, length);
//            }
//            bufferedOutputStream.flush();
//            bufferedOutputStream.close();
//            bufferedInputStream.close();
//            inputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public ResponseResult<BucketVo> getBucket(String bucketId) {
        Bucket bucket = bucketMapper.getBucketById(bucketId);
        if (bucket == null) {
            return new ResponseResult<>(500, "桶不存在", null);
        }
//        Set<String> fileSet = getService.getBucket(bucketId).getData();
//        Set<String> set = new HashSet<>(new HashSet<>(fileSet));
        Set<String> set = new HashSet<>();
        List<ServiceInstance> instances = servicesUtil.getInstance("node01-service");
        for (ServiceInstance instance : instances) {
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            ResponseResult result;
            try {
                result = testFeign.getBucket(new URI(url), bucketId, 1);
//                Set<String> newSet = (Set<String>)result.getData();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            if (result.getData() != null) {
                set.addAll(new HashSet<>((Set<String>) result.getData()));
            }
        }
        BucketVo bucketVo = new BucketVo(Integer.getInteger(bucketId));
        bucketVo.setFileSet(set);
        bucketVo.setBucket(bucket);
        return new ResponseResult<>(200, "查询桶信息", bucketVo);
    }
//
//    @Override
//    public void coldGet(String bucketId, String fileName) {
//        PullColdRequestMessage message = new PullColdRequestMessage(bucketId, fileName);
//        Set<Node> nodes = ClusterProperty.SocketAliveMap.keySet();
//        for (Node node : nodes) {
//            Client.sendMessage(node.getIp(), node.getTCPPort(), message);
//        }
////        int cnt = 0;
////        while (cnt < 5) {
////
////        }
//    }
//
//    @Override
//    public BucketMsg getBucketMsg(BucketMsg bucketMsg) {
//        return bucketCache.bucketMsgScan(bucketMsg);
//    }
}

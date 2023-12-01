package org.example.service.impl;

import org.example.context.UserContextHolder;
import org.example.feign.TestFeign;
import org.example.mapper.BucketMapper;
import org.example.pojo.Bucket;
import org.example.pojo.ResponseResult;
import org.example.pojo.vo.BucketVo;
import org.example.service.BucketService;
import org.example.util.FileUtil;
import org.example.util.ServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

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

    @Value("${base-url.windows}")
    private String baseUrl;

    @Override
    public ResponseResult<Bucket> createBucket(String bucketName, int acl, int lockEnable) {
        Bucket bucket = new Bucket(null, bucketName,
                UserContextHolder.getContext().getProperty("id").toString(), acl, lockEnable);
        bucketMapper.createBucket(bucket);
        return new ResponseResult<>(200, "创建桶", bucket);
    }

    @Override
    public ResponseResult deleteBucket(Map<String, String> parameters) {

        String bucketId = parameters.get("bucketId");
        int isInternal = Integer.parseInt(parameters.get("isInternal"));
        if (bucketId == null) {
            return new ResponseResult(400, "参数错误");
        }
        //是否创建者
        String userId = UserContextHolder.getContext().getProperty("id").toString();
        if (bucketMapper.isCreator(bucketId, userId
        ) == 0) {
            return new ResponseResult(500, "没有权限");
        } else if (isInternal == 0) {
            bucketMapper.deleteAdvancedAcl(bucketId, userId);
            servicesUtil.getInstance("node01-service").forEach(instance -> {
                try {
                    String uri = "http://" + instance.getHost() + ":" + instance.getPort();
                    parameters.put("isInternal", "1");
                    testFeign.deleteBucket(new URI(uri), parameters);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
            bucketMapper.deleteBucket(bucketId);
        }
        //删去bucket下的所有文件
        FileUtil.deleteDirectory(baseUrl + "realPath\\" + bucketId);
        return new ResponseResult(200, "删除桶");
    }

    @Override
    public ResponseResult putAcl(Integer aclType, Integer bucketId, Integer acl, List<String> grantRead, List<String> grantWrite) {
        if (aclType == 1) {
            bucketMapper.putAcl(bucketId, acl);
        } else {
            try {
                bucketMapper.putAcl(bucketId, 4);
                bucketMapper.deleteAdvancedAcl(bucketId.toString(),
                        UserContextHolder.getContext().getProperty("id").toString());
                bucketMapper.putGrantRead(bucketId, grantRead);
                bucketMapper.putGrantWrite(bucketId, grantWrite);
            } catch (Exception e) {
                return new ResponseResult(500, "有用户不存在，修改权限失败");
            }
        }
        return new ResponseResult(200, "修改权限");
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

    @Override
    public ResponseResult<List<BucketVo>> getBucketList() {
        String userId = UserContextHolder.getContext().getProperty("id").toString();
        List<BucketVo> bucketList = bucketMapper.getBucketList(userId);
        return new ResponseResult<>(200, "获取桶列表", bucketList);
    }
}

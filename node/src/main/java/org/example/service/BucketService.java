package org.example.service;

import org.example.pojo.Bucket;
import org.example.pojo.ResponseResult;
import org.example.pojo.vo.BucketVo;

import java.util.List;
import java.util.Map;

public interface BucketService {
    ResponseResult<Bucket> createBucket(String bucketName, int acl, int lockEnable);

    ResponseResult putAcl(Integer aclType, Integer bucketId,
                          Integer acl, List<String> grantRead, List<String> grantWrite);

    ResponseResult deleteBucket(Map<String, String> parameters);

    String test(String str);

    ResponseResult<List<BucketVo>> getBucketList();
}

package org.example.service;

import org.example.pojo.Bucket;
import org.example.pojo.ResponseResult;

public interface BucketService {
    ResponseResult<Bucket> createBucket(String bucketName, int acl, int lockEnable);

    String test(String str);
}

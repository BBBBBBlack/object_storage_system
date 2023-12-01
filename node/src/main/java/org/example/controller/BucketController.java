package org.example.controller;

import org.example.pojo.vo.BucketVo;
import org.example.service.BucketService;
import org.example.pojo.Bucket;
import org.example.pojo.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bucket")
public class BucketController {

    @Autowired
    private BucketService bucketService;

    @GetMapping("/test")
    public String test(@RequestParam String str) {
        return bucketService.test(str);
    }

    @PostMapping("/create_bucket")
    public ResponseResult<Bucket> createBucket(@RequestHeader String bucket,
                                               @RequestHeader(required = false) int acl,
                                               @RequestHeader Integer lockEnable) {
        return bucketService.createBucket(bucket, acl, lockEnable);
    }

    @PostMapping("/delete_bucket")
    public ResponseResult deleteBucket(@RequestBody Map<String, String> parameters) {

        return bucketService.deleteBucket(parameters);
    }

    @GetMapping("/get_bucket_list")
    public ResponseResult<List<BucketVo>> getBucketList() {
        return bucketService.getBucketList();
    }

    @PostMapping("/put_acl")
    public ResponseResult putAcl(@RequestHeader Integer aclType,
                                 @RequestHeader Integer bucketId,
                                 @RequestParam(required = false) Integer acl,
                                 @RequestParam(required = false) List<String> grantRead,
                                 @RequestParam(required = false) List<String> grantWrite) {
        return bucketService.putAcl(aclType, bucketId, acl, grantRead, grantWrite);
    }
}

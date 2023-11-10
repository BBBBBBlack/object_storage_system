package org.example.controller;

import org.example.pojo.Bucket;
import org.example.pojo.ResponseResult;
import org.example.service.BucketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}

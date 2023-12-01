package org.example.controller;


import org.example.service.ClusterPutService;
import org.example.service.PutService;
import org.example.pojo.ResponseResult;
import org.example.pojo.ShardFile;
import org.example.pojo.ShardMessage;
import org.example.pojo.SimpleFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/put")
public class PutController {
    @Autowired
    private PutService putService;

    @Autowired
    private ClusterPutService cPutService;

//    @Autowired
//    FeignClientFactory clientFactory;

    @PostMapping("/uploadSimple")
    public ResponseResult<String> uploadSimple(@RequestParam MultipartFile file,
                                               @RequestParam String originMd5,
                                               @RequestParam String bucketId,
                                               @RequestParam Boolean isZip,
                                               @RequestParam Integer isInternal) {
        SimpleFile simpleFile = new SimpleFile(file, originMd5, bucketId, isZip);
        if (isInternal == 1) {
            return putService.uploadSimple(simpleFile);
        } else {
            return cPutService.uploadSimple(simpleFile);
        }
    }

    @PostMapping("/shardPreparation")
    public ResponseResult shardPreparation(@RequestParam String fileName,
                                           @RequestParam Integer shardNum,
                                           @RequestParam Long shardSize,
                                           @RequestParam String bucketId,
                                           @RequestParam Boolean isZip,
                                           @RequestParam String originMd5,
                                           @RequestParam Integer isInternal) {
        ShardMessage message =
                new ShardMessage(fileName, shardNum, shardSize, bucketId, isZip, new HashSet<>(), null);
        if (isInternal == 1) {
            return putService.shardPreparation(message, originMd5);
        } else {
            return cPutService.shardPreparation(message, originMd5);
        }

    }

    @PostMapping("/uploadShard")
    public ResponseResult<Integer> uploadShard(@RequestParam Integer no,
                                               @RequestParam String totalMd5,
                                               @RequestParam String ownMd5,
                                               @RequestParam MultipartFile file,
                                               @RequestParam String key,
                                               @RequestParam Integer isInternal) {
        ShardFile shardFile = new ShardFile(no, totalMd5, ownMd5, file);
        if (isInternal == 1) {
            return putService.uploadShard(shardFile);
        } else {
            return cPutService.uploadShard(shardFile, key);
        }
    }

    @PostMapping("/checkShard")
    public ResponseResult<Set<Integer>> checkShard(@RequestParam String md5,
                                                   @RequestParam String key,
                                                   @RequestParam Integer isInternal) {
        if (isInternal == 1) {
            return putService.checkShard(md5);
        } else {
            return cPutService.checkShard(md5, key);
        }
    }

    @PostMapping("/coldStore")
    public ResponseResult coldStore(@RequestParam String bucketId,
                                    @RequestParam String fileName) {
        return cPutService.coldStore(bucketId, fileName);
    }
}

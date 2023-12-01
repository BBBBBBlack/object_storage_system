package org.example.controller;


import org.example.pojo.ResponseResult;
import org.example.service.ClusterGetService;
import org.example.service.GetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/get")
public class GetController {

    @Autowired
    private GetService getService;

    @Autowired
    private ClusterGetService cGetService;

    @GetMapping("/getBucket/{bucketId}/{isInternal}")
    public ResponseResult getBucket(@PathVariable String bucketId,
                                    @PathVariable Integer isInternal) {
        if (isInternal == 1) {
            return getService.getBucket(bucketId);
        } else {
            return cGetService.getBucket(bucketId);
        }
    }

    @PostMapping("/getFile")
    public ResponseResult getFile(@RequestParam String bucketId,
                                  @RequestParam String fileName,
                                  @RequestParam(required = false) Integer version,
                                  @RequestParam boolean isBase64,
                                  @RequestParam Integer isInternal,
                                  HttpServletResponse response) {
        if (isInternal == 1) {
            getService.getFile(bucketId, fileName, version, response);
            return null;
        } else {
            return cGetService.getFile(bucketId, fileName, version, isBase64, response);
        }
    }

//    @GetMapping("/coldGet")
//    public void coldGet(@RequestParam String bucketId,
//                        @RequestParam String fileName) {
//        cGetService.coldGet(bucketId, fileName);
//    }
//
//    @PostMapping("/getBucketMsg")
//    public BucketMsg getBucketMsg(@RequestBody BucketMsg bucketMsg) {
//        return cGetService.getBucketMsg(bucketMsg);
//    }
}

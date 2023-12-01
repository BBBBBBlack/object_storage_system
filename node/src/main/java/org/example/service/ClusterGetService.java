package org.example.service;

//import com.example.test01_coll.domain.entity.Bucket;
//import com.example.test01_coll.domain.entity.Result;
//import com.example.test01_coll.pojo.BucketMsg;

import org.example.pojo.ResponseResult;
import org.example.pojo.vo.BucketVo;

import javax.servlet.http.HttpServletResponse;

public interface ClusterGetService {
    ResponseResult getFile(String bucketId, String fileName,
                           Integer version, boolean isBase64, HttpServletResponse response);

    //
    ResponseResult<BucketVo> getBucket(String bucketId);
//
//    void coldGet(String bucketId, String fileName);
//
//    BucketMsg getBucketMsg(BucketMsg bucketMsg);
}

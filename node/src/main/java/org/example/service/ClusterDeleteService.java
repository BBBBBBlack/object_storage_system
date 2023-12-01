package org.example.service;


import org.example.pojo.ResponseResult;

public interface ClusterDeleteService {
    ResponseResult delFile(String bucketId, String fileName, Boolean isForever);

    ResponseResult recoverFile(String bucketId, String fileName);

    void delBucket(String bucketId);
}

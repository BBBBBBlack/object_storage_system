package org.example.service;


import org.example.pojo.ResponseResult;

public interface DeleteService {
    ResponseResult delFile(String bucketId, String fileName, Boolean isForever);

    ResponseResult recoverFile(String bucketId, String fileName);
}

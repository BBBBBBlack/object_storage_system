package org.example.service;

import org.example.pojo.ResponseResult;

import javax.servlet.http.HttpServletResponse;
import java.util.Set;

public interface GetService {
    ResponseResult<Set<String>> getBucket(String bucketId);

    void getFile(String bucketId, String fileName, Integer version,HttpServletResponse response);
}

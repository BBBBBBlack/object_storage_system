package org.example.service;


import org.example.pojo.ResponseResult;
import org.example.pojo.ShardFile;
import org.example.pojo.ShardMessage;
import org.example.pojo.SimpleFile;

import java.util.Set;

public interface ClusterPutService {

    ResponseResult<String> uploadSimple(SimpleFile simpleFile);

    ResponseResult shardPreparation(ShardMessage message, String originMd5);

    ResponseResult<Integer> uploadShard(ShardFile shardFile, String key);

    ResponseResult<Set<Integer>> checkShard(String md5, String key);

    ResponseResult coldStore(String bucketId, String fileName);
}

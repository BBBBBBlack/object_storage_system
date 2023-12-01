package org.example.service;



import org.example.pojo.ResponseResult;
import org.example.pojo.ShardFile;
import org.example.pojo.ShardMessage;
import org.example.pojo.SimpleFile;

import java.util.Set;

public interface PutService {
    ResponseResult<String> uploadSimple(SimpleFile file);

    ResponseResult shardPreparation(ShardMessage message, String Md5);

    ResponseResult<Integer> uploadShard(ShardFile shardFile);

    ResponseResult<Set<Integer>> checkShard(String md5);
}

package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.Bucket;
import org.springframework.stereotype.Repository;

@Mapper
public interface BucketMapper {
    void createBucket(Bucket bucket);
}

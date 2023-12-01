package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.pojo.Bucket;
import org.example.pojo.vo.BucketVo;

import java.util.List;

@Mapper
public interface BucketMapper {

    void createBucket(Bucket bucket);

    void putAcl(Integer bucketId, Integer acl);

    void deleteAdvancedAcl(String bucketId, String userId);

    void putGrantRead(Integer bucketId, List<String> grantRead);

    void putGrantWrite(Integer bucketId, List<String> grantWrite);

    Integer isCreator(String bucketId, String userId);

    void deleteBucket(String bucketId);

    List<BucketVo> getBucketList(String userId);

    Bucket getBucketById(String bucketId);
}

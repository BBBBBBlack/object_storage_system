package org.example.cache;

import org.example.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class Md5Cache {
    @Autowired
    private BucketCache bucketCache;

    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "bucketMD5Cache",
                            key = "#bucketName", condition = "#flag==1")
            },
            put = {
                    @CachePut(cacheNames = "bucketMD5Cache",
                            key = "#bucketName", condition = "#flag==2")
            },
            evict = {
                    @CacheEvict(cacheNames = "bucketMD5Cache",
                            key = "#bucketName", condition = "#flag==3")
            }
    )
    public Map<String, String> getMD5SetByName(String bucketName, int flag) {
        //<文件夹路径，文件最新版本>
        Map<String, Integer> nvMap = bucketCache.getFileSetByName(bucketName, flag);
        Map<String, String> map = new HashMap<>();
        Set<String> keySet = nvMap.keySet();//文件集合
        for (String filePath : keySet) {
            Integer version = nvMap.get(filePath);
            String latestVersionPath = filePath + "/" + version;
            String md5 = FileUtil.getMd5(latestVersionPath);
            map.put(md5, filePath);
        }
        return map;
    }
}

package org.example.util;

import org.example.cache.Md5Cache;
import org.example.pojo.FileLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Component
public class RedisUtil {
    @Autowired
    RedisTemplate<Object, Object> template;
    @Autowired
    Md5Cache md5Cache;

    /**
     * 将文件临时存储至redis，接下来将异步存储至本地磁盘
     */
    public boolean storeFile(String key, MultipartFile file, Duration duration) {
        try {
            template.opsForValue()
                    .set(key, Base64.getEncoder().encodeToString(file.getBytes()), duration);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void delKey(String key) {
        template.delete(key);
    }

    /**
     * 将文件临时存储至redis，接下来将异步存储至本地磁盘
     */
    public boolean storeFile(String bucketId, String key,
                             String filePath, MultipartFile file, Duration duration) {
        FileLock.waitLock(filePath);
        try {
            int flag = 1;
            synchronized (RedisUtil.class) {
                if (!FileLock.isLocked(filePath)) {
                    FileLock.lock(filePath);
                } else {
                    flag = 0;
                }
            }
            if (flag == 0) {
                FileLock.waitLock(filePath);
                FileLock.lock(filePath);
            }
            try {
                String md5 = FileUtil.doGetMd5(file.getInputStream());
                Map<String, String> md5Set = md5Cache.getMD5SetByName(bucketId, 1);
//            System.out.println(md5Set);
                if (md5Set.containsKey(md5)) {
                    return false;
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            template.opsForValue()
                    .set(key, Base64.getEncoder().encodeToString(file.getBytes()), duration);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

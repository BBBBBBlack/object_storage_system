package org.example.cache;

import org.example.property.FileProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import sun.misc.Cleaner;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Base64;
import java.util.Map;

@Component
public class FileCache {
    @Autowired
    BucketCache bucketCache;

    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "fileCache",
                            key = "#bucketId+#fileName", condition = "#flag==1", unless = "#result==null")
            },
            put = {
                    @CachePut(cacheNames = "fileCache",
                            key = "#bucketId+#fileName", condition = "#flag==2", unless = "#result==null")
            },
            evict = {
                    @CacheEvict(cacheNames = "fileCache",
                            key = "#bucketId+#fileName", condition = "#flag==3")
            }
    )
    public String getFile(String bucketId, String fileName,
                          HttpServletResponse response, int flag) {
        String path = FileProperty.realPath + bucketId + "/" + fileName;
        Map<String, Integer> nvMap = bucketCache.getFileSetByName(bucketId, flag);
        Integer version = nvMap.get(path);
        File file = new File(path + "/" + version);
        if (file.exists()) {
            MappedByteBuffer buffer = null;
            BufferedOutputStream bos = null;
            try {
                FileChannel channel = new FileInputStream(file).getChannel();
                if (file.length() < 1024 * 1024 * 2) {
                    byte[] bytes = new byte[(int) file.length()];
                    buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
                    buffer.get(bytes);
                    Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
                    cleaner.clean();
                    return Base64.getEncoder().encodeToString(bytes);
                } else if (response != null) {
                    byte[] bytes = new byte[1024 * 1024];
                    bos = new BufferedOutputStream(response.getOutputStream());
                    for (int i = 0; i < file.length(); i += 1024 * 1024) {
                        int size = (file.length() - i) < 1024 * 1024 ?
                                (int) (file.length() - i) : 1024 * 1024;
                        buffer = channel.map(FileChannel.MapMode.READ_ONLY, i, size);
                        buffer.get(bytes, 0, size);
                        bos.write(bytes, 0, size);
                        bos.close();
                        Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
                        cleaner.clean();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            finally {
//                Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
//                cleaner.clean();
//            }
        }
        return null;
    }
}

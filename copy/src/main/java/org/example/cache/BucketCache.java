package org.example.cache;

//import com.example.test01_coll.pojo.BucketMsg;
//import com.example.test01_coll.property.FileProperty;
import org.example.pojo.temp.BucketMsg;
import org.example.property.FileProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class BucketCache {


    //查看某一桶下所有文件
    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "bucketCache",
                            key = "#bucketId", condition = "#flag==1")
            },
            put = {
                    @CachePut(cacheNames = "bucketCache",
                            key = "#bucketId", condition = "#flag==2")
            },
            evict = {
                    @CacheEvict(cacheNames = "bucketCache",
                            key = "#bucketId", condition = "#flag==3")
            }
    )
    public Map<String, Integer> getFileSetByName(String bucketId, int flag) {
        return fileVersionScan(bucketId);
    }

    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "bucketCache-cold",
                            key = "#bucketId", condition = "#fresh==false")
            },
            put = {
                    @CachePut(cacheNames = "bucketCache-cold",
                            key = "#bucketId", condition = "#fresh==true")
            }
    )
    public Map<String, Integer> getColdFileSetByName(String bucketId, Boolean fresh) {
        return coldFileVersionScan(bucketId);
    }

    public Map<String, Integer> fileVersionScan(String bucketId) {
        String uri = FileProperty.realPath + bucketId + "/";
        Path path = Paths.get(uri);
        Map<String, Integer> nvMap = new HashMap<>();//name-versionMap
        if (Files.isDirectory(path)) {
            try {
                //fileName层
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                if (!dir.equals(path) && dir.getParent().equals(path)) {
                                    //version层（文件）
                                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                                        @Override
                                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                            if (file.getParent().equals(dir)) {
                                                String fileName = String.valueOf(file.getFileName());
                                                int newVersion = Integer.parseInt(fileName);
                                                nvMap.merge(dir.toString().replace('\\', '/'), newVersion, (a, b) -> b > a ? b : a);
                                                return super.visitFile(file, attrs);
                                            }
                                            return FileVisitResult.TERMINATE;
                                        }
                                    });
                                }
                                return super.preVisitDirectory(dir, attrs);
                            }
                        }
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return nvMap;
    }

    public Map<String, Integer> coldFileVersionScan(String bucketId) {
        String uri = FileProperty.realPath + bucketId + "/";
        Path path = Paths.get(uri);
        Map<String, Integer> nvMap = new HashMap<>();//name-versionMap
        if (Files.isDirectory(path)) {
            try {
                //fileName层
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                if (!dir.equals(path) && String.valueOf(dir.getFileName()).endsWith("-cold")) {
                                    //version层
                                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                                        @Override
                                        public FileVisitResult preVisitDirectory(Path versionDir, BasicFileAttributes attrs) throws IOException {
                                            if (!versionDir.equals(dir)) {
                                                String fileName = String.valueOf(versionDir.getFileName());
                                                int newVersion = Integer.parseInt(fileName);
                                                nvMap.merge(dir.toString().replace('\\', '/'), newVersion, (a, b) -> b > a ? b : a);
                                            }
                                            return super.preVisitDirectory(versionDir, attrs);
                                        }
                                    });
                                }
                                return super.preVisitDirectory(dir, attrs);
                            }
                        }
                );
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return nvMap;
    }

    public BucketMsg bucketMsgScan(BucketMsg bucketMsg) {
        Long bucketId = bucketMsg.getId();
        String uri = FileProperty.realPath + bucketId + "/";
        Path path = Paths.get(uri);
        AtomicInteger fileNum = new AtomicInteger(0);
        AtomicLong bucketSize = new AtomicLong(0L);
        if (Files.isDirectory(path)) {
            try {
                //fileName层
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                if (!dir.equals(path) && dir.getParent().equals(path)) {
                                    fileNum.incrementAndGet();
                                    //version层（文件）
                                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                                        @Override
                                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                            if (file.getParent().equals(dir)) {
                                                bucketSize.addAndGet(Files.size(file));
                                                return super.visitFile(file, attrs);
                                            }
                                            return FileVisitResult.TERMINATE;
                                        }
                                    });
                                }
                                return super.preVisitDirectory(dir, attrs);
                            }
                        }
                );

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bucketMsg.setFileNum(fileNum.get());
        bucketMsg.setBucketSize(bucketSize.get());
        return bucketMsg;
    }

}

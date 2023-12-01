package org.example.util;

import org.example.cache.BucketCache;
import org.example.cache.FileCache;
import org.example.cache.Md5Cache;
import org.example.pojo.FileLock;
import org.example.property.FileProperty;
import org.example.property.TCPProperty;
import org.example.protocol.Message.WriteRequestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class AsyncTask {
    @Autowired
    private Md5Cache md5Cache;

    @Autowired
    private BucketCache bucketCache;

    @Autowired
    private FileCache fileCache;

    @Autowired
    private FileUtil fileUtil;

    //    @Autowired
//    private ReedSolomonEncoder solomonEncoder;
//
    @Async
    public void combineTask(WriteRequestMessage msg, String uri) {
        combine(msg);
        FileUtil.deleteDirectory(uri);
        bucketCache.getColdFileSetByName(msg.getBucketId(), true);
    }

    @Async
    public void autoColdStore(String bucketId, String fileName) {
        coldStore(bucketId, fileName);
    }

    @Async
    public void freshFileMessage(String bucketId, String fileName, int flag) {
        md5Cache.getMD5SetByName(bucketId, flag);
        fileCache.getFile(bucketId, fileName, null, flag);
    }

    @Async
    public void redis2Local(String bucketId, String key, String originFileName) {

        fileUtil.redis2Local(bucketId, key, originFileName);
        freshFileMessage(bucketId, originFileName, 2);
//        FileLock.unlock(filePath);
    }


    private void combine(WriteRequestMessage message) {
        String from = FileProperty.tempPath + message.getBucketId() + "/"
                + message.getFileName() + "/" + message.getVersion();
        String to = FileProperty.realPath + message.getBucketId()
                + "/" + message.getFileName() + "/" + message.getVersion();
        if (!FileLock.isLocked(to)) {
            try {
                FileUtil.createFile(to.substring(0, to.lastIndexOf('/')), message.getVersion());
                RandomAccessFile accessFile = new RandomAccessFile(to, "rw");
                AtomicInteger fileCount = new AtomicInteger();
                Files.walkFileTree(Paths.get(from), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        String path = from + "/" + file.getFileName();
                        System.out.println(path);
                        File fromFile = new File(path);
                        FileInputStream fis = new FileInputStream(fromFile);
                        long offset = (Long.parseLong(String.valueOf(file.getFileName())) - 1)
                                * TCPProperty.maxSend;
                        accessFile.seek(offset);
                        byte[] bytes = new byte[TCPProperty.maxSend];
                        int len = 0;
                        while ((len = fis.read(bytes)) != -1) {
                            accessFile.write(bytes, 0, len);
                        }
                        fis.close();
                        if (fileCount.incrementAndGet() == message.getTotalNum()) {
                            accessFile.close();
                        }
                        return super.visitFile(file, attrs);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FileLock.addCopyFlag(from, 3);
        }
    }

    public Integer coldStore(String bucketId, String fileName) {
//        String filePath = FileProperty.realPath + bucketId + "/" + fileName;
//        Map<String, Integer> nvMap = bucketCache.getFileSetByName(bucketId, 1);
//        Integer version = nvMap.get(filePath);
//        try {
//            solomonEncoder.encodeFile(filePath + "/" + version, filePath + "-cold/" + version);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return -1;
////            return new Result(200, "存储失败");
//        }
//        int count = 0;
//        Set<Node> nodes = ClusterProperty.SocketAliveMap.keySet();
//        for (Node node : nodes) {
//            if (count >= ReedSolomonEncoder.TOTAL_SHARDS - 1) {
//                break;
//            }
//            AskRequestMessage message = new AskRequestMessage(bucketId, fileName + "-cold/" + version, count);
//            TCPUtil.autoCopy(node.getIp(), node.getTCPPort(), message);
//            FileUtil.deleteFile(filePath + "-cold/" + version + "/" + count);
//            count++;
//        }
//        if (count + 1 < ReedSolomonEncoder.DATA_SHARDS) {
//            return -2;
////            return new Result(200, "节点数量不足");
//        }
        return 1;
//        return new Result(200, "开启");
    }

}

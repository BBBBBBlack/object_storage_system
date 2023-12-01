package org.example.service.impl;


import org.example.cache.BucketCache;
import org.example.cache.Md5Cache;
import org.example.property.FileProperty;
import org.example.service.PutService;
import org.example.util.AsyncTask;
import org.example.util.FileUtil;
import org.example.util.RedisUtil;
import org.example.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Service
public class PutServiceImpl implements PutService {
    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private Md5Cache md5Cache;

    @Autowired
    private BucketCache bucketCache;

    @Autowired
    private AsyncTask asyncTask;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public ResponseResult<String> uploadSimple(SimpleFile file) {
        String fileName = file.getMultipartFile().getOriginalFilename();
        Integer res = fileUtil.checkFile(file);
//        if (res == -1) {
//            return new Result<>(200, "有重名文件");
        if (res == -2) {
            return new ResponseResult<>(200, "文件已存在");
        } else if (res == -3) {
            return new ResponseResult<>(200, "文件内容缺失");
        }
        String path;
        String key;
        if (file.getIsZip()) {
            assert fileName != null;
            path = FileProperty.realPath +
                    file.getBucketId() + "/" + fileName.substring(0, fileName.lastIndexOf('.')) + ".zip";
            key = "zipStoreCache::" + path;
        } else {
            path = FileProperty.realPath +
                    file.getBucketId() + "/" + fileName;
            key = "normalStoreCache::" + path;
        }
//        Map<String, Integer> nvMap = bucketCache.getFileSetByName(file.getBucketId(), 1);
//        Integer version = nvMap.get(path);
//        if (version == null) {
//            version = 0;
//        }
//        version++;
////        String versionPath = path + "/" + version;
//        String finalKey = key + "/" + version;
        String finalKey = key + "/" + FileVersion.addVersion(key);
        boolean success = redisUtil.storeFile(finalKey,
                file.getMultipartFile(), Duration.ofMinutes(10L));
        if (success) {
            asyncTask.redis2Local(file.getBucketId(), finalKey, fileName);
            return new ResponseResult<>(200, "已上传");
        } else {
            return new ResponseResult<>(400, "上传失败");
        }
    }

    @Override
    public ResponseResult shardPreparation(ShardMessage message, String md5) {
        String path = FileProperty.realPath +
                message.getBucketId() + "/" + message.getFileName() + ".temp";

        if (FileLock.isLocked(path)) {
            return new ResponseResult(200, "有一个进程未关闭，文件暂时无法上传");
        }
        if (md5Cache.getMD5SetByName(message.getBucketId(), 1).containsKey(md5)) {
            return new ResponseResult(200, "文件已存在");
        }
        Map<String, Integer> nvMap = bucketCache.getFileSetByName(message.getBucketId(), 1);
        String to;
        if (!message.getIsZip()) {
            to = FileProperty.realPath + message.getBucketId() + "/" + message.getFileName();
        } else {
            String zipName = message.getFileName()
                    .substring(0, message.getFileName().lastIndexOf('.')) + ".zip";
            to = FileProperty.realPath + message.getBucketId() + "/" + zipName;
        }
        Integer version = nvMap.get(to);
        if (version == null) {
            version = 0;
        }
        version++;
        message.setVersion(version);
        //锁住
        FileLock.lock(path);
        ShardMessage.getInstance().put(md5, message);
        return new ResponseResult(200, "已准备");
    }

    @Override
    public ResponseResult<Integer> uploadShard(ShardFile shardFile) {
        String totalMD5 = shardFile.getTotalMD5();
        ShardMessage message = ShardMessage.getInstance().get(totalMD5);
        if (message == null) {
            return new ResponseResult<>(404, "文件信息不存在", -1);
        }
        if (message.getShardSet().contains(shardFile.getNo())) {
            return new ResponseResult<>(205, "分片已上传过", -1);
        }
//        if (!FileUtil.getMd5(shardFile.getFile()).equals(shardFile.getOwnMD5())) {
//            return new ResponseResult<>(400, "文件传输有误", -1);
//        }
        if (shardFile.getNo() >= message.getShardNum()) {
            return new ResponseResult<>(200, "传的是什么鬼");
        }
        if (!FileUtil.uploadShard(shardFile)) {
            return new ResponseResult<>(500, "上传失败", -1);
        }
        message.getShardSet().add(shardFile.getNo());
        return new ResponseResult<>(200, "上传成功", shardFile.getNo());
    }

    @Override
    public ResponseResult<Set<Integer>> checkShard(String md5) {
        ShardMessage message = ShardMessage.getInstance().get(md5);
        if (message == null) {
            return new ResponseResult<>(404, "文件信息不存在");
        }
        if (message.getShardNum() != message.getShardSet().size()) {
            return new ResponseResult<>(200, "分片上传不完整", message.getShardSet());
        }
        //删除分片信息
        ShardMessage.getInstance().remove(md5);
        //解锁
        String path = FileProperty.realPath + message.getBucketId() + "/";
        String newName = message.getFileName();
        String oldName = newName + ".temp";

        ResponseResult<Set<Integer>> result;
        if (!message.getIsZip()) {
            //文件重命名
            if (!FileLock.isLocked(path + newName + "/" + message.getVersion())) {
                FileUtil.moveFile(path + oldName, path + newName + "/" + message.getVersion());
//                FileUtil.renameFile(path, oldName, newName);
                //更新文件列表
                asyncTask.freshFileMessage(message.getBucketId(), newName, 2);
            } else {
                FileLock.addCopyFlag(path + oldName, 1);
            }
            // TODO: 2021/4/7 冷存
//            if (ClusterProperty.cluster) {
//                asyncTask.autoColdStore(message.getBucketId(), message.getFileName());
//            }
            result = new ResponseResult<>(200, "上传文件");
        } else {
            newName = newName.substring(0, newName.lastIndexOf('.')) + ".zip";
            if (!FileLock.isLocked(path + newName + "/" + message.getVersion())) {
                if (!FileUtil.getZip(message)) {
                    result = new ResponseResult<>(500, "压缩上传失败");
                } else {
                    //删除原有文件
                    FileUtil.deleteFile(path + oldName);
                    asyncTask.freshFileMessage(message.getBucketId(), newName, 2);
                }
            } else {
                FileLock.addCopyFlag(path + oldName, 2);
            }
//            if (ClusterProperty.cluster) {
//                asyncTask.autoColdStore(message.getBucketId(), newName);
//            }
            result = new ResponseResult<>(200, "上传压缩文件");
        }
        FileLock.unlock(path + oldName);
        return result;
    }
}

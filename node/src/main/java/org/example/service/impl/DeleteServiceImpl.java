package org.example.service.impl;

import org.example.cache.BucketCache;
import org.example.pojo.ResponseResult;
import org.example.property.FileProperty;
import org.example.service.DeleteService;
import org.example.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteServiceImpl implements DeleteService {

    @Autowired
    BucketCache bucketCache;

    @Override
    public ResponseResult delFile(String bucketId, String fileName, Boolean isForever) {
        if (isForever) {
            FileUtil.deleteDirectory(FileProperty.realPath + bucketId + "/" + fileName);
            FileUtil.deleteDirectory(FileProperty.delPath + bucketId + "/" + fileName);
        } else {
            if(!FileUtil.moveDirectory(FileProperty.realPath + bucketId + "/" + fileName,
                    FileProperty.delPath + bucketId + "/" + fileName)){
                return new ResponseResult(200, "找不到文件，删除失败");
            }
        }
        bucketCache.getFileSetByName(bucketId, 3);
        return new ResponseResult(200, "已删除");
    }

    @Override
    public ResponseResult recoverFile(String bucketId, String fileName) {
        if (FileUtil.moveDirectory(FileProperty.delPath + bucketId + "/" + fileName,
                FileProperty.realPath + bucketId + "/" + fileName)) {
            return new ResponseResult(200, "已恢复");
        }
        return new ResponseResult(200, "恢复失败");
    }
}

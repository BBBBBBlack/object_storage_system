package org.example.service.impl;

import org.example.cache.BucketCache;
import org.example.cache.FileCache;
import org.example.pojo.ResponseResult;
import org.example.property.FileProperty;
import org.example.service.GetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class GetServiceImpl implements GetService {
    @Autowired
    private BucketCache bucketCache;
    @Autowired
    private FileCache fileCache;

    @Override
    public ResponseResult<Set<String>> getBucket(String bucketId) {
//        BucketVo bucket = new BucketVo(Integer.getInteger(bucketId));
        Map<String, Integer> nvMap = bucketCache.getFileSetByName(bucketId, 1);
        Set<String> keySet = nvMap.keySet();
        Set<String> fileSet = new HashSet<>();
        for (String path : keySet) {
            fileSet.add(path.substring(path.lastIndexOf('/') + 1));
        }
        return new ResponseResult<>(200, "查询桶信息", fileSet);
    }

    //167
    @Override
    public void getFile(String bucketId, String fileName, Integer version, HttpServletResponse response) {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            response.reset();
            response.addHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            if (version == null) {
                String base64 = fileCache.getFile(bucketId, fileName, response, 1);
                if (base64 != null) {
                    byte[] bytes = Base64.getDecoder().decode(base64);
//                response.getOutputStream().write(bytes, 0, bytes.length);
//                response.getOutputStream().flush();
//                response.getOutputStream().close();
                    response.setContentLength(bytes.length);
                    bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
                    bufferedOutputStream.write(bytes, 0, bytes.length);
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
            } else {
                Map<String, Integer> nvMap = bucketCache.getFileSetByName(bucketId, 1);
                Integer latestVersion = nvMap.get(FileProperty.realPath + bucketId + "/" + fileName);
                int cnt = version;
                while (latestVersion + cnt > 0) {
                    int curVersion = latestVersion + cnt;
                    String path = FileProperty.realPath + bucketId + "/" + fileName + "/" + curVersion;
                    //写入response
                    if (Files.exists(Paths.get(path))) {
                        bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
                        int len;
                        while ((len = bis.read()) != -1) {
                            bufferedOutputStream.write(len);
                        }
                        break;
                    }
                    cnt--;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
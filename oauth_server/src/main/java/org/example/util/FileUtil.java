package org.example.util;


import org.example.myException.NullFileException;
import org.example.myException.WrongTypeException;
import org.example.myException.WrongUrlException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtil {
    private static String realPath;
    private static String virPath;

    @Value("${file.windows.real-path}")
    public void setRealPath(String realPath) {
        FileUtil.realPath = realPath;
    }

    @Value("${file.windows.vir-path}")
    public void setVirPath(String virPath) {
        FileUtil.virPath = virPath;
    }

    private static String getPrefix(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        //后缀名
        String prefix = originalFilename.substring(originalFilename.lastIndexOf("."));
        return prefix;
    }

    public static String upLoad(MultipartFile file) {
        String prefix = getPrefix(file);
        //新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + prefix;
        String realUrl = realPath + newFileName;
        String virUrl = virPath + newFileName;
        File newFile = new File(realUrl);
        try {
            file.transferTo(newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return virUrl;
    }

    public static String upLoadProImag(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new NullFileException("文件为空");
        }
        String prefix = getPrefix(file);
        if (!prefix.equals(".jpg") && !prefix.equals(".png")) {
            throw new WrongTypeException(prefix + "不是允许上传的文件类型");
        }
        String virUrl = upLoad(file);
        if (virUrl == null || virUrl.length() <= 0) {
            throw new WrongUrlException("url值错误，上传失败");
        }
        return virUrl;
    }

    public static boolean delete(String fileUrl) {
        int index = fileUrl.lastIndexOf('/');
        String fileName = fileUrl.substring(index + 1);
        String realUrl = realPath + fileName;
        File file = new File(realUrl);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}

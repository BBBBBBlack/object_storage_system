package org.example.pojo;

import org.example.util.FileUtil;
import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageUtil {
    public static Map<Integer, String> typeMap;

    static {
        typeMap = new HashMap<>();
        typeMap.put(1, "jpg");
        typeMap.put(2, "png");
        typeMap.put(3, "gif");
        typeMap.put(4, "tif");
        typeMap.put(5, "bmp");
    }

    //    常见的图片 文件头标志：
//    JPEG (jpg)，文件头：FFD8FF
//    PNG (png)，文件头：89504E47
//    GIF (gif)，文件头：47494638
//    TIFF (tif)，文件头：49492A00
//    Windows Bitmap (bmp)，文件头：424D
    public static Integer isImage(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return 0;
        }
        String hex = bytesToHexString(bytes, 8);
        if (hex == null || hex.length() == 0) {
            return 0;
        }
        hex = hex.toUpperCase();
        if (hex.startsWith("FFD8FF")) {
            return 1;
        } else if (hex.startsWith("89504E47")) {
            return 2;
        } else if (hex.startsWith("47494638")) {
            return 3;
        } else if (hex.startsWith("49492A00")) {
            return 4;
        } else if (hex.startsWith("424D")) {
            return 5;
        } else {
            return 0;
        }
    }

    public static String bytesToHexString(byte[] src, int cnt) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < cnt * 4; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static boolean getLittleOne(byte[] bytes, String path, Integer version, Integer type) {
        try {
            FileUtil.createFile(path, version);
            Thumbnails.of(new ByteArrayInputStream(bytes))
                    .scale(0.25f)
                    .outputQuality(0.25f)
                    .toFile(path + "/" + version + "." + typeMap.get(type));
            FileUtil.renameFile(path,
                    version.toString() + "." + typeMap.get(type), version.toString());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

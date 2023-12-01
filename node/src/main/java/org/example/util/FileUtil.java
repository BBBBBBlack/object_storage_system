package org.example.util;

import org.example.cache.BucketCache;
import org.example.cache.Md5Cache;
import org.example.pojo.*;
import org.example.property.FileProperty;
import org.example.property.TCPProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import sun.nio.ch.FileChannelImpl;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileUtil {
    @Autowired
    private Md5Cache md5Cache;
    @Autowired
    private BucketCache bucketCache;

    @Autowired
    RedisTemplate<Object, Object> template;

    public static String doGetMd5(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        BufferedInputStream stream = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[8192];
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        int len = 0;
        while ((len = stream.read(buffer)) != -1) {
            md5.update(buffer, 0, len);
        }
        byte[] bytes = md5.digest();
        BigInteger bigInteger = new BigInteger(1, bytes);
        stream.close();
        return bigInteger.toString(16);
    }

    private static Boolean doGetZip(String path, String name, InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        File file = new File(path);
        if (file.exists()) {
            return null;
        }
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(file));
        outputStream.putNextEntry(new ZipEntry(name));
        int len = 0;
        byte[] bytes = new byte[1024];
        while ((len = inputStream.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        outputStream.close();
        inputStream.close();
        return true;
    }

    private static boolean doCopyFile(String from, String to) {
        FileChannel input = null;
        FileChannel output = null;
        try {
            input = new FileInputStream(from).getChannel();
            output = new FileOutputStream(to).getChannel();
            long size = input.size();
            int pos = 0;
            while (pos < size - 1) {
                long max = (size - pos - 1) > TCPProperty.maxSend ? TCPProperty.maxSend : (size - pos - 1);
                input.transferTo(pos, max, output);
                pos += max;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static Boolean getZip(SimpleFile simpleFile, Integer version) {
        String originName = simpleFile.getMultipartFile().getOriginalFilename();
        assert originName != null;
        String zipName = originName.substring(0, originName.lastIndexOf('.')) + ".zip";
        String dir = FileProperty.realPath + simpleFile.getBucketId()
                + "/" + zipName;
        if (createFile(dir, version)) {
            try {
                return doGetZip(dir + "/" + version, originName, simpleFile.getMultipartFile().getInputStream());
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static Boolean getZip(String filePath, String originFileName, String version, byte[] bytes) {
        if (createFile(filePath, Integer.parseInt(version))) {
            try {
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                return doGetZip(filePath + "/" + version, originFileName, new ByteArrayInputStream(bytes));
            } catch (IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static Boolean getZip(ShardMessage message) {
        String from = FileProperty.realPath
                + message.getBucketId() + "/" + message.getFileName() + ".temp";
        try {
            String zipName = message.getFileName()
                    .substring(0, message.getFileName().lastIndexOf('.')) + ".zip";
            String dir = FileProperty.realPath + message.getBucketId() + "/" + zipName;
            createFile(dir, message.getVersion());
            File file = new File(from);
            InputStream inputStream = new FileInputStream(file);

            String to = dir + "/" + message.getVersion();
            return doGetZip(to, message.getVersion().toString(), inputStream);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getMd5(String path) {
        String md5 = null;
        try {
            File file = new File(path);
            if (file.exists()) {
                FileInputStream inputStream = new FileInputStream(file);
                md5 = doGetMd5(inputStream);
            } else {
                throw new FileNotFoundException();
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static String getMd5(MultipartFile file) {
        String md5 = null;
        try {
            InputStream inputStream = file.getInputStream();
            md5 = doGetMd5(inputStream);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static Boolean uploadSimple(SimpleFile file, Integer version) {
        String dir = FileProperty.realPath + file.getBucketId()
                + "/" + file.getMultipartFile().getOriginalFilename();
        if (createFile(dir, version)) {
            try {
                file.getMultipartFile().transferTo(Paths.get(dir + "/" + version));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static Boolean uploadSimple(String filePath, String version, byte[] bytes) {
        String totalFilePath = filePath + "/" + version;
        System.out.println(totalFilePath + "---------------------------------");
        // TODO 用现成的锁
        FileLock.lock(totalFilePath);
        if (createFile(filePath, Integer.parseInt(version))) {
            FileChannel channel = null;
            MappedByteBuffer buffer = null;
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(totalFilePath, "rw");
                channel = raf.getChannel();
                buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bytes.length);
                buffer.put(bytes);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    buffer.force();
                    channel.close();
                    raf.close();
                    Method unmap = FileChannelImpl.class.getDeclaredMethod("unmap", MappedByteBuffer.class);
                    unmap.setAccessible(true);
                    unmap.invoke(FileChannelImpl.class, buffer);
                } catch (IOException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                FileLock.unlock(totalFilePath);
            }
        }
        return false;
    }

    public static Boolean uploadShard(ShardFile file) {
        ShardMessage message = ShardMessage.getInstance().get(file.getTotalMD5());
        String path = FileProperty.realPath
                + message.getBucketId() + "/" + message.getFileName() + ".temp";
        File toFile = new File(path);
        if (!toFile.exists()) {
            try {
                toFile.createNewFile();
                toFile.setWritable(true, false);
                toFile.setReadable(true, false);
                toFile.setExecutable(true, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        RandomAccessFile accessFile = null;
        try {
            accessFile = new RandomAccessFile(path, "rw");
            long offset = message.getShardSize() * file.getNo();
            accessFile.seek(offset);
            accessFile.write(file.getFile().getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                assert accessFile != null;
                accessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Integer checkFile(SimpleFile simpleFile) {
        //重名
//        String name = simpleFile.getMultipartFile().getOriginalFilename();
//        Set<String> fileSet = bucketCache.getFileSetByName(simpleFile.getBucketName(), false);
//        if (fileSet.contains(name) ||
//                (simpleFile.getIsZip() && fileSet.contains(Objects.requireNonNull(name).substring(0, name.lastIndexOf('.')) + ".zip"))) {
//            return -1;
//        }
        //重复上传
        String md5 = getMd5(simpleFile.getMultipartFile());
        Map<String, String> md5Set = md5Cache.getMD5SetByName(simpleFile.getBucketId(), 1);
//        Set<Object> keys = template.keys("normalStoreCache::"
//                + FileProperty.realPath + simpleFile.getBucketId() + "/*");
//        if (keys != null) {
//            for (Object key : keys) {
//                String content = (String) template.opsForValue().get(key);
//                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(content));
//                try {
//                    String strKey = (String) key;
//                    String redisMd5 = doGetMd5(bis);
//                    String totalFilePath = strKey.substring(strKey.indexOf("::") + 2);
//                    String filePath = totalFilePath.substring(0, totalFilePath.lastIndexOf("/"));
//                    md5Set.put(redisMd5, filePath);
//                } catch (IOException | NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        if (md5Set.containsKey(md5)) {
            return -2;
        }
        //内容校验错误
        if (!md5.equals(simpleFile.getOriginMD5())) {
            return -3;
        }
        return 1;
    }

    public static Integer deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return -1;
        }
        return file.delete() ? 1 : 0;
    }

    public static void renameFile(String path, String oldName, String newName) {
        File file = new File(path + "/" + oldName);
        File file1 = new File(path + "/" + newName);
        if (file.exists()) {
            if (!file1.exists()) {
                file.renameTo(new File(path + "/" + newName));
            } else {
                copyFile(path + "/" + oldName, path + "/" + newName);
                deleteFile(path + "/" + oldName);
            }
        }
    }

    public static boolean copyFile(String from, String to) {
        FileLock.lock(from);
        boolean flag = true;
        File path = new File(to.substring(0, to.lastIndexOf('/')));
        if (!path.isDirectory()) {
            path.mkdirs();
            path.setWritable(true, false);
            path.setReadable(true, false);
            path.setExecutable(true, false);
        }
        File toFile = new File(to);
        if (!toFile.exists()) {
            try {
                toFile.createNewFile();
                toFile.setWritable(true, false);
                toFile.setReadable(true, false);
                toFile.setExecutable(true, false);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            flag = !getMd5(to).equals(getMd5(from));
        }
        if (flag) {
            doCopyFile(from, to);
        }
        FileLock.unlock(from);
        return true;
    }

    public static void moveFile(String from, String to) {
        copyFile(from, to);
        deleteFile(from);
    }

    public static void deleteDirectory(String uri) {
        try {
            Path path = Paths.get(uri);
            if (Files.isDirectory(path)) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                            // 先去遍历删除文件
                            @Override
                            public FileVisitResult visitFile(Path file,
                                                             BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }
                        }
                );
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean moveDirectory(String from, String to) {
        Path path = Paths.get(from);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                        // 先去遍历删除文件
                        @Override
                        public FileVisitResult visitFile(Path file,
                                                         BasicFileAttributes attrs) throws IOException {
                            String fromFile = from + "/" + file.getFileName();
                            String toFile = to + "/" + file.getFileName();
                            moveFile(fromFile, toFile);
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean createFile(String dir, Integer version) {
        File dirPath = new File(dir);
        File filePath = new File(dir + "/" + version);
        if (!dirPath.isDirectory()) {
            dirPath.mkdirs();
            dirPath.setWritable(true, false);
            dirPath.setReadable(true, false);
            dirPath.setExecutable(true, false);
        }
        if (!filePath.isFile()) {
            try {
                filePath.createNewFile();
                filePath.setWritable(true, false);
                filePath.setReadable(true, false);
                filePath.setExecutable(true, false);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public static boolean createDirectory(String uri) {
        Path path = Paths.get(uri);
        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public boolean findRepeat(String bucketId, byte[] decode) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decode);
        try {
            String md5 = doGetMd5(byteArrayInputStream);
            Map<String, String> md5Set = md5Cache.getMD5SetByName(bucketId, 1);
            byteArrayInputStream.close();
//            System.out.println(md5Set);
            if (md5Set.containsKey(md5)) {
                return true;
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 从redis中读取文件信息，并写入本地
     */
    public void redis2Local(String bucketId, String key, String originFileName) {
        //缓存空间名
        String cacheName = key.substring(0, key.indexOf("::"));
        //文件夹路径（不包括版本）
        String path = key.substring(key.indexOf("::") + 2, key.lastIndexOf('/'));
        //文件版本
        Long version = Long.parseLong(key.substring(key.lastIndexOf('/') + 1));
        String realKey = key.substring(0, key.lastIndexOf('/'));
        //文件版本号低于最新更新版本号
        if (!version.equals(FileVersion.getVersion(realKey))) {
            template.delete(key);
            return;
        }
        String content = (String) template.opsForValue().get(key);
        byte[] decode = Base64.getDecoder().decode(content);
        if (content == null) {
            return;//redis找不到文件
        }
        //是否有重复文件
        if (findRepeat(bucketId, decode)) {
            template.delete(key);
            return;
        }
        Boolean success = null;
        synchronized (FileUtil.class) {
            //是否有重复文件
            if (findRepeat(bucketId, decode)) {
                template.delete(key);
                return;
            }
            Map<String, Integer> nvMap = bucketCache.getFileSetByName(bucketId, 1);
            version = nvMap.get(path) == null ? 0L : nvMap.get(path).longValue();
            version++;
            //无压缩
            if (cacheName.equals("normalStoreCache")) {
                success = uploadSimple(path, version.toString(), decode);
            } else if (cacheName.equals("zipStoreCache")) {
                int type;
                if ((type = ImageUtil.isImage(decode)) != 0) {
                    path = path.substring(0, path.lastIndexOf(".") + 1)
                            + ImageUtil.typeMap.get(type);
                    success = ImageUtil.getLittleOne(decode, path, version.intValue(), type);
                } else {
                    success = getZip(path, originFileName, version.toString(), decode);
                }
            }
            md5Cache.getMD5SetByName(bucketId, 2);
        }
        if (Boolean.TRUE.equals(success)) {
            template.delete(key);
        }
    }


}
package org.example.pojo;

import java.util.HashMap;
import java.util.Map;

public class FileLock {

    private static final Map<String, Integer> lockMap = new HashMap<>();
    //若copyFlag中存在某一文件路径，则该文件原件暂时被锁，且存在待替换的副本
    //1——realPath中存在.temp文件未替换
    //2——realPath中存在.temp文件未压缩替换
    //3——tempPath中存在副本文件未备份
    private static final Map<String, Integer> copyFlag = new HashMap<>();

    public static void lock(String filePath) {
        Integer integer = lockMap.get(filePath);
        if (integer == null) {
            lockMap.put(filePath, 1);
        } else {
            lockMap.put(filePath, integer + 1);
        }
//        lockMap.merge(filePath, 1, Integer::sum);
    }

    public static void unlock(String filePath) {
        Integer locks = lockMap.get(filePath);
        if (locks != null && locks > 0) {
            lockMap.put(filePath, locks - 1);
        }
    }

    public static boolean isLocked(String filePath) {
        Integer locks = lockMap.get(filePath);
        return locks != null && locks != 0;
    }

    public static void addCopyFlag(String filePath, Integer flag) {
        copyFlag.put(filePath, flag);
    }

    public static void rmCopyFlag(String filePath) {
        copyFlag.remove(filePath);
    }

    public static boolean isFlagged(String filePath) {
        return copyFlag.containsKey(filePath);
    }

    public static boolean waitLock(String filePath) {
        int cnt = 0;
        while (FileLock.isLocked(filePath)) {
            System.out.println("文件暂时不可上传1");
            if (cnt >= 5) {
                return false;//new Result<>(400, "文件暂时不可上传");
            }
            try {
                Thread.sleep(500);
                cnt++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}

package org.example.util;


import org.example.pojo.FileLock;
import org.example.property.FileProperty;
import org.example.protocol.Client;
import org.example.protocol.Message.AskRequestMessage;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TCPUtil {

    public static void autoCopy(String ip, Integer port, AskRequestMessage message) {
        String fileName = message.getFileName();
        String from;
        //单机备份,复制一份
        from = FileProperty.realPath + message.getBucketId()
                + "/" + fileName + "/" + message.getVersion();
        String to = FileProperty.copyPath + message.getBucketId()
                + "/" + fileName + "/" + message.getVersion();
        if (!FileLock.isLocked(from) && !FileLock.isLocked(to)) {
            System.out.println("进来了");
            FileUtil.copyFile(from, to);//有创建目录功能
            message.complete();
            Client.sendMessage(ip, port, message);
        }
    }

    public static String getMyIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

}

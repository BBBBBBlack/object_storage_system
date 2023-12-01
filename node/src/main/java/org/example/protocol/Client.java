package org.example.protocol;


import org.example.protocol.Message.Message;
import io.netty.channel.Channel;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Client {
    public static Map<String, Channel> channelMap = new HashMap<>();

    public static void sendMessage(String ip, int port, Message message) {
//        if (message instanceof )
        Channel channel = channelMap.get(ip + ":" + port);
        if (channel != null) {
            channel.writeAndFlush(message);
        }
    }


    public static void shutdown(String ip, int port) {
        Channel channel = channelMap.get(ip + ":" + port);
        if (channel != null) {
            channel.close();
            channelMap.remove(ip + ":" + port);
        }
    }
}

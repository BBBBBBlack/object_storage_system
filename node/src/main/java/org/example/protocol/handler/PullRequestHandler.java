package org.example.protocol.handler;

import org.example.cache.BucketCache;
import org.example.util.FileUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.property.FileProperty;
import org.example.protocol.Message.AskRequestMessage;
import org.example.protocol.Message.PullRequestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

//server端——数据恢复
@ChannelHandler.Sharable
@Component
public class PullRequestHandler extends SimpleChannelInboundHandler<PullRequestMessage> {
    @Autowired
    BucketCache bucketCache;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PullRequestMessage msg) throws Exception {
        Map<String, Integer> nvMap = bucketCache.getFileSetByName(msg.getBucketId(), 1);
        Set<String> keySet = nvMap.keySet();
        for (String filePath : keySet) {
            Integer version = nvMap.get(filePath);
            String from = filePath + "/" + version;
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            String to = FileProperty.copyPath + msg.getBucketId()
                    + "/" + fileName + "/" + version;
            FileUtil.copyFile(from, to);//有创建目录功能
            AskRequestMessage message =
                    new AskRequestMessage(msg.getBucketId(), fileName, version);
            message.complete();
            ctx.channel().writeAndFlush(message);
        }
    }
}

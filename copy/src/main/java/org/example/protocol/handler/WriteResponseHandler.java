package org.example.protocol.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.pojo.FileLock;
import org.example.property.FileProperty;
import org.example.protocol.Message.WriteResponseMessage;
import org.example.util.DateUtil;
import org.example.util.FileUtil;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class WriteResponseHandler extends SimpleChannelInboundHandler<WriteResponseMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WriteResponseMessage msg) throws Exception {
        ctx.channel().flush();
        String path = FileProperty.copyPath + msg.getBucketId()
                + "/" + msg.getFileName() + "/" + msg.getVersion();
        FileUtil.deleteFile(path);
        FileLock.unlock(path);
        DateUtil.printDate();
    }
}

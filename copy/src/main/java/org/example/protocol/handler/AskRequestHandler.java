package org.example.protocol.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.property.FileProperty;
import org.example.protocol.Message.AskRequestMessage;
import org.example.protocol.Message.AskResponseMessage;
import org.example.util.FileUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
@ChannelHandler.Sharable
@Component
public class AskRequestHandler extends SimpleChannelInboundHandler<AskRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AskRequestMessage msg) throws Exception {
        AskResponseMessage message =
                new AskResponseMessage(msg.getBucketId(), msg.getFileName(), msg.getVersion());
        String tempPath = FileProperty.tempPath + msg.getBucketId()
                + "/" + msg.getFileName() + "/" + msg.getVersion();
        String realPath = FileProperty.realPath + msg.getBucketId()
                + "/" + msg.getFileName() + "/" + msg.getVersion();

//        String dirUri = tempPath.substring(0, tempPath.lastIndexOf('.'));
        //是否存在目录
        Path dirPath = Paths.get(tempPath);
        //存在文件的未合并分片
        if (Files.isDirectory(dirPath)) {
            message.setFileSet(new HashSet<>());
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    message.getFileSet().add(Integer.parseInt(String.valueOf(file.getFileName())));
                    return super.visitFile(file, attrs);
                }
            });
        } else {
            Path filePath = Paths.get(realPath);
            //存在文件
            if (Files.exists(filePath)) {
                String md5 = FileUtil.getMd5(realPath);
                //文件被修改
                if (!md5.equals(msg.getMd5())) {
                    FileUtil.deleteFile(realPath);
                    message.setFileSet(new HashSet<>());
                    Files.createDirectories(dirPath);
                }
            } else {
                message.setFileSet(new HashSet<>());
                Files.createDirectories(dirPath);
            }
        }
        ctx.channel().writeAndFlush(message);
    }
}

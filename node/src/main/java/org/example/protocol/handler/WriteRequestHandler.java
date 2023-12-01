package org.example.protocol.handler;


import org.example.cache.BucketCache;
import org.example.util.AsyncTask;
import org.example.util.FileUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.example.property.FileProperty;
import org.example.protocol.Message.WriteRequestMessage;
import org.example.protocol.Message.WriteResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.Cleaner;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
@Component
public class WriteRequestHandler extends SimpleChannelInboundHandler<WriteRequestMessage> {
    @Autowired
    BucketCache bucketCache;
    @Autowired
    AsyncTask asyncTask;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WriteRequestMessage msg) throws Exception {
        System.out.println(Thread.currentThread().getName());
        String uri = FileProperty.tempPath + msg.getBucketId()
                + "/" + msg.getFileName() + "/" + msg.getVersion();
        String path = uri + "/" + msg.getSequenceId();
//        File file = new File(path);
//        if (!file.exists()) {
////            System.out.println(path);
//            file.createNewFile();
//        }
//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(msg.getContent(), 0, msg.getContent().length);
//        fos.close();
        FileUtil.createFile(uri, msg.getSequenceId());
        System.out.println(path);
        FileChannel fileChannel = FileChannel.open(Paths.get(path),
                StandardOpenOption.READ, StandardOpenOption.WRITE);
        MappedByteBuffer buffer = fileChannel
                .map(FileChannel.MapMode.READ_WRITE, 0, msg.getContent().length);
//        buffer.flip();
        buffer.put(msg.getContent());
        fileChannel.close();
        Cleaner cleaner = ((sun.nio.ch.DirectBuffer) buffer).cleaner();
        if (cleaner != null) {
            cleaner.clean();
        }
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(Paths.get(uri), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });
        if (fileCount.get() == msg.getTotalNum()) {
            asyncTask.combineTask(msg, uri);
            ctx.channel().writeAndFlush
                    (new WriteResponseMessage(msg.getBucketId(), msg.getFileName(), msg.getVersion()));
        }
    }


}

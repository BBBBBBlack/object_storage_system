package org.example.protocol.handler;

import org.example.protocol.configuration.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageCodec;
import org.example.property.TCPProperty;
import org.example.protocol.Message.Message;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@ChannelHandler.Sharable
@Component
public class MessageCodec extends MessageToMessageCodec<ByteBuf, Message> {

    ConcurrentLinkedQueue<Runnable> linkedQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        linkedQueue.poll();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        //        ByteBuf buf = ctx.alloc().buffer();
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        //魔数bytes
        buf.writeBytes(TCPProperty.magicNum.getBytes(StandardCharsets.UTF_8));
        //版本int
        buf.writeInt(1);
        //序列化方式bytes
        buf.writeBytes(TCPProperty.serializable.getBytes(StandardCharsets.UTF_8));
        //指令类型int
        buf.writeInt(msg.getType());
        //请求序号int
        buf.writeInt(msg.getSequenceId());
        //获取内容的字节数组
        byte[] bytes = Serializer.serialize(msg);
        //长度int
        buf.writeInt(bytes.length);
        //写入内容bytes
        buf.writeBytes(bytes);
        out.add(buf);
        linkedQueue.add(buf::release);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        System.out.println(Thread.currentThread().getName());
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        msg.readBytes(buf, 6);
        //魔数bytes
        String magicNum = buf.toString(StandardCharsets.UTF_8);
        buf.clear();
        //版本号int
        int version = msg.readInt();
        msg.readBytes(buf, 4);
        //序列化方法bytes
        String serializable = buf.toString(StandardCharsets.UTF_8);
        buf.clear();
        //指令类型int
        int type = msg.readInt();
        //请求序号int
        int sequenceId = msg.readInt();
        //内容长度int
        int length = msg.readInt();
        byte[] bytes = new byte[length];
        msg.readBytes(bytes, 0, length);
        Class<?> messageClass = Message.getMessageClass(type);
        Object message = Serializer.deserialize(messageClass, bytes);
        out.add(message);
        buf.release();
    }

}

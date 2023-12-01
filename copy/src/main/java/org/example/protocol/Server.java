package org.example.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.protocol.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@Component
public class Server {
    private static boolean isOpen = false;
    private static Channel channel;
    @Autowired
    AskRequestHandler askRequestHandler;
    @Autowired
    AskResponseHandler askResponseHandler;
    @Autowired
    MessageCodec messageCodec;
    @Autowired
    PullColdRequestHandler pullColdRequestHandler;
    @Autowired
    PullRequestHandler pullRequestHandler;
    @Autowired
    WriteRequestHandler writeRequestHandler;
    @Autowired
    WriteResponseHandler writeResponseHandler;


    public static void shutdown(Integer port) {
        if (isOpen) {
            channel.close();
            channel = null;
            isOpen = false;
        }
    }

    public boolean createServer(Integer port) {
        if (Server.channel == null || !Server.channel.isOpen()) {
            NioEventLoopGroup boss = new NioEventLoopGroup(1);
            NioEventLoopGroup worker = new NioEventLoopGroup(2);
            Channel channel = null;
            try {
                channel = new ServerBootstrap()
                        .group(boss, worker)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                ch.pipeline()
                                        .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 1024, 22, 4, 0, 0))
                                        .addLast(messageCodec)
                                        .addLast(askRequestHandler)
                                        .addLast(new DefaultEventLoopGroup(2), writeRequestHandler)
                                        //拉取
                                        .addLast(pullRequestHandler)
                                        .addLast(pullColdRequestHandler)
                                        .addLast(askResponseHandler)
                                        .addLast(writeResponseHandler);
                            }
                        })
                        .bind(port)
                        .sync()
                        .channel();
                ChannelFuture closeFuture = channel.closeFuture();
                closeFuture.addListener((ChannelFutureListener) channelFuture -> {
                    boss.shutdownGracefully();
                    worker.shutdownGracefully();
                });
                Server.channel = channel;
                isOpen = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                isOpen = false;
                return false;
            }
        }
        return true;
    }
}

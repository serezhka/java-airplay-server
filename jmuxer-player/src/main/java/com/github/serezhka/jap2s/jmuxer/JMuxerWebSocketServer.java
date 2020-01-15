package com.github.serezhka.jap2s.jmuxer;

import com.github.serezhka.jap2s.receiver.handler.mirroring.MirrorDataConsumer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Slf4j
@Component
@ChannelHandler.Sharable
public class JMuxerWebSocketServer extends SimpleChannelInboundHandler<BinaryWebSocketFrame> implements MirrorDataConsumer {

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final Class<? extends ServerSocketChannel> serverSocketChannelClass;

    private ChannelHandlerContext ctx;

    private ByteBuf mirrorData = ByteBufAllocator.DEFAULT.buffer();
    private int nalus = 0;

    @Autowired
    public JMuxerWebSocketServer(EventLoopGroup bossGroup,
                                 EventLoopGroup workerGroup,
                                 Class<? extends ServerSocketChannel> serverSocketChannelClass) {
        this.bossGroup = bossGroup;
        this.workerGroup = workerGroup;
        this.serverSocketChannelClass = serverSocketChannelClass;
    }

    @PostConstruct
    public void init() {

        int port = 8081;
        String path = "/ws";

        new Thread(() -> {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            try {
                serverBootstrap.group(bossGroup, workerGroup)
                        .channel(serverSocketChannelClass)
                        .localAddress(new InetSocketAddress(port))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(final SocketChannel ch) {
                                ch.pipeline().addLast(
                                        new HttpServerCodec(),
                                        new HttpObjectAggregator(65536),
                                        new WebSocketServerProtocolHandler(path),
                                        JMuxerWebSocketServer.this);
                            }
                        });
                log.info("Starting websocket server on port: {}, path: {}", port, path);
                serverBootstrap.bind().sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) {
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        this.ctx = null;
    }

    @Override
    public void onData(byte[] data) {
        mirrorData.writeBytes(data);
        nalus++;
        if (nalus > 30) {
            nalus = 0;
            sendData(mirrorData);
            mirrorData = ByteBufAllocator.DEFAULT.buffer();
        }
    }

    private void sendData(ByteBuf message) {
        if (ctx != null) {
            ctx.executor().execute(() -> ctx.writeAndFlush(new BinaryWebSocketFrame(message)));
            //ctx.writeAndFlush(new TextWebSocketFrame(Base64.encode(message)));
        } else message.release();
    }
}

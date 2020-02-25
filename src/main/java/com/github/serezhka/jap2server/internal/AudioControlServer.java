package com.github.serezhka.jap2server.internal;

import com.github.serezhka.jap2server.internal.handler.audio.AudioControlHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class AudioControlServer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(AudioControlServer.class);

    @Override
    public void run() {
        var port = 4999;
        var bootstrap = new Bootstrap();
        var workerGroup = eventLoopGroup();
        var audioControlHandler = new AudioControlHandler();

        try {
            bootstrap
                    .group(workerGroup)
                    .channel(datagramChannelClass())
                    .localAddress(new InetSocketAddress(port))
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        public void initChannel(final DatagramChannel ch) {
                            ch.pipeline().addLast(audioControlHandler);
                        }
                    });
            var channelFuture = bootstrap.bind().sync();
            log.info("Audio control server listening on port: {}", port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("Audio control server interrupted");
        } finally {
            log.info("Audio control server stopped");
            workerGroup.shutdownGracefully();
        }
    }

    private EventLoopGroup eventLoopGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    private Class<? extends DatagramChannel> datagramChannelClass() {
        return Epoll.isAvailable() ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }
}
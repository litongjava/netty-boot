package com.litongjava.netty.boot.server;

import com.litongjava.tio.utils.environment.EnvUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultNettyServerBootstrap {
  private int port;
  private DefaultChannelInitializer defaultChannelInitializer;
  private EventLoopGroup parentGroup = new NioEventLoopGroup();
  private EventLoopGroup chiledGroup = new NioEventLoopGroup();
  private ChannelFuture future;

  public DefaultNettyServerBootstrap(int port, DefaultChannelInitializer channelInitializer) {
    this.port = port;
    this.defaultChannelInitializer = channelInitializer;
  }

  public void start(long startTime) {
    parentGroup = new NioEventLoopGroup();
    chiledGroup = new NioEventLoopGroup();
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(parentGroup, chiledGroup);

    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.option(ChannelOption.SO_BACKLOG, EnvUtils.getInt("NETTY_SO_BACKLOG", 1024));
    bootstrap.option(ChannelOption.TCP_NODELAY, EnvUtils.getBoolean("NETTY_TCP_NODELAY", true));
    bootstrap.childOption(ChannelOption.SO_KEEPALIVE, EnvUtils.getBoolean("NETTY_SO_KEEPALIVE", true));

    bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, EnvUtils.getInt("NETTY_CONNECT_TIMEOUT_MILLIS", 30000));

    bootstrap.childHandler(defaultChannelInitializer);

    try {
      // 异步启动 Netty 服务
      future = bootstrap.bind(port).sync();

      future.addListener(f -> {
        if (f.isSuccess()) {
          //log.info("Netty started successfully on port {}", port);
        } else {
          log.error("Failed to start Netty: {}", f);
        }
      });

      // future.channel().closeFuture().sync();
    } catch (Exception e) {
      log.error("Failed to start Netty server: {}", e.getMessage());
      e.printStackTrace();
    }
  }

  public void close() {
    log.info("Closing Netty server...");
    if (future != null) {
      try {
        future.channel().close().sync();
      } catch (InterruptedException e) {
        log.error("Error while closing the Netty server: {}", e.getMessage());
        Thread.currentThread().interrupt();
      }
    }
    if (parentGroup != null) {
      parentGroup.shutdownGracefully();
    }
    if (chiledGroup != null) {
      chiledGroup.shutdownGracefully();
    }
    log.info("Netty server closed.");
  }

  public void restart(long startTime) {
    log.info("Restarting Netty server...");
    close();
    start(startTime);
  }

  public boolean isRunning() {
    return future != null && future.channel().isActive();
  }
}

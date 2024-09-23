package com.litongjava.netty.boot.server;

import com.litongjava.netty.boot.router.HttpRequestRouter;

import lombok.Data;

@Data
public class NettyBootServer {

  private static NettyBootServer me = new NettyBootServer();
  private HttpRequestRouter httpRequestRouter;

  public static NettyBootServer me() {
    return me;
  }

  public void start(int port) {
    DefaultChannelInitializer MyChannelInitializer = new DefaultChannelInitializer();
    DefaultNettyServerBootstrap nettyServerBootstrap = new DefaultNettyServerBootstrap(port, MyChannelInitializer);
    nettyServerBootstrap.start();
  }

}

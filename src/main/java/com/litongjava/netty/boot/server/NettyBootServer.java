package com.litongjava.netty.boot.server;

import com.litongjava.netty.boot.http.DefaultHttpReqeustRouter;
import com.litongjava.netty.boot.http.HttpRequestRouter;
import com.litongjava.netty.boot.websocket.DefaultWebsocketRouter;
import com.litongjava.netty.boot.websocket.WebsocketRouter;

import lombok.Data;

@Data
public class NettyBootServer {

  private static NettyBootServer me = new NettyBootServer();
  private HttpRequestRouter httpRequestRouter = new DefaultHttpReqeustRouter();
  private WebsocketRouter websocketRouter = new DefaultWebsocketRouter();
  private DefaultNettyServerBootstrap nettyServerBootstrap; // Keep a reference

  public static NettyBootServer me() {
    return me;
  }

  public void start(int port, String contextPath) {
    DefaultChannelInitializer defaultChannelInitializer = new DefaultChannelInitializer(contextPath);
    nettyServerBootstrap = new DefaultNettyServerBootstrap(port, defaultChannelInitializer);
    nettyServerBootstrap.start();
  }

  // Add this method to stop the server
  public void stop() {
    if (nettyServerBootstrap != null) {
      nettyServerBootstrap.close();
    }
  }
}

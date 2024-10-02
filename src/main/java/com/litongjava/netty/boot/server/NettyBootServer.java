package com.litongjava.netty.boot.server;

import java.util.Iterator;
import java.util.List;

import com.litongjava.hook.HookContainer;
import com.litongjava.netty.boot.http.HttpRequestRouter;
import com.litongjava.netty.boot.websocket.WebsocketRouter;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NettyBootServer {

  private static NettyBootServer me = new NettyBootServer();

  private NettyBootServer() {
  }

  public static NettyBootServer me() {
    return me;
  }

  private HttpRequestRouter httpRequestRouter = null;
  private WebsocketRouter websocketRouter = null;
  private DefaultNettyServerBootstrap nettyServerBootstrap; // Keep a reference

  // Add this method to stop the server
  public void stop() {
    List<Runnable> destroyMethods = HookContainer.me().getDestroyMethods();
    Iterator<Runnable> iterator = destroyMethods.iterator();
    while (iterator.hasNext()) {
      Runnable runnable = iterator.next();
      iterator.remove();
      try {
        runnable.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (nettyServerBootstrap != null) {
      nettyServerBootstrap.close();
    }
    me = new NettyBootServer();
  }

  public void start(int port, String contextPath, long startTime) {
    DefaultChannelInitializer defaultChannelInitializer = new DefaultChannelInitializer(contextPath);
    nettyServerBootstrap = new DefaultNettyServerBootstrap(port, defaultChannelInitializer);
    nettyServerBootstrap.start(startTime);
  }
  
  public void restart(long startTime) {
    nettyServerBootstrap.restart(startTime);
  }

  public boolean isRunning() {
    return nettyServerBootstrap.isRunning();
  }
 
}

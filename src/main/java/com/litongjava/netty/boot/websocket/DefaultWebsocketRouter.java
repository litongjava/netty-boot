package com.litongjava.netty.boot.websocket;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class DefaultWebsocketRouter implements WebsocketRouter {
  private Map<String, Supplier<SimpleChannelInboundHandler<WebSocketFrame>>> routeMapping = new ConcurrentHashMap<>();

  @Override
  public void add(String path, Supplier<SimpleChannelInboundHandler<WebSocketFrame>> handler) {
    routeMapping.put(path, handler);
  }

  @Override
  public Supplier<SimpleChannelInboundHandler<WebSocketFrame>> find(String path) {
    Supplier<SimpleChannelInboundHandler<WebSocketFrame>> handler = routeMapping.get(path);
    if (handler != null) {
      return handler;
    }

    // Wildcard matching
    Set<Map.Entry<String, Supplier<SimpleChannelInboundHandler<WebSocketFrame>>>> entrySet = routeMapping.entrySet();

    for (Map.Entry<String, Supplier<SimpleChannelInboundHandler<WebSocketFrame>>> entry : entrySet) {
      String key = entry.getKey();

      if (key.endsWith("/*")) {
        String baseRoute = key.substring(0, key.length() - 1);
        if (path.startsWith(baseRoute)) {
          return entry.getValue();
        }
      } else if (key.endsWith("/**")) {
        String baseRoute = key.substring(0, key.length() - 2);
        if (path.startsWith(baseRoute)) {
          return entry.getValue();
        }
      }
    }

    return null;
  }

  @Override
  public Map<String, Supplier<SimpleChannelInboundHandler<WebSocketFrame>>> mapping() {
    return routeMapping;
  }
}

package com.litongjava.netty.boot.websocket;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultWebsocketRouter implements WebsocketRouter {
  private Map<String, WebSocketFrameHandler> routeMapping = new ConcurrentHashMap<>();

  @Override
  public void add(String path, WebSocketFrameHandler handler) {
    routeMapping.put(path, handler);
  }

  @Override
  public WebSocketFrameHandler find(String path) {
    WebSocketFrameHandler handler = routeMapping.get(path);
    if (handler != null) {
      return handler;
    }

    // Wildcard matching
    Set<Map.Entry<String, WebSocketFrameHandler>> entrySet = routeMapping.entrySet();

    for (Map.Entry<String, WebSocketFrameHandler> entry : entrySet) {
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
  public Map<String, WebSocketFrameHandler> mapping() {
    return routeMapping;
  }
}

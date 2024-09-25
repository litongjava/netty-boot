package com.litongjava.netty.boot.websocket;

public interface WebsocketRouter {

    /**
     * Add a route
     * @param path
     * @param handler
     */
    void add(String path, WebSocketFrameHandler handler);

    /**
     * Find a handler for the given path
     * @param path
     * @return
     */
    WebSocketFrameHandler find(String path);
}

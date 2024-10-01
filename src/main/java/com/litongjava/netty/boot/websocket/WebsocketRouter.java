package com.litongjava.netty.boot.websocket;

import java.util.Map;

public interface WebsocketRouter {

    /**
     * Add a route
     * @param path
     * @param handler
     */
    public void add(String path, WebSocketFrameHandler handler);

    /**
     * Find a handler for the given path
     * @param path
     * @return
     */
    public WebSocketFrameHandler find(String path);
    
    /**
     * @return
     */
    public Map<String, WebSocketFrameHandler> mapping(); 
}


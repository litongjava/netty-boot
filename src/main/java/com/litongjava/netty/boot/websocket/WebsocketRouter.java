package com.litongjava.netty.boot.websocket;

import java.util.Map;
import java.util.function.Supplier;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public interface WebsocketRouter {

    /**
     * Add a route
     * @param path
     * @param handler
     */
    public void add(String path, Supplier<SimpleChannelInboundHandler<WebSocketFrame>> handlerSupplier);

    /**
     * Find a handler for the given path
     * @param path
     * @return
     */
    public Supplier<SimpleChannelInboundHandler<WebSocketFrame>> find(String path);
    
    /**
     * @return
     */
    public Map<String, Supplier<SimpleChannelInboundHandler<WebSocketFrame>>> mapping(); 
}


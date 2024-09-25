package com.litongjava.netty.boot.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

@FunctionalInterface
public interface WebSocketFrameHandler {
  void handle(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception;
}

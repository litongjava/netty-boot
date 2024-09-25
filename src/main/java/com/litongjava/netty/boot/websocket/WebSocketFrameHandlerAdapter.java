// WebSocketFrameHandlerAdapter.java
package com.litongjava.netty.boot.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketFrameHandlerAdapter extends SimpleChannelInboundHandler<WebSocketFrame> {

  private final WebSocketFrameHandler frameHandler;

  public WebSocketFrameHandlerAdapter(WebSocketFrameHandler frameHandler) {
    this.frameHandler = frameHandler;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
    frameHandler.handle(ctx, frame);
  }
}

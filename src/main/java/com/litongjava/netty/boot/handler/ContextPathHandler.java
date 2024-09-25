package com.litongjava.netty.boot.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

public class ContextPathHandler extends ChannelInboundHandlerAdapter {

  private final String contextPath;

  public ContextPathHandler(String contextPath) {
    this.contextPath = contextPath;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (contextPath != null) {
      if (msg instanceof HttpRequest) {
        HttpRequest request = (HttpRequest) msg;
        String uri = request.uri();

        if (uri.startsWith(contextPath)) {
          // Strip the contextPath from the URI
          String newUri = uri.substring(contextPath.length());
          if (newUri.isEmpty()) {
            newUri = "/";
          }

          // set url
          request.setUri(newUri);
          // Pass the new request down the pipeline
          super.channelRead(ctx, request);
        } else {
          super.channelRead(ctx, msg);
        }
      } else {
        // Pass other messages down the pipeline
        super.channelRead(ctx, msg);
      }
    } else {
      super.channelRead(ctx, msg);
    }
  }
}

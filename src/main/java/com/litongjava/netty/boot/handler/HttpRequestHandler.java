package com.litongjava.netty.boot.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

@FunctionalInterface
public interface HttpRequestHandler {
  FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest  httpRequest) throws Exception;
}

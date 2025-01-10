package com.litongjava.netty.boot.http;

import com.litongjava.netty.boot.adapter.HttpRequest;
import com.litongjava.netty.boot.adapter.HttpResponse;

import io.netty.channel.ChannelHandlerContext;

@FunctionalInterface
public interface HttpRequestHandler {
  HttpResponse handle(ChannelHandlerContext ctx, HttpRequest  httpRequest) throws Exception;
}

package com.litongjava.netty.boot.server;

import com.litongjava.netty.boot.adapter.DefaultNettyHandlerAdapter;
import com.litongjava.netty.boot.handler.ContextPathHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final String contextPath;

  public DefaultChannelInitializer(String contextPath) {
    this.contextPath = contextPath;
  }

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    ChannelPipeline pipeline = socketChannel.pipeline();

    // Add HTTP server codec
    pipeline.addLast("httpServerCodec", new HttpServerCodec());
    pipeline.addLast(new ChunkedWriteHandler());
    // Add HTTP object aggregator (optional)
    pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(65536));
    pipeline.addLast("contextPathHandler", new ContextPathHandler(contextPath));
    pipeline.addLast(new DefaultNettyHandlerAdapter());
  }
}

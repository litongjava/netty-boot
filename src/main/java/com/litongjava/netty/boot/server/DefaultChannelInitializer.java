package com.litongjava.netty.boot.server;

import com.litongjava.constatns.ServerConfigKeys;
import com.litongjava.netty.boot.adapter.DefaultNettyHandlerAdapter;
import com.litongjava.netty.boot.handler.ContextPathHandler;
import com.litongjava.tio.utils.environment.EnvUtils;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

  private final String contextPath;
  private int readerTimeout = EnvUtils.getInt("netty.reader.idle.time.seconds", 60);
  private int writerTimeout = EnvUtils.getInt("netty.writer.idle.time.seconds", 60);
  private int allTimeout = EnvUtils.getInt("netty.all.idle.time.seconds", 0);

  public DefaultChannelInitializer(String contextPath) {
    this.contextPath = contextPath;
  }

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    ChannelPipeline pipeline = socketChannel.pipeline();
    // Read timeout of 60s, write timeout of 30s
    pipeline.addLast(new IdleStateHandler(readerTimeout, writerTimeout, allTimeout));

    // Add HTTP server codec
    pipeline.addLast("httpServerCodec", new HttpServerCodec());
    pipeline.addLast(new ChunkedWriteHandler());
    // Add HTTP object aggregator (optional)
    int maxContentLength = EnvUtils.getInt(ServerConfigKeys.HTTP_MULTIPART_MAX_REQUEST_SIZE, 10 * 1024 * 1024);
    pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(maxContentLength));

    if (contextPath != null) {
      pipeline.addLast("contextPathHandler", new ContextPathHandler(contextPath));
    }
    pipeline.addLast(new DefaultNettyHandlerAdapter());
  }
}

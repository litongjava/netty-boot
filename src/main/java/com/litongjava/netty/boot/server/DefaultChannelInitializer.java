package com.litongjava.netty.boot.server;

import com.litongjava.constants.ServerConfigKeys;
import com.litongjava.netty.boot.adapter.DefaultNettyHandlerAdapter;
import com.litongjava.netty.boot.handler.ContextPathHandler;
import com.litongjava.netty.boot.websocket.WebSocketHandshakeHandler;
import com.litongjava.netty.boot.websocket.WebsocketRouter;
import com.litongjava.tio.utils.environment.EnvUtils;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> {

  private int readerTimeout = EnvUtils.getInt("netty.reader.idle.time.seconds", 60);
  private int writerTimeout = EnvUtils.getInt("netty.writer.idle.time.seconds", 60);
  private int allTimeout = EnvUtils.getInt("netty.all.idle.time.seconds", 0);

  private final String contextPath;
  private WebsocketRouter websocketRouter;
  //private SslContext sslContext;
  private SslContext sslContext;

  public DefaultChannelInitializer(String contextPath, WebsocketRouter websocketRouter) {
    this.contextPath = contextPath;
    this.websocketRouter = websocketRouter;
  }

  //  public DefaultChannelInitializer(String contextPath, WebsocketRouter websocketRouter, SslContext sslContext) {
  //    this.contextPath = contextPath;
  //    this.websocketRouter = websocketRouter;
  //    this.sslContext = sslContext;
  //  }

  public DefaultChannelInitializer(String contextPath, WebsocketRouter websocketRouter, SslContext sslContext) {
    this.contextPath = contextPath;
    this.websocketRouter = websocketRouter;
    this.sslContext = sslContext;
  }

  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    ChannelPipeline pipeline = socketChannel.pipeline();
    if (sslContext != null) {
      pipeline.addLast(sslContext.newHandler(socketChannel.alloc()));
    }
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
    // WebSocket
    pipeline.addLast(new WebSocketHandshakeHandler(websocketRouter));
    // http
    pipeline.addLast(new DefaultNettyHandlerAdapter());
  }
}

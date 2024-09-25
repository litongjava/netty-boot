package com.litongjava.netty.boot.adapter;

import com.litongjava.netty.boot.http.HttpRequestHandler;
import com.litongjava.netty.boot.http.HttpRequestRouter;
import com.litongjava.netty.boot.server.NettyBootServer;
import com.litongjava.netty.boot.websocket.WebSocketFrameHandler;
import com.litongjava.netty.boot.websocket.WebSocketFrameHandlerAdapter;
import com.litongjava.netty.boot.websocket.WebsocketRouter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class DefaultNettyHandlerAdapter extends SimpleChannelInboundHandler<Object> {

  private WebSocketServerHandshaker handshaker;

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      handleHttpRequest(ctx, (FullHttpRequest) msg);
    } else if (msg instanceof WebSocketFrame) {
      // Delegate to the next handler in the pipeline
      ctx.fireChannelRead(msg);
    }
  }

  private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
    // Check for WebSocket upgrade
    if ("websocket".equalsIgnoreCase(request.headers().get(HttpHeaderNames.UPGRADE))) {
      // Handle WebSocket handshake
      WebsocketRouter websocketRouter = NettyBootServer.me().getWebsocketRouter();
      String uri = request.uri();
      WebSocketFrameHandler wsHandler = websocketRouter.find(uri);

      if (wsHandler == null) {
        // No handler found, send 404
        sendHttpResponse(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
        return;
      }

      String webSocketLocation = getWebSocketLocation(request);
      WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(webSocketLocation, null, true);
      handshaker = wsFactory.newHandshaker(request);
      if (handshaker == null) {
        // Version not supported
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
      } else {
        ChannelPipeline pipeline = ctx.pipeline();

        // Replace HTTP handler with WebSocket frame handler
        pipeline.replace(this, "websocketFrameHandler", new WebSocketFrameHandlerAdapter(wsHandler));

        handshaker.handshake(ctx.channel(), request);
      }
      return;
    }

    // Handle HTTP request
    String uri = request.uri();
    HttpRequestRouter httpRequestRouter = NettyBootServer.me().getHttpRequestRouter();

    HttpRequestHandler httpRequestHandler = httpRequestRouter.find(uri);
    FullHttpResponse response;
    if (httpRequestHandler != null) {
      response = httpRequestHandler.handle(ctx, request);
    } else {
      String responseContent = "404 Not Found";
      ByteBuf byteBuffer = Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8);
      response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, byteBuffer);
    }

    boolean keepAlive = HttpUtil.isKeepAlive(request);
    if (keepAlive) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      HttpUtil.setContentLength(response, response.content().readableBytes());
    }

    // Write response
    ChannelFuture f = ctx.writeAndFlush(response);

    if (!keepAlive) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private static String getWebSocketLocation(FullHttpRequest req) {
    String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
    return "ws://" + location;
  }

  private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
    // Generate error page if response status code is not OK (200)
    if (res.status().code() != HttpResponseStatus.OK.code()) {
      ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
      res.content().writeBytes(buf);
      buf.release();
      HttpUtil.setContentLength(res, res.content().readableBytes());
    }

    // Send the response and close the connection if necessary
    ChannelFuture f = ctx.writeAndFlush(res);
    if (!HttpUtil.isKeepAlive(req) || res.status().code() != HttpResponseStatus.OK.code()) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }
}

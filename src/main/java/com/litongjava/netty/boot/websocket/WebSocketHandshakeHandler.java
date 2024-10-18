package com.litongjava.netty.boot.websocket;

import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

public class WebSocketHandshakeHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  private final WebsocketRouter websocketRouter;

  public WebSocketHandshakeHandler(WebsocketRouter websocketRouter) {
    this.websocketRouter = websocketRouter;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
    if (HttpMethod.GET.equals(req.method()) && "websocket".equalsIgnoreCase(req.headers().get(HttpHeaderNames.UPGRADE))) {
      // 根据请求路径选择对应的WebSocket处理器
      String uri = req.uri();
      Supplier<SimpleChannelInboundHandler<WebSocketFrame>> handlerSupplier = websocketRouter.mapping().get(uri);

      if (handlerSupplier != null) {
        // WebSocket握手
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
          WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
          handshaker.handshake(ctx.channel(), req);

          // 替换 HTTP 处理器为 WebSocket 处理器
          ctx.pipeline().replace(this, "websocketHandler", handlerSupplier.get());
        }
      } else {
        // 如果找不到对应的 WebSocket 路径处理器，则发送 404
        sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
      }
    } else {
      ctx.fireChannelRead(req.retain());
    }
  }

  private static String getWebSocketLocation(FullHttpRequest req) {
    String location = req.headers().get(HttpHeaderNames.HOST) + req.uri();
    return "ws://" + location;
  }

  private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
    // 返回响应给客户端
    if (res.status().code() != 200) {
      ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
      res.content().writeBytes(buf);
      buf.release();
      HttpUtil.setContentLength(res, res.content().readableBytes());
    }
    ChannelFuture f = ctx.channel().writeAndFlush(res);
    if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }
}

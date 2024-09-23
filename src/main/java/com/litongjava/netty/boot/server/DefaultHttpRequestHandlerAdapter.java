package com.litongjava.netty.boot.server;

import com.litongjava.netty.boot.handler.HttpRequestHandler;
import com.litongjava.netty.boot.router.HttpRequestRouter;

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
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class DefaultHttpRequestHandlerAdapter extends SimpleChannelInboundHandler<FullHttpRequest> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
    String uri = request.uri();
    HttpRequestRouter httpRequestRouter = NettyBootServer.me().getHttpRequestRouter();

    HttpRequestHandler httpRequestHandler = httpRequestRouter.find(uri);
    FullHttpResponse response = null;
    if (httpRequestHandler != null) {
      response = (FullHttpResponse) httpRequestHandler.handle(ctx, request);
    } else {
      String responseContent = "404";
      ByteBuf byteBuffer = Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8);
      response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, byteBuffer);
    }

    boolean keepAlive = HttpUtil.isKeepAlive(request);
    if (keepAlive) {
      response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
      HttpUtil.setContentLength(response, response.content().readableBytes());
    }

    // 写出响应
    ChannelFuture f = ctx.writeAndFlush(response);

    if (!keepAlive) {
      // 如果不保持连接，在写操作完成后关闭连接
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}

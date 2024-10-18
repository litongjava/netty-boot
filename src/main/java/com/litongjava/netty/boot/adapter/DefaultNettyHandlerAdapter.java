package com.litongjava.netty.boot.adapter;

import com.litongjava.constatns.ServerConfigKeys;
import com.litongjava.netty.boot.context.NettyRequestContext;
import com.litongjava.netty.boot.http.HttpRequestHandler;
import com.litongjava.netty.boot.http.HttpRequestRouter;
import com.litongjava.netty.boot.inteceptor.HttpRequestInterceptor;
import com.litongjava.netty.boot.listener.ChannelConnectionListener;
import com.litongjava.netty.boot.server.NettyBootServer;
import com.litongjava.tio.utils.environment.EnvUtils;

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
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultNettyHandlerAdapter extends SimpleChannelInboundHandler<Object> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      handleHttpRequest(ctx, (FullHttpRequest) msg);
    } else if (msg instanceof WebSocketFrame) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
    }
  }

  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {

  }

  private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
    NettyBootServer nettyBootServer = NettyBootServer.me();

    // Handle HTTP request
    String uri = request.uri();
    HttpMethod method = request.method();
    HttpVersion protocolVersion = request.protocolVersion();
    String requestLine = method.toString() + " " + uri + " " + protocolVersion.toString();

    // print url
    if (EnvUtils.getBoolean(ServerConfigKeys.SERVER_HTTP_REQUEST_PRINT_URL)) {
      log.info("access:{}", requestLine);
    }

    boolean printReport = EnvUtils.getBoolean(ServerConfigKeys.SERVER_HTTP_REQUEST_PRINTREPORT, false);
    HttpRequestInterceptor httpRequestInterceptorDispather = nettyBootServer.getHttpRequestInterceptorDispather();
    FullHttpResponse response = null;
    long start = System.currentTimeMillis();
    try {
      // Interceptor
      NettyRequestContext.hold(request);
      response = httpRequestInterceptorDispather.before(request);
      if (response != null) {
        if (printReport) {
          if (log.isInfoEnabled()) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("\n-----------httpRequestInterceptor report---------------------\n");

            stringBuffer.append("request:").append(requestLine).append("\n")//
                .append("httpServerInterceptor:" + httpRequestInterceptorDispather).append("\n")//
                .append("response:" + response).append("\n")//
                .append("\n");

            log.info(stringBuffer.toString());

          }
        }
        writeHttpResponse(ctx, request, response);
        return;
      }

      HttpRequestRouter httpRequestRouter = nettyBootServer.getHttpRequestRouter();

      HttpRequestHandler httpRequestHandler = httpRequestRouter.find(uri);
      if (httpRequestHandler != null) {
        try {
          response = httpRequestHandler.handle(ctx, request);
        } catch (Exception e) {
          e.printStackTrace();
          String responseContent = "500 Internal Sever Error";
          ByteBuf byteBuffer = Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8);
          response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, byteBuffer);
        }
      } else {
        String responseContent = "404 Not Found";
        ByteBuf byteBuffer = Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8);
        response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, byteBuffer);
      }
    } catch (Exception e) {
      e.printStackTrace();
      String responseContent = "500 Internal Sever Error";
      ByteBuf byteBuffer = Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8);
      response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, byteBuffer);
    } finally {
      long end = System.currentTimeMillis();
      httpRequestInterceptorDispather.after(request, response, (end - start));
      NettyRequestContext.release();
    }
    writeHttpResponse(ctx, request, response);
  }

  private void writeHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
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

  /**
   * 当客户端连接服务端之后（打开链接） 获取客户端的channel，并且放到channelGroup中去管理
   * 
   * @param ctx
   * @throws Exception
   */
  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    ChannelConnectionListener channelConnectListener = NettyBootServer.me().getChannelConnectionListener();
    if (channelConnectListener != null) {
      channelConnectListener.handlerAdded(ctx);
    }
  }

  /**
   * 客户端断开
   * 
   * @param ctx
   * @throws Exception
   */
  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    ChannelConnectionListener channelConnectListener = NettyBootServer.me().getChannelConnectionListener();
    if (channelConnectListener != null) {
      channelConnectListener.handlerRemoved(ctx);
    }
  }
}

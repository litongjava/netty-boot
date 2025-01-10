package com.litongjava.netty.boot.context;

import com.litongjava.netty.boot.adapter.HttpRequest;
import com.litongjava.netty.boot.adapter.HttpResponse;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class NettyRequestContext {
  private static ThreadLocal<NettyHttpAction> requests = new ThreadLocal<>();

  public static void hold(HttpRequest httpRequest) {
    HttpResponse httpResponse = new HttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    
    requests.set(new NettyHttpAction(httpRequest, httpResponse));
  }

  public static HttpRequest getRequest() {
    return requests.get().getRequest();
  }

  public static void release() {
    requests.remove();
  }

  public static HttpResponse getResponse() {
    return requests.get().getResponse();
  }

  public static void setUserId(Object userId) {
    getRequest().headers().set("userId", userId);
  }

  public static Object getUserId() {
    return getRequest().headers().get("userId");
  }

  public static String getUserIdString() {
    return (String) getRequest().headers().get("userId");
  }

  public static Long getUserIdLong() {
    return Long.parseLong(getRequest().headers().get("userId"));
  }
}

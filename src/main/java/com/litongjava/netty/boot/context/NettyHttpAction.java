package com.litongjava.netty.boot.context;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class NettyHttpAction {
  private FullHttpRequest request;
  private FullHttpResponse response;

  public NettyHttpAction() {
    super();
  }

  public NettyHttpAction(FullHttpRequest request, FullHttpResponse response) {
    super();
    this.request = request;
    this.response = response;
  }

  public FullHttpRequest getRequest() {
    return request;
  }

  public void setRequest(FullHttpRequest request) {
    this.request = request;
  }

  public FullHttpResponse getResponse() {
    return response;
  }

  public void setResponse(FullHttpResponse response) {
    this.response = response;
  }

}

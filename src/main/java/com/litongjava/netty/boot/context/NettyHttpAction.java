package com.litongjava.netty.boot.context;

import com.litongjava.netty.boot.adapter.HttpRequest;
import com.litongjava.netty.boot.adapter.HttpResponse;

public class NettyHttpAction {
  private HttpRequest request;
  private HttpResponse response;

  public NettyHttpAction() {
    super();
  }

  public NettyHttpAction(HttpRequest request, HttpResponse response) {
    super();
    this.request = request;
    this.response = response;
  }

  public HttpRequest getRequest() {
    return request;
  }

  public void setRequest(HttpRequest request) {
    this.request = request;
  }

  public HttpResponse getResponse() {
    return response;
  }

  public void setResponse(HttpResponse response) {
    this.response = response;
  }

}

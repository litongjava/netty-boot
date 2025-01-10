package com.litongjava.netty.boot.adapter;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpRequest {
  private FullHttpRequest request;

  public HttpRequest(FullHttpRequest request) {
    this.request = request;
  }

  public HttpMethod method() {
    return this.method();
  }

  public HttpRequest setMethod(HttpMethod method) {
    this.request.setMethod(method);
    return this;
  }

  public HttpHeaders headers() {
    return request.headers();
  }

  public HttpVersion protocolVersion() {
    return request.protocolVersion();
  }

  public HttpMessage setProtocolVersion(HttpVersion version) {
    return request.setProtocolVersion(version);
  }

  public String uri() {
    return request.uri();
  }

  public ByteBuf content() {
    return request.content();
  }
}

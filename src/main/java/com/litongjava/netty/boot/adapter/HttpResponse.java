package com.litongjava.netty.boot.adapter;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HttpResponse {

  private FullHttpResponse response;

  public HttpResponse(FullHttpResponse response) {
    this.response = response;
  }

  public HttpResponse(HttpVersion http11, HttpResponseStatus ok) {
    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    this.response = response;
  }

  public HttpVersion protocolVersion() {
    return response.protocolVersion();
  }

  public HttpMessage setProtocolVersion(HttpVersion version) {
    return response.setProtocolVersion(version);
  }

  public HttpHeaders headers() {
    return response.headers();
  }

  public HttpResponse(HttpVersion http11, HttpResponseStatus internalServerError, ByteBuf byteBuffer) {
    this.response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, byteBuffer);
  }

  public ByteBuf content() {
    return this.response.content();
  }

}

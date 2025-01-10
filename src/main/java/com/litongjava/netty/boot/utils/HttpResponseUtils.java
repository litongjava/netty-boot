package com.litongjava.netty.boot.utils;

import com.litongjava.netty.boot.adapter.HttpResponse;
import com.litongjava.tio.utils.json.JsonUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class HttpResponseUtils {

  public static HttpResponse txt(String responseContent) {
    ByteBuf byteBuffer = Unpooled.copiedBuffer(responseContent, CharsetUtil.UTF_8);
    return ok(byteBuffer);
  }

  private static HttpResponse ok(ByteBuf byteBuffer) {
    return new HttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuffer);
  }

  public static HttpResponse json(Object object) {
    // 将对象转换为 JSON 字符串
    String json = JsonUtils.toJson(object);
    // 创建包含 JSON 数据的 ByteBuf
    ByteBuf byteBuffer = Unpooled.copiedBuffer(json, CharsetUtil.UTF_8);

    // 创建响应对象
    HttpResponse response = ok(byteBuffer);

    // 设置响应头的内容类型为 application/json
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + "; charset=UTF-8");

    // 返回构建的 JSON 响应
    return response;
  }
}

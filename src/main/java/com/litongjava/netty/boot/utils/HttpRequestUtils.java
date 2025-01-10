package com.litongjava.netty.boot.utils;

import java.util.List;

import com.litongjava.netty.boot.adapter.HttpRequest;
import com.litongjava.tio.utils.json.JsonUtils;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

public class HttpRequestUtils {

  public static <T> T parseJson(HttpRequest httpRequest, Class<T> clazz) {
    // 获取请求内容的 ByteBuf
    ByteBuf content = httpRequest.content();

    // 将 ByteBuf 转换为字符串
    String jsonString = content.toString(CharsetUtil.UTF_8);

    // 通过 JsonUtils 将字符串转换为指定类型的 Java 对象
    return JsonUtils.parse(jsonString, clazz);
  }

  public static <T> List<T> parseJsonArray(HttpRequest httpRequest, Class<T> clazz) {
    // 获取请求内容的 ByteBuf
    ByteBuf content = httpRequest.content();

    // 将 ByteBuf 转换为字符串
    String jsonString = content.toString(CharsetUtil.UTF_8);

    // 通过 JsonUtils 将字符串转换为指定类型的 Java 对象
    return JsonUtils.parseArray(jsonString, clazz);
  }

  public static String getHttpRequestAsString(HttpRequest httpRequest) {
    StringBuffer requestBuffer = new StringBuffer();

    // 1. Append the request line (e.g., POST /api/login HTTP/1.1)
    String method = httpRequest.method().name();
    String uri = httpRequest.uri();
    String protocolVersion = httpRequest.protocolVersion().text();
    requestBuffer.append(method).append(" ").append(uri).append(" ").append(protocolVersion).append("\r\n");

    // 2. Append the headers
    httpRequest.headers().forEach(header -> {
      requestBuffer.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
    });

    // Add a blank line to separate headers from the body
    requestBuffer.append("\r\n");

    // 3. Append the request body (if it exists)
    ByteBuf content = httpRequest.content();
    if (content.isReadable()) {
      String requestBody = content.toString(CharsetUtil.UTF_8);
      requestBuffer.append(requestBody);
    }

    // Return the complete HTTP request as a string
    return requestBuffer.toString();
  }
}

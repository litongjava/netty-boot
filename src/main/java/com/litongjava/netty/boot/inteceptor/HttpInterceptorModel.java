package com.litongjava.netty.boot.inteceptor;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpInterceptorModel {
  private String name;
  private List<String> allowedUrls; // 允许的地址
  private List<String> blockedUrls; // 被拦截的地址
  private HttpRequestInterceptor interceptor;

  public HttpInterceptorModel addAllowedUrl(String string) {
    if (allowedUrls == null) {
      allowedUrls = new ArrayList<>();
    }
    allowedUrls.add(string);
    return this;
  }

  public HttpInterceptorModel addAlloweeUrls(String... strings) {
    if (allowedUrls == null) {
      allowedUrls = new ArrayList<>();
    }
    for (String string : strings) {
      allowedUrls.add(string);
    }
    return this;
  }

  public HttpInterceptorModel addBlockeUrl(String string) {
    if (blockedUrls == null) {
      blockedUrls = new ArrayList<>();
    }
    blockedUrls.add(string);
    return this;
  }

  public HttpInterceptorModel addBlockeUrls(String... strings) {
    if (blockedUrls == null) {
      blockedUrls = new ArrayList<>();
    }
    for (String string : strings) {
      blockedUrls.add(string);
    }
    return this;
  }
}

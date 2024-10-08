package com.litongjava.netty.boot.http;

import java.util.Map;

public interface HttpRequestRouter {

  /**
   * 添加路由
   * @param path
   * @param handler
   */
  public void add(String path, HttpRequestHandler handler);

  /**
   * 查找路由
   * @param path
   * @return
   */
  public HttpRequestHandler find(String path);
  
  
  public Map<String, HttpRequestHandler> mapping();
}

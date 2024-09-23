package com.litongjava.netty.boot.router;

import com.litongjava.netty.boot.handler.HttpRequestHandler;

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
}
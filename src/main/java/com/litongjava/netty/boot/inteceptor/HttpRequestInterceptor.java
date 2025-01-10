package com.litongjava.netty.boot.inteceptor;

import com.litongjava.netty.boot.adapter.HttpRequest;
import com.litongjava.netty.boot.adapter.HttpResponse;

/**
 * @author Tong Li
 */
public interface HttpRequestInterceptor {


  /**
   * 
   * @param request
   * @return
   * @throws Exception
   */
  public HttpResponse before(HttpRequest request) throws Exception;


  /**
   * 
   * @param request
   * @param response
   * @param cost
   * @throws Exception
   */
  public void after(HttpRequest request,HttpResponse response, long cost) throws Exception;
}

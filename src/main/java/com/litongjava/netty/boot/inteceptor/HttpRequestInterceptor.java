package com.litongjava.netty.boot.inteceptor;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

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
  public FullHttpResponse before(FullHttpRequest request) throws Exception;


  /**
   * 
   * @param request
   * @param response
   * @param cost
   * @throws Exception
   */
  public void after(FullHttpRequest request,FullHttpResponse response, long cost) throws Exception;
}

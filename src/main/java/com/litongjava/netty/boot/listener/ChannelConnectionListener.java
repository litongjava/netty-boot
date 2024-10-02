package com.litongjava.netty.boot.listener;

import io.netty.channel.ChannelHandlerContext;

public interface ChannelConnectionListener {
  /**
   * 客户端连接
   * @param ctx
   * @throws Exception
   */
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception;

  /**
   * 客户端断开
   * 
   * @param ctx
   * @throws Exception
   */
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception;
}

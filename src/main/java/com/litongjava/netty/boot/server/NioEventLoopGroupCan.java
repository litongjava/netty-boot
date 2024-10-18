package com.litongjava.netty.boot.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public interface NioEventLoopGroupCan {
  public EventLoopGroup parentGroup = new NioEventLoopGroup();
  public EventLoopGroup chiledGroup = new NioEventLoopGroup();
}

package com.litongjava.netty.boot;

import com.litongjava.context.BootConfiguration;
import com.litongjava.context.Context;
import com.litongjava.netty.boot.context.NettyApplicationContext;

public class NettyApplication {
  public static Context run(Class<?> primarySource, String[] args) {
    return run(new Class<?>[] { primarySource }, args);
  }

  public static Context run(Class<?>[] primarySources, String[] args) {
    Context context = new NettyApplicationContext();
    return context.run(primarySources, args);
  }

  public static Context run(Class<?> primarySource, BootConfiguration tioBootConfiguration, String[] args) {
    return run(new Class<?>[] { primarySource }, tioBootConfiguration, args);
  }

  public static Context run(Class<?>[] primarySources, BootConfiguration tioBootConfiguration, String[] args) {
    Context context = new NettyApplicationContext();
    return context.run(primarySources, tioBootConfiguration, args);
  }
}

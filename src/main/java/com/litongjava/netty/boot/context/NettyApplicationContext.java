package com.litongjava.netty.boot.context;

import java.util.List;

import com.litongjava.constatns.ServerConfigKeys;
import com.litongjava.context.BootConfiguration;
import com.litongjava.context.Context;
import com.litongjava.netty.boot.server.NettyBootServer;
import com.litongjava.tio.utils.environment.EnvUtils;

public class NettyApplicationContext implements Context {

  private NettyBootServer nettyBootServer = NettyBootServer.me();
  private int port = 0;

  public Context run(Class<?>[] primarySources, String[] args) {
    return run(primarySources, null, args);
  }

  public Context run(Class<?>[] primarySources, BootConfiguration tioBootConfiguration, String[] args) {
    long scanClassStartTime = 0L;
    long scanClassEndTime = 0L;
    long configStartTime = 0L;
    long configEndTimeTime = 0L;

    long initServerStartTime = System.currentTimeMillis();
    EnvUtils.buildCmdArgsMap(args);
    EnvUtils.load();
    // port and contextPath
    port = EnvUtils.getInt(ServerConfigKeys.SERVER_PORT, 80);
    String contextPath = EnvUtils.get(ServerConfigKeys.SERVER_CONTEXT_PATH);

    nettyBootServer.start(port, contextPath);
    return null;
  }

  public void initAnnotation(List<Class<?>> scannedClasses) {
  }

  public boolean isRunning() {
    return false;
  }

  public void close() {
    nettyBootServer.stop();
  }

  public void restart(Class<?>[] primarySources, String[] args) {
    close();
    run(primarySources, null, args);

  }

  public int getPort() {
    return port;
  }

}

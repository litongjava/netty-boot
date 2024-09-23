package com.litongjava.netty.boot.context;

import java.util.List;

import com.litongjava.constatns.ServerConfigKeys;
import com.litongjava.context.BootConfiguration;
import com.litongjava.context.Context;
import com.litongjava.netty.boot.server.NettyBootServer;
import com.litongjava.tio.utils.environment.EnvUtils;

public class NettyApplicationContext implements Context {

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
    int port = EnvUtils.getInt(ServerConfigKeys.SERVER_PORT, 80);
    String contextPath = EnvUtils.get(ServerConfigKeys.SERVER_CONTEXT_PATH);

    NettyBootServer tioBootServer = NettyBootServer.me();
    tioBootServer.start(port);
    return null;
  }

  public void initAnnotation(List<Class<?>> scannedClasses) {
    // TODO Auto-generated method stub

  }

  public boolean isRunning() {
    // TODO Auto-generated method stub
    return false;
  }

  public void close() {
    // TODO Auto-generated method stub

  }

  public void restart(Class<?>[] primarySources, String[] args) {
    // TODO Auto-generated method stub

  }

  public int getPort() {
    // TODO Auto-generated method stub
    return 0;
  }

}

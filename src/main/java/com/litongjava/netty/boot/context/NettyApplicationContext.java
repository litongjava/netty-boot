package com.litongjava.netty.boot.context;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.litongjava.annotation.AImport;
import com.litongjava.constatns.AopClasses;
import com.litongjava.constatns.ServerConfigKeys;
import com.litongjava.context.BootConfiguration;
import com.litongjava.context.Context;
import com.litongjava.jfinal.aop.process.BeanProcess;
import com.litongjava.jfinal.aop.process.BeforeStartConfigurationProcess;
import com.litongjava.jfinal.aop.scaner.ComponentScanner;
import com.litongjava.netty.boot.http.DefaultHttpReqeustRouter;
import com.litongjava.netty.boot.http.HttpRequestHandler;
import com.litongjava.netty.boot.http.HttpRequestRouter;
import com.litongjava.netty.boot.inteceptor.DefaultHttpRequestInterceptorDispatcher;
import com.litongjava.netty.boot.inteceptor.HttpRequestInterceptor;
import com.litongjava.netty.boot.server.NettyBootServer;
import com.litongjava.netty.boot.websocket.DefaultWebsocketRouter;
import com.litongjava.netty.boot.websocket.WebsocketRouter;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.json.MapJsonUtils;
import com.litongjava.tio.utils.reflicaiton.ClassCheckUtils;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyApplicationContext implements Context {

  private NettyBootServer nettyBootServer = NettyBootServer.me();
  private Integer port = 0;

  public Context run(Class<?>[] primarySources, String[] args) {
    return run(primarySources, null, args);
  }

  public Context run(Class<?>[] primarySources, BootConfiguration bootConfiguration, String[] args) {
    long scanClassStartTime = 0L;
    long scanClassEndTime = 0L;
    long configStartTime = 0L;
    long configEndTimeTime = 0L;

    long initServerStartTime = System.currentTimeMillis();
    EnvUtils.buildCmdArgsMap(args);
    EnvUtils.load();
    // port and contextPath

    String contextPath = EnvUtils.get("netty.server.context-path");
    if (contextPath == null) {
      contextPath = EnvUtils.get(ServerConfigKeys.SERVER_CONTEXT_PATH);
    }

    long initServerEndTime = System.currentTimeMillis();

    List<Class<?>> scannedClasses = null;
    boolean printScannedClasses = EnvUtils.getBoolean(ServerConfigKeys.AOP_PRINT_SCANNED_CLASSSES, false);
    // 添加自定义组件注解
    if (ClassCheckUtils.check(AopClasses.Aop)) {
      scanClassStartTime = System.currentTimeMillis();
      // process @AComponentScan
      try {
        scannedClasses = ComponentScanner.scan(primarySources, printScannedClasses);
      } catch (Exception e1) {
        e1.printStackTrace();
      }

      log.info("scanned classes size:{}", scannedClasses.size());
      // process @Improt
      for (Class<?> primarySource : primarySources) {
        if (ClassCheckUtils.check("com.litongjava.annotation.AImport")) {
          AImport importAnnotaion = primarySource.getAnnotation(AImport.class);
          if (importAnnotaion != null) {
            Class<?>[] value = importAnnotaion.value();
            for (Class<?> clazzz : value) {
              scannedClasses.add(clazzz);
            }
          }
        }
      }
      scannedClasses = this.processBeforeStartConfiguration(scannedClasses);
      scanClassEndTime = System.currentTimeMillis();
    } else {
      log.info("not found:{}", AopClasses.Aop);
    }

    configStartTime = System.currentTimeMillis();

    HttpRequestInterceptor httpRequestInterceptor = nettyBootServer.getHttpRequestInterceptorDispather();
    if (httpRequestInterceptor == null) {
      httpRequestInterceptor = new DefaultHttpRequestInterceptorDispatcher();
      nettyBootServer.setHttpRequestInterceptorDispather(httpRequestInterceptor);
    }

    HttpRequestRouter httpRequestRouter = nettyBootServer.getHttpRequestRouter();
    if (httpRequestRouter == null) {
      httpRequestRouter = new DefaultHttpReqeustRouter();
      nettyBootServer.setHttpRequestRouter(httpRequestRouter);
    }

    WebsocketRouter websocketRouter = nettyBootServer.getWebsocketRouter();
    if (websocketRouter == null) {
      websocketRouter = new DefaultWebsocketRouter();
      nettyBootServer.setWebsocketRouter(websocketRouter);
    }

    if (bootConfiguration != null) {
      try {
        // Configure TioBootConfiguration
        bootConfiguration.config();
      } catch (IOException e) {
        throw new RuntimeException("Failed to configure bootConfiguration", e);
      }
    }

    if (ClassCheckUtils.check(AopClasses.Aop)) {
      if (scannedClasses != null && scannedClasses.size() > 0) {
        this.initAnnotation(scannedClasses);
      }
    }

    configEndTimeTime = System.currentTimeMillis();

    long routeStartTime = System.currentTimeMillis();

    Map<String, HttpRequestHandler> httpRequestMapping = httpRequestRouter.mapping();

    Map<String, Supplier<SimpleChannelInboundHandler<WebSocketFrame>>> websocketMapping = websocketRouter.mapping();

    log.info(":{},{},{}", nettyBootServer, httpRequestRouter, websocketRouter);
    if (httpRequestMapping.size() > 0) {
      log.info("http  mapping\r\n{}", MapJsonUtils.toPrettyJson(httpRequestMapping));
    }
    if (websocketMapping.size() > 0) {
      log.info("websocket  mapping\r\n{}", MapJsonUtils.toPrettyJson(websocketMapping));
    }

    long routeEndTime = System.currentTimeMillis();

    log.info("init:{}(ms),scan class:{}(ms),config:{}(ms),http route:{}(ms)", initServerEndTime - initServerStartTime, scanClassEndTime - scanClassStartTime, configEndTimeTime - configStartTime,
        routeEndTime - routeStartTime);

    port = EnvUtils.getInt("netty.server.port");
    if (port == null) {
      port = EnvUtils.getInt(ServerConfigKeys.SERVER_PORT, 0);
    }
    Integer sslPort = EnvUtils.getInt("netty.server.ssl.port");
    if (sslPort == null) {
      sslPort = EnvUtils.getInt(ServerConfigKeys.SERVER_SSL_PORT, 0);
    }
    if (port.equals(0) && sslPort.equals(0)) {
      port = 80;
    }
    if (!EnvUtils.getBoolean(ServerConfigKeys.SERVER_LISTENING_ENABLE, false)) {
      printUrl(port, contextPath);
    }

    configEndTimeTime = System.currentTimeMillis();

    // 根据参数判断是否启动服务器,默认启动服务器
    if (EnvUtils.getBoolean(ServerConfigKeys.SERVER_LISTENING_ENABLE, true)) {
      nettyBootServer.start(port, sslPort, contextPath, websocketRouter, initServerStartTime);
    }

    return this;
  }

  /**
   * 打印启动端口和访问地址
   *
   * @param port
   * @param contextPath
   */
  private void printUrl(int port, String contextPath) {
    log.info("port:{}", port);
    String fullUrl = "http://localhost";
    if (port != 80) {
      fullUrl += (":" + port);
    }
    if (contextPath != null) {
      fullUrl += contextPath;
    }
    System.out.println(fullUrl);
  }

  public void initAnnotation(List<Class<?>> scannedClasses) {
    new BeanProcess().initAnnotation(scannedClasses);
  }

  public boolean isRunning() {
    return nettyBootServer.isRunning();
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

  private List<Class<?>> processBeforeStartConfiguration(List<Class<?>> scannedClasses) {
    return new BeforeStartConfigurationProcess().process(scannedClasses);
  }

}

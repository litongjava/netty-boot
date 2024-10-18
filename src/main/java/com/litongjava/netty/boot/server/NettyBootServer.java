package com.litongjava.netty.boot.server;

import java.io.File;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLException;

import com.litongjava.constatns.ServerConfigKeys;
import com.litongjava.hook.HookContainer;
import com.litongjava.netty.boot.http.HttpRequestRouter;
import com.litongjava.netty.boot.inteceptor.HttpRequestInterceptor;
import com.litongjava.netty.boot.inteceptor.ServerInteceptorConfigure;
import com.litongjava.netty.boot.listener.ChannelConnectionListener;
import com.litongjava.netty.boot.websocket.WebsocketRouter;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.hutool.ResourceUtil;
import com.litongjava.tio.utils.hutool.StrUtil;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class NettyBootServer {

  private static NettyBootServer me = new NettyBootServer();

  private NettyBootServer() {
  }

  public static NettyBootServer me() {
    return me;
  }

  private HttpRequestRouter httpRequestRouter = null;
  private WebsocketRouter websocketRouter = null;
  private DefaultNettyServerBootstrap nettyServerBootstrap;
  private DefaultNettyServerBootstrap nettySslServerBootstrap;
  private ChannelConnectionListener channelConnectionListener;
  private ServerInteceptorConfigure serverInteceptorConfigure;
  private HttpRequestInterceptor httpRequestInterceptorDispather;

  // Add this method to stop the server
  public void stop() {
    List<Runnable> destroyMethods = HookContainer.me().getDestroyMethods();
    Iterator<Runnable> iterator = destroyMethods.iterator();
    while (iterator.hasNext()) {
      Runnable runnable = iterator.next();
      iterator.remove();
      try {
        runnable.run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (nettyServerBootstrap != null) {
      nettyServerBootstrap.close();
    }
    if (nettySslServerBootstrap != null) {
      nettySslServerBootstrap.close();
    }
    me = new NettyBootServer();
  }

  public void start(Integer port, Integer sslPort, String contextPath, WebsocketRouter websocketRouter, long startTime) {

    if (EnvUtils.getBoolean(ServerConfigKeys.SERVER_SSL_ENEABLE, false)) {
      DefaultChannelInitializer httpsChannelIntializer = null;
      String sslKeyFile = EnvUtils.getStr(ServerConfigKeys.SERVER_SSL_KEY_FILE);
      String sslKeyCertChainFile = EnvUtils.getStr(ServerConfigKeys.SERVER_SSL_KEY_CERT_CHAIN_FILE);
      String sslKeyPassword = EnvUtils.getStr(ServerConfigKeys.SERVER_SSL_KEY_PASSWORD);

      if (StrUtil.isNotBlank(sslKeyFile) && StrUtil.isNotBlank(sslKeyCertChainFile)) {
        log.info("enable ssl port:{},key file", sslKeyFile);
        try {
          if (sslKeyFile.startsWith("classpath")) {
            try (InputStream keyInputStream = ResourceUtil.getResourceAsStream(sslKeyFile);
                //
                InputStream keyCertChainInputStream = ResourceUtil.getResourceAsStream(sslKeyCertChainFile);) {
              log.info("keyInputStream:{},keyCertChainInputStream:{}", keyInputStream, keyCertChainInputStream);
              SslContextBuilder forServer = SslContextBuilder.forServer(keyCertChainInputStream,
                  //
                  keyInputStream, sslKeyPassword);
              SslContext sslContext = forServer.build();
              httpsChannelIntializer = new DefaultChannelInitializer(contextPath, websocketRouter, sslContext);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          } else {
            SslContextBuilder forServer = SslContextBuilder.forServer(new File(sslKeyCertChainFile),
                //
                new File(sslKeyFile), sslKeyPassword);
            try {
              SslContext sslContext = forServer.build();
              httpsChannelIntializer = new DefaultChannelInitializer(contextPath, websocketRouter, sslContext);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        } catch (Exception e) {
          log.error("Failed to create SslContext: {}", e.getMessage());
          e.printStackTrace();
          return;
        }
      } else {
        // 使用临时签发的一个证书
        SelfSignedCertificate ssc = null;
        try {
          ssc = new SelfSignedCertificate();
        } catch (CertificateException e) {
          throw new RuntimeException(e);
        }
        log.info("enable ssl:{}", ssc);
        try {
          SslContext sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
          httpsChannelIntializer = new DefaultChannelInitializer(contextPath, websocketRouter, sslContext);
        } catch (SSLException e) {
          throw new RuntimeException(e);
        }
      }
      if (sslPort > 0) {
        nettySslServerBootstrap = new DefaultNettyServerBootstrap(sslPort, httpsChannelIntializer);
        nettySslServerBootstrap.start(startTime);

        if (port > 0) {
          DefaultChannelInitializer httpChannelInitializer = new DefaultChannelInitializer(contextPath, websocketRouter);
          nettyServerBootstrap = new DefaultNettyServerBootstrap(port, httpChannelInitializer);
          nettyServerBootstrap.start(startTime);
        }
        
      } else {
        nettyServerBootstrap = new DefaultNettyServerBootstrap(port, httpsChannelIntializer);
        nettyServerBootstrap.start(startTime);
      }

    } else {
      if (port > 0) {
        DefaultChannelInitializer httpChannelInitializer = new DefaultChannelInitializer(contextPath, websocketRouter);
        nettyServerBootstrap = new DefaultNettyServerBootstrap(port, httpChannelInitializer);
        nettyServerBootstrap.start(startTime);
      }
    }

  }

  public void restart(long startTime) {
    if (nettyServerBootstrap != null) {
      nettyServerBootstrap.restart(startTime);
    }

    if (nettySslServerBootstrap != null) {
      nettySslServerBootstrap.restart(startTime);
    }

  }

  public boolean isRunning() {
    if (nettyServerBootstrap != null) {
      return nettyServerBootstrap.isRunning();
    }

    if (nettySslServerBootstrap != null) {
      return nettySslServerBootstrap.isRunning();
    }
    return false;

  }

}

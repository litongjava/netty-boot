# netty-boot

## 简介

`Netty-Boot` 是一个基于 Netty 的轻量级 Web 中间件框架，专为高性能 Web 应用开发而设计。它充分利用了 Netty 的高效 IO 模型，支持快速开发高并发的 Web 服务。该框架整合了多种常见的中间件，包括 PostgreSQL、Redis、MongoDB、Elasticsearch、消息队列（MQ）、Dubbo 等，提供了强大的扩展能力，使开发者能够专注于业务逻辑开发。

`Netty-Boot` 的目标是简化 Netty 的使用，提供类似 Spring Boot 的易用性，帮助开发者轻松构建基于 Netty 的 Web 应用，同时享受 Netty 带来的高性能和可扩展性。

### 特性

- **高性能**：基于 Netty 的异步非阻塞 IO，支持高并发和大规模连接。
- **模块化设计**：支持 PostgreSQL、Redis、MongoDB、Elasticsearch、消息队列（MQ）和 Dubbo 等中间件的无缝集成。
- **灵活性**：通过注解驱动的组件扫描和依赖注入，简化配置，增强代码的可维护性。
- **WebSocket 支持**：轻松实现 WebSocket 通信，支持长连接和实时数据推送。
- **易扩展性**：通过 AOP 和自定义中间件，开发者可以扩展 Netty-Boot 的功能，满足复杂业务需求。

### 开源地址

- [github](https://github.com/litongjava/netty-boot) | -[gitee](https://github.com/ppnt/netty-boot)

## 入门示例

### 添加依赖

在 `pom.xml` 中添加以下依赖配置：

```xml
  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <java.version>1.8</java.version>
    <maven.compiler.source>${java.version}</maven.compiler.source>
    <maven.compiler.target>${java.version}</maven.compiler.target>
    <graalvm.version>23.1.1</graalvm.version>
    <lombok-version>1.18.30</lombok-version>
    <jfinal-aop.version>1.2.9</jfinal-aop.version>
    <fastjson2.version>2.0.52</fastjson2.version>
    <main.class>com.litongjava.study.netty.boot.MainApp</main.class>
  </properties>

  <dependencies>
    <dependency>
      <groupId>com.litongjava</groupId>
      <artifactId>netty-boot</artifactId>
      <version>1.0.8</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>1.2.3</version>
    </dependency>

    <!-- JFinal AOP -->
    <dependency>
      <groupId>com.litongjava</groupId>
      <artifactId>jfinal-aop</artifactId>
      <version>${jfinal-aop.version}</version>
    </dependency>

    <!-- FastJSON2 用于 JSON 解析 -->
    <dependency>
      <groupId>com.alibaba.fastjson2</groupId>
      <artifactId>fastjson2</artifactId>
      <version>${fastjson2.version}</version>
    </dependency>

    <!-- OkHttp 客户端 -->
    <dependency>
      <groupId>com.squareup.okhttp3</groupId>
      <artifactId>okhttp</artifactId>
      <version>3.11.0</version>
    </dependency>

    <!-- Lombok 用于简化代码 -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>${lombok-version}</version>
      <optional>true</optional>
      <scope>provided</scope>
    </dependency>

    <!-- JUnit 用于测试 -->
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.12</version>
      <scope>test</scope>
    </dependency>

  </dependencies>
  <profiles>
    <!-- development -->
    <profile>
      <id>development</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <build>
        <plugins>
          <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>2.7.4</version>
            <configuration>
              <fork>true</fork>
              <mainClass>${main.class}</mainClass>
              <excludeGroupIds>org.projectlombok</excludeGroupIds>
              <arguments>
                <argument>--mode=dev</argument>
              </arguments>
            </configuration>
          </plugin>
        </plugins>
      </build>
    </profile>

    <!-- production -->
    <profile>
      <id>production</id>
      <build>
        <plugins>
          <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>2.7.4</version>
            <configuration>
              <mainClass>${main.class}</mainClass>
              <excludeGroupIds>org.projectlombok</excludeGroupIds>
            </configuration>
            <executions>
              <execution>
                <goals>
                  <goal>repackage</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </build>
    </profile>
  </profiles>
```

### 启动类

```java
package com.litongjava.study.netty.boot;

import com.litongjava.annotation.AComponentScan;
import com.litongjava.netty.boot.NettyApplication;

@AComponentScan
public class NettyHelloApp {
  public static void main(String[] args) {
    NettyApplication.run(NettyHelloApp.class, args);
  }
}
```

### 配置类

```java
package com.litongjava.study.netty.boot.config;

import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.AInitialization;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.netty.boot.http.HttpRequestRouter;
import com.litongjava.netty.boot.server.NettyBootServer;
import com.litongjava.netty.boot.websocket.WebsocketRouter;
import com.litongjava.study.netty.boot.handler.OkHandler;
import com.litongjava.study.netty.boot.handler.WsHandler;

@AConfiguration
public class WebConfig {

  @AInitialization
  public void config() {
    // 设置 HTTP 路由
    HttpRequestRouter httpRouter = NettyBootServer.me().getHttpRequestRouter();
    OkHandler okHandler = Aop.get(OkHandler.class);
    httpRouter.add("/txt", okHandler::txt);
    httpRouter.add("/json", okHandler::json);
    httpRouter.add("/echo", okHandler::echo);

    // 设置 WebSocket 路由
    WebsocketRouter websocketRouter = NettyBootServer.me().getWebsocketRouter();
    WsHandler wsHandler = Aop.get(WsHandler.class);
    websocketRouter.add("/ws", wsHandler::handle);
  }
}
```

### HTTP 请求处理器

```java
package com.litongjava.study.netty.boot.handler;

import com.litongjava.model.body.RespBodyVo;
import com.litongjava.netty.boot.utils.HttpRequestUtils;
import com.litongjava.netty.boot.utils.HttpResponseUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

public class OkHandler {

  public FullHttpResponse txt(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
    String responseContent = "Hello, this is the HTTP response!";
    return HttpResponseUtils.txt(responseContent);
  }

  public FullHttpResponse json(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
    RespBodyVo ok = RespBodyVo.ok();
    return HttpResponseUtils.json(ok);
  }

  public FullHttpResponse echo(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
    String fullHttpRequestString = HttpRequestUtils.getFullHttpRequestAsString(httpRequest);
    return HttpResponseUtils.txt(fullHttpRequestString);
  }
}
```

### WebSocket 请求处理器

```java
package com.litongjava.study.netty.boot.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.*;

public class WsHandler {

  public void handle(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
    if (frame instanceof TextWebSocketFrame) {
      String requestText = ((TextWebSocketFrame) frame).text();
      // 返回相同的消息
      ctx.channel().writeAndFlush(new TextWebSocketFrame("Server received: " + requestText));

    } else if (frame instanceof CloseWebSocketFrame) {
      ctx.channel().close();

    } else if (frame instanceof PingWebSocketFrame) {
      ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));

    } else {
      throw new UnsupportedOperationException("不支持的帧类型: " + frame.getClass().getName());
    }
  }
}
```

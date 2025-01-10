package com.litongjava.netty.boot.inteceptor;

import java.util.List;
import java.util.Map;

import com.litongjava.netty.boot.adapter.HttpRequest;
import com.litongjava.netty.boot.adapter.HttpResponse;
import com.litongjava.netty.boot.server.NettyBootServer;


/**
 * DefaultHttpServerInterceptor
 * @author Tong Li
 *
 */
public class DefaultHttpRequestInterceptorDispatcher implements HttpRequestInterceptor {

  /**
   * /* 表示匹配任何以特定路径开始的路径，/** 表示匹配该路径及其下的任何子路径
   * @param path
   * @return
   */
  @Override
  public HttpResponse before(HttpRequest request) throws Exception {
    ServerInteceptorConfigure serverInteceptorConfigure = NettyBootServer.me().getServerInteceptorConfigure();
    if (serverInteceptorConfigure == null) {
      return null;
    }

    Map<String, HttpInterceptorModel> inteceptors = serverInteceptorConfigure.getInteceptors();
    String path = request.uri();
    for (HttpInterceptorModel model : inteceptors.values()) {
      boolean isBlock = isMatched(path, model);
      if (isBlock) {
        HttpRequestInterceptor interceptor = model.getInterceptor();
        if (interceptor != null) {
          HttpResponse response = interceptor.before(request);
          if (response != null) {
            return response; // 如果拦截器返回响应，直接返回
          }
        }
      }
    }
    return null; // 没有拦截器处理，继续后续流程
  }

  @Override
  public void after(HttpRequest request, HttpResponse response, long cost) throws Exception {
    ServerInteceptorConfigure serverInteceptorConfigure = NettyBootServer.me().getServerInteceptorConfigure();
    if (serverInteceptorConfigure == null) {
      return;
    }

    Map<String, HttpInterceptorModel> inteceptors = serverInteceptorConfigure.getInteceptors();
    String path = request.uri();
    for (HttpInterceptorModel model : inteceptors.values()) {
      boolean isBlock = isMatched(path, model);
      if (isBlock) {
        HttpRequestInterceptor interceptor = model.getInterceptor();
        if (interceptor != null) {
          interceptor.after(request, response, cost);
        }
      }
    }

  }

  /**
   * 判断是否Mathch
   * @param path
   * @param model
   * @return
   */
  private boolean isMatched(String path, HttpInterceptorModel model) {
    List<String> blockedUrls = model.getBlockedUrls();
    List<String> allowedUrls = model.getAllowedUrls();

    boolean isBlocked = (blockedUrls != null && !blockedUrls.isEmpty()) && isUrlBlocked(path, blockedUrls);
    boolean isAllowed = (allowedUrls != null && !allowedUrls.isEmpty()) && isUrlAllowed(path, allowedUrls);

    return isBlocked && !isAllowed;
  }

  private boolean isUrlBlocked(String path, List<String> blockedUrls) {
    return blockedUrls.stream().anyMatch(urlPattern -> pathMatchesPattern(path, urlPattern));
  }

  private boolean isUrlAllowed(String path, List<String> allowedUrls) {
    // 这里添加逻辑以检查路径是否匹配 allowedUrls 中的任何模式
    // 示例逻辑
    return allowedUrls.stream().anyMatch(urlPattern -> pathMatchesPattern(path, urlPattern));
  }

  private boolean pathMatchesPattern(String path, String pattern) {
    // 这里添加逻辑以处理通配符匹配，如 "/path/**" 或 "/path/*"
    // 示例逻辑
    if (pattern.endsWith("/**")) {
      return path.startsWith(pattern.substring(0, pattern.length() - 3));
    } else if (pattern.endsWith("/*")) {
      return path.startsWith(pattern.substring(0, pattern.length() - 2));
    }
    return path.equals(pattern);
  }

}
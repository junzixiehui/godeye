package com.ziroom.godeye.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class IpUtils {

  private static final Logger LOG = LoggerFactory.getLogger(IpUtils.class);

  /** 静态变量缓存IP. */
  private static String cachedIp = null;

  /*
   * 同步块使用
   */
  private static Object syncObject = new Object();

  static {
    try {
      cachedIp = getRealIp();
    } catch (SocketException ex) {
      LOG.error("", ex);
      cachedIp = "127.0.0.1";
    }
  }

  /**
   * 取得本机的IP，并把结果放到static变量中.
   *
   * @return 如果有多个IP地址返回外网的IP，多个外网IP返回第一个IP（在多网管等特殊情况下）
   * @throws SocketException
   */
  public static String getRealIpWithStaticCache() {
    if (cachedIp == null) {
      synchronized (syncObject) {
        try {
          cachedIp = getRealIp();
        } catch (SocketException ex) {
          LOG.error("", ex);
          cachedIp = "127.0.0.1";
        }
      }
      return cachedIp;
    } else {
      return cachedIp;
    }
  }

  /** 刷新getRealIpWithStaticCache()方法的static变量. */
  public static void flushIpStaticCache() {
    synchronized (syncObject) {
      cachedIp = null;
    }
  }

  /**
   * 取得本机的IP.
   *
   * @return 如果有多个IP地址返回外网的IP，多个外网IP返回第一个IP（在多网管等特殊情况下）
   * @throws SocketException
   */
  public static String getRealIp() throws SocketException {
    String localIp = null; // 本地IP，如果没有配置外网IP则返回它
    String netIp = null; // 外网IP

    Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
    InetAddress ip = null;
    boolean isFind = false; // 是否找到外网IP
    while (netInterfaces.hasMoreElements() && !isFind) {
      NetworkInterface ni = netInterfaces.nextElement();
      Enumeration<InetAddress> address = ni.getInetAddresses();
      while (address.hasMoreElements()) {
        ip = address.nextElement();
        if (!ip.isSiteLocalAddress()
            && !ip.isLoopbackAddress()
            && ip.getHostAddress().indexOf(":") == -1) { // 外网IP
          netIp = ip.getHostAddress();
          isFind = true;
          break;
        } else if (ip.isSiteLocalAddress()
            && !ip.isLoopbackAddress()
            && ip.getHostAddress().indexOf(":") == -1) { // 内网IP
          localIp = ip.getHostAddress();
        }
      }
    }
    if (netIp != null && !"".equals(netIp)) {
      return netIp;
    } else {
      return localIp;
    }
  }
}

package com.ziroom.godeye.entity.trace;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Endpoint implements Serializable {
  private static final long serialVersionUID = -1819879293130044091L;
  private String serviceName;
  private String ip;
  private int port;

  public static Endpoint create(String serviceName, String ip, int port) {
    return new Endpoint(serviceName, ip, port);
  }

  Endpoint(String serviceName, String ip, int port) {
    this.ip = ip;
    this.port = port;
    if (serviceName != null) {
      serviceName = serviceName.toLowerCase();
    } else {
      serviceName = "";
    }
    this.serviceName = serviceName;
  }

  @Override
  public String toString() {
    return "Endpoint{" + "ip='" + ip + '\'' + ", port=" + port + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Endpoint)) {
      return false;
    }

    Endpoint endpoint = (Endpoint) obj;
    if (!ip.equals(endpoint.ip)) {
      return false;
    }
    if (!serviceName.equals(endpoint.serviceName)) {
      return false;
    }
    if (port != endpoint.port) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (ip != null ? ip.hashCode() : 0);
    result = 31 * result + port;
    result = 31 * result + serviceName.hashCode();
    return result;
  }
}

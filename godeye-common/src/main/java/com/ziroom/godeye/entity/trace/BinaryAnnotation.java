package com.ziroom.godeye.entity.trace;

import com.ziroom.godeye.enums.BinaryAnnotationType;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;



@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BinaryAnnotation implements Serializable {
  private static final long serialVersionUID = -5829069208358509837L;

  private String key;
  private String value;
  private BinaryAnnotationType type = BinaryAnnotationType.EVENT;
  private long timestamp = System.currentTimeMillis();
  private String ip;
  private int port;
  private int duration = 0;

  public void setThrowable(String className, String methodName, Throwable e) {
    if (e != null) {
      e.printStackTrace();
      setKey(className);
      setValue(methodName + "," + e.toString());
      setType(BinaryAnnotationType.EXCEPTION);
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public void setEndpoint(Endpoint endpoint) {
    if (endpoint == null) {
      return;
    }

    this.ip = endpoint.getIp();
    this.port = endpoint.getPort();
  }
}

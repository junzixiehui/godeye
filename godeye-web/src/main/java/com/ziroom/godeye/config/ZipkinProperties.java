package com.ziroom.godeye.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @Author: by qlb
 * @Date: 2017/1/18  13:28
 * @Version: 1.0
 */
@Configuration
@ConfigurationProperties(locations = "classpath:/application.properties",prefix = "com.ziroom")
@Setter
@Getter
public class ZipkinProperties {

    private String serviceName;

    private String url;

    private int connectTimeout;

    private int readTimeout;

    private int flushInterval;

    private boolean compressionEnabled;


}
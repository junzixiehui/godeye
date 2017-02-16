package com.ziroom.godeye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * @Description:
 * @Author: by qlb
 * @Date: 2017/1/18  13:21
 * @Version: 1.0
 */
@EnableAspectJAutoProxy
@EnableConfigurationProperties
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

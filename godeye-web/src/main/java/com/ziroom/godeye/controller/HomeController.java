package com.ziroom.godeye.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/** @Description: @Author: by qlb @Date: 2017/1/18 13:30 @Version: 1.0 */
@RestController
@RequestMapping("/")
public class HomeController {

  @Autowired private OkHttpClient client;

  private Random random = new Random();

  @RequestMapping("start")
  public String start() throws InterruptedException, IOException {
    int sleep = random.nextInt(100);
    TimeUnit.MILLISECONDS.sleep(sleep);
    Request request = new Request.Builder().url("http://localhost:8080/foo").get().build();
    Response response = client.newCall(request).execute();
    return " [service1 sleep " + sleep + " ms]" + response.body().toString();
  }

  @RequestMapping("foo")
  public String foo() throws InterruptedException, IOException {
    int sleep = random.nextInt(100);
    TimeUnit.MILLISECONDS.sleep(sleep);
    Request request =
        new Request.Builder().url("http://localhost:8080/bar").get().build(); //service3
    Response response = client.newCall(request).execute();
    String result = response.body().string();
    request = new Request.Builder().url("http://localhost:8080/tar").get().build(); //service4
    response = client.newCall(request).execute();
    result += response.body().string();
    return " [service2 sleep " + sleep + " ms]" + result;
  }

  @RequestMapping("bar")
  public String bar() throws InterruptedException, IOException { //service3 method
    int sleep = random.nextInt(100);
    TimeUnit.MILLISECONDS.sleep(sleep);
    return " [service3 sleep " + sleep + " ms]";
  }

  @RequestMapping("tar")
  public String tar() throws InterruptedException, IOException { //service4 method
    int sleep = random.nextInt(100);
    TimeUnit.MILLISECONDS.sleep(sleep);
    return " [service4 sleep " + sleep + " ms]";
  }
}

package com.ziroom.godeye.filter;

/**
 * Description: 采样率
 *
 * @author: by qlb
 * @date: 2017/4/22 18:25
 * @version: 1.0
 */
public interface Sampler {

  boolean isSample();

  void setSampleRate(int rate);
}

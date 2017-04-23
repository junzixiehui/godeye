package com.ziroom.godeye.filter;

/**
 * <p>Description: 采样率</p>
 *
 * @author: by qlb
 * @date: 2017/4/22  18:25
 * @version: 1.0
 */
public interface Sampler {

    boolean isSample();

    void setSampleRate(int rate);
}

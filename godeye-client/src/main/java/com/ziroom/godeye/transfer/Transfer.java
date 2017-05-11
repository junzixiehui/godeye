package com.ziroom.godeye.transfer;

import com.ziroom.godeye.entity.trace.Span;

/**
 * <p>Description: 传送</p>
 *
 * @author: by qlb
 * @date: 2017/5/11  22:40
 * @version: 1.0
 */
public interface Transfer {

    boolean isReady();

    boolean isServiceReady(String serviceName);

    void start() throws Exception;

    void cancel();

    void asyncSend(Span span);
}

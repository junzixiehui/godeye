package com.ziroom.godeye.transfer.disruptor;

import com.lmax.disruptor.EventHandler;

/**
 * <p>Description: 定义事件处理的具体实现
 * 通过实现接口 com.lmax.disruptor.EventHandler<T> 定义事件处理的具体实现。</p>
 *
 * @author: by qlb
 * @date: 2017/5/11  22:52
 * @version: 1.0
 */
public class SpanEventHandler implements EventHandler<SpanEvent> {


    @Override
    public void onEvent(SpanEvent spanEvent, long l, boolean b) throws Exception {

    }
}

package com.ziroom.godeye.transfer.disruptor;

import com.lmax.disruptor.EventFactory;

/**
 * <p>Description: 事件工厂(SpanEventFactory)定义了如何实例化事件(spanEvent)，
 * 需要实现接口 com.lmax.disruptor.EventFactory<T>。
 * Disruptor 通过 EventFactory 在 RingBuffer 中预创建 Event 的实例。
 * 一个 Event 实例实际上被用作一个“数据槽”，发布者发布前，
 * 先从 RingBuffer 获得一个 Event 的实例，然后往 Event 实例中填充数据，
 * 之后再发布到 RingBuffer 中，之后由 Consumer 获得该 Event 实例并从中读取数据。</p>
 *
 * @author: by qlb
 * @date: 2017/5/11  22:48
 * @version: 1.0
 */
public class SpanEventFactory implements EventFactory<SpanEvent> {


    @Override
    public SpanEvent newInstance() {
        return new SpanEvent();
    }
}

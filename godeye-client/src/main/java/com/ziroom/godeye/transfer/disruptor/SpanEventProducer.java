package com.ziroom.godeye.transfer.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.ziroom.godeye.entity.trace.Span;

/**
 * <p>Description: </p>
 *
 * @author: by qlb
 * @date: 2017/5/11  23:03
 * @version: 1.0
 */
public class SpanEventProducer {

    private final RingBuffer<SpanEvent> ringBuffer;

    public SpanEventProducer(final RingBuffer<SpanEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }


    public void onData(final Span span) {
        // 发布事件；
        final long sequence = ringBuffer.next();//请求下一个事件序号；
        try {
            SpanEvent event = ringBuffer.get(sequence);//获取该序号对应的事件对象；
            //long data = getEventData();//获取要通过事件传递的业务数据；
            event.setSpan(span);
        } finally {
            ringBuffer.publish(sequence);//发布事件；
        }
    }

}

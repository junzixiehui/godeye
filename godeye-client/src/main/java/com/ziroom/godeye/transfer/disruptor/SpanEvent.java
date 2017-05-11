package com.ziroom.godeye.transfer.disruptor;

import com.ziroom.godeye.entity.trace.Span;

/**
 * <p>Description: </p>
 *
 * @author: by qlb
 * @date: 2017/5/11  22:46
 * @version: 1.0
 */
public class SpanEvent {


    private Span span;


    public Span getSpan() {
        return span;
    }

    public void setSpan(Span span) {
        this.span = span;
    }
}

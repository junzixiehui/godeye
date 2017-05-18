package com.ziroom.godeye.transfer.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.ziroom.godeye.entity.trace.Span;
import com.ziroom.godeye.transfer.Transfer;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class DisruptorTransfer implements Transfer {

  private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

  private final AtomicBoolean ready = new AtomicBoolean(false);

  private Disruptor<SpanEvent> disruptor;

  private SpanEventProducer producer;

  public DisruptorTransfer(final SpanEventHandler spanEventHandler) {
    this(spanEventHandler, DEFAULT_BUFFER_SIZE);
  }

  public DisruptorTransfer(final SpanEventHandler spanEventHandler, final int buffSize) {
    final ThreadFactory threadFactory = Executors.defaultThreadFactory();
    final SpanEventFactory factory = new SpanEventFactory();
    final int bufferSize = buffSize;// RingBuffer 大小，必须是 2 的 N 次方；
    disruptor = new Disruptor<>(factory, bufferSize, threadFactory);

    disruptor.handleEventsWith(spanEventHandler);// disruptor.handleEventsWith(new SpanEventHandler("http://localhost:9080/upload"));
    disruptor.start();// Start the Disruptor, starts all threads running
    final RingBuffer<SpanEvent> ringBuffer = disruptor.getRingBuffer();
    producer = new SpanEventProducer(ringBuffer);
  }

  public boolean isReady() {
    return ready.get();
  }

  public boolean isServiceReady(final String serviceName) {
    return ready.get();
  }

  public void start() throws Exception {
    // do nothing
  }

  public void cancel() {
    disruptor.shutdown();
  }

  public void asyncSend(final Span span) {
    producer.onData(span);
  }

}

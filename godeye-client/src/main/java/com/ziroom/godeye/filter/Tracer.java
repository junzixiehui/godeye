package com.ziroom.godeye.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ziroom.godeye.TraceContext;
import com.ziroom.godeye.constant.TracerConstant;
import com.ziroom.godeye.entity.trace.Annotation;
import com.ziroom.godeye.entity.trace.BinaryAnnotation;
import com.ziroom.godeye.entity.trace.Endpoint;
import com.ziroom.godeye.entity.trace.Span;
import com.ziroom.godeye.enums.AnnotationType;
import com.ziroom.godeye.sample.CustomSampler;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Tracer {

  private static Tracer instance = new Tracer();

  private final Sampler sampler = new CustomSampler();

   private Transfer transfer;

  // 传递parentSpan
 /* private final ThreadLocal<Span> spanThreadLocal = new ThreadLocal<Span>();

  private final ThreadLocal<Span> ctxThreadLocal = new ThreadLocal<Span>();*/

  private Tracer() {}

  public static Tracer getInstance() {
    return instance;
  }
/*
  void removeParentSpan() {
    spanThreadLocal.remove();
  }

  Span getParentSpan() {
    return spanThreadLocal.get();
  }

  void setParentSpan(final Span span) {
    spanThreadLocal.set(span);
  }*/

  // 构件Span，参数通过上游接口传递过来
  Span genSpan(
      final String appName,
      final String serviceName,
      final String methodName,
      final String traceId,
      final String pid,
      final String id,
      final boolean sample) {
    final Span span = new Span();
    span.setAppName(appName);
    span.setServiceName(serviceName);
    span.setMethodName(methodName);
    span.setId(id);
    span.setParentId(pid);
    span.setTraceId(traceId);
    span.setSample(sample);

    return span;
  }

  // 构件rootSpan,是否采样
  Span newSpan(final String appName, final String serviceName, final String methodName) {
    final Span span = new Span();
    span.setAppName(appName);
    span.setServiceName(serviceName);
    span.setMethodName(methodName);
    if (this.isSample()) {
      span.setSample(true);
      span.setTraceId(genTracerId());
      span.setId("1");
    } else {
      span.setSample(false);
      span.setTraceId(null);
      span.setId(null);
    }

    return span;
  }

  // 构件rootSpan,是否采样
  Span newSpan(
      final String appName,
      final String serviceName,
      final String methodName,
      final Span parentSpan) {
    final Span span = new Span();
    span.setAppName(appName);
    span.setServiceName(serviceName);
    span.setMethodName(methodName);
    if (parentSpan.isSample()) {
      final int subSpanNum = parentSpan.getSubSpanNum() + 1;
      parentSpan.setSubSpanNum(subSpanNum);

      span.setSample(true);
      span.setTraceId(parentSpan.getTraceId());
      span.setParentId(parentSpan.getId());
      span.setId(parentSpan.getId() + "." + subSpanNum);
    } else {
      span.setSample(false);
      span.setTraceId(null);
      span.setParentId(null);
      span.setId(null);
    }

    return span;
  }

  public void setSampleRate(final int rate) {
    sampler.setSampleRate(rate);
  }

  boolean isSample() {
    return sampler.isSample();
  }

  void addBinaryAnnotation(final BinaryAnnotation bin) {
    final Span span = TraceContext.getParentSpan();
    if (span != null) {
      span.addBinaryAnnotation(bin);
    }
  }

  // 构件cs annotation
  void clientSendRecord(final Span span, final Endpoint endpoint, final long start) {
    final Annotation annotation = new Annotation();
    annotation.setType(AnnotationType.CLIENT_SEND);
    annotation.setTimestamp(start);
    annotation.setEndpoint(endpoint);
    span.addAnnotation(annotation);
  }

  /*   // 构件cr annotation
  void clientReceiveRecord(final Span span, final Endpoint endpoint, final long end) {
      if (span.isSample() && transfer != null) {
          final Annotation annotation = new Annotation();
          annotation.setType(AnnotationType.CLIENT_RECEIVE);
          annotation.setEndpoint(endpoint);
          annotation.setTimestamp(end);
          span.addAnnotation(annotation);
          transfer.asyncSend(span);
      }
  }*/

  // 构件sr annotation
  void serverReceiveRecord(final Span span, final Endpoint endpoint, final long start) {
    if (span.isSample()) {
      final Annotation annotation = new Annotation();
      annotation.setType(AnnotationType.SERVER_RECEIVE);
      annotation.setEndpoint(endpoint);
      annotation.setTimestamp(start);
      span.addAnnotation(annotation);
    }

    TraceContext.setParentSpan(span);
  }

  // 构件 ss annotation
      void serverSendRecord(final Span span, final Endpoint endpoint, final long end) {
      if (span.isSample() && transfer != null) {
          final Annotation annotation = new Annotation();
          annotation.setTimestamp(end);
          annotation.setEndpoint(endpoint);
          annotation.setType(AnnotationType.SERVER_SEND);
          span.addAnnotation(annotation);
          transfer.asyncSend(span);
      }

      this.removeParentSpan();
  }

  String genTracerId() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /*
      public void setTransfer(final Transfer transfer) {
          this.transfer = transfer;
      }
  */

  // =======================以下为提供给对外开放的方法=================================

   public void addBinaryAnnotation(final String className, final String methodName, final int duration) {
      final Span span = TraceContext.getParentSpan();
      if (span != null && StringUtils.isNotBlank(className) && StringUtils.isNotBlank(methodName) && duration >= 0) {
          final BinaryAnnotation binaryAnnotation = new BinaryAnnotation();
          binaryAnnotation.setKey(className);
          binaryAnnotation.setValue(methodName);
          binaryAnnotation.setDuration(duration);
          binaryAnnotation.setTimestamp(System.currentTimeMillis());
          binaryAnnotation.setEndpoint(span.getEndpoint());
          span.addBinaryAnnotation(binaryAnnotation);
      }
  }

  public void addBinaryAnnotation(final String key, final String value) {
      addBinaryAnnotation(key, value, 0);
  }

  public void addBinaryAnnotation(final String className, final String methodName, final Throwable ex) {
      final Span span = TraceContext.getParentSpan();
      if (span != null && ex != null) {
          final Endpoint endpoint = span.getEndpoint();
          if (endpoint != null) {
              final BinaryAnnotation exAnnotation = new BinaryAnnotation();
              exAnnotation.setThrowable(className, methodName, ex);
              exAnnotation.setEndpoint(endpoint);
              addBinaryAnntation(exAnnotation);
          }
      }
  }

  public String getTraceCtx() {
      String retStr = "";

      final Span span = TraceContext.getParentSpan();
      if (span != null) {
          final String traceId = span.getTraceId();
          final String parenSpantId = span.getParentId();
          final String spanId = span.getId();
          if (StringUtils.isNotBlank(traceId) && StringUtils.isNotBlank(spanId)) {
              final Map<String, String> retMap = new ConcurrentHashMap<String, String>();
              retMap.put(TracerConstant.TRACE_ID, traceId);
              retMap.put(TracerConstant.PARENT_SPAN_ID, parenSpantId);
              retMap.put(TracerConstant.SPAN_ID, spanId);

              retStr = JSON.toJSONString(retMap);
          }
      }

      return retStr;
  }

  public void setTraceCtx(final String ctx) {
      if (StringUtils.isNotBlank(ctx)) {
          final JSONObject jsonObject = JSON.parseObject(ctx);
          final String traceId = jsonObject.getString(TracerConstant.TRACE_ID);
          final String parenSpanId = jsonObject.getString(TracerConstant.PARENT_SPAN_ID);
          final String spanId = jsonObject.getString(TracerConstant.SPAN_ID);

          if (StringUtils.isNotBlank(traceId) && StringUtils.isNotBlank(spanId)) {
              final Span span = new Span();
              span.setTraceId(traceId);
              span.setParentId(parenSpanId);
              span.setId(spanId);

              TraceContext.setCtxThreadLocal(span);
          }
      }
  }

  Span getAndRemoveTraceCtx() {
    final Span retSpan = TraceContext.getCtxThreadLocal();
    TraceContext.removeCtxThreadLocal();
    return retSpan;
  }
}

package com.ziroom.godeye.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.ziroom.godeye.constant.TracerConstant;
import com.ziroom.godeye.entity.trace.Endpoint;
import com.ziroom.godeye.entity.trace.Span;
import com.ziroom.godeye.utils.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Activate(group = {Constants.PROVIDER, Constants.CONSUMER})
public class DubboFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DubboFilter.class);

  private static Tracer tracer;

  // 调用过程拦截
  public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
    if (tracer == null) {
      return invoker.invoke(invocation);
    }
    final long startTime = System.currentTimeMillis();
    final RpcContext context = RpcContext.getContext();

    // 传入参数，暂不做处理

    final String localIp = IpUtils.getRealIpWithStaticCache();
    final int localPort = context.getLocalPort();

    final URL url = context.getUrl();
    final String appName = url.getParameter("application");
    final String serviceName = url.getServiceInterface();
    final String methodName = context.getMethodName();
    final Endpoint endpoint = Endpoint.create(serviceName,localIp, localPort);

    final boolean isConsumerSide = context.isConsumerSide();
    Span span = null;
    try {
      if (isConsumerSide) { // 是否是消费者
        final Span parentSpan = tracer.getParentSpan();
        if (parentSpan == null) { // 为rootSpan
          // 生成root Span
          span = tracer.newSpan(appName, serviceName, methodName);
        } else {
          span = tracer.newSpan(appName, serviceName, methodName, parentSpan);
        }
      } else if (context.isProviderSide()) {
        final String traceId = invocation.getAttachment(TracerConstant.TRACE_ID);
        final String parentId = invocation.getAttachment(TracerConstant.PARENT_SPAN_ID);
        final String spanId = invocation.getAttachment(TracerConstant.SPAN_ID);
        final boolean sample = traceId != null;
        span = tracer.genSpan(appName, serviceName, methodName, traceId, parentId, spanId, sample);
      } else {
        LOGGER.error("[" + url + "] [notConsumerNorProvider]");
        return invoker.invoke(invocation);
      }

      invokerBefore(invocation, span, endpoint, startTime);
      final Result result = invoker.invoke(invocation);
      final Throwable throwable = result.getException();
      if (throwable != null && !isConsumerSide) {
        span.addException(serviceName, methodName, throwable, endpoint);
      }

      return result;
    } catch (final RpcException ex) {
      if (span != null) {
        span.addException(serviceName, methodName, ex, endpoint);
      }
      throw ex;
    } finally {
      if (span != null) {
        final long end = System.currentTimeMillis();
        invokerAfter(endpoint, span, end, isConsumerSide); // 调用后记录annotation
      }
    }
  }

  private void invokerAfter(
      final Endpoint endpoint, final Span span, final long end, final boolean isConsumerSide) {
    if (isConsumerSide) {
      tracer.clientReceiveRecord(span, endpoint, end);
    } else {
      //tracer.serverSendRecord(span, endpoint, end);
    }
  }

  private void invokerBefore(
      final Invocation invocation, final Span span, final Endpoint endpoint, final long start) {
    final RpcContext context = RpcContext.getContext();
    if (context.isConsumerSide()) {
      if (span.isSample()) {
        tracer.clientSendRecord(span, endpoint, start);

        final RpcInvocation rpcInvocation = (RpcInvocation) invocation;
        rpcInvocation.setAttachment(TracerConstant.PARENT_SPAN_ID, span.getParentId());
        rpcInvocation.setAttachment(TracerConstant.SPAN_ID, span.getId());
        rpcInvocation.setAttachment(TracerConstant.TRACE_ID, span.getTraceId());
      }
    } else if (context.isProviderSide()) {
      tracer.serverReceiveRecord(span, endpoint, start);
    }
  }

  // setter
  public static void setTracer(final Tracer tra) {
    tracer = tra;
  }
}

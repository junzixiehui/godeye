package com.ziroom.godeye;


import com.ziroom.godeye.entity.trace.Span;


public class TraceContext {

    // 传递parentSpan
    private static ThreadLocal<Span> spanThreadLocal = new InheritableThreadLocal<Span>();

    private static ThreadLocal<Span> ctxThreadLocal = new InheritableThreadLocal<Span>();

    public static void removeParentSpan() {
        spanThreadLocal.remove();
    }

    public static Span getParentSpan() {
        return spanThreadLocal.get();
    }

    public static void setParentSpan(final Span span) {
        spanThreadLocal.set(span);
    }

    public static void removeCtxThreadLocal() {
        ctxThreadLocal.remove();
    }

    public static Span getCtxThreadLocal() {
        return ctxThreadLocal.get();
    }

    public static void setCtxThreadLocal(final Span span) {
        ctxThreadLocal.set(span);
    }

    public static void clear(){
        spanThreadLocal.remove();
        ctxThreadLocal.remove();
    }

/*
    public static void start() {
        SPANS.set(new ArrayList<Span>());
    }
*/

   /* public static void print(){
        System.err.println("Current thread: " + Thread.currentThread() + ", trace context: traceId="
                + getTraceId() + ", spanId=" + getSpanId() + ", spans=" + getSpans());
    }*/
}

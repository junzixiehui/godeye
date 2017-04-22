package com.ziroom.godeye.entity.trace;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: by qlb
 * @Date: 2017/2/23  13:32
 * @Version: 1.0
 */
@Setter
@Getter
public class Span implements Serializable {


    private String traceId;
    private String id;
    private String parentId;

    private String appName;
    private String serviceName;
    private String methodName;

    private int subSpanNum;
    private int durationServer;
    private int durationClient;

    private boolean hasException = false;

    private boolean sample;


    private List<Annotation> annotations = Lists.newArrayList();
    private List<BinaryAnnotation> binaryAnnotations = Lists.newArrayList();

    public boolean isRootSpan() {
        return (parentId == null);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    public void addBinaryAnnotation(BinaryAnnotation annotation) {
        binaryAnnotations.add(annotation);
    }

    public boolean addException(String className, String methodName, Throwable ex, Endpoint endpoint) {
        this.hasException = true;
        BinaryAnnotation exAnnotation = new BinaryAnnotation();
        exAnnotation.setThrowable(className, methodName, ex);
        exAnnotation.setEndpoint(endpoint);
        addBinaryAnnotation(exAnnotation);
        return true;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + id.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Span)) {
            return false;
        }

        Span that = (Span) obj;
        return this.traceId.equals(that.traceId) && this.id.equals(that.id);
    }
}

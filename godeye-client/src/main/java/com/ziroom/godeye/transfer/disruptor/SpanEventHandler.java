package com.ziroom.godeye.transfer.disruptor;

import com.google.common.collect.Lists;
import com.lmax.disruptor.EventHandler;
import com.ziroom.godeye.entity.trace.Span;
import com.ziroom.godeye.transfer.DeliverService;
import com.ziroom.godeye.transfer.impl.HttpPostDeliverService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: 定义事件处理的具体实现
 * 通过实现接口 com.lmax.disruptor.EventHandler<T> 定义事件处理的具体实现。</p>
 *
 * @author: by qlb
 * @date: 2017/5/11  22:52
 * @version: 1.0
 */
public class SpanEventHandler implements EventHandler<SpanEvent> {


    private static final Logger LOGGER = LoggerFactory.getLogger(SpanEventHandler.class);

    private final List<Span> spanList = Lists.newArrayList();

    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_SO_TIMEOUT = 5000;
    private static final int DEFAULT_BATCH_SIZE = 32;
    private static final int DEFAULT_TPS_LIMIT = 2048;

    private DeliverService transferService;
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int tpsLimit = DEFAULT_TPS_LIMIT;

    private long lastRecordTime;
    private int spanNum;
    private int dropNum;

    public SpanEventHandler(final String url) {
        this(url, DEFAULT_CONNECT_TIMEOUT, DEFAULT_SO_TIMEOUT, DEFAULT_BATCH_SIZE, DEFAULT_TPS_LIMIT);
        lastRecordTime = System.currentTimeMillis() / 1000;
    }

    public SpanEventHandler(final String url, final int connectTimeout, final int soTimeout, final int batchSize,
                            final int tpsLimit) {
        if (StringUtils.isBlank(url)) {
            LOGGER.error("url shoud not empty!");
            throw new IllegalArgumentException("url shoud not empty!");
        }

        int finConnectTimeout = connectTimeout;
        int finSoTimeout = soTimeout;

        if (finConnectTimeout <= 0) {
            finConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
        }

        if (finSoTimeout <= 0) {
            finSoTimeout = DEFAULT_SO_TIMEOUT;
        }

        transferService = new HttpPostDeliverService(url, finConnectTimeout, finSoTimeout);

        if (batchSize > 0) {
            this.batchSize = batchSize;
        } else {
            this.batchSize = DEFAULT_BATCH_SIZE;
        }

        this.tpsLimit = tpsLimit;
        if (this.tpsLimit <= 0) {
            this.tpsLimit = DEFAULT_TPS_LIMIT;
        }
    }

    @Override
    public void onEvent(final SpanEvent spanEvent, final long sequence, final boolean endOfBatch) throws Exception {
        final long currentTime = System.currentTimeMillis() / 1000;

        if (n  != lastRecordTime) {
            lastRecordTime = currentTime;
            spanNum = 1;
            if (dropNum > 0) {
                LOGGER.warn("too fast,drop some message!{}", dropNum);
                dropNum = 0;
            }
        } else {
            spanNum++;
            if (spanNum > tpsLimit) {
                dropNum++;
                return;
            }
        }

        try {
            final Span span = spanEvent.getSpan();
            spanList.add(span);

            if (endOfBatch || spanList.size() >= batchSize) {
                transferService.deliver(spanList);
                spanList.clear();
            }

        } catch (Exception ex) {
            LOGGER.error("CreateHttpConnection error", ex);
        }
    }
}

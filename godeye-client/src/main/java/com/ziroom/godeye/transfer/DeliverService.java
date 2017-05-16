package com.ziroom.godeye.transfer;

import com.ziroom.godeye.entity.trace.Span;

import java.util.List;

/**
 * <p>Description: </p>
 *
 * @author: by qlb
 * @date: 2017/5/12  21:45
 * @version: 1.0
 */
public interface DeliverService {

    boolean deliver(Span span);
    boolean deliver(List<Span> spanList);
}

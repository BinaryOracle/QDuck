package com.dhy.nio.handler;

import com.dhy.nio.domain.Msg;

/**
 * @author 大忽悠
 * @create 2022/6/1 20:33
 */
public interface Handler {
    /**
     * 是否是当前处理器支持解析的类型
     */
    boolean support(Msg msg);
}

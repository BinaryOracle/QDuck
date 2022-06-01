package com.dhy.nio.handler;

import com.dhy.nio.domain.Msg;

/**
 * <p>
 *     入站处理器
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 20:24
 */
public interface InHandler extends Handler {
    /**
     * 处理客户端传过来的数据
     */
    Msg doHandleDataIn(Msg msg);

    /**
      * 如果支持当前消息格式处理,那么就处理后返回,否则返回原本消息格式
     */
    default Msg handleDataIn(Msg msg){
        return support(msg)?doHandleDataIn(msg):msg;
    }
}

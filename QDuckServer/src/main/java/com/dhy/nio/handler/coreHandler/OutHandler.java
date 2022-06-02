package com.dhy.nio.handler.coreHandler;

import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;

/**
 * <p>
 *     出站处理器
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 20:25
 */
public interface OutHandler extends Handler {
    /**
     * 处理要写给客户端的数据
     */
    Msg doHandleDataOut(Msg msg, Attr attr);

    /**
     * 如果支持当前消息格式处理,那么就处理后返回,否则返回原本消息格式
     */
    default Msg handleDataOut(Msg msg,Attr attr){
        return support(msg)?doHandleDataOut(msg,attr):msg;
    }
}

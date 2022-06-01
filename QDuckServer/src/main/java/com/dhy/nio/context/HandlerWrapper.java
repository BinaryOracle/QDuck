package com.dhy.nio.context;

import com.dhy.nio.domain.Msg;
import com.dhy.nio.handler.*;
import lombok.Builder;
import lombok.Data;

/**
 * @author 大忽悠
 * @create 2022/6/1 20:43
 */
@Data
@Builder
public class HandlerWrapper {
    private Handler curHandler;
    private HandlerWrapper next;
    private HandlerWrapper prev;

    /**
     * 如果当前处理器不是入站处理器,那么调用下一个
     */
    public void invokeInHandler(Msg msg){
        if(curHandler instanceof InHandler) {
            msg = ((InHandler) curHandler).handleDataIn(msg);
        }
        //触发调用链下一条
        next.invokeInHandler(msg);
    }

    /**
     * 如果当前处理器不是出站处理器,那么调用下一个
     */
    public void invokeOutHandler(Msg msg){
        if(curHandler instanceof OutHandler) {
            msg = ((OutHandler) curHandler).handleDataOut(msg);
        }
        //触发调用链下一条
        next.invokeOutHandler(msg);
    }


    public void addAfter(HandlerWrapper handlerWrapper){
         next=handlerWrapper;
    }

    public void addBefore(HandlerWrapper handler){
        prev=handler;
    }
}

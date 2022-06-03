package com.dhy.nio.context;

import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.context.handler.coreHandler.Handler;
import com.dhy.nio.context.handler.coreHandler.InHandler;
import com.dhy.nio.context.handler.coreHandler.OutHandler;
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
    public void invokeInHandler(Msg msg, Attr attr){
        if(curHandler instanceof InHandler) {
            msg = ((InHandler) curHandler).handleDataIn(msg,attr);
        }
        //如果其中一个处理器返回Null,那么直接中断当前处理器链的执行
        if(msg==null || next==null){
            return;
        }
        //触发调用链下一条
        next.invokeInHandler(msg,attr);
    }

    /**
     * 如果当前处理器不是出站处理器,那么调用下一个
     */
    public void invokeOutHandler(Msg msg, Attr attr){
        if(curHandler instanceof OutHandler) {
            msg = ((OutHandler) curHandler).handleDataOut(msg,attr);
        }
        //如果其中一个处理器返回Null,那么直接中断当前处理器链的执行
        if(msg==null || next==null){
            return;
        }
        //触发调用链下一条
        next.invokeOutHandler(msg,attr);
    }

    public void addBefore(HandlerWrapper handler){
        prev.setNext(handler);
        prev=handler;
    }
}

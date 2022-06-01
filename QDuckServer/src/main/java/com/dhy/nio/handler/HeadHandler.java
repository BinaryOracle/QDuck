package com.dhy.nio.handler;

import com.dhy.nio.domain.Msg;

/**
 * <p>
 *     进行数据准备工作
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 20:50
 */
public class HeadHandler implements InHandler,OutHandler{
    @Override
    public boolean support(Msg msg) {
        return true;
    }

    @Override
    public Msg doHandleDataIn(Msg msg) {
        return msg;
    }


    @Override
    public Msg doHandleDataOut(Msg msg) {
        return msg;
    }
}

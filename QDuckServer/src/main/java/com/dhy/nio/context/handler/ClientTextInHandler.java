package com.dhy.nio.context.handler;

import com.dhy.nio.context.handler.coreHandler.InHandler;
import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import lombok.extern.slf4j.Slf4j;

import static com.dhy.nio.constants.MessageType.COMMON_MSG;
import static com.dhy.nio.constants.MsgType.STRING;

/**
 * @author 大忽悠
 * @create 2022/6/4 10:48
 */
@Slf4j
public class ClientTextInHandler implements InHandler {
    /**
     * 是否是当前处理器支持解析的类型
     */
    @Override
    public boolean support(Msg msg) {
        return msg.getType()==STRING && msg.getMsgType()==COMMON_MSG;
    }

    /**
     * 处理服务器传过来的数据
     */
    @Override
    public Msg doHandleDataIn(Msg msg, Attr attr) {
        String str = msg.getString();
        log.info("收到{}发来的消息: {}",msg.getMe(),str);
        return null;
    }
}

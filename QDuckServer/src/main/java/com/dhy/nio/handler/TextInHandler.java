package com.dhy.nio.handler;

import com.dhy.nio.domain.Msg;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.dhy.nio.constants.MsgType.STRING;

/**
 * <p>
 *     处理入站类型为字符串的MSG
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 20:25
 */
@Data
@Slf4j
public class TextInHandler implements InHandler{
    /**
     * 是否是当前处理器支持解析的类型
     */
    @Override
    public boolean support(Msg msg) {
        return msg.getType() == STRING;
    }

    /**
     * 处理客户端传过来的数据
     */
    @Override
    public Msg doHandleDataIn(Msg msg) {
        String data = new String(msg.getData(), msg.getCharset());
        msg.setAfterParse(data);
        log.info("客户端发送来的数据为: {}",data);
        return msg;
    }
}

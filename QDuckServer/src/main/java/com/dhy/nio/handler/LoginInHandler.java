package com.dhy.nio.handler;

import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.handler.coreHandler.InHandler;
import com.dhy.nio.online.OnlineUsers;
import com.dhy.nio.constants.MessageType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.dhy.nio.constants.MsgType.STRING;

/**
 * <p>
 *     处理消息类型为登录的消息
 * </p>
 * @author 大忽悠
 * @create 2022/6/2 8:44
 */
@Data
@Slf4j
public class LoginInHandler implements InHandler {
    /**
     * 是否是当前处理器支持解析的类型
     */
    @Override
    public boolean support(Msg msg) {
        return msg.getType()==STRING && msg.getMsgType()== MessageType.LOGIN;
    }

    /**
     * 保存用户登录信息到当前在线用户列表中去
     */
    @Override
    public Msg doHandleDataIn(Msg msg, Attr attr) {
        String username = msg.getString();
        OnlineUsers.addOneOnlineUser(username,msg.getSocketChannel());
        //查询当前用户是否存在未读消息
        log.info("用户: {} 已经登录",username);
        return null;
    }
}

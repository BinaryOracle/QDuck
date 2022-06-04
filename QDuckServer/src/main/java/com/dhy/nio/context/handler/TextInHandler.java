package com.dhy.nio.context.handler;

import com.dhy.nio.db.RedisDb;
import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.context.handler.coreHandler.InHandler;
import com.dhy.nio.online.OnlineUsers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


import static com.dhy.nio.constants.MessageType.COMMON_MSG;
import static com.dhy.nio.constants.MsgType.STRING;
import static com.dhy.nio.context.handler.util.MsgUtil.forwardTextMsgToUser;

/**
 * <p>
 * 处理入站类型为字符串的MSG
 * </p>
 *
 * @author 大忽悠
 * @create 2022/6/1 20:25
 */
@Data
@Slf4j
public class TextInHandler implements InHandler {
    /**
     * 当前处理器只能处理类型为字符串并且消息类型为普通消息
     */
    @Override
    public boolean support(Msg msg) {
        return msg.getType() == STRING && msg.getMsgType() == COMMON_MSG;
    }

    /**
     * 判断消息是发送给哪个客户端的
     */
    @Override
    public Msg doHandleDataIn(Msg msg, Attr attr) {
        //获取消息是发送给谁的
        String toUser = msg.getToUsername();
        //查询用户是否存在--去数据查询
        RedisDb redisDb = (RedisDb) attr.getAttr(Attr.REDIS_ATTR);
        if (!redisDb.isUserExist(toUser)) {
            //发送错误信息回复用户,告知对应的用户不存在
            forwardTextMsgToUser(msg.getSocketChannel(), "不能将消息转发给不存在的用户",redisDb,msg.getMe(),"server");
            log.info("客户端[{}]所要通知的用户不存在: {}",msg.getMe(), toUser);
        } else {
            //将消息发送给对应的用户,并且保存消息到数据库
            String toUserMsg = msg.getString();
            //判断当前用户是否在线,如果在线就直接发送给他,否则不发送
            forwardTextMsgToUser(OnlineUsers.searchOneOnlineUser(toUser), toUserMsg,redisDb,toUser,msg.getMe());
            log.info("客户端[{}]发送给{}的消息为: {}", msg.getMe(),toUser,toUserMsg);
        }
        return msg;
    }

}

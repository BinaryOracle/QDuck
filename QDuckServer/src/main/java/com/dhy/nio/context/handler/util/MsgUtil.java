package com.dhy.nio.context.handler.util;

import com.dhy.nio.db.RedisDb;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.message.builder.MessageBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static com.dhy.nio.constants.MsgType.STRING;

/**
 * @author 大忽悠
 * @create 2022/6/2 10:36
 */
@Slf4j
public class MsgUtil {
    private static final MessageBuilder MESSAGE_BUILDER=new MessageBuilder();
    private static final ByteBuffer WRITE_BUFFER=ByteBuffer.allocate(1024*1024);
    /**
     * 转发文本消息给指定用户
     * publisher为null,说明是服务器发送给用户的信息
     */
    public static void forwardTextMsgToUser(SocketChannel socketChannel, String msg, RedisDb redisDb, String receiver, String publisher) {
            //用户在线---直接发送
            if (socketChannel != null) {
                //服务器将消息转发给客户端--遵循QDuck报文协议
                MESSAGE_BUILDER.sendMessage(socketChannel,WRITE_BUFFER,publisher,receiver,msg.getBytes(StandardCharsets.UTF_8),STRING);
                //保存为已读消息
                redisDb.saveOneReadMsg(publisher, receiver, msg);
                return;
            }
            //保存数据库,设置为未读消息
            redisDb.saveOneUnReadMsg(receiver,publisher==null?"server":publisher,msg);
    }
}

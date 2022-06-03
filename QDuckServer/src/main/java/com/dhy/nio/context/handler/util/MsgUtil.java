package com.dhy.nio.context.handler.util;

import com.dhy.nio.db.RedisDb;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author 大忽悠
 * @create 2022/6/2 10:36
 */
@Slf4j
public class MsgUtil {
    /**
     * 转发消息给指定用户
     * publisher为null,说明是服务器发送给用户的信息
     */
    public static void forwardMsgToUser(SocketChannel socketChannel, String msg, RedisDb redisDb,String receiver,String publisher) {
        try {
            //用户在线---直接发送
            if (socketChannel != null) {
                //TODO:没有遵守协议--待完善
                ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8));
                while (buffer.hasRemaining()) {
                    socketChannel.write(buffer);
                }
                //保存为已读消息
                redisDb.saveOneReadMsg(publisher, receiver, msg);
                return;
            }
            //保存数据库,设置为未读消息
            redisDb.saveOneUnReadMsg(receiver,publisher==null?"server":publisher,msg);
        } catch (IOException e) {
            log.error("转发消息出错: ",e);
        }
    }
}

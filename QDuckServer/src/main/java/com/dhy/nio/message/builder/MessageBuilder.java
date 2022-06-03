package com.dhy.nio.message.builder;

import com.dhy.nio.constants.MsgType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static com.dhy.nio.constants.Charset.UTF8;
import static com.dhy.nio.constants.MessageProtocolConstants.MAGIC;
import static com.dhy.nio.constants.MessageType.COMMON_MSG;
import static com.dhy.nio.constants.MessageType.LOGIN;
import static com.dhy.nio.util.ByteUtil.int2byte;

/**
 * <P>
 *     构建QDuck报文协议并发送
 * </P>
 * @author 大忽悠
 * @create 2022/6/3 9:59
 */
@Slf4j
public class MessageBuilder {
    /**
     * 发送登录报文
     */
    public void sendLoginMessage(SocketChannel sc, ByteBuffer wf,String curUserName){
        try {
            wf.clear();
            //魔数--3字节
            wf.put(MAGIC.getBytes());
            //消息类型---1字节
            wf.put(LOGIN);
            //当前用户名长度---4字节
            wf.put(int2byte(curUserName.length()));
            //当前用户名
            wf.put(curUserName.getBytes(StandardCharsets.UTF_8));
            //切换读模式
            wf.flip();
            //写出数据
            sc.write(wf);
            //清空缓冲区
            wf.clear();
            log.info("send login message success !");
        } catch (IOException e) {
           log.error("writer login message error: ",e);
        }
    }

    /**
     * 构建并发送QDuck通用报文
     */
    public void sendMessage(SocketChannel sc, ByteBuffer wf, String curUserName, String toUser, byte[] data, byte msgType){
        try {
            wf.clear();
            //魔数--3字节
            wf.put(MAGIC.getBytes());
            //消息类型---1字节
            wf.put(COMMON_MSG);
            //后面数据总长度--4字节
            wf.put(int2byte(curUserName.length() + toUser.length() + data.length + 14));
            //当前用户名长度---4字节
            wf.put(int2byte(curUserName.length()));
            //当前用户名
            wf.put(curUserName.getBytes(StandardCharsets.UTF_8));
            //接收方的用户名长度---4字节
            wf.put(int2byte(toUser.length()));
            //接收方用户名
            wf.put(toUser.getBytes(StandardCharsets.UTF_8));
            //数据类型--1字节
            wf.put(msgType);
            //字符串编码--1字节
            wf.put(UTF8);
            //消息长度--4字节
            wf.put(int2byte(data.length));
            //文本消息
            wf.put(data);
            //切换读模式
            wf.flip();
            //输出
            sc.write(wf);
            //清空缓冲区
            wf.clear();
            log.info("send message to {} success !",toUser);
        } catch (IOException e) {
           log.error("send message to {} error !",toUser,e);
        }
    }
}

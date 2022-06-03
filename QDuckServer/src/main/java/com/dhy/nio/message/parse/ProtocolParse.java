package com.dhy.nio.message.parse;

import com.dhy.nio.domain.Msg;
import com.dhy.nio.constants.MessageType;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;


import static com.dhy.nio.constants.Charset.GBK;
import static com.dhy.nio.constants.Charset.UTF8;
import static com.dhy.nio.constants.MessageProtocolConstants.MAGIC;
import static com.dhy.nio.constants.MessageType.PONG;
import static com.dhy.nio.constants.MsgType.*;
import static com.dhy.nio.util.ByteUtil.byte2int;

/**
 * <p>
 * 对QDuck报文协议进行解析
 * </p>
 *
 * @author 大忽悠
 * @create 2022/6/1 22:47
 */
@Slf4j
public class ProtocolParse {
    /**
     * 解析客户端发送来的消息
     * read是buffer里面有多少字节的数据
     */
    public static Msg parse(ByteBuffer buffer, SocketChannel sc, Integer read) {
        if (read < 8 && buffer.limit()-buffer.position()<8) {
            buffer.compact();
            return null;
        }
        //解析魔数
        byte[] magic = new byte[3];
        buffer.get(magic);
        String magicStr = new String(magic);
        if (!magicStr.equals(MAGIC)) {
            log.error("magic字段为非法值: {}", magicStr);
            return null;
        }
        //解析消息类型
        return doParseByMsgType(buffer, sc);
    }

    /**
     * 按照消息的类型进行解析
     */
    public static Msg doParseByMsgType(ByteBuffer buffer, SocketChannel sc) {
        byte msgType = buffer.get();
        //校验数据完整性--pong报文不需要
        if (msgType!=PONG&&!integrityVerify(buffer)) {
            return null;
        }
        Msg res;
        switch (msgType) {
            case MessageType.LOGIN: {
                //将pos指针前移四个字节
                buffer.position(buffer.position()-4);
                res=loginMsgParse(buffer, sc);
                 break;
            }
            case MessageType.COMMON_MSG: {
                res=commonMsgParse(buffer, sc);
                break;
            }
            case PONG: {
                res=pongMsgParse(buffer, sc);
                break;
            }
            default: {
                log.error("客户端发送消息类型解析异常");
                return null;
            }
        }
        //可能本次缓冲区还有下一次客户端提前发来的数据
        //不能直接清空,而是将没读完的数据移动到头部
        buffer.compact();
        return res;
    }

    private static boolean integrityVerify(ByteBuffer buffer) {
        //按照消息类型解析前,先判断数据是否传送完整,如果不完整,就不进行解析
        byte[] len = new byte[4];
        buffer.get(len);
        int leftLen = byte2int(len);
        //判断如果缓冲区中剩余数据没有指定字节大小
        //那么将缓冲区切换为写模式,然后返回null
        int bufferLeftLen= buffer.limit()- buffer.position();
        if(leftLen>bufferLeftLen){
            buffer.position(buffer.limit());
            buffer.limit(buffer.capacity());
            return false;
        }
        return true;
    }

    /**
     * 解析心跳响应包
     */
    private static Msg pongMsgParse(ByteBuffer buffer, SocketChannel sc) {
        return Msg.buildDefaultMsg().msgType(PONG).socketChannel(sc).build();
    }

    /**
     * 对用户名采用UTF-8编码
     */
    private static Msg loginMsgParse(ByteBuffer buffer, SocketChannel sc) {
        byte[] bytes = readTargetLenData(buffer);
        String username = new String(bytes, Charset.defaultCharset());
        return Msg.buildDefaultMsg().msgType(MessageType.LOGIN).len(username.length()).string(username).socketChannel(sc).build();
    }

    /**
     * 普通消息解析
     */
    private static Msg commonMsgParse(ByteBuffer buffer, SocketChannel sc) {
        //解析出publisher的用户名
        String publisherName = getName(buffer);
        String receiverName = getName(buffer);
        //根据不同的数据类型进行解析: 字符串,图片 or 视频
        byte dataType = buffer.get();
        switch (dataType) {
            case STRING: {
                return doResolveString(buffer,publisherName,receiverName,sc);
            } case IMG: {
                return null;
            } case VIDEO: {
                return null;
            } case FILE: {
                return null;
            } default: {
                log.error("客户端发送消息类型解析异常");
                return null;
            }
        }
    }

    private static Msg doResolveString(ByteBuffer buffer,String pub,String rec,SocketChannel sc){
        //解析出字符串的编码格式
        byte charSetType=buffer.get();
        //解析出数据对应的byte数组
        byte[] dataBytes = readTargetLenData(buffer);
        String res;
        switch (charSetType){
               case UTF8:{
                   res= new String(dataBytes,Charset.defaultCharset());
                   break;
               } case GBK:{
                   res=new String(dataBytes,Charset.forName("GBK"));
                    break;
               } default:{
                   log.error("发现不支持的字符串编码格式");
                   return null;
               }
           }
        return Msg.buildDefaultMsg().string(res)
               .me(pub)
               .toUsername(rec)
               .len(rec.length())
                .socketChannel(sc)
                .build();
    }

    private static String getName(ByteBuffer buffer) {
        byte[] len=new byte[4];
        buffer.get(len);
        byte[] name = new byte[byte2int(len)];
        buffer.get(name);
        return new String(name);
    }

    private static byte[] readTargetLenData(ByteBuffer buffer) {
           //解析数据长度
            byte[] len = new byte[4];
            buffer.get(len);
            //读取指定长度的数据
            byte[] data = new byte[byte2int(len)];
            buffer.get(data);
            return data;
    }

}

package com.dhy.nio.constants;

/**
 * <p>
 *     报文类型
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 23:02
 */
public class MessageType {
    /**
     * 普通消息
     */
    public static final byte COMMON_MSG=1;
    /**
     * 心跳包: PING消息
     */
    public static final byte PING=2;
    /**
     * 心跳包: PONG消息
     */
    public static final byte PONG=3;
    /**
     * 登录消息
     */
    public static final byte LOGIN=4;
}

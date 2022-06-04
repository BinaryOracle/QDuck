package com.dhy.nio.eventLoop.coreEventLoop;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author 大忽悠
 * @create 2022/6/1 11:58
 */
public interface EventLoop extends Runnable {
    /**
     * 注册感兴趣的事件
     */
    default void register(SocketChannel sc){};
    default void register(){};
}

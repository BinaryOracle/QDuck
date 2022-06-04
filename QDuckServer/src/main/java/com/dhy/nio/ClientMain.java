package com.dhy.nio;

import com.dhy.nio.context.handler.ClientTextInHandler;
import com.dhy.nio.eventLoop.ClientEventLoop;
import com.dhy.nio.context.handler.ClientOutHandler;

/**
 * <p>
 *     客户端启动--QDuckClient是用于测试,使用BIO,这里使用NIO完成
 *     注意: 客户端写线程和读线程需要区分开来,即ClientOutHandler运行在单独的写线程中
 * </p>
 * @author 大忽悠
 * @create 2022/6/3 9:36
 */
public class ClientMain {
    public static void main(String[] args) {
        ClientEventLoop clientEventLoop = new ClientEventLoop();
        clientEventLoop.addInHandler(new ClientTextInHandler()).addOutHandler(new ClientOutHandler()).invokeOutHandler().start();
    }
}

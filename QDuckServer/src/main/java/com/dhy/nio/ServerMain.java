package com.dhy.nio;

import com.dhy.nio.eventLoop.BossEventLoop;
import com.dhy.nio.context.handler.LoginInHandler;
import com.dhy.nio.context.handler.TextInHandler;

/**
 * @author 大忽悠
 * @create 2022/6/1 11:29
 */
public class ServerMain {
    public static void main(String[] args) {
        BossEventLoop bossEventLoop = new BossEventLoop();
        bossEventLoop.addInHandler(new LoginInHandler()).addInHandler(new TextInHandler()).start();
    }
}

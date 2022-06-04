package com.dhy.nio.eventLoop.rejectPolicy;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * @author 大忽悠
 * @create 2022/6/3 21:01
 */
@Slf4j
public class AbandonPolicy implements RejectPolicy{
    @Override
    public void handleReject(SocketChannel sc) {
        try {
            log.error("客户端[{}]的连接请求因为服务器超载运行而被抛弃",sc.getRemoteAddress());
        } catch (IOException e) {
            log.error("获取client地址出错: ",e);
        }
    }
}

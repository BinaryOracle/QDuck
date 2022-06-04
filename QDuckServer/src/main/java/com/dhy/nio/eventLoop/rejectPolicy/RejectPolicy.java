package com.dhy.nio.eventLoop.rejectPolicy;

import java.nio.channels.SocketChannel;

/**
 * @author 大忽悠
 * @create 2022/6/3 21:01
 */
public interface RejectPolicy {
    void handleReject(SocketChannel sc);
}

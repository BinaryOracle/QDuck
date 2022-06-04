package com.dhy.nio.eventLoop;

import com.dhy.nio.context.HandlerContext;
import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.context.handler.coreHandler.InHandler;
import com.dhy.nio.context.handler.coreHandler.OutHandler;
import com.dhy.nio.eventLoop.coreEventLoop.SingleEventLoop;
import com.dhy.nio.eventLoop.util.ThreadNameGenerator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static com.dhy.nio.domain.Attr.REDIS_ATTR;
import static com.dhy.nio.domain.Attr.SOCKET_CHANNEL;

/**
 * <p>
 * 客户端连接服务器使用的事件循环
 * </p>
 *
 * @author 大忽悠
 * @create 2022/6/3 9:34
 */
@Slf4j
public class ClientEventLoop extends WorkerEventLoop {
    private SocketChannel sc;
    private static final String DEFAULT_HOST = "localhost";
    private static final Integer DEFAULT_PORT = 5200;

    /**
     * 采用默认端口: 5200 和 默认主机 localhost进行连接
     */
    public ClientEventLoop() {
        super(ThreadNameGenerator.generatorUniqueThreadName());
        try {
            //初始化父类组件
            super.init();
            //设置处理器环境上下文
            setHandlerContext(new HandlerContext());
            sc = SocketChannel.open(new InetSocketAddress(DEFAULT_HOST, DEFAULT_PORT));
            sc.configureBlocking(false);
            log.info("connect to server success !");
        } catch (IOException e) {
            throw new RuntimeException("服务器连接失败");
        }
    }

    /**
     * 指定主机和端口号
     */
    public ClientEventLoop(String host, Integer port) {
        super(ThreadNameGenerator.generatorUniqueThreadName());
        try {
            //初始化父类组件
            super.init();
            //设置处理器环境上下文
            setHandlerContext(new HandlerContext());
            sc = SocketChannel.open(new InetSocketAddress(host, port));
            sc.configureBlocking(false);
            log.info("connect to server success !");
        } catch (IOException e) {
            throw new RuntimeException("服务器连接失败");
        }
    }

    /**
     * 启动组件
     */
    @Override
    public void start() {
        log.info("client starting ... ");
        setIsCoreThread(true);
        super.register(sc);
    }


    public ClientEventLoop addInHandler(InHandler handler) {
        handlerContext.addInHandlers(handler);
        return this;
    }

    public ClientEventLoop addOutHandler(OutHandler handler) {
        handlerContext.addOutHandlers(handler);
        return this;
    }

    /**
     * 开启单独的写线程来触发客户端的写处理器
     */
    public ClientEventLoop invokeOutHandler() {
        log.info("invoke client out handler");
        Attr attr = new Attr();
        attr.addAttr(SOCKET_CHANNEL, sc);
        attr.addAttr(REDIS_ATTR, redisDb);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                handlerContext.invokeOutHandlers(Msg.builder().build(), attr);
            }
        });
        return this;
    }
}

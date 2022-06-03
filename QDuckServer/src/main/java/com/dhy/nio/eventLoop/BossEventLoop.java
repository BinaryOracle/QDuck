package com.dhy.nio.eventLoop;

import com.dhy.nio.context.HandlerContext;
import com.dhy.nio.eventLoop.coreEventLoop.GroupEventLoop;
import com.dhy.nio.context.handler.coreHandler.InHandler;
import com.dhy.nio.context.handler.coreHandler.OutHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import static com.dhy.nio.constants.ConfigConstants.PORT;

/**
 * @author 大忽悠
 * @create 2022/6/1 12:00
 */
@Slf4j
public class BossEventLoop extends GroupEventLoop {
    /**
     * boss线程选择器,用于接收客户端的连接
     */
    private Selector boss;
    /**
     * 服务端Boss线程是否启动
     */
    private volatile boolean start = false;
    /**
     * 关闭Boss线程
     */
    private volatile boolean stop = false;
    /**
     * 处理器环境上下文
     */
    private final HandlerContext handlerContext=new HandlerContext();

    /**
     * 注册感兴趣的事件
     */
    @Override
    public void register() {
            //防止服务端重复启动
            if (!start)
            {
                init();
                //启动Boss线程
                executorService.execute(this);
                log.debug("boss start...");
                //请求成功，设置标记
                start = true;
            }
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                //Boss选择器阻塞接收客户端连接请求
                boss.select();
                Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
                while (iter.hasNext())
                {
                    SelectionKey key = iter.next();
                    //将处理完的事件移除，防止死循环产生
                    iter.remove();
                    //客户端连接事件
                    if (key.isAcceptable())
                    {
                        //接收客户端的连接
                        ServerSocketChannel c = (ServerSocketChannel) key.channel();
                        SocketChannel sc = c.accept();
                        //设置客户端为非阻塞模式
                        sc.configureBlocking(false);
                        log.debug("{} connected", sc.getRemoteAddress());
                        WorkerEventLoop workerEventLoop = new WorkerEventLoop();
                        workerEventLoop.init();
                        workerEventLoop.setHandlerContext(handlerContext);
                        workerEventLoop.register(sc);
                        //每个客户端都从线程池中选出一个线程进行监听,直到客户端关闭连接
                        groupPool.execute(workerEventLoop);
                    }
                }
            } catch (IOException e) {
                log.error("Boss select error: ",e);
            }
        }
    }

    /**
     * 初始化组件
     */
    @Override
    public void init() {
        try {
            super.init();
            log.info("Boss init...");
            //创建一个ServerSocketChannel通道
            ServerSocketChannel ssc = ServerSocketChannel.open();
            //绑定端口
            Integer port = Integer.valueOf(qDuckConfig.getValue(PORT));
            ssc.bind(new InetSocketAddress(port));
            log.info("服务器端绑定端口号为: {}",port);
            //设置为非阻塞
            ssc.configureBlocking(false);
            //创建选择器
            boss = Selector.open();
            //当前ServerSocketChannel通道注册到Boss选择器上
            SelectionKey ssckey = ssc.register(boss, 0, null);
            //服务端通道用来接收客户端的连接
            ssckey.interestOps(SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            log.error("Boss init error: ",e);
        }
    }

    /**
     * 启动组件---启动Boss线程,初始化工作线程
     */
    @Override
    public void start() {
       register();
    }

    /**
     * 停止组件
     */
    @Override
    public void stop() {
        groupPool.shutdown();
        executorService.shutdown();
    }

    public BossEventLoop addInHandler(InHandler handler){
        handlerContext.addInHandlers(handler);
        return this;
    }

    public BossEventLoop addOutHandler(OutHandler handler){
        handlerContext.addOutHandlers(handler);
        return this;
    }

    /**
     * 关闭服务器
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }
}

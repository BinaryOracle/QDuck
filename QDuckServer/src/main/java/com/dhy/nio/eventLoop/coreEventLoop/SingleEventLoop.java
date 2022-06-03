package com.dhy.nio.eventLoop.coreEventLoop;



import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * <p>
 *     单线程事件循环
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 17:02
 */
public abstract class SingleEventLoop extends AbstractEventLoop {
    protected ExecutorService executorService = Executors.newFixedThreadPool(1,new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"Single-Thread-"+ UUID.randomUUID().toString().substring(0,10));
                }
            });
}

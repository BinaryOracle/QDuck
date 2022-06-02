package com.dhy.nio.eventLoop;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dhy.nio.constants.ConfigConstants.*;

/**
 * <p>
 *     事件组循环---即提供线程池功能
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 17:00
 */
@Slf4j
public abstract class GroupEventLoop extends SingleEventLoop{
    private  Integer core_pool_size;
    private  Integer max_thread_size;
    private  Integer alive_time;
    private  Integer queue_max_size;
    private  String reject_policy;
    protected ThreadPoolExecutor groupPool;

    /**
     * 初始化组件
     * TODO: 默认暂时只提供了拒绝策略
     */
    @Override
    public void init() {
        super.init();
        core_pool_size= Integer.valueOf(qDuckConfig.getValue(GROUP_CORE_POOL_SIZE));
        core_pool_size=core_pool_size==-1?Runtime.getRuntime().availableProcessors()+1:core_pool_size;
        max_thread_size= Integer.valueOf(qDuckConfig.getValue(GROUP_MAX_POOL_SIZE));
        alive_time= Integer.valueOf(qDuckConfig.getValue(GROUP_KEEP_ALIVE_TIME));
        queue_max_size= Integer.valueOf(qDuckConfig.getValue(GROUP_QUEUE_MAX_SIZE));
        reject_policy= qDuckConfig.getValue(GROUP_REJECTION_POLICY);
        groupPool=new ThreadPoolExecutor(core_pool_size,max_thread_size,alive_time, TimeUnit.SECONDS
                ,new LinkedBlockingDeque<>(queue_max_size),new ThreadFactory() {
            private AtomicInteger add=new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"Group-Thread-"+add.getAndIncrement());
            }
        },new ThreadPoolExecutor.AbortPolicy());
        log.info("初始化工作线程: core_pool_size={} , max_thread_size={} ," +
                "alive_time={} , queue_max_size={} , " +
                "reject_policy={} ",core_pool_size,max_thread_size,alive_time,queue_max_size,reject_policy);
    }

}

package com.dhy.nio.eventLoop.coreEventLoop;

import com.dhy.nio.config.QDuckConfig;
import com.dhy.nio.context.HandlerContext;
import com.dhy.nio.db.RedisDb;
import com.dhy.nio.lifecycle.Lifecycle;
import lombok.Data;

import static com.dhy.nio.constants.ConfigConstants.*;


/**
 * @author 大忽悠
 * @create 2022/6/1 17:07
 */
@Data
public abstract class AbstractEventLoop extends Thread implements EventLoop, Lifecycle {
    protected QDuckConfig qDuckConfig;
    protected RedisDb redisDb;
    protected HandlerContext handlerContext;


    public AbstractEventLoop() {
    }

    public AbstractEventLoop(String name) {
        super(name);
    }

    /**
     * 初始化组件
     */
    @Override
    public void init() {
        qDuckConfig=new QDuckConfig();
        handlerContext=new HandlerContext();
        redisDb=new RedisDb(qDuckConfig.getValue(REDIS_HOST),Integer.parseInt(qDuckConfig.getValue(REDIS_PORT)),qDuckConfig.getValue(REDIS_PWD));
    }

    /**
     * 启动组件
     */
    @Override
    public void start() {
        super.start();
    }

    /**
     * 停止组件
     */
    @Override
    public void over() {

    }
}

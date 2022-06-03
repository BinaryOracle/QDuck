package com.dhy.nio.eventLoop.coreEventLoop;

import com.dhy.nio.config.QDuckConfig;
import com.dhy.nio.db.RedisDb;
import com.dhy.nio.lifecycle.Lifecycle;

import static com.dhy.nio.constants.ConfigConstants.*;


/**
 * @author 大忽悠
 * @create 2022/6/1 17:07
 */
public abstract class AbstractEventLoop implements EventLoop, Lifecycle {
    protected final QDuckConfig qDuckConfig=new QDuckConfig();
    protected RedisDb redisDb;

    /**
     * 初始化组件
     */
    @Override
    public void init() {
      redisDb=new RedisDb(qDuckConfig.getValue(REDIS_HOST),Integer.parseInt(qDuckConfig.getValue(REDIS_PORT)),qDuckConfig.getValue(REDIS_PWD));
    }

    /**
     * 启动组件
     */
    @Override
    public void start() {

    }

    /**
     * 停止组件
     */
    @Override
    public void stop() {

    }
}

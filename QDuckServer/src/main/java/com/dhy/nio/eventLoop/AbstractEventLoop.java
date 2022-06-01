package com.dhy.nio.eventLoop;

import com.dhy.nio.config.QDuckConfig;
import com.dhy.nio.lifecycle.Lifecycle;


/**
 * @author 大忽悠
 * @create 2022/6/1 17:07
 */
public abstract class AbstractEventLoop implements EventLoop, Lifecycle {
    protected final QDuckConfig qDuckConfig=new QDuckConfig();

    /**
     * 初始化组件
     */
    @Override
    public void init() {

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

package com.dhy.nio.lifecycle;

/**
 * <p>生命周期接口</p>
 * @author 大忽悠
 * @create 2022/6/1 12:03
 */
public interface Lifecycle {
    /**
     * 初始化组件
     */
   void init();
    /**
     * 启动组件
     */
  void start();

    /**
     * 停止组件
     */
  void stop();
}

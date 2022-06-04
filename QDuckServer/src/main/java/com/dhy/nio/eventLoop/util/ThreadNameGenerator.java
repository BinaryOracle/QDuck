package com.dhy.nio.eventLoop.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 *     线程名字生成器
 * </p>
 * @author 大忽悠
 * @create 2022/6/3 14:07
 */
public class ThreadNameGenerator {
    /**
     * 线程池中线程索引
     */
  private static final AtomicInteger index=new AtomicInteger(1);
    /**
     * 线程池中线程名前缀
     */
  private static final String POOL_THREAD_NAME_PREFIX="QDuck-pool-thread-";

    /**
     * 生成唯一的线程名
     */
  public static String generatorUniqueThreadName(){
      return POOL_THREAD_NAME_PREFIX+index.getAndIncrement();
  }
}

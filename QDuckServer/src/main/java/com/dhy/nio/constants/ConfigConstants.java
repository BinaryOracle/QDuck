package com.dhy.nio.constants;

/**
 * @author 大忽悠
 * @create 2022/6/1 11:47
 */
public class ConfigConstants {

    //------------------线程常量---------------------------------

    public static final String BOSS_THREAD_NAME="boss";

    //------------------线程池常量---------------------------------

    /**
     * 组事件循环中线程池核心线程数
     */
    public static final String GROUP_CORE_POOL_SIZE="QDuck.pool.threadCoreSize";
    /**
     * 最大线程数目
     */
    public static final String GROUP_MAX_POOL_SIZE="QDuck.pool.threadMaxSize";
    /**
     * 生存时间 - 针对救急线程 ---单位为秒
     */
    public static final String GROUP_KEEP_ALIVE_TIME="QDuck.pool.aliveTime";
    /**
     * 队列最大容量
     */
    public static final String GROUP_QUEUE_MAX_SIZE="QDuck.pool.queueMaxSize";
    /**
     * 拒绝策略
     */
    public static final String GROUP_REJECTION_POLICY="qduck.pool.rejectPolicy";

    //------------------配置文件路径常量---------------------------------

    /**
     * 默认配置文件路径
     */
    public static final String DEFAULT_CONFIG_YML_PATH="QDuck.yml";
    /**
     * 环境变量中指定配置文件路径的key
     */
    public static final String DEFAULT_CONFIG_PROPERTY="qduck.config.path";


    //------------------配置信息中其他相关常量---------------------------------

    /**
     * 端口号
     */
    public static final String PORT="QDuck.port";
}

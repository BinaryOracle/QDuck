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
     * 一个核心线程最多可监听客户端数量
     */
    public static final String CAN_CORE_THREAD_MAX_BIND_CHANNEL_NUM="QDuck.pool.maxCoreThreadBindChannelNum";
    /**
     * 一个救急线程最多可监听客户端数量
     */
    public static final String CAN_TEMP_THREAD_MAX_BIND_CHANNEL_NUM="QDuck.pool.maxTempThreadBindChannelNum";

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
    /**
     * redis数据库的ip地址
     */
    public static final String REDIS_HOST="QDuck.redis.host";
    /**
     * redis数据库的端口号
     */
    public static final String REDIS_PORT="QDuck.redis.port";
    /**
     * redis数据库的密码
     */
    public static final String REDIS_PWD="QDuck.redis.pwd";
}

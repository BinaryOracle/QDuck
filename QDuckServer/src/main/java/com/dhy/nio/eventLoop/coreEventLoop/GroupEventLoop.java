package com.dhy.nio.eventLoop.coreEventLoop;

import com.dhy.nio.config.QDuckConfig;
import com.dhy.nio.context.HandlerContext;
import com.dhy.nio.db.RedisDb;
import com.dhy.nio.eventLoop.WorkerEventLoop;
import com.dhy.nio.eventLoop.rejectPolicy.AbandonPolicy;
import com.dhy.nio.eventLoop.rejectPolicy.RejectPolicy;
import com.dhy.nio.eventLoop.util.ThreadNameGenerator;
import lombok.extern.slf4j.Slf4j;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;


import static com.dhy.nio.constants.ConfigConstants.*;


/**
 * <p>
 *     QDuck线程池
 *     ps: 这里自定义线程池的目的在于更好适配当前工作场景
 *     because:
 *        boss线程监听到客户端连接后,需要将该客户端通道交给一个工作线程进行监听
 *        但是,如果采用传统线程池,设置线程池最大线程数为10,
 *        因为每个工作线程一旦与某个客户端绑定后，就会一直去不断监听对应客户端消息,
 *        这样的话对应工作线程无法被释放，重新回到线程池中，也就无法同时监听多个客户端,
 *        这就是问题所在: 单线程无法同时监听多个客户端
 *     thus:
 *       因此,这里自定义线程池的目的，就是改变其原本的工作方式,
 *       boss线程拿到一个客户端通道连接后,会挨个轮询线程池中的工作线程进行处理,
 *       这样首先可以确保公平分配，其次可以保证在客户端连接足够多的情况下,
 *       每个工作线程可以监听处理多个客户端连接，提高工作效率
 *     notice:
 *        详细可以参考reactor变式模型流程图
 * </p>
 * @author 大忽悠
 * @create 2022/6/3 14:05
 */
@Slf4j
public class GroupEventLoop extends AbstractEventLoop {
    /**
     * 核心线程数大小-- -1表示为cpu核心数+1
     */
    private Integer CORE_THREAD_SIZE;
    /**
     * 线程池中线程最大数量
     */
    private  Integer THREAD_MAX_NUM;
    /**
     * 每一个核心线程上最大可绑定客户端线程上限
     */
    private  Integer CORE_THREAD_MAX_BIND_CHANNEL_NUM;
    /**
     * 每一个救急线程上最大可绑定客户端线程上限
     */
    private  Integer TEMP_THREAD_MAX_BIND_CHANNEL_NUM;
    /**
     * 线程池
     */
    private final List<WorkerEventLoop> qDuckPool=new ArrayList<>();
    /**
     * 拒绝策略
     * TODO: 这里拒绝策略默认为直接抛弃,因此并没有添加到配置文件中,后期再添加
     */
    protected RejectPolicy rejectPolicy=new AbandonPolicy();


    /**
     * TODO: 配置值校验待完善....
     */
    public GroupEventLoop(QDuckConfig qDuckConfig, HandlerContext handlerContext,RedisDb redisDb) {
        this.qDuckConfig = qDuckConfig;
        this.redisDb=redisDb;
        this.handlerContext=handlerContext;
        //配置信息读取
        CORE_THREAD_SIZE=Integer.parseInt(qDuckConfig.getValue(GROUP_CORE_POOL_SIZE));
        CORE_THREAD_SIZE=CORE_THREAD_SIZE==-1?Runtime.getRuntime().availableProcessors()+1:CORE_THREAD_SIZE;
        THREAD_MAX_NUM=Integer.parseInt(qDuckConfig.getValue(GROUP_MAX_POOL_SIZE));
        if(CORE_THREAD_SIZE>THREAD_MAX_NUM){
            log.error("配置的CORE_THREAD_SIZE={}大于THREAD_MAX_NUM={}",CORE_THREAD_SIZE,THREAD_MAX_NUM);
            CORE_THREAD_SIZE=Runtime.getRuntime().availableProcessors()+1;
            THREAD_MAX_NUM=1000;
            log.error("已将CORE_THREAD_SIZE设置为: {}, THREAD_MAX_NUM设置为: {}",CORE_THREAD_SIZE,THREAD_MAX_NUM);
        }
        CORE_THREAD_MAX_BIND_CHANNEL_NUM=Integer.parseInt(qDuckConfig.getValue(CAN_CORE_THREAD_MAX_BIND_CHANNEL_NUM));
        TEMP_THREAD_MAX_BIND_CHANNEL_NUM=Integer.parseInt(qDuckConfig.getValue(CAN_TEMP_THREAD_MAX_BIND_CHANNEL_NUM));
    }

    /**
     *  将客户端连接注册到线程池中某个线程的选择器上，进行监听
     * notice:
     *     在核心线程还没创建满之前,每个核心线程固定监听一个客户端连接
     *     当核心线程创建满之后,在从头遍历每个核心线程,判断每个核心线程监听的客户端数量是否达到了最大值
     * TODO: 这里客户端分配机制也待完善,后期需要根据每个线程的负载
     * TODO: 尽可能将新进入的客户端连接注册到负载较低的线程上去
     * TODO: 每个线程的负载计算取决于,当前线程已经有的客户端数量和线程的空闲时间占比，待完善....
     * TODO: 目前实现的就是简单的轮询机制
     */
    public void execute(SocketChannel sc){
        //先将已经结束的救急线程移除掉
        removeStopThread();
        //核心线程还没创建足够多
        int curThreadSize = qDuckPool.size();
        if(curThreadSize<CORE_THREAD_SIZE){
            log.info("one core worker thread created");
            qDuckPool.add(buildOneWorkEventLoop(sc,true));
            return;
        }
       //当核心线程创建满之后,在从头遍历每个线程,包括工作线程,判断每个核心线程监听的客户端数量是否达到了最大值
        for (WorkerEventLoop workEventLoop : qDuckPool) {
            //区分核心线程和救急线程分别可以监听的最大客户端数量
            if(workEventLoop.getIsCoreThread()&&workEventLoop.getBindChannelNum()<CORE_THREAD_MAX_BIND_CHANNEL_NUM){
                //将当前客户端通道绑定到该线程上去
                workEventLoop.register(sc);
                return;
            }
            if(!workEventLoop.getIsCoreThread()&&workEventLoop.getBindChannelNum()<TEMP_THREAD_MAX_BIND_CHANNEL_NUM){
                //将当前客户端通道绑定到该线程上去
                 workEventLoop.register(sc);
                 return;
            }
        }
        //创建救急线程
        //1.创建之前先判断线程数量是否到达上限值
        if(curThreadSize>=THREAD_MAX_NUM){
            //当前客户端连接被拒绝后,怎么处理
            rejectPolicy.handleReject(sc);
            return;
        }
        //2.创建一个救急线程
        qDuckPool.add(buildOneWorkEventLoop(sc,false));
    }

    private void removeStopThread() {
        qDuckPool.removeIf(WorkerEventLoop::isStop);
    }

    /**
     * 向线程池中新注册一个线程
     */
    private WorkerEventLoop buildOneWorkEventLoop(SocketChannel sc,Boolean isCoreThread) {
        return  new WorkerEventLoop(
                ThreadNameGenerator.generatorUniqueThreadName(),
                isCoreThread,
                handlerContext,
                qDuckConfig,
                redisDb,
                sc
        );
    }

}

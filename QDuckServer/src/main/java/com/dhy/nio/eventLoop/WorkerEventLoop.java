package com.dhy.nio.eventLoop;
import com.dhy.nio.config.QDuckConfig;
import com.dhy.nio.context.HandlerContext;
import com.dhy.nio.db.RedisDb;
import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.eventLoop.coreEventLoop.AbstractEventLoop;
import com.dhy.nio.eventLoop.coreEventLoop.SingleEventLoop;
import com.dhy.nio.lifecycle.Lifecycle;
import com.dhy.nio.online.OnlineUsers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static com.dhy.nio.domain.Attr.REDIS_ATTR;
import static com.dhy.nio.message.parse.ProtocolParse.parse;


/**
 * <p>
 * 每个QDuck线程关联一个选择器
 * 该选择器上面可以同时注册多个客户端连接通道,一遍监听他们的读写事件
 * </p>
 *
 * @author 大忽悠
 * @create 2022/6/3 16:43
 */
@Data
@Slf4j
public class WorkerEventLoop extends SingleEventLoop implements Lifecycle,Runnable{
    /**
     * 当前线程上绑定的客户端通道个数
     * 即: 当前线程正在监听多少个客户端通道
     */
    private Integer bindChannelNum = 0;
    /**
     * 是否是核心线程
     */
    private Boolean isCoreThread;
    /**
     * 选择器,该选择器上可以注册多个客户端进行监听
     * 这样就完成了一个线程监听多个客户端
     */
    private Selector selector;
    /**
     * 是否启动
     */
    private volatile boolean start = false;
    /**
     * 是否停止
     */
    private volatile boolean stop = false;
    /**
     * 是否初始化过
     */
    private boolean init=false;

    public WorkerEventLoop() {
    }

    public WorkerEventLoop(String threadName) {
          super(threadName);
    }

    public WorkerEventLoop(String threadName, Boolean isCoreThread, HandlerContext handlerContext, QDuckConfig qDuckConfig,
                           RedisDb redisDb, SocketChannel sc) {
        super(threadName);
        this.isCoreThread = isCoreThread;
        this.handlerContext=handlerContext;
        this.qDuckConfig=qDuckConfig;
        this.redisDb=redisDb;
        init=true;
        init();
        register(sc);
    }



    public void addBindChannelNum() {
        bindChannelNum++;
    }

    /**
     * 初始化组件
     */
    @Override
    public void init() {
        try {
            if(!init){
                super.init();
            }
            selector = Selector.open();
        } catch (IOException e) {
            log.error("selector create error ...");
        }
    }

    /**
     * 注册感兴趣的事件
     */
    @Override
    public void register(SocketChannel sc) {
        try {
            log.info("worker registering...");
            //当前客户端注册进工作线程的选择器中
            SelectionKey sckey = sc.register(selector, 0, null);
            //监听感兴趣的事件
            sckey.interestOps(SelectionKey.OP_READ);
            //给其绑定了一个读缓冲区
            ByteBuffer readBuf = ByteBuffer.allocate(1024 * 1024);
            sckey.attach(readBuf);
            //绑定客户端数量加一
            bindChannelNum++;
            log.info("worker registered");
            if(!start){
                //启动线程
                super.start();
                this.start=true;
            }
            log.info("worker thread start");
        } catch (IOException e) {
            log.error("worker register error: ", e);
        }
    }


    /**
     * 不断轮询，监听当前选择器上是否存在某个客户端的读写事件
     */
    @Override
    public void run() {
        //如果是救急线程,在bindChannelNum==0的情况下,会结束当前线程
        while (!stop && (isCoreThread || bindChannelNum != 0)) {
            try {
                selector.select();
                log.info("interesting things happened");
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    //可读事件---处理客户端发送过来的消息
                    if (key.isReadable()) {
                        log.info("read fd happened");
                        SocketChannel sc = (SocketChannel) key.channel();
                        try {
                            //从附件中取出读缓冲区
                            ByteBuffer readBuf = (ByteBuffer) key.attachment();
                            int read = sc.read(readBuf);
                            //客户端正常关闭
                            if (read == -1) {
                                log.info(sc.getRemoteAddress() + " down normal");
                                clientDown(key, sc);
                            } else {
                                //切换读写模式--读取完当前批次数据后
                                //清空读缓冲区,为下次客户端消息过来进行准备
                                readBuf.flip();
                                msgHandle(readBuf, sc, read);
                                log.debug("{} message:", sc.getRemoteAddress());
                            }
                        } catch (IOException e) {
                            log.error(sc.getRemoteAddress() + " down abnormal");
                            clientDown(key, sc);
                        }
                    }
                    //移除处理完后的事件
                    iter.remove();
                }
            } catch (IOException e) {
                log.error("select error : ", e);
            }
        }
        //线程结束
        stop=true;
    }

    private void clientDown(SelectionKey key, SocketChannel sc) throws IOException {
        OnlineUsers.removeOneOnLineUser(sc);
        bindChannelNum--;
        //客户端异常关闭
        key.cancel();
        sc.close();
    }

    /**
     * 因为Thread的stop方法为final,并且被废弃了,因此无法重写
     */
    @Override
    public void over(){
        stop=true;
    }

    /**
     * 当前线程是否已经结束
     */
    public boolean isStop(){
        return stop;
    }

    /**
     * 处理读入缓冲区中的消息
     * TODO:这里还有很多细节没有处理,比如数据量过多,一次性没读取完,那么序列化就会报错等
     */
    private void msgHandle(ByteBuffer buffer, SocketChannel sc, Integer read) {
        //触发入站处理器进行数据处理
        Attr attr = new Attr();
        attr.addAttr(REDIS_ATTR, redisDb);
        Msg msg = parse(buffer, sc, read);
        //解析出现异常或者数据不完整
        if (msg == null) {
            return;
        }
        handlerContext.invokeInHandlers(msg, attr);
    }

}

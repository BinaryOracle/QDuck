package com.dhy.nio.eventLoop;



import com.dhy.nio.context.HandlerContext;
import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.online.OnlineUsers;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dhy.nio.domain.Attr.REDIS_ATTR;
import static com.dhy.nio.parse.ProtocolParse.parse;

/**
 * @author 大忽悠
 * @create 2022/6/1 12:01
 */
@Slf4j
public class WorkerEventLoop extends GroupEventLoop {
    /**
     * 客户端选择器
     */
    private Selector selector;
    /**
     * 是否重复启动
     */
    private volatile boolean start = false;
    /**
     *   任务队列，用于在多线程间进行安全通信
     */
    private final ConcurrentLinkedQueue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    /**
     * 处理器环境上下文
     */
    private HandlerContext handlerContext;

    private volatile boolean stop = false;


    /**
     * 注册感兴趣的事件
     */
    public void register(SocketChannel sc) {
        try {
            if(!start){
                selector = Selector.open();
                //启动worker线程
                executorService.execute(this);
                start=true;
                log.info("worker start first");
            }
            log.info("new client task add");
            //添加任务对队列
            tasks.add(() ->
            {
                try {
                    //当前客户端注册进工作线程的选择器中
                    SelectionKey sckey = sc.register(selector, 0, null);
                    //监听感兴趣的事件
                    sckey.interestOps(SelectionKey.OP_READ);
                    //给其绑定了一个读缓冲区
                    ByteBuffer readBuf=ByteBuffer.allocate(1024*1024);
                    sckey.attach(readBuf);
                    //不管有没有事件，立刻返回，可以根据返回值进行判断
                    selector.selectNow();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            //唤醒select阻塞住的线程
            selector.wakeup();
        } catch (IOException e) {
            log.error("worker init error: ",e);
        }
    }

    /**
     * 不断从任务队列轮询新客户端连接任务,
     * 如果发现了,就将该客户端后续读写事件交给一个新的工作线程去处理
     * TODO: 需要将客户端通道绑定到线程池中某个线程上,待完善
     */
    @Override
    public void run() {
        while (!stop) {
            try {
                //监听事件---如果队列放入了新任务,那么wakeup会唤醒这里阻塞住的select
                selector.select();
                log.info("interesting things happened or new task added");
                //获取队列头部的任务，如果没有任务，返回null
                Runnable task = tasks.poll();
                //如果任务存在,执行任务
                //这里因为select方法先执行，因此执行的任务里面才有register---->selectNow
                if (task != null) {
                    task.run();
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iter = keys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    //可读事件---处理客户端发送过来的消息
                    if (key.isReadable()) {
                        SocketChannel sc = (SocketChannel) key.channel();
                        try {
                            //从附件中取出读缓冲区
                            ByteBuffer readBuf = (ByteBuffer)key.attachment();
                            int read = sc.read(readBuf);
                            //客户端正常关闭
                            if (read == -1) {
                                //取消当前客户端通道的注册
                                key.cancel();
                                sc.close();
                                OnlineUsers.removeOneOnLineUser(sc);
                                log.info("client down normal");
                            }
                            else {
                                //切换读写模式--读取完当前批次数据后
                                //清空读缓冲区,为下次客户端消息过来进行准备
                                readBuf.flip();
                                msgHandle(readBuf,sc,read);
                                log.debug("{} message:", sc.getRemoteAddress());
                            }
                        } catch (IOException e) {
                            log.error("client down abnormal");
                            //客户端异常关闭
                            key.cancel();
                            sc.close();
                            OnlineUsers.removeOneOnLineUser(sc);
                        }
                    }
                    //移除处理完后的事件
                    iter.remove();
                }
            } catch (IOException e) {
               log.error("client select error : ",e);
            }
        }
    }

    /**
     * 处理读入缓冲区中的消息
     * TODO:这里还有很多细节没有处理,比如数据量过多,一次性没读取完,那么序列化就会报错等
     */
    private void msgHandle(ByteBuffer buffer,SocketChannel sc,Integer read) {
        //触发入站处理器进行数据处理
        Attr attr = new Attr();
        attr.addAttr(REDIS_ATTR,redisDb);
        Msg msg = parse(buffer, sc, read);
        //解析出现异常或者数据不完整
        if(msg==null){
            return;
        }
        handlerContext.invokeInHandlers(msg,attr);
    }

    /**
     * 停止组件
     */
    @Override
    public void stop() {
      stop=true;
      //已提交任务会执行完
      groupPool.shutdown();
    }

    /**
     * 注册感兴趣的事件
     */
    @Override
    public void register() {}

    public void setHandlerContext(HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }
}

package com.dhy.nio.context.handler;

import com.dhy.nio.context.handler.coreHandler.OutHandler;
import com.dhy.nio.db.RedisDb;
import com.dhy.nio.domain.Attr;
import com.dhy.nio.domain.Msg;
import com.dhy.nio.message.builder.MessageBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.dhy.nio.constants.MsgType.STRING;

/**
 * <p>
 *     处理客户端写出消息
 * </p>
 * @author 大忽悠
 * @create 2022/6/3 9:50
 */
@Data
@Slf4j
public class ClientOutHandler implements OutHandler {
    /**
     * 当前登录的用户名
     */
    private String meName;
    /**
     * QDuck协议构建工具
     */
    private MessageBuilder messageBuilder=new MessageBuilder();
    /**
     * 数据写出缓冲区
     */
    private ByteBuffer writeBuffer=ByteBuffer.allocate(1024*1024);
    /**
     * 输入
     */
    private Scanner scanner=new Scanner(System.in);
    /**
     * 退回输入命令界面
     */
    private static final String EXIT="exit";
    /**
     * 当用户输入QUIT时,表示关闭当前客户端连接
     */
    private static final String STOP="stop";
    /**
     * 当用户输入QUIT时,表示关闭当前客户端连接
     */
    private static final String CHAT="chat";
    /**
     * 命令分隔符
     */
    private static final String SEPARATOR =" ";

    @Override
    public boolean support(Msg msg) {
        return true;
    }

    /**
     * 处理要写给客户端的数据
     */
    @Override
    public Msg doHandleDataOut(Msg msg, Attr attr) {
        try {
            SocketChannel sc = (SocketChannel) attr.getAttr(Attr.SOCKET_CHANNEL);
            RedisDb redisDb = (RedisDb) attr.getAttr(Attr.REDIS_ATTR);
            printBanner();
            getLoginUName(redisDb,sc);
            displayUnReadMsg(redisDb);
            doChat(sc);
        } catch (IOException e) {
           log.error("writer msg error: ",e);
        }
        return null;
    }

    /**
     * 真正进行聊天
     */
    private void doChat(SocketChannel sc) throws IOException {
        System.out.println("请输入命令: ");
        String command=scanner.nextLine();
        while(!command.equals(STOP)){
            String[] commands = command.split(SEPARATOR);
            switch (commands[0]){
                case CHAT:{
                    System.out.println("进入私聊模式:");
                    if(commands.length!=2){
                        System.out.println("命令错误,请重新输入");
                        break;
                    }
                    privateChat(commands[1],sc);
                    break;
                }case EXIT:{
                    break;
                } case STOP:{
                    System.out.println("程序关闭中...");
                    return;
                } default:{
                    System.out.println("命令错误,请重新输入");
                    break;
                }
            }
            System.out.println("请输入命令: ");
            command=scanner.nextLine();
        }
    }

    /**
     * 处理私聊消息
     */
    private void privateChat(String toUser, SocketChannel sc) throws IOException {
        System.out.println("请输入要发送的消息,输入exit退出私聊模式: ");
        String msg = scanner.nextLine();
        while(!msg.equals(EXIT)){
            messageBuilder.sendMessage(sc,writeBuffer,meName,toUser,msg.getBytes(StandardCharsets.UTF_8),STRING);
            System.out.println("还发吗? 不发输入exit退出私聊模式,谢谢-_-!");
            msg=scanner.nextLine();
        }
        System.out.println("私聊模式已退出");
    }

    /**
     * 展示未读消息
     */
    private void displayUnReadMsg(RedisDb redisDb) {
        //读取未读消息,并进行展示
        Map<String, List<String>> oneUnReadMessages = redisDb.getOneUnReadMessages(meName);
        System.out.println("================================================");
        System.out.println("未读消息展示如下: ");
        oneUnReadMessages.forEach((key,value)->{
            System.out.println("收到"+key+"用户发来的未读消息有: ");
            value.forEach(System.out::println);
            //移除当前未读消息,并保存为已读
            redisDb.removeUnReadMessages(meName,key);
            redisDb.saveOneReadMsg(meName,key,value.toArray(new String[]{}));
        });
        System.out.println("================================================");
    }

    /**
     * 获取登录用户名
     */
    private void getLoginUName(RedisDb redisDb,SocketChannel sc) {
        System.out.println("请先输入用户,用户名不能为空,并且用户名长度不超过10个字符:");
        meName=scanner.nextLine();
        while(meName==null||meName.isEmpty()||meName.length()>10){
            System.out.println("用户名不符合要求,请重新输入");
            meName=scanner.nextLine();
        }
        //保存登录用户
        redisDb.saveOneUser(meName);
        //发送登录消息到服务器
        messageBuilder.sendLoginMessage(sc,writeBuffer,meName);
    }

    /**
     * 打印介绍信息
     */
    private void printBanner() {
        System.out.println("================================================");
        System.out.println("欢迎来到QDuck聊天平台!");
        System.out.println("QDuck聊天平台提供了如下服务:");
        System.out.println("输入命令: chat username 进入私聊模式");
        System.out.println("输入命令: create group   创建群聊,并指定本群用户(开发中...)");
        System.out.println("输入命令: enter group    进入指定群聊,然后可以发送消息(开发中...)");
        System.out.println("输入命令: leave group    退出指定群聊(开发中...)");
        System.out.println("输入命令: kick username group  群主将某人踢出群聊(开发中...)");
        System.out.println("输入命令: exit  退回到命令输入界面");
        System.out.println("输入命令: stop  退出程序");
        System.out.println("================================================");
    }
}

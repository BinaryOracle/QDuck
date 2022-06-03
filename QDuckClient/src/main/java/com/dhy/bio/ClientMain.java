package com.dhy.bio;

import com.dhy.bio.db.RedisDb;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.net.Socket;


import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static com.dhy.bio.constants.Charset.UTF8;
import static com.dhy.bio.constants.MessageProtocolConstants.MAGIC;
import static com.dhy.bio.constants.MessageType.COMMON_MSG;
import static com.dhy.bio.constants.MessageType.LOGIN;
import static com.dhy.bio.constants.MsgType.STRING;
import static com.dhy.bio.util.ByteUtil.int2byte;


/**
 * <p>
 *     客户端代码使用简单的BIO编写
 * </p>
 * @author 大忽悠
 * @create 2022/6/1 11:30
 */
@Slf4j
public class ClientMain {
    /**
     * 当前登录的用户名
     */
    private static String curUserName;
    private static final RedisDb redis=new RedisDb("localhost",6379,"123456");

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("localhost", 5200);
        new Thread(()->{
            try {
                while(true){
                    receiveMsg(socket);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        },"接收消息的线程").start();
        sendMsg(socket);
    }

    /**
     * TODO: 假设数据量很小,一次性可以读取完毕
     */
    private static void receiveMsg(Socket socket) throws IOException, ClassNotFoundException {
        InputStream inputStream = socket.getInputStream();
        byte[] data = new byte[1024 * 1024];
        int read = inputStream.read(data);
        log.info("收到消息: {}",new String(data,0,read));
    }

    private static void sendMsg(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        log.info("已经与服务器建立连接,下面可以发送消息....");
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入你的用户名: ");
        curUserName = scanner.nextLine();
        redis.saveOneUser(curUserName);
        //发送登录消息给服务端
        sendLoginMessage(printStream);
        //未读消息展示
        UnReadMsgsDisplay();
        while(true){
            System.out.println("要发消息给谁: ");
            String toUser=scanner.nextLine();
            System.out.println("请输入要发送的消息: ");
            String msg = scanner.nextLine();
            System.out.println("是否结束发送消息,输入yes or no: ");
            String stopIf = scanner.nextLine();
             if(stopIf.equals("yes")){
                 break;
             }
             //发送消息报文
            sendMessage(toUser, msg,printStream);
        }
        //客户端主动断开连接
        socket.close();
    }

    private static void UnReadMsgsDisplay() {
        //读取未读消息,并进行展示
        Map<String, List<String>> oneUnReadMessages = redis.getOneUnReadMessages(curUserName);
        System.out.println("-----------未读消息展示如下: ----------------");
        oneUnReadMessages.forEach((key,value)->{
            System.out.println("收到"+key+"用户发来的未读消息有: ");
            value.forEach(System.out::println);
            //移除当前未读消息,并保存为已读
            redis.removeUnReadMessages(curUserName,key);
            redis.saveOneReadMsg(curUserName,key,value.toArray(new String[]{}));
        });
        System.out.println("-------------------------------------------");
    }

    /**
     * 发送登录消息给服务端,让服务器将当前用户保存在在线用户列表中
     */
    private static void sendLoginMessage(PrintStream printStream) throws IOException {
        //魔数--3字节
        printStream.print(MAGIC);
        //消息类型---1字节
        printStream.write(LOGIN);
        //当前用户名长度---4字节
        printStream.write(getCurNameLen());
        //当前用户名
        printStream.print(curUserName);
        printStream.flush();
    }

    private static void sendMessage(String toUser, String msg,PrintStream printStream) throws IOException {
        //报文首部字段
        //魔数--3字节
        printStream.print(MAGIC);
        //消息类型---1字节
        printStream.write(COMMON_MSG);
        //后面数据总长度--4字节
        printStream.write(int2byte(curUserName.length() + toUser.length() + msg.length() + 14));
        //当前用户名长度---4字节
        printStream.write(getCurNameLen());
        //当前用户名
        printStream.print(curUserName);
        //接收方的用户名长度---4字节
        printStream.write(getToUserLen(toUser));
        //接收方用户名
        printStream.print(toUser);
        //数据类型--1字节
        printStream.write(STRING);
        //字符串编码--1字节
        printStream.write(UTF8);
        //消息长度--4字节
        printStream.write(getMsgLen(msg));
        //文本消息
        printStream.print(msg);
        //刷新缓冲区
        printStream.flush();
    }

    private static byte[] getMsgLen(String msg) {
        return int2byte(msg.length());
    }

    private static byte[] getToUserLen(String toUser) {
        return int2byte(toUser.length());
    }

    private static byte[] getCurNameLen() {
        return int2byte(curUserName.length());
    }
}

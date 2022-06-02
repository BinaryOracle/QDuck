package com.dhy.nio.online;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 *     在线用户信息记录
 * </p>
 * @author 大忽悠
 * @create 2022/6/2 8:48
 */
public class OnlineUsers {
   private static final Map<String, SocketChannel> ONLINE_USERS=new ConcurrentHashMap<>();
   private static final Map<SocketChannel,String> SOCKETS=new ConcurrentHashMap<>();
   public static void addOneOnlineUser(String username,SocketChannel socketChannel){
       ONLINE_USERS.put(username,socketChannel);
       SOCKETS.put(socketChannel,username);
   }

   public static void removeOneOnLineUser(SocketChannel socketChannel){
       ONLINE_USERS.remove(SOCKETS.get(socketChannel));
   }

   public static SocketChannel searchOneOnlineUser(String username){
       return ONLINE_USERS.get(username);
   }
}


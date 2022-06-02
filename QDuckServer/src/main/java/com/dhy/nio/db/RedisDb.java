package com.dhy.nio.db;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *     redis数据库作为存放用户信息的地点
 *     用list集合存放用户消息:
 *    QDuck:msg:dhy:xpy:消息1 消息2 消息3 (最新的消息显示在list集合最前面)
 *    QDuck:msg:dhy:cjt: 消息4 消息5
 *    key的结构为: QDuck:msg:receiver:publisher
 *     TODO: 只支持存放文本信息
 * </p>
 * @author 大忽悠
 * @create 2022/6/2 8:59
 */
public class RedisDb {
    private Jedis jedis;
    private static final String MSG_READ_PREFIX="QDuck:msg:read:";
    private static final String MSG_UNREAD_PREFIX="QDuck:msg:unread:";
    private static final String USER_KEY="QDuck:user";
    private static final String SEPARATOR=":";

    public RedisDb(String host,Integer port,String password){
        jedis=new Jedis(host,port);
        if(password!=null){
            jedis.auth(password);
        }
    }

    /**
     * 添加一个新用户到redis
     * 返回0表示用户已经注册过了,返回1表示用户注册成功
     */
    public long saveOneUser(String username){
        return jedis.sadd(USER_KEY, username);
    }

    /**
     * 用户是否存在
     */
    public boolean isUserExist(String username){
        return jedis.sismember(USER_KEY,username);
    }

    /**
     * publisher发送msg给receiver
     */
    public void saveOneReadMsg(String receiver,String publisher,String ...msgs){
        jedis.rpush(MSG_READ_PREFIX+receiver+SEPARATOR+publisher,msgs);
    }

    /**
     * 返回publisher发送给receiver的所有消息
     */
    public List<String> getOneReadMessages(String receiver,String publisher){
         return jedis.lrange(MSG_READ_PREFIX+receiver+SEPARATOR+publisher,0,-1);
    }

    /**
     * 返回receiver所有消息
     */
    public Map<String,List<String>> getOneReadMessages(String receiver){
        //首先获取所有与receiver的ReadMessages相关key
        String keyPrefix = MSG_READ_PREFIX + receiver + SEPARATOR+"*";
        Set<String> keys = jedis.keys(keyPrefix);
        Map<String,List<String>> msgs=new HashMap<>();
        for (String key : keys) {
            List<String> list = jedis.lrange(key, 0, -1);
            String publisherName=key.split(SEPARATOR)[4];
            msgs.put(publisherName,list);
        }
        return msgs;
    }


    /**
     * publisher发送msg给receiver
     */
    public void saveOneUnReadMsg(String receiver,String publisher,String msg){
        jedis.rpush(MSG_UNREAD_PREFIX+receiver+SEPARATOR+publisher,msg);
    }

    /**
     * 返回publisher发送给receiver的所有消息
     */
    public List<String> getOneUnReadMessages(String receiver,String publisher){
        return jedis.lrange(MSG_UNREAD_PREFIX+receiver+SEPARATOR+publisher,0,-1);
    }

    public void removeUnReadMessages(String receiver,String publisher){
        jedis.del(MSG_UNREAD_PREFIX+receiver+SEPARATOR+publisher);
    }

    /**
     * 返回receiver所有未读消息
     */
    public Map<String,List<String>> getOneUnReadMessages(String receiver){
        //首先获取所有与receiver的ReadMessages相关key
        String keyPrefix = MSG_UNREAD_PREFIX + receiver + SEPARATOR+"*";
        Set<String> keys = jedis.keys(keyPrefix);
        Map<String,List<String>> msgs=new HashMap<>();
        for (String key : keys) {
            List<String> list = jedis.lrange(key, 0, -1);
            String publisherName=key.split(SEPARATOR)[4];
            msgs.put(publisherName,list);
        }
        return msgs;
    }
}

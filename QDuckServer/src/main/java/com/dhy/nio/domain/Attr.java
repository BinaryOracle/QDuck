package com.dhy.nio.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *     处理器中可以额外传递的一些属性
 * </p>
 * @author 大忽悠
 * @create 2022/6/2 9:29
 */
public class Attr {
    private static final Map<String,Object> attrs=new HashMap<>();

    //---------------常见ATTR属性如下-------------------

    public static final String REDIS_ATTR="redisDB";
    public static final String SOCKET_CHANNEL="sc";

    public void addAttr(String key,Object value){
        attrs.put(key,value);
    }

    public Object getAttr(String key){
        return attrs.get(key);
    }

    public void removeAttr(String key){
        attrs.remove(key);
    }
}

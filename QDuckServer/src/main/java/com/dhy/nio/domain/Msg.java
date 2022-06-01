package com.dhy.nio.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.Charset;

import static com.dhy.nio.constants.MsgType.STRING;


/**
 * @author 大忽悠
 * @create 2022/6/1 20:12
 */
@Builder
@Data
@Slf4j
public class Msg implements Serializable {

    /**
     * 数据长度
     */
  private Integer len;

    /**
     * 图片二进制流或者视频二进制流
     **/
  private byte[] data;

  /**
   * 普通字符串文本
   */
  private String string;

    /**
     * 消息类型: 字符串,图片,视频或者其他文件--不指定默认为字符串
     */
  private byte type=STRING;

  /**
     * 如果是字符串需要指明编码
     * 不指定默认为UTF-8
   */
  private Charset charset=Charset.defaultCharset();

  /**
   * 想要将消息发送给谁
   */
  private String username;

  public static Msg byteToMsg(byte[] bytes){
    try {
      ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
      return  (Msg) inputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
         log.info("byte数组序列化为Msg对象报错: ",e);
    }
    return null;
  }
}

package com.dhy.bio.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import static com.dhy.bio.constants.MessageType.COMMON_MSG;
import static com.dhy.bio.constants.MsgType.STRING;


/**
 * @author 大忽悠
 * @create 2022/6/1 20:12
 */
@Builder
@Data
@Slf4j
public class Msg implements Serializable {

    /**
     * 数据长度--如果是图片或者视频则为对应二进制流长度
     * 如果是字符串,则为字符串长度
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
   * 默认消息类型为普通消息
   */
  private byte msgType=COMMON_MSG;

  /**
     * 如果是字符串需要指明编码
     * 不指定默认为UTF-8
   */
  private Charset charset=Charset.defaultCharset();
  /**
   * 自己
   */
  private String me;
  /**
   * 想要将消息发送给谁--如果是登录消息或者心跳包则这里不填
   */
  private String toUsername;
  /**
   * 对应客户端的SocketChannel
   */
  private SocketChannel socketChannel;

}

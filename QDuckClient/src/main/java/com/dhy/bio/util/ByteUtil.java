package com.dhy.bio.util;

/**
 * @author 大忽悠
 * @create 2022/6/2 19:14
 */
public class ByteUtil {
    /**
     * int转换为4字节byte
     */
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);
        targets[1] = (byte) ((res >> 8) & 0xff);
        targets[2] = (byte) ((res >> 16) & 0xff);
        targets[3] = (byte) (res >>> 24);
        return targets;
    }

    /**
     * 4字节byte数组转换为Int
     */
    public static int byte2int(byte[] res) {
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00)
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }
}

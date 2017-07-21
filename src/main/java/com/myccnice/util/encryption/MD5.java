package com.myccnice.util.encryption;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MD5加密
 * Create At 2012-04-09
 *
 * @author ketqi
 */
public abstract class MD5 {

    protected static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final Logger LOGGER = LoggerFactory.getLogger(MD5.class);

    /**
     * @param path 待加密文件的路径
     * @return 加密后字串
     */
    public static String md5File(String path) {
        try (@SuppressWarnings("resource")
        FileChannel ch = new FileInputStream(path).getChannel()) {
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(byteBuffer);
            return byteToHex(messageDigest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        } 
    }

    /**
     * @param str 加密前字串
     * @return 加密后字串
     * 获取MD5加密字串
     */
    public static String md5(String str) {
        try {
            byte[] strTemp = str.getBytes("UTF-8");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(strTemp);
            return byteToHex(messageDigest.digest());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将直接数据转换为16进制的数据
     *
     * @param bytes 字节数组
     * @return 16进制字符串
     */
    private static String byteToHex(byte[] bytes) {
        int size = bytes.length;
        char[] result = new char[size * 2];
        int k = 0;
        for (byte b : bytes) {
            result[k++] = HEX_DIGITS[b >>> 4 & 0xf];
            result[k++] = HEX_DIGITS[b & 0xf];
        }
        return new String(result);
    }
}

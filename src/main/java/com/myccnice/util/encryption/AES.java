package com.myccnice.util.encryption;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myccnice.util.PlatformUtils;

/**
 * Create at 2012-08-08
 * AES加密与解密
 *
 * @author 王正伟
 */
public abstract class AES {
    // 密钥
    private static final byte[] AES_KEY = "F8L7UX797S4V40JI".getBytes();
    // 偏移向量串
    private static final byte[] AES_IV = "6694PIPVG15GQ24G".getBytes();
    // 暗号
    private static final String CIPHER = "AES/CBC/PKCS5Padding";
    // 编码方式
    private static final String CHARSET = "ASCII";
    // 加解密方式
    private static final String AES_TYPE = "AES";

    /** 日志记录器 */
    private static final Logger LOGGER = LoggerFactory.getLogger(AES.class);

    /**
     * @param s source str
     * @return encrypt str
     * AES加密
     */
    public static String encrypt(String s) {
        if (!PlatformUtils.hasText(s)) {
            return s;
        }
        // AES加密
        SecretKeySpec skeySpec = new SecretKeySpec(AES.AES_KEY, AES_TYPE);
        IvParameterSpec iv = new IvParameterSpec(AES.AES_IV);
        byte[] result;
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            result = cipher.doFinal(s.getBytes());
            // BASE64
            return new String(Base64.encode(result), CHARSET);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            LOGGER.error("AES加密失败", e);
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * @param s source
     * @return decrypt str
     * AES解密
     */
    public static String decrypt(String s) {
        if (!PlatformUtils.hasText(s)) {
            return s;
        }
        IvParameterSpec iv = new IvParameterSpec(AES.AES_IV);
        SecretKeySpec skeySpec = new SecretKeySpec(AES.AES_KEY, AES_TYPE);
        try {
            // BASE64
            byte[] resource = Base64.decode(s.getBytes(CHARSET));
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            return new String(cipher.doFinal(resource));
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error("AES解密失败", e);
            throw new RuntimeException("AES解密失败", e);
        }
    }
}

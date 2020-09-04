package com.paladin.framework.utils.secure;


import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class SecureUtil {

    private static SecureRandom secureRandom = new SecureRandom();
    private static int DEFAULT_NUM_BYTES = 16;

    public static String createSalt() {
        return createSalt(DEFAULT_NUM_BYTES);
    }

    public static String createSalt(int numBytes) {
        byte[] bytes = new byte[numBytes];
        secureRandom.nextBytes(bytes);
        return Hex.encodeHexString(bytes);
    }

    public static String hashByMD5(String content, String salt) {
        // 进行一次加盐操作
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] saltBytes = salt.getBytes("UTF-8");
            byte[] contentBytes = content.getBytes("UTF-8");

            digest.reset();
            digest.update(saltBytes);

            byte[] hashed = digest.digest(contentBytes);
            return Hex.encodeHexString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("hash by md5 error", e);
        }
    }

    public static void main(String[] args) {
        System.out.println(hashByMD5("liyuanmei", "48d33e833a78e41d6bf791ae1ef23d9a"));
    }
}

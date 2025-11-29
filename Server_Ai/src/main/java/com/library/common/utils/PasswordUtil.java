package com.library.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类
 */
public class PasswordUtil {

    private static final SecureRandom random = new SecureRandom();

    /**
     * 生成密码哈希
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // 组合盐和哈希值
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码哈希算法不可用", e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        // 临时支持：如果存储的密码是简单明文（长度<50且不包含Base64特殊字符），则直接比较
        if (storedHash != null && storedHash.length() < 50 && !storedHash.contains("/") && !storedHash.contains("+")) {
            return inputPassword.equals(storedHash);
        }
        
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);

            // 提取盐和哈希值
            byte[] salt = new byte[16];
            byte[] storedHashedPassword = new byte[combined.length - 16];
            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, storedHashedPassword, 0, storedHashedPassword.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] inputHashedPassword = md.digest(inputPassword.getBytes());

            // 比较哈希值
            return MessageDigest.isEqual(storedHashedPassword, inputHashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
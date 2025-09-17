package com.university.mentalhealth.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordUtil {
    private static final Logger logger = Logger.getLogger(PasswordUtil.class.getName());

    /**
     * 使用SHA-256加密密码
     */
    public static String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "密码加密失败", e);
            return password; // 加密失败时返回原密码（不安全，仅用于演示）
        }
    }

    /**
     * 验证密码是否匹配
     */
    public static boolean verifyPassword(String inputPassword, String storedPassword) {
        String encryptedInput = encryptPassword(inputPassword);
        return encryptedInput.equals(storedPassword);
    }

    /**
     * 验证密码强度
     */
    public static boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        // 密码必须包含大小写字母、数字和特殊字符中的至少三种
        int criteriaMet = 0;
        if (hasUpperCase) criteriaMet++;
        if (hasLowerCase) criteriaMet++;
        if (hasDigit) criteriaMet++;
        if (hasSpecialChar) criteriaMet++;

        return criteriaMet >= 3;
    }
}

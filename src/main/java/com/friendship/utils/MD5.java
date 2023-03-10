package com.friendship.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

public class MD5 {
    private static final String salt = "5FC5D41850C2836";

    private static final Random random = new Random();
    private static final String[] digits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    private static String getMd5(String password) throws Exception {
        String encryption = "";
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(password.getBytes(StandardCharsets.UTF_8));
        for (byte b : digest) {
            int i = b;
            if (i < 0) {
                i += 256;
            }
            encryption += digits[i % 16] + digits[i / 16];
        }
        return encryption;
    }

    public static String getEncryption(String password) throws Exception{
        return getMd5(getMd5(getMd5(getMd5(getMd5(password)+salt)+salt)+salt)+salt);
    }

    public static String getGroupId() {
        StringBuilder groupId = new StringBuilder();
        while (groupId.length() < 10) {
            groupId.append(random.nextInt(10));
        }
        return groupId.toString();
    }
}

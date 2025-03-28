package com.github.khalaimovda.shopview.utils;

import java.security.SecureRandom;

public class ImageUtils {

    public static byte[] createRandomBytes() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[1024];
        random.nextBytes(bytes);
        return bytes;
    }
}

package com.myccnice.util;

public class PlatformUtils {

    public static boolean hasText(String str) {
        return str != null && !"".equals(str.trim());
    }
}

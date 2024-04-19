package com.zst.registrycenter.utils;

public class StringUtils {
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean equals(String str1, String str2) {
        return str1 != null && str1.equals(str2);
    }
}

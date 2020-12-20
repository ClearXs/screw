package com.jw.screw.common.util;

/**
 * 一些字符串的操作
 * @author jiangw
 * @date 2020/12/7 15:04
 * @since 1.0
 */
public class StringUtils {

    public static boolean isEmpty(String s) {
        if (s == null) {
            return true;
        }
        return s.toCharArray().length == 0;
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}

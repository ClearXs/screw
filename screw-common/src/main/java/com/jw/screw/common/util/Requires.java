package com.jw.screw.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * 一些关于请求验证的方法
 * @author jiangw
 * @date 2020/12/8 16:11
 * @since 1.0
 */
public class Requires {

    public static void isNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException("requires: " + message + " is empty");
        }
        if (obj instanceof String) {
            if (StringUtils.isEmpty((String) obj)) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
        if (obj instanceof Collection) {
            if (Collections.isEmpty((Collection<? extends Object>) obj)) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
        if (obj instanceof Map) {
            if (Collections.isEmpty((Map<? extends Object, ? extends Object>) obj)) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
        if (obj instanceof Integer) {
            if ((Integer) obj == 0) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
        if (obj instanceof Long) {
            if ((Long) obj == 0L) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
        if (obj instanceof Double) {
            if ((Double) obj == 0) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
        if (obj instanceof Float) {
            if ((Float) obj == 0) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
        if (obj instanceof Boolean) {
            if (!(Boolean) obj) {
                throw new IllegalArgumentException("requires: " + message + " is empty");
            }
        }
    }
}


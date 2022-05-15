package com.ch.cloud.utils;

import com.ch.utils.CommonUtils;

/**
 * decs:
 *
 * @author 01370603
 * @since 2021/2/3
 */
public class ContextUtil {

    private ContextUtil() {
    }

    private static ThreadLocal<String> LOCAL_USER = new ThreadLocal<>();

    public static void setUser(String user) {
        if (CommonUtils.isNotEmpty(user))
            LOCAL_USER.set(user);
    }

    public static String getUser() {
        return LOCAL_USER.get();
    }
}

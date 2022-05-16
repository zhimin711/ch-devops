package com.ch.cloud.utils;

import com.ch.utils.CommonUtils;
import com.ch.utils.NumberUtils;

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
    private static ThreadLocal<String> LOCAL_TENANT = new ThreadLocal<>();

    public static void setUser(String user) {
        if (CommonUtils.isNotEmpty(user))
            LOCAL_USER.set(user);
    }

    public static void setTenant(String tenant) {
        if (CommonUtils.isNotEmpty(tenant))
            LOCAL_TENANT.set(tenant);
    }

    public static String getUser() {
        return LOCAL_USER.get();
    }

    public static Long getTenant() {
        String tenant = LOCAL_TENANT.get();
        return NumberUtils.isNumeric(tenant) ? Long.parseLong(tenant) : null;
    }
}

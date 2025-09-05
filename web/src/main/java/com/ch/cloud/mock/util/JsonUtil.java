package com.ch.cloud.mock.util;

import com.alibaba.fastjson2.JSONObject;

/**
 * @author zhimin.ma
 * @description json工具类
 * @since 2018/7/13 15:08
 */
public class JsonUtil {

    /**
     * 对象转string
     *
     * @param obj 对象
     * @return String
     */
    public static String toStr(Object obj) {
        return JSONObject.toJSONString(obj);
    }

}

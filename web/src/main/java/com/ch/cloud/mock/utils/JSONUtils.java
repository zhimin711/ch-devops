package com.ch.cloud.mock.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.utils.CommonUtils;
import com.ch.utils.NumberUtils;

/**
 * decs:
 *
 * @author zhimin.ma
 * @since 2020/10/10
 */
public class JSONUtils {

    public static Object parse(String json) {
        try {
            if (isArray(json)) {
                JSONArray array = JSON.parseArray(json);
                if (array.isEmpty()) {
                    return null;
                }
                Object obj = array.get(0);
                if (NumberUtils.isNumeric(obj.toString())) {

                }

            } else {
                JSONObject object = JSON.parseObject(json);
                if (object.isEmpty()) {
                    return null;
                }
                object.keySet().forEach(r -> {

                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isArray(final String json) {
        if (CommonUtils.isEmpty(json)) return false;
        String tmp = json.trim();
        return tmp.startsWith("[") && tmp.endsWith("]");
    }
}

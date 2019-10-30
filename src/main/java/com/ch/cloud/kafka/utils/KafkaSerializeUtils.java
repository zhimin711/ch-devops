package com.ch.cloud.kafka.utils;

import com.ch.utils.CommonUtils;
import com.ch.utils.JarUtils;
import com.google.common.collect.Maps;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/10/30
 */
@Slf4j
public class KafkaSerializeUtils {

    //加载过不用重新加载类对象
    private static Map<String, Class<?>> clazzMap = Maps.newConcurrentMap();

    public static  <T> T deSerialize(byte[] data, Class<T> clazz) {
        if (clazz != null && data != null) {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T t = null;
            try {
                t = clazz.newInstance();
                ProtostuffIOUtil.mergeFrom(data, t, schema);
            } catch (InstantiationException | IllegalAccessException var5) {
                log.error("deSerialize error, Class=" + clazz, var5);
            }
            return t;
        } else {
            return null;
        }
    }

    public static Class<?> loadClazz(String path, String className) {
        String prefix = "file:";
        log.debug("load class file path: {} | {}", prefix, path);
        try {
            Class<?> clazz = clazzMap.get(className);
            if (clazz == null) {
                if (CommonUtils.isEmpty(className)) {
                    clazz = Class.forName(className);
                } else {//加载过不用重新加载类对象
                    clazz = JarUtils.loadClassForJar(prefix + path, className);
                }
                clazzMap.put(className, clazz);
            }
            return clazz;
        } catch (MalformedURLException | ClassNotFoundException e) {
            log.error("load class to deSerialize error!", e);
        }
        return null;
    }
}

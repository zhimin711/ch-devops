package com.ch.cloud.kafka.utils;

import com.ch.utils.JSONUtils;
import com.ch.utils.JarUtils;
import com.google.common.collect.Maps;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
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

    /**
     * 序列化
     *
     * @param obj
     * @return
     */
    public static <T> byte[] serializer(T obj) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
        return new byte[]{};
    }

    public static <T> T deSerialize(byte[] data, Class<T> clazz) {
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

                try {//先从加载器加载类
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException ignored) {
                }
                if (clazz == null) {//加载器类不存在，从Jar文件加载
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


    public static ZkSerializer jsonZk() {
        return new ZkSerializer() {
            @Override
            public byte[] serialize(Object o) throws ZkMarshallingError {
                return JSONUtils.toJson(o).getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        };
    }
}

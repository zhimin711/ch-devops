package com.ch.cloud.kafka.utils;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ch.Separator;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * decs:
 *
 * @author zhimin.ma
 * @date 2020/1/6
 */
@Slf4j
public class DubboCallUtils {


    // 当前应用的信息
    private static ApplicationConfig application = new ApplicationConfig();
    // 注册中心信息缓存
    private static Map<String, RegistryConfig> registryConfigCache = new ConcurrentHashMap<>();

    // 各个业务方的ReferenceConfig缓存
    private static Map<String, ReferenceConfig> referenceCache = new ConcurrentHashMap<>();

    static {
        application.setName("consumer-test");
    }

    /**
     * 获取注册中心信息
     *
     * @param address zk注册地址
     * @param group   dubbo服务所在的组
     * @return
     */
    private static RegistryConfig getRegistryConfig(String address, String group, String version) {
        String key = address + "-" + group + "-" + version;
        RegistryConfig registryConfig = registryConfigCache.get(key);
        if (null == registryConfig) {
            registryConfig = new RegistryConfig();
            if (StringUtils.isNotEmpty(address)) {
                registryConfig.setAddress(address);
            }
            if (StringUtils.isNotEmpty(version)) {
                registryConfig.setVersion(version);
            }
            if (StringUtils.isNotEmpty(group)) {
                registryConfig.setGroup(group);
            }
            registryConfigCache.put(key, registryConfig);
        }
        return registryConfig;
    }

    private static ReferenceConfig getReferenceConfig(String interfaceName, String address, String group, String version) {
        String referenceKey = address + Separator._3 + interfaceName;

        ReferenceConfig referenceConfig = referenceCache.get(referenceKey);
        if (null == referenceConfig) {
            try {
                referenceConfig = new ReferenceConfig<>();
                referenceConfig.setApplication(application);
                referenceConfig.setRegistry(getRegistryConfig(address, group, version));
//                Class<?> interfaceClass = Class.forName(interfaceName);
//                referenceConfig.setInterface(interfaceClass);
                referenceConfig.setInterface(interfaceName);
                if (StringUtils.isNotEmpty(version)) {
                    referenceConfig.setVersion(version);
                }
                referenceConfig.setProtocol("dubbo");
                referenceConfig.setGeneric(true);
                referenceConfig.setTimeout(1000);
                //referenceConfig.setUrl("dubbo://10.1.50.167:20880/com.test.service.HelloService");
                referenceCache.put(referenceKey, referenceConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return referenceConfig;
    }

    public static Object invoke(String interfaceName, String methodName, List<Object> paramList, String address, String version) {
        ReferenceConfig reference = getReferenceConfig(interfaceName, address, null, version);
        if (null != reference) {
            GenericService genericService = (GenericService) reference.get();
            if (genericService == null) {
                log.debug("GenericService 不存在:{}", interfaceName);
                return null;
            }

            String[] paramType = null;
            Object[] paramObject = null;
            if (!CommonUtils.isEmpty(paramList)) {
                paramType = new String[paramList.size()];
                paramObject = new Object[paramList.size()];
                for (int i = 0; i < paramList.size(); i++) {
                    paramType[i] = paramList.get(i).getClass().getCanonicalName();
                    paramObject[i] = paramList.get(i);
                }
            }
//            paramType = getMethodParamType(interfaceName, methodName);
//            return genericService.$invoke(methodName, getMethodParamType(interfaceName, methodName), paramObject);
            return genericService.$invoke(methodName, paramType, paramObject);
        }
        return null;
    }

    public static Object invoke(String address, String version, String interfaceName, String methodName, String parameterClassName, String... parameterJsons) {
        ReferenceConfig reference = getReferenceConfig(interfaceName, address, null, version);
        if (null != reference) {
            GenericService genericService = (GenericService) reference.get();
            if (genericService == null) {
                log.debug("GenericService 不存在:{}", interfaceName);
                return null;
            }

            //基本类型以及Date,List,Map等不需要转换，直接调用
            String[] parameterTypes = parameterClassName.split(",");

            if (parameterTypes.length > parameterJsons.length) {
                log.debug("parameter length not same! {}-{}", parameterTypes.length, parameterJsons.length);
                return null;
            }

            List<String> paramList = Lists.newArrayList(parameterJsons);

            List<Object> objs = Lists.newArrayList();
            for (int i = 0; i < parameterTypes.length; i++) {
                try {
                    JSONObject jsonObject = JSON.parseObject(paramList.get(i));
                    jsonObject.put("class", parameterTypes[i]);
                    objs.add(jsonObject);
                } catch (Exception e) {
                    objs.add(paramList.get(i));
                }
            }
//            paramType = getMethodParamType(interfaceName, methodName);
//            return genericService.$invoke(methodName, getMethodParamType(interfaceName, methodName), paramObject);
            return genericService.$invoke(methodName, parameterTypes, objs.toArray());
        }
        return null;
    }

    /**
     * 转换方法类型
     *
     * @param interfaceName 接口类
     * @param methodName    方法名称
     * @return 方法类型
     */
    public static String[] getMethodParamType(String interfaceName, String methodName) {
        try {
            //创建类
            Class<?> class1 = Class.forName(interfaceName);
            //获取所有的公共的方法
            Method[] methods = class1.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    Class[] paramClassList = method.getParameterTypes();
                    String[] paramTypeList = new String[paramClassList.length];
                    int i = 0;
                    for (Class className : paramClassList) {
                        paramTypeList[i] = className.getTypeName();
                        i++;
                    }
                    return paramTypeList;
                }
            }
        } catch (Exception e) {
            log.error(interfaceName + " getMethodParamType!", e);
        }
        return null;

    }
}

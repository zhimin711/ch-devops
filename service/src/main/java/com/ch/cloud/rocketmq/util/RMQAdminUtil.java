package com.ch.cloud.rocketmq.util;

import cn.hutool.core.lang.Pair;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.utils.CommonUtils;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/15
 */
@Slf4j
public class RMQAdminUtil {
    
    private static final ThreadLocal<Pair<String, MQAdminExt>> MQ_ADMIN_EXT_THREAD_LOCAL = new ThreadLocal<>();
    
    public static synchronized void initMQAdminExt(String nameSrvAddr) {
        Pair<String, MQAdminExt> pair = MQ_ADMIN_EXT_THREAD_LOCAL.get();
        if (pair == null || !nameSrvAddr.equals(pair.getKey())) {
            DefaultMQAdminExt mqAdminExt = new DefaultMQAdminExt();
            mqAdminExt.setNamesrvAddr(nameSrvAddr);
            mqAdminExt.setAdminExtGroup("admin_ext_group_" + System.currentTimeMillis());
            try {
                mqAdminExt.start();
                MQ_ADMIN_EXT_THREAD_LOCAL.set(Pair.of(nameSrvAddr, mqAdminExt));
            } catch (Exception e) {
                log.error("{} init MQAdminExt error", nameSrvAddr, e);
            }
        }
    }
    
    public static MQAdminExt getClient() {
        Pair<String, MQAdminExt> pair = MQ_ADMIN_EXT_THREAD_LOCAL.get();
        Assert.notNull(pair, PubError.NOT_EXISTS, "MQAdminExt should be init before you get this");
        return pair.getValue();
    }
    
    public static String getClientAddr() {
        Pair<String, MQAdminExt> pair = MQ_ADMIN_EXT_THREAD_LOCAL.get();
        Assert.notNull(pair, PubError.NOT_EXISTS, "MQAdminExt should be init before you get this");
        return pair.getKey();
    }
    
    public static void destroyMQAdminExt() {
        Pair<String, MQAdminExt> pair = MQ_ADMIN_EXT_THREAD_LOCAL.get();
        if (pair != null) {
            pair.getValue().shutdown();
            MQ_ADMIN_EXT_THREAD_LOCAL.remove();
        }
    }
    
    public static Set<String> changeToBrokerNameSet(HashMap<String, Set<String>> clusterAddrTable,
            List<String> clusterNameList, List<String> brokerNameList) {
        Set<String> finalBrokerNameList = Sets.newHashSet();
        if (CommonUtils.isNotEmpty(clusterNameList)) {
            try {
                for (String clusterName : clusterNameList) {
                    finalBrokerNameList.addAll(clusterAddrTable.get(clusterName));
                }
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
        if (CommonUtils.isNotEmpty(brokerNameList)) {
            finalBrokerNameList.addAll(brokerNameList);
        }
        return finalBrokerNameList;
    }
    
}

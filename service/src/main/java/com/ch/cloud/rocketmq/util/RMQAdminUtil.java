package com.ch.cloud.rocketmq.util;

import cn.hutool.core.lang.Pair;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.CommonUtils;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.impl.MQClientManager;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.RemotingClient;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.remoting.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExtImpl;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.joor.Reflect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.rocketmq.remoting.protocol.RemotingSerializable.decode;

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
            if (CommonUtils.isNotEmpty(ContextUtil.getUsername())) {
                mqAdminExt.setAdminExtGroup("admin_ext_group_" + ContextUtil.getUsername()+"_" + System.currentTimeMillis());
                mqAdminExt.setInstanceName("rmq_admin_user_" + ContextUtil.getUsername());
            } else {
                mqAdminExt.setAdminExtGroup("admin_ext_group_" + System.currentTimeMillis());
                mqAdminExt.setInstanceName("rmq_admin_util");
            }
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

    public static Set<String> changeToBrokerNameSet(Map<String, Set<String>> clusterAddrTable,
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


    public static TopicConfig examineTopicConfig(String addr, String topic) {
        RemotingClient remotingClient = getRemotingClient();
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_ALL_TOPIC_CONFIG, null);
        RemotingCommand response = null;
        try {
            response = remotingClient.invokeSync(addr, request, 3000);
        } catch (Exception err) {
            throw Throwables.propagate(err);
        }
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                TopicConfigSerializeWrapper topicConfigSerializeWrapper = decode(response.getBody(), TopicConfigSerializeWrapper.class);
                return topicConfigSerializeWrapper.getTopicConfigTable().get(topic);
            }
            default:
                throw Throwables.propagate(new MQBrokerException(response.getCode(), response.getRemark()));
        }
    }

    private static RemotingClient getRemotingClient() {
        MQClientInstance mqClientInstance = getMqClientInstance();
        MQClientAPIImpl mQClientAPIImpl = Reflect.on(mqClientInstance).get("mQClientAPIImpl");
        return Reflect.on(mQClientAPIImpl).get("remotingClient");
    }

    private static MQClientInstance getMqClientInstance() {
        MQAdminExt client = getClient();
        DefaultMQAdminExtImpl defaultMQAdminExtImpl = Reflect.on(client)
                .get("defaultMQAdminExtImpl");
        return Reflect.on(defaultMQAdminExtImpl).get("mqClientInstance");
    }

    public  static SubscriptionGroupConfig examineSubscriptionGroupConfig(String addr, String group) {
        RemotingClient remotingClient = getRemotingClient();
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG, null);
        RemotingCommand response = null;
        try {
            response = remotingClient.invokeSync(addr, request, 3000);
        } catch (Exception err) {
            throw Throwables.propagate(err);
        }
        assert response != null;
        switch (response.getCode()) {
            case ResponseCode.SUCCESS: {
                SubscriptionGroupWrapper subscriptionGroupWrapper = decode(response.getBody(), SubscriptionGroupWrapper.class);
                return subscriptionGroupWrapper.getSubscriptionGroupTable().get(group);
            }
            default:
                throw Throwables.propagate(new MQBrokerException(response.getCode(), response.getRemark()));
        }
    }

    public static void initManager(){
        DefaultMQAdminExt mqAdminExt = (DefaultMQAdminExt)getClient();
        MQClientManager.getInstance().getOrCreateMQClientInstance(mqAdminExt);
    }

    public static DefaultMQPullConsumer createConsumer(String toolsConsumerGroup, RPCHook rpcHook) {
        RMQAdminUtil.initManager();
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(toolsConsumerGroup, rpcHook);
        consumer.resetClientConfig((DefaultMQAdminExt)RMQAdminUtil.getClient());
        return consumer;
    }
}

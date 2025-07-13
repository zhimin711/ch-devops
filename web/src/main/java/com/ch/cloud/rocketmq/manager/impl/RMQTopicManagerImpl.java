package com.ch.cloud.rocketmq.manager.impl;

import com.ch.cloud.rocketmq.manager.RMQTopicManager;
import com.ch.cloud.rocketmq.model.request.SendTopicMessageRequest;
import com.ch.cloud.rocketmq.model.request.TopicConfigInfo;
import com.ch.cloud.rocketmq.util.RMQAdminUtil;
import com.ch.e.Assert;
import com.ch.e.PubError;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.protocol.admin.TopicStatsTable;
import org.apache.rocketmq.remoting.protocol.body.ClusterInfo;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.body.TopicList;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
@Service
@Slf4j
public class RMQTopicManagerImpl implements RMQTopicManager {

    @SneakyThrows
    @Override
    public TopicList fetchAllTopicList() {
        return RMQAdminUtil.getClient().fetchAllTopicList();
    }

    @SneakyThrows
    @Override
    public TopicStatsTable stats(String topic) {
        return RMQAdminUtil.getClient().examineTopicStats(topic);
    }

    @SneakyThrows
    @Override
    public TopicRouteData route(String topic) {
        return RMQAdminUtil.getClient().examineTopicRouteInfo(topic);
    }

    @SneakyThrows
    @Override
    public GroupList queryTopicConsumerInfo(String topic) {
        return RMQAdminUtil.getClient().queryTopicConsumeByWho(topic);
    }

    @SneakyThrows
    @Override
    public void createOrUpdate(TopicConfigInfo topicCreateOrUpdateRequest) {
        TopicConfig topicConfig = new TopicConfig();
        BeanUtils.copyProperties(topicCreateOrUpdateRequest, topicConfig);
        ClusterInfo clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        for (String brokerName : RMQAdminUtil.changeToBrokerNameSet(clusterInfo.getClusterAddrTable(),
                topicCreateOrUpdateRequest.getClusterNameList(), topicCreateOrUpdateRequest.getBrokerNameList())) {
            RMQAdminUtil.getClient()
                    .createAndUpdateTopicConfig(clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr(),
                            topicConfig);
        }
    }

    @SneakyThrows
    @Override
    public TopicConfig examineTopicConfig(String topic, String brokerName) {
        ClusterInfo clusterInfo  = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        String addr = clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr();
        return RMQAdminUtil.examineTopicConfig(addr, topic);
    }

    @Override
    public List<TopicConfigInfo> examineTopicConfig(String topic) {
        List<TopicConfigInfo> topicConfigInfoList = Lists.newArrayList();
        TopicRouteData topicRouteData = route(topic);
        for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
            TopicConfigInfo topicConfigInfo = new TopicConfigInfo();
            TopicConfig topicConfig = examineTopicConfig(topic, brokerData.getBrokerName());
            Assert.notNull(topicConfig, PubError.NOT_EXISTS, "TopicConfig ");
            BeanUtils.copyProperties(topicConfig, topicConfigInfo);
            topicConfigInfo.setBrokerNameList(Lists.newArrayList(brokerData.getBrokerName()));
            topicConfigInfoList.add(topicConfigInfo);
        }
        return topicConfigInfoList;
    }

    @SneakyThrows
    @Override
    public boolean deleteTopic(String topic, String clusterName) {
        if (StringUtils.isBlank(clusterName)) {
            return deleteTopic(topic);
        }
        Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(RMQAdminUtil.getClient(), clusterName);
        RMQAdminUtil.getClient().deleteTopicInBroker(masterSet, topic);
        Set<String> nameServerSet = null;
        if (StringUtils.isNotBlank(RMQAdminUtil.getClientAddr())) {
            String[] ns = RMQAdminUtil.getClientAddr().split(";");
            nameServerSet = new HashSet<>(Arrays.asList(ns));
        }
        RMQAdminUtil.getClient().deleteTopicInNameServer(nameServerSet, topic);
        return true;
    }

    @SneakyThrows
    @Override
    public boolean deleteTopic(String topic) {
        ClusterInfo clusterInfo = null;
        clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        for (String clusterName : clusterInfo.getClusterAddrTable().keySet()) {
            deleteTopic(topic, clusterName);
        }
        return true;
    }

    @SneakyThrows
    @Override
    public boolean deleteTopicInBroker(String brokerName, String topic) {

        ClusterInfo clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        RMQAdminUtil.getClient().deleteTopicInBroker(
                Sets.newHashSet(clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr()), topic);
        return true;
    }

    @Override
    public SendResult sendTopicMessageRequest(SendTopicMessageRequest sendTopicMessageRequest) {
        DefaultMQProducer producer = new DefaultMQProducer(MixAll.SELF_TEST_PRODUCER_GROUP);
        producer.setInstanceName(String.valueOf(System.currentTimeMillis()));
        producer.setNamesrvAddr(RMQAdminUtil.getClientAddr());
        try {
            producer.start();
            Message msg = new Message(sendTopicMessageRequest.getTopic(), sendTopicMessageRequest.getTag(),
                    sendTopicMessageRequest.getKey(), sendTopicMessageRequest.getMessageBody().getBytes());
            return producer.send(msg);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            producer.shutdown();
        }
    }

}

package com.ch.cloud.rocketmq.manager.impl;

import com.ch.cloud.rocketmq.manager.RMQConsumerManager;
import com.ch.cloud.rocketmq.model.ConsumerGroupRollBackStat;
import com.ch.cloud.rocketmq.model.GroupConsumeInfo;
import com.ch.cloud.rocketmq.model.QueueStatInfo;
import com.ch.cloud.rocketmq.model.TopicConsumerInfo;
import com.ch.cloud.rocketmq.model.request.ConsumerConfigInfo;
import com.ch.cloud.rocketmq.model.request.DeleteSubGroupRequest;
import com.ch.cloud.rocketmq.model.request.ResetOffsetRequest;
import com.ch.cloud.rocketmq.util.RMQAdminUtil;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.RollbackStats;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
@Service
@Slf4j
public class RMQConsumerManagerImpl implements RMQConsumerManager {
    
    @Override
    @SneakyThrows
    public List<GroupConsumeInfo> queryGroupList() {
        Set<String> consumerGroupSet = Sets.newHashSet();
        ClusterInfo clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
            SubscriptionGroupWrapper subscriptionGroupWrapper = RMQAdminUtil.getClient()
                    .getAllSubscriptionGroup(brokerData.selectBrokerAddr(), 3000L);
            consumerGroupSet.addAll(subscriptionGroupWrapper.getSubscriptionGroupTable().keySet());
        }
        List<GroupConsumeInfo> groupConsumeInfoList = Lists.newArrayList();
        for (String consumerGroup : consumerGroupSet) {
            groupConsumeInfoList.add(queryGroup(consumerGroup));
        }
        Collections.sort(groupConsumeInfoList);
        return groupConsumeInfoList;
    }
    
    @Override
    public GroupConsumeInfo queryGroup(String consumerGroup) {
        GroupConsumeInfo groupConsumeInfo = new GroupConsumeInfo();
        try {
            ConsumeStats consumeStats = null;
            try {
                consumeStats = RMQAdminUtil.getClient().examineConsumeStats(consumerGroup);
            } catch (Exception e) {
                log.warn("examineConsumeStats exception, " + consumerGroup, e);
            }
            
            ConsumerConnection consumerConnection = null;
            try {
                consumerConnection = RMQAdminUtil.getClient().examineConsumerConnectionInfo(consumerGroup);
            } catch (Exception e) {
                log.warn("examineConsumerConnectionInfo exception, " + consumerGroup, e);
            }
            
            groupConsumeInfo.setGroup(consumerGroup);
            
            if (consumeStats != null) {
                groupConsumeInfo.setConsumeTps((int) consumeStats.getConsumeTps());
                groupConsumeInfo.setDiffTotal(consumeStats.computeTotalDiff());
            }
            
            if (consumerConnection != null) {
                groupConsumeInfo.setCount(consumerConnection.getConnectionSet().size());
                groupConsumeInfo.setMessageModel(consumerConnection.getMessageModel());
                groupConsumeInfo.setConsumeType(consumerConnection.getConsumeType());
                groupConsumeInfo.setVersion(MQVersion.getVersionDesc(consumerConnection.computeMinVersion()));
            }
        } catch (Exception e) {
            log.warn("examineConsumeStats or examineConsumerConnectionInfo exception, {}", consumerGroup, e);
        }
        return groupConsumeInfo;
    }
    
    @Override
    public List<TopicConsumerInfo> queryConsumeStatsListByGroupName(String groupName) {
        return queryConsumeStatsList(null, groupName);
    }
    
    @SneakyThrows
    @Override
    public List<TopicConsumerInfo> queryConsumeStatsList(final String topic, String groupName) {
        ConsumeStats consumeStats = RMQAdminUtil.getClient().examineConsumeStats(groupName);
        List<MessageQueue> mqList = Lists.newArrayList(Iterables.filter(consumeStats.getOffsetTable().keySet(),
                o -> StringUtils.isBlank(topic) || o.getTopic().equals(topic)));
        Collections.sort(mqList);
        List<TopicConsumerInfo> topicConsumerInfoList = Lists.newArrayList();
        TopicConsumerInfo nowTopicConsumerInfo = null;
        Map<MessageQueue, String> messageQueueClientMap = getClientConnection(groupName);
        for (MessageQueue mq : mqList) {
            if (nowTopicConsumerInfo == null || (!StringUtils.equals(mq.getTopic(), nowTopicConsumerInfo.getTopic()))) {
                nowTopicConsumerInfo = new TopicConsumerInfo(mq.getTopic());
                topicConsumerInfoList.add(nowTopicConsumerInfo);
            }
            QueueStatInfo queueStatInfo = QueueStatInfo.fromOffsetTableEntry(mq, consumeStats.getOffsetTable().get(mq));
            queueStatInfo.setClientInfo(messageQueueClientMap.get(mq));
            nowTopicConsumerInfo.appendQueueStatInfo(queueStatInfo);
        }
        return topicConsumerInfoList;
    }
    
    private Map<MessageQueue, String> getClientConnection(String groupName) {
        Map<MessageQueue, String> results = Maps.newHashMap();
        try {
            ConsumerConnection consumerConnection = RMQAdminUtil.getClient().examineConsumerConnectionInfo(groupName);
            for (Connection connection : consumerConnection.getConnectionSet()) {
                String clinetId = connection.getClientId();
                ConsumerRunningInfo consumerRunningInfo = RMQAdminUtil.getClient()
                        .getConsumerRunningInfo(groupName, clinetId, false);
                for (MessageQueue messageQueue : consumerRunningInfo.getMqTable().keySet()) {
                    //                    results.put(messageQueue, clinetId + " " + connection.getClientAddr());
                    results.put(messageQueue, clinetId);
                }
            }
        } catch (Exception err) {
            log.error("op=getClientConnection_error", err);
        }
        return results;
    }
    
    @SneakyThrows
    @Override
    public Map<String /*groupName*/, TopicConsumerInfo> queryConsumeStatsListByTopicName(String topic) {
        Map<String, TopicConsumerInfo> group2ConsumerInfoMap = Maps.newHashMap();
        GroupList groupList = RMQAdminUtil.getClient().queryTopicConsumeByWho(topic);
        for (String group : groupList.getGroupList()) {
            List<TopicConsumerInfo> topicConsumerInfoList = queryConsumeStatsList(topic, group);
            group2ConsumerInfoMap.put(group, CommonUtils.isEmpty(topicConsumerInfoList) ? new TopicConsumerInfo(topic)
                    : topicConsumerInfoList.get(0));
        }
        return group2ConsumerInfoMap;
    }
    
    @Override
    public Map<String, ConsumerGroupRollBackStat> resetOffset(ResetOffsetRequest resetOffsetRequest) {
        Map<String, ConsumerGroupRollBackStat> groupRollbackStats = Maps.newHashMap();
        for (String consumerGroup : resetOffsetRequest.getConsumerGroupList()) {
            try {
                Map<MessageQueue, Long> rollbackStatsMap = RMQAdminUtil.getClient()
                        .resetOffsetByTimestamp(resetOffsetRequest.getTopic(), consumerGroup,
                                resetOffsetRequest.getResetTime(), resetOffsetRequest.isForce());
                ConsumerGroupRollBackStat consumerGroupRollBackStat = new ConsumerGroupRollBackStat(true);
                List<RollbackStats> rollbackStatsList = consumerGroupRollBackStat.getRollbackStatsList();
                for (Map.Entry<MessageQueue, Long> rollbackStatsEntty : rollbackStatsMap.entrySet()) {
                    RollbackStats rollbackStats = new RollbackStats();
                    rollbackStats.setRollbackOffset(rollbackStatsEntty.getValue());
                    rollbackStats.setQueueId(rollbackStatsEntty.getKey().getQueueId());
                    rollbackStats.setBrokerName(rollbackStatsEntty.getKey().getBrokerName());
                    rollbackStatsList.add(rollbackStats);
                }
                groupRollbackStats.put(consumerGroup, consumerGroupRollBackStat);
            } catch (MQClientException e) {
                if (ResponseCode.CONSUMER_NOT_ONLINE == e.getResponseCode()) {
                    try {
                        ConsumerGroupRollBackStat consumerGroupRollBackStat = new ConsumerGroupRollBackStat(true);
                        List<RollbackStats> rollbackStatsList = RMQAdminUtil.getClient()
                                .resetOffsetByTimestampOld(consumerGroup, resetOffsetRequest.getTopic(),
                                        resetOffsetRequest.getResetTime(), true);
                        consumerGroupRollBackStat.setRollbackStatsList(rollbackStatsList);
                        groupRollbackStats.put(consumerGroup, consumerGroupRollBackStat);
                        continue;
                    } catch (Exception err) {
                        log.error("op=resetOffset_which_not_online_error", err);
                    }
                } else {
                    log.error("op=resetOffset_error", e);
                }
                groupRollbackStats.put(consumerGroup, new ConsumerGroupRollBackStat(false, e.getMessage()));
            } catch (Exception e) {
                log.error("op=resetOffset_error", e);
                groupRollbackStats.put(consumerGroup, new ConsumerGroupRollBackStat(false, e.getMessage()));
            }
        }
        return groupRollbackStats;
    }
    
    @Override
    @SneakyThrows
    public List<ConsumerConfigInfo> examineSubscriptionGroupConfig(String group) {
        List<ConsumerConfigInfo> consumerConfigInfoList = Lists.newArrayList();
        ClusterInfo clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        for (String brokerName : clusterInfo.getBrokerAddrTable().keySet()) { //foreach brokerName
            String brokerAddress = clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr();
            SubscriptionGroupConfig subscriptionGroupConfig = RMQAdminUtil.getClient()
                    .examineSubscriptionGroupConfig(brokerAddress, group);
            if (subscriptionGroupConfig == null) {
                continue;
            }
            consumerConfigInfoList.add(new ConsumerConfigInfo(Lists.newArrayList(brokerName), subscriptionGroupConfig));
        }
        return consumerConfigInfoList;
    }
    
    
    @SneakyThrows
    @Override
    public boolean deleteSubGroup(DeleteSubGroupRequest deleteSubGroupRequest) {
        ClusterInfo clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        for (String brokerName : deleteSubGroupRequest.getBrokerNameList()) {
            log.info("addr={} groupName={}", clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr(),
                    deleteSubGroupRequest.getGroupName());
            RMQAdminUtil.getClient()
                    .deleteSubscriptionGroup(clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr(),
                            deleteSubGroupRequest.getGroupName());
        }
        return true;
    }
    
    @SneakyThrows
    @Override
    public boolean createAndUpdateSubscriptionGroupConfig(ConsumerConfigInfo consumerConfigInfo) {
        ClusterInfo clusterInfo = RMQAdminUtil.getClient().examineBrokerClusterInfo();
        for (String brokerName : RMQAdminUtil.changeToBrokerNameSet(clusterInfo.getClusterAddrTable(),
                consumerConfigInfo.getClusterNameList(), consumerConfigInfo.getBrokerNameList())) {
            RMQAdminUtil.getClient().createAndUpdateSubscriptionGroupConfig(
                    clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr(),
                    consumerConfigInfo.getSubscriptionGroupConfig());
        }
        return true;
    }
    
    
    @SneakyThrows
    @Override
    public Set<String> fetchBrokerNameSetBySubscriptionGroup(String group) {
        Set<String> brokerNameSet = Sets.newHashSet();
        List<ConsumerConfigInfo> consumerConfigInfoList = examineSubscriptionGroupConfig(group);
        for (ConsumerConfigInfo consumerConfigInfo : consumerConfigInfoList) {
            brokerNameSet.addAll(consumerConfigInfo.getBrokerNameList());
        }
        return brokerNameSet;
        
    }
    
    @SneakyThrows
    @Override
    public ConsumerConnection getConsumerConnection(String consumerGroup) {
        return RMQAdminUtil.getClient().examineConsumerConnectionInfo(consumerGroup);
    }
    
    @SneakyThrows
    @Override
    public ConsumerRunningInfo getConsumerRunningInfo(String consumerGroup, String clientId, boolean jstack) {
        return RMQAdminUtil.getClient().getConsumerRunningInfo(consumerGroup, clientId, jstack);
    }
}

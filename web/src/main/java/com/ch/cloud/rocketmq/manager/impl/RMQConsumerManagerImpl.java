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
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.remoting.protocol.ResponseCode;
import org.apache.rocketmq.remoting.protocol.admin.ConsumeStats;
import org.apache.rocketmq.remoting.protocol.admin.RollbackStats;
import org.apache.rocketmq.remoting.protocol.body.*;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.subscription.SubscriptionGroupConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author zhimi
 * @since 2025/5/16
 */
@Service
@Slf4j
public class RMQConsumerManagerImpl implements RMQConsumerManager, InitializingBean, AutoCloseable {

    private volatile boolean isCacheBeingBuilt = false;

    private  static final Set<String> SYSTEM_GROUP_SET = new HashSet<>();

    private ExecutorService executorService;

    private final List<GroupConsumeInfo> cacheConsumeInfoList = Collections.synchronizedList(new ArrayList<>());

    private final HashMap<String, List<String>> consumerGroupMap = Maps.newHashMap();


    static {
        SYSTEM_GROUP_SET.add(MixAll.TOOLS_CONSUMER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.FILTERSRV_CONSUMER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.SELF_TEST_CONSUMER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.ONS_HTTP_PROXY_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_ONSAPI_PULL_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_ONSAPI_PERMISSION_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_ONSAPI_OWNER_GROUP);
        SYSTEM_GROUP_SET.add(MixAll.CID_SYS_RMQ_TRANS);
    }
    @Override
    public void afterPropertiesSet() {
        Runtime runtime = Runtime.getRuntime();
        int corePoolSize = Math.max(10, runtime.availableProcessors() * 2);
        int maximumPoolSize = Math.max(20, runtime.availableProcessors() * 2);
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicLong threadIndex = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "QueryGroup_" + this.threadIndex.incrementAndGet());
            }
        };
        RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardOldestPolicy();
        this.executorService = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000), threadFactory, handler);
    }

    @Override
    public void close() {
        ThreadUtils.shutdownGracefully(executorService, 10L, TimeUnit.SECONDS);
    }

    @Override
    @SneakyThrows
    public List<GroupConsumeInfo> queryGroupList(boolean skipSysGroup, String address) {
        if (isCacheBeingBuilt) {
            throw new RuntimeException("Cache is being built, please try again later");
        }

        synchronized (this) {
            if (cacheConsumeInfoList.isEmpty() && !isCacheBeingBuilt) {
                isCacheBeingBuilt = true;
                try {
                    makeGroupListCache();
                } finally {
                    isCacheBeingBuilt = false;
                }
            }
        }

        if (cacheConsumeInfoList.isEmpty()) {
            throw new RuntimeException("No consumer group information available");
        }

        List<GroupConsumeInfo> groupConsumeInfoList = new ArrayList<>(cacheConsumeInfoList);

        if (!skipSysGroup) {
            groupConsumeInfoList.stream().map(group -> {
                if (SYSTEM_GROUP_SET.contains(group.getGroup())) {
                    group.setGroup(String.format("%s%s", "%SYS%", group.getGroup()));
                }
                return group;
            }).collect(Collectors.toList());
        }
        Collections.sort(groupConsumeInfoList);
        return groupConsumeInfoList;
    }


    public void makeGroupListCache() {
        SubscriptionGroupWrapper subscriptionGroupWrapper = null;
        try {
            ClusterInfo clusterInfo = clusterInfoService.get();
            for (BrokerData brokerData : clusterInfo.getBrokerAddrTable().values()) {
                subscriptionGroupWrapper = RMQAdminUtil.getClient().getAllSubscriptionGroup(brokerData.selectBrokerAddr(), 30000L);
                for (String groupName : subscriptionGroupWrapper.getSubscriptionGroupTable().keySet()) {
                    if (!consumerGroupMap.containsKey(groupName)) {
                        consumerGroupMap.putIfAbsent(groupName, new ArrayList<>());
                    }
                    List<String> addresses = consumerGroupMap.get(groupName);
                    addresses.add(brokerData.selectBrokerAddr());
                    consumerGroupMap.put(groupName, addresses);
                }
            }
        } catch (Exception err) {
            Throwables.throwIfUnchecked(err);
            throw new RuntimeException(err);
        }

        if (subscriptionGroupWrapper != null && subscriptionGroupWrapper.getSubscriptionGroupTable().isEmpty()) {
            log.warn("No subscription group information available");
            isCacheBeingBuilt = false;
            return;
        }
        final ConcurrentMap<String, SubscriptionGroupConfig> subscriptionGroupTable = subscriptionGroupWrapper.getSubscriptionGroupTable();
        List<GroupConsumeInfo> groupConsumeInfoList = Collections.synchronizedList(Lists.newArrayList());
        CountDownLatch countDownLatch = new CountDownLatch(consumerGroupMap.size());
        for (Map.Entry<String, List<String>> entry : consumerGroupMap.entrySet()) {
            String consumerGroup = entry.getKey();
            executorService.submit(() -> {
                try {
                    GroupConsumeInfo consumeInfo = queryGroup(consumerGroup, "");
                    consumeInfo.setAddress(entry.getValue());
                    if (SYSTEM_GROUP_SET.contains(consumerGroup)) {
                        consumeInfo.setSubGroupType("SYSTEM");
                    } else {
                        try {
                            consumeInfo.setSubGroupType(subscriptionGroupTable.get(consumerGroup).isConsumeMessageOrderly() ? "FIFO" : "NORMAL");
                        } catch (NullPointerException e) {
                            log.warn("SubscriptionGroupConfig not found for consumer group: {}", consumerGroup);
                            boolean isFifoType = examineSubscriptionGroupConfig(consumerGroup)
                                    .stream().map(ConsumerConfigInfo::getSubscriptionGroupConfig)
                                    .allMatch(SubscriptionGroupConfig::isConsumeMessageOrderly);
                            consumeInfo.setSubGroupType(isFifoType ? "FIFO" : "NORMAL");
                        }
                    }
                    consumeInfo.setUpdateTime(new Date());
                    groupConsumeInfoList.add(consumeInfo);
                } catch (Exception e) {
                    log.error("queryGroup exception, consumerGroup: {}", consumerGroup, e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interruption occurred while waiting for task completion", e);
        }
        log.info("All consumer group query tasks have been completed");
        isCacheBeingBuilt = false;
        Collections.sort(groupConsumeInfoList);

        cacheConsumeInfoList.clear();
        cacheConsumeInfoList.addAll(groupConsumeInfoList);
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
            SubscriptionGroupConfig subscriptionGroupConfig = RMQAdminUtil.examineSubscriptionGroupConfig(brokerAddress, group);
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

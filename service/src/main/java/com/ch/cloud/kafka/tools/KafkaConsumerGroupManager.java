package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.dto.ConsumerGroupDTO;
import com.ch.cloud.kafka.dto.ConsumerGroupDescribeDTO;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.pojo.ResetOffset;
import com.ch.cloud.kafka.pojo.TopicOffset;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.e.PubError;
import com.ch.utils.AssertUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author zhimin
 * @since 2022/3/27 6:10 下午
 */
@Service
public class KafkaConsumerGroupManager {

    @Autowired
    private KafkaClusterService kafkaClusterService;
    @Autowired
    private KafkaClusterManager kafkaClusterManager;

    public int countConsumerGroup(Long clusterId) throws ExecutionException, InterruptedException {
        AdminClient adminClient = kafkaClusterManager.getAdminClient(clusterId);
        return adminClient.listConsumerGroups().all().get().size();
    }

    /**
     * 根据topic获取消费组
     *
     * @param clusterId 集群ID
     * @param topicName topic名称
     * @return 消费组列表
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<ConsumerGroupDTO> consumerGroups(Long clusterId,String topicName) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AssertUtils.isEmpty(config, PubError.NOT_EXISTS, "cluster id" + clusterId);
        AdminClient adminClient = KafkaClusterUtils.getAdminClient(config);
        Collection<ConsumerGroupListing> consumerGroupListings = adminClient.listConsumerGroups().all().get();
        List<String> groupIds = consumerGroupListings.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        Map<String, ConsumerGroupDescription> groups = adminClient.describeConsumerGroups(groupIds).all().get();
        List<ConsumerGroupDTO> consumerGroups = new ArrayList<>();
        for (ConsumerGroupListing consumerGroupListing : consumerGroupListings) {
            String groupId = consumerGroupListing.groupId();
            ConsumerGroupDescription consumerGroupDescription = groups.get(groupId);
            consumerGroupDescription.members()
                    .stream()
                    .map(s -> s.assignment().topicPartitions())
                    .flatMap(Collection::stream)
                    .filter(s -> s.topic().equals(topicName))
                    .findAny()
                    .ifPresent(topicPartition -> {
                        ConsumerGroupDTO consumerGroup = new ConsumerGroupDTO();
                        consumerGroup.setGroupId(groupId);
                        consumerGroups.add(consumerGroup);
                    });
        }
        // 统计消费组剩余多少未消费
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClusterUtils.createConsumer(config)) {
            for (ConsumerGroupDTO consumerGroup : consumerGroups) {
                List<TopicOffset> topicOffsetList = getTopicOffsets(topicName, consumerGroup.getGroupId(), adminClient, kafkaConsumer);
                long lag = topicOffsetList.stream()
                        .mapToLong(x -> x.getEndOffset() - x.getConsumerOffset())
                        .sum();
                consumerGroup.setLag(lag);
            }
        }

        return consumerGroups;
    }

    public List<TopicOffset> offset(String topicName, String groupId, Long clusterId) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AssertUtils.isEmpty(config, PubError.NOT_EXISTS, "cluster id" + clusterId);
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClusterUtils.createConsumer(config)) {
            return getTopicOffsets(topicName, groupId, KafkaClusterUtils.getAdminClient(config), kafkaConsumer);
        }
    }

    private List<TopicOffset> getTopicOffsets(String topicName, String groupId, AdminClient adminClient, KafkaConsumer<String, String> kafkaConsumer) throws InterruptedException, ExecutionException {
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(groupId)
                .partitionsToOffsetAndMetadata()
                .get();
        Set<TopicPartition> topicPartitions = topicPartitionOffsetAndMetadataMap.keySet();
        Map<TopicPartition, Long> endOffsets = kafkaConsumer.endOffsets(topicPartitions);
        Map<TopicPartition, Long> beginningOffsets = kafkaConsumer.beginningOffsets(topicPartitions);
        List<TopicOffset> topicOffsets = new ArrayList<>();

        topicPartitions.stream()
                .filter(topicPartition -> Objects.equals(topicPartition.topic(), topicName))
                .forEachOrdered(topicPartition -> {
                    int partition = topicPartition.partition();
                    OffsetAndMetadata offsetAndMetadata = topicPartitionOffsetAndMetadataMap.get(topicPartition);
                    Long beginningOffset = beginningOffsets.get(topicPartition);
                    Long endOffset = endOffsets.get(topicPartition);
                    long offset = offsetAndMetadata.offset();

                    TopicOffset topicOffset = new TopicOffset();
                    topicOffset.setGroupId(groupId);
                    topicOffset.setTopic(topicName);
                    topicOffset.setPartition(partition);
                    topicOffset.setConsumerOffset(offset);
                    topicOffset.setBeginningOffset(beginningOffset);
                    topicOffset.setEndOffset(endOffset);
                    topicOffsets.add(topicOffset);
                });
        return topicOffsets;
    }

    public void resetOffset(String topic, String groupId, String clusterId, ResetOffset resetOffset) {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AssertUtils.isEmpty(config, PubError.NOT_EXISTS, "cluster id" + clusterId);
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClusterUtils.createConsumer(config.getBrokers(), groupId, "earliest", config.getSecurityProtocol(), config.getSaslMechanism(), config.getAuthUsername(), config.getAuthPassword())) {
            TopicPartition topicPartition = new TopicPartition(topic, resetOffset.getPartition());
            List<TopicPartition> topicPartitions = Collections.singletonList(topicPartition);

            kafkaConsumer.assign(topicPartitions);
            if (Objects.equals("beginning", resetOffset.getSeek())) {
                kafkaConsumer.seek(topicPartition, 0);
            } else if (Objects.equals("end", resetOffset.getSeek())) {
                Map<TopicPartition, Long> topicPartitionLongMap = kafkaConsumer.endOffsets(topicPartitions);
                Long logSize = topicPartitionLongMap.get(topicPartition);
                kafkaConsumer.seek(topicPartition, logSize);
            } else {
                kafkaConsumer.seek(topicPartition, resetOffset.getOffset());
            }
            kafkaConsumer.commitSync();
        }
    }

    public List<ConsumerGroupDTO> consumerGroup(Long clusterId, String filterGroupId) throws ExecutionException, InterruptedException {

        AdminClient adminClient = kafkaClusterManager.getAdminClient(clusterId);
        Collection<ConsumerGroupListing> consumerGroupListings = adminClient.listConsumerGroups().all().get();
        List<String> groupIds = consumerGroupListings.stream()
                .map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        if (StringUtils.hasText(filterGroupId)) {
            groupIds = groupIds.stream()
                    .filter(x -> x.toLowerCase().contains(filterGroupId.toLowerCase()))
                    .collect(Collectors.toList());
        }
        Map<String, ConsumerGroupDescription> groups = adminClient.describeConsumerGroups(groupIds).all().get();

        return groupIds
                .stream()
                .map(groupId -> {
                    ConsumerGroupDTO consumerGroup = new ConsumerGroupDTO();
                    consumerGroup.setGroupId(groupId);

                    ConsumerGroupDescription consumerGroupDescription = groups.get(groupId);
                    Set<String> topics = consumerGroupDescription.members()
                            .stream()
                            .map(s -> s.assignment().topicPartitions())
                            .flatMap(Collection::stream)
                            .map(TopicPartition::topic)
                            .collect(Collectors.toSet());
                    consumerGroup.setTopics(topics);
                    return consumerGroup;
                })
                .collect(Collectors.toList());
    }

    public ConsumerGroupDTO info(Long clusterId, String groupId) throws ExecutionException, InterruptedException {
        AdminClient adminClient = kafkaClusterManager.getAdminClient(clusterId);
        ConsumerGroupDescription consumerGroupDescription = adminClient.describeConsumerGroups(Collections.singletonList(groupId)).all().get().get(groupId);
        Set<String> topicNames = consumerGroupDescription
                .members()
                .stream()
                .flatMap(x -> x.assignment().topicPartitions().stream())
                .map(TopicPartition::topic)
                .collect(Collectors.toSet());

        ConsumerGroupDTO consumerGroupInfo = new ConsumerGroupDTO();
        consumerGroupInfo.setGroupId(groupId);
        consumerGroupInfo.setTopics(topicNames);

        return consumerGroupInfo;
    }

    public void delete(Long clusterId, String groupId) throws ExecutionException, InterruptedException {
        AdminClient adminClient = kafkaClusterManager.getAdminClient(clusterId);
        adminClient.deleteConsumerGroups(Collections.singletonList(groupId)).all().get();
    }

    public List<ConsumerGroupDescribeDTO> describe(Long clusterId, String groupId) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AssertUtils.isEmpty(config, PubError.NOT_EXISTS, "cluster id" + clusterId);
        AdminClient adminClient = KafkaClusterUtils.getAdminClient(config);
        ConsumerGroupDescription consumerGroupDescription = adminClient.describeConsumerGroups(Collections.singletonList(groupId)).all().get().get(groupId);

        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(groupId)
                .partitionsToOffsetAndMetadata()
                .get();
        Set<TopicPartition> topicPartitions = topicPartitionOffsetAndMetadataMap.keySet();
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClusterUtils.createConsumer(config)) {
            Map<TopicPartition, Long> endOffsets = kafkaConsumer.endOffsets(topicPartitions);
            Map<TopicPartition, Long> beginningOffsets = kafkaConsumer.beginningOffsets(topicPartitions);

            return consumerGroupDescription.members()
                    .stream()
                    .flatMap(consumer -> {
                        return consumer.assignment().topicPartitions()
                                .stream()
                                .map(topicPartition -> {
                                    String topic = topicPartition.topic();
                                    int partition = topicPartition.partition();
                                    String consumerId = consumer.consumerId();
                                    String host = consumer.host();
                                    String clientId = consumer.clientId();
                                    OffsetAndMetadata offsetAndMetadata = topicPartitionOffsetAndMetadataMap.get(topicPartition);
                                    Long beginningOffset = beginningOffsets.get(topicPartition);
                                    Long endOffset = endOffsets.get(topicPartition);
                                    Long offset = null;
                                    if (offsetAndMetadata != null) {
                                        offset = offsetAndMetadata.offset();
                                    }
                                    ConsumerGroupDescribeDTO consumerGroupDescribe = new ConsumerGroupDescribeDTO();
                                    consumerGroupDescribe.setGroupId(groupId);
                                    consumerGroupDescribe.setTopic(topic);
                                    consumerGroupDescribe.setPartition(partition);
                                    consumerGroupDescribe.setCurrentOffset(offset);
                                    consumerGroupDescribe.setLogBeginningOffset(beginningOffset);
                                    consumerGroupDescribe.setLogEndOffset(endOffset);
                                    if (endOffset != null && offset != null) {
                                        consumerGroupDescribe.setLag(endOffset - offset);
                                    } else {
                                        consumerGroupDescribe.setLag(null);
                                    }
                                    consumerGroupDescribe.setConsumerId(consumerId);
                                    consumerGroupDescribe.setHost(host);
                                    consumerGroupDescribe.setClientId(clientId);

                                    return consumerGroupDescribe;
                                });
                    })
                    .collect(Collectors.toList());
        }
    }
}

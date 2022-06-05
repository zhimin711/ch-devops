package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.dto.BrokerDTO;
import com.ch.cloud.kafka.pojo.Partition;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.utils.AssertUtils;
import com.ch.utils.CommonUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author zhimin.ma
 * @since 2018/9/21 15:48
 */
@Component
public class KafkaClusterManager {

    private Logger logger = LoggerFactory.getLogger(KafkaClusterManager.class);

    private int timeout    = 100000;
    private int bufferSize = 64 * 1024;

    private Map<String, Integer> brokers;

    public enum SearchType {
        CONTENT, EARLIEST, LATEST
    }

    @Autowired
    private KafkaClusterService kafkaClusterService;
    @Autowired
    private KafkaTopicService   topicService;


    public AdminClient getAdminClient(Long id) {
        KafkaCluster config = kafkaClusterService.find(id);
        AssertUtils.isEmpty(config, PubError.NOT_EXISTS, "cluster id" + id);
        return KafkaClusterUtils.getAdminClient(config);
    }

    public List<BrokerDTO> brokers(String topic, Long clusterId) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        return brokers(topic, config);
    }

    public List<BrokerDTO> brokers(String topic, KafkaCluster config) throws ExecutionException, InterruptedException {
        AssertUtils.isEmpty(config, PubError.NOT_EXISTS, "cluster config");
        AdminClient adminClient = KafkaClusterUtils.getAdminClient(config);
        DescribeClusterResult describeClusterResult = adminClient.describeCluster();
        Collection<Node> clusterDetails = describeClusterResult.nodes().get();
        List<BrokerDTO> brokers = new ArrayList<>(clusterDetails.size());
        for (Node node : clusterDetails) {
            BrokerDTO broker = new BrokerDTO();
            broker.setId(node.id());
            broker.setHost(node.host());
            broker.setPort(node.port());
            brokers.add(broker);
        }

        Set<String> topicNames;
        if (StringUtils.hasText(topic)) {
            topicNames = new HashSet<>(Collections.singletonList(topic));
        } else {
            List<KafkaTopic> topics = topicService.findByClusterIdLikeTopicName(config.getId(), null);
            if (CommonUtils.isNotEmpty(topics)) {
                topicNames = topics.stream().map(KafkaTopic::getTopicName).collect(Collectors.toSet());
            } else {
                return brokers;
            }
        }

        Map<String, TopicDescription> stringTopicDescriptionMap = adminClient.describeTopics(topicNames).all().get();
        for (TopicDescription topicDescription : stringTopicDescriptionMap.values()) {
            List<TopicPartitionInfo> partitions = topicDescription.partitions();
            for (TopicPartitionInfo partitionInfo : partitions) {
                Node leader = partitionInfo.leader();
                for (BrokerDTO broker : brokers) {
                    if (leader != null && broker.getId() == leader.id()) {
                        broker.getLeaderPartitions().add(partitionInfo.partition());
                        break;
                    }
                }

                List<Node> replicas = partitionInfo.replicas();
                for (BrokerDTO broker : brokers) {
                    for (Node replica : replicas) {
                        if (broker.getId() == replica.id()) {
                            broker.getFollowerPartitions().add(partitionInfo.partition());
                            break;
                        }
                    }
                }
            }
        }

        if (StringUtils.hasText(topic)) {
            // 使用topic过滤时只展示相关的broker
            brokers = brokers.stream().filter(broker -> broker.getFollowerPartitions().size() > 0 || broker.getLeaderPartitions().size() > 0).collect(Collectors.toList());
        }

        return brokers;
    }

    /**
     * 获取指定 topic 的所有分区 offset
     *
     * @param topicName 主题
     * @param clusterId 集群ID
     * @return
     */

    public List<Partition> partitions(String topicName, Long clusterId) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AdminClient adminClient = KafkaClusterUtils.getAdminClient(config);
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClusterUtils.createConsumer(config)) {
            Map<String, TopicDescription> stringTopicDescriptionMap = adminClient.describeTopics(Collections.singletonList(topicName)).all().get();
            TopicDescription topicDescription = stringTopicDescriptionMap.get(topicName);

            List<TopicPartitionInfo> partitionInfos = topicDescription.partitions();
            List<TopicPartition> topicPartitions = partitionInfos.stream().map(x -> new TopicPartition(topicName, x.partition())).collect(Collectors.toList());
            Map<TopicPartition, Long> beginningOffsets = kafkaConsumer.beginningOffsets(topicPartitions);
            Map<TopicPartition, Long> endOffsets = kafkaConsumer.endOffsets(topicPartitions);

            Iterator<TopicPartitionInfo> iterator = partitionInfos.iterator();
            List<Partition> partitionArrayList = new ArrayList<>(partitionInfos.size());
            while (iterator.hasNext()) {
                TopicPartitionInfo partitionInfo = iterator.next();
                Node leader = partitionInfo.leader();

                Partition partition = new Partition();
                partition.setPartition(partitionInfo.partition());
                partition.setLeader(new Partition.Node(leader.id(), leader.host(), leader.port()));

                List<Partition.Node> isr = partitionInfo.isr().stream().map(node -> new Partition.Node(node.id(), node.host(), node.port())).collect(Collectors.toList());
                List<Partition.Node> replicas = partitionInfo.replicas().stream().map(node -> new Partition.Node(node.id(), node.host(), node.port())).collect(Collectors.toList());

                partition.setIsr(isr);
                partition.setReplicas(replicas);

                partitionArrayList.add(partition);

                for (TopicPartition topicPartition : topicPartitions) {
                    if (partition.getPartition() == topicPartition.partition()) {
                        Long beginningOffset = beginningOffsets.get(topicPartition);
                        Long endOffset = endOffsets.get(topicPartition);
                        partition.setBeginningOffset(beginningOffset);
                        partition.setEndOffset(endOffset);
                        break;
                    }
                }
            }

            List<Integer> brokerIds = brokers(topicName, clusterId).stream().map(BrokerDTO::getId).collect(Collectors.toList());
            Map<Integer, Map<String, LogDirDescription>> integerMapMap = null;
            try {
                integerMapMap = adminClient.describeLogDirs(brokerIds).allDescriptions().get();
            } catch (InterruptedException | ExecutionException ignored) {
                for (Partition partition : partitionArrayList) {
                    for (Partition.Node replica : partition.getReplicas()) {
                        replica.setLogSize(-1L);
                    }
                }
            }
            if (integerMapMap != null) {
                for (Partition partition : partitionArrayList) {
                    for (Partition.Node replica : partition.getReplicas()) {
                        Map<String, LogDirDescription> logDirDescriptionMap = integerMapMap.get(replica.getId());
                        if (logDirDescriptionMap != null) {
                            for (LogDirDescription logDirDescription : logDirDescriptionMap.values()) {
                                Map<TopicPartition, ReplicaInfo> topicPartitionReplicaInfoMap = logDirDescription.replicaInfos();
                                for (Map.Entry<TopicPartition, ReplicaInfo> replicaInfoEntry : topicPartitionReplicaInfoMap.entrySet()) {
                                    TopicPartition topicPartition = replicaInfoEntry.getKey();
                                    if (Objects.equals(topicName, topicPartition.topic()) && topicPartition.partition() == partition.getPartition()) {
                                        ReplicaInfo replicaInfo = replicaInfoEntry.getValue();
                                        long size = replicaInfo.size();
                                        replica.setLogSize(replica.getLogSize() + size);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return partitionArrayList;
        }
    }

    public TopicInfo info(Long clusterId, String topicName) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AdminClient adminClient = KafkaClusterUtils.getAdminClient(config);
        try (KafkaConsumer<String, String> kafkaConsumer = KafkaClusterUtils.createConsumer(config)) {
            TopicDescription topicDescription = adminClient.describeTopics(Collections.singletonList(topicName)).all().get().get(topicName);
            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setClusterId(clusterId);
            topicInfo.setName(topicName);

            List<TopicPartitionInfo> partitionInfos = topicDescription.partitions();
            int replicaCount = 0;
            for (TopicPartitionInfo topicPartitionInfo : partitionInfos) {
                replicaCount += topicPartitionInfo.replicas().size();
            }
            topicInfo.setReplicaSize(replicaCount);

            List<TopicPartition> topicPartitions = partitionInfos.stream().map(x -> new TopicPartition(topicName, x.partition())).collect(Collectors.toList());
            Map<TopicPartition, Long> beginningOffsets = kafkaConsumer.beginningOffsets(topicPartitions);
            Map<TopicPartition, Long> endOffsets = kafkaConsumer.endOffsets(topicPartitions);

            List<TopicInfo.Partition> partitions = topicPartitions.stream().map(topicPartition -> {
                Long beginningOffset = beginningOffsets.get(topicPartition);
                Long endOffset = endOffsets.get(topicPartition);
                return new TopicInfo.Partition(topicPartition.partition(), beginningOffset, endOffset);
            }).collect(Collectors.toList());

            topicInfo.setPartitions(partitions);


            List<Integer> brokerIds = brokers(topicName, clusterId).stream().map(BrokerDTO::getId).collect(Collectors.toList());
            Map<Integer, Map<String, LogDirDescription>> integerMapMap;
            try {
                integerMapMap = adminClient.describeLogDirs(brokerIds).allDescriptions().get();
                topicInfo.setTotalLogSize(0L);
                for (Map<String, LogDirDescription> descriptionMap : integerMapMap.values()) {
                    for (LogDirDescription logDirDescription : descriptionMap.values()) {
                        Map<TopicPartition, ReplicaInfo> topicPartitionReplicaInfoMap = logDirDescription.replicaInfos();
                        for (Map.Entry<TopicPartition, ReplicaInfo> replicaInfoEntry : topicPartitionReplicaInfoMap.entrySet()) {
                            TopicPartition topicPartition = replicaInfoEntry.getKey();
                            if (!Objects.equals(topicName, topicPartition.topic())) {
                                continue;
                            }
                            ReplicaInfo replicaInfo = replicaInfoEntry.getValue();
                            long size = replicaInfo.size();
                            topicInfo.setTotalLogSize(topicInfo.getTotalLogSize() + size);
                        }
                    }
                }
            } catch (InterruptedException | ExecutionException ignored) {
                topicInfo.setTotalLogSize(-1L);
            }
            return topicInfo;
        }
    }

    public List<KafkaTopicDTO> topics(Long clusterId, String name) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.find(clusterId);
        AssertUtils.isEmpty(config, PubError.NON_NULL, "集群ID:" + clusterId);
        Set<String> topicNames = KafkaClusterUtils.fetchTopicNames(config);
        if (StringUtils.hasText(name)) {
            topicNames = topicNames
                    .stream()
                    .filter(topic -> topic.toLowerCase().contains(name.toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toSet());
        }
        return topics(config, topicNames);
    }

    public List<KafkaTopicDTO> topics(KafkaCluster cluster, Set<String> topicNames) throws InterruptedException, ExecutionException {
        AdminClient adminClient = KafkaClusterUtils.getAdminClient(cluster);
        Map<String, TopicDescription> stringTopicDescriptionMap = adminClient.describeTopics(topicNames).all().get();

        List<KafkaTopicDTO> topics = stringTopicDescriptionMap
                .entrySet()
                .stream().map(e -> {
                    KafkaTopicDTO topic = new KafkaTopicDTO();
                    topic.setClusterId(cluster.getId());
                    topic.setTopicName(e.getKey());
                    topic.setPartitionSize(e.getValue().partitions().size());
                    topic.setTotalLogSize(0L);
                    topic.setReplicaSize(0);
                    return topic;
                })
                .collect(Collectors.toList());

        List<Integer> brokerIds = brokers(null, cluster.getId()).stream().map(BrokerDTO::getId).collect(Collectors.toList());
        Map<Integer, Map<String, LogDirDescription>> integerMapMap = null;
        try {
            integerMapMap = adminClient.describeLogDirs(brokerIds).allDescriptions().get();
        } catch (InterruptedException | ExecutionException ignored) {
            for (KafkaTopicDTO topic : topics) {
                topic.setTotalLogSize(-1L);
            }
        }

        if (integerMapMap != null) {
            for (KafkaTopicDTO topic : topics) {
                for (Map<String, LogDirDescription> descriptionMap : integerMapMap.values()) {
                    for (LogDirDescription logDirDescription : descriptionMap.values()) {
                        Map<TopicPartition, ReplicaInfo> topicPartitionReplicaInfoMap = logDirDescription.replicaInfos();
                        for (Map.Entry<TopicPartition, ReplicaInfo> replicaInfoEntry : topicPartitionReplicaInfoMap.entrySet()) {
                            TopicPartition topicPartition = replicaInfoEntry.getKey();
                            if (!Objects.equals(topic.getTopicName(), topicPartition.topic())) {
                                continue;
                            }
                            ReplicaInfo replicaInfo = replicaInfoEntry.getValue();
                            long size = replicaInfo.size();
                            topic.setReplicaSize(topic.getReplicaSize() + 1);
                            topic.setTotalLogSize(topic.getTotalLogSize() + size);
                        }
                    }
                }
            }
        }

        return topics;
    }

}

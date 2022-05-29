package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.model.KafkaCluster;
import com.ch.cloud.kafka.model.KafkaTopic;
import com.ch.cloud.kafka.dto.BrokerDTO;
import com.ch.cloud.kafka.pojo.Partition;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.KafkaClusterService;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
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
        ExceptionUtils.assertEmpty(config, PubError.NOT_EXISTS, "cluster id" + id);
        return KafkaClusterUtils.getAdminClient(config);
    }

    public List<BrokerDTO> brokers(String topic, String clusterId) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.findByClusterName(clusterId);
        return brokers(topic, config);
    }

    public List<BrokerDTO> brokers(String topic, KafkaCluster config) throws ExecutionException, InterruptedException {
        ExceptionUtils.assertEmpty(config, PubError.NOT_EXISTS, "cluster config");
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
            List<KafkaTopic> topics = topicService.findByClusterLikeTopic(config.getClusterName(), null);
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
     * @param topicName   主题
     * @param clusterName 集群名称
     * @return
     */

    public List<Partition> partitions(String topicName, String clusterName) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.findByClusterName(clusterName);
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

            List<Integer> brokerIds = brokers(topicName, clusterName).stream().map(BrokerDTO::getId).collect(Collectors.toList());
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
/*
    public List<String> searchTopicStringContent(String topic, String content, SearchType searchType, Class<?> clazz) {
        return searchTopicContent(ContentType.STRING, searchType, topic, content, clazz);
    }

    public List<String> searchTopicProtostuffContent(String topic, String content, Class<?> clazz, SearchType searchType) {
        return searchTopicContent(ContentType.PROTO_STUFF, searchType, topic, content, clazz);
    }

    public List<String> searchTopicContent(ContentType contentType, SearchType searchType, String topic, String content, Class<?> clazz) {
        List<String> resultList = Lists.newArrayList();
        Map<Integer, Long> earliestOffsetMap = getEarliestOffset(topic);
        Map<Integer, kafka.javaapi.PartitionMetadata> leaders = this.findLeader(brokers, topic);

        int offset = 10;
        if ((searchType == SearchType.LATEST || searchType == SearchType.EARLIEST) && CommonUtils.isNumeric(content)) {
            int total = Integer.parseInt(content);
            if (total > 0)
                offset = total / leaders.size();
        }
        for (int partitionId : leaders.keySet()) {
            kafka.javaapi.PartitionMetadata metadata = leaders.get(partitionId);
            String leadBroker = metadata.leader().host();
            int leadPort = metadata.leader().port();
            SimpleConsumer consumer = new SimpleConsumer(leadBroker, leadPort, timeout, bufferSize, getClientId(topic, partitionId));
            try {
                long earliestOffset = earliestOffsetMap.get(partitionId);
                long latestOffset = this.getPartitionOffset(consumer, topic, partitionId, OffsetRequest.LatestTime());
//            long[] latestOffsets = this.getPartitionOffsets(consumer, topic, partitionId, OffsetRequest.LatestTime());

                long startOffset = earliestOffset;
                long endOffset = latestOffset;
                if (searchType == SearchType.LATEST && (latestOffset - offset) > startOffset) {
                    startOffset = latestOffset - offset;
                } else if (searchType == SearchType.EARLIEST && (earliestOffset + offset) > latestOffset) {
                    endOffset = earliestOffset + offset;
                }
                logger.info("info\t=====> partition: {}, earliestOffset: {}, latestOffset: {}, startOffset: {}, endOffset: {}", partitionId, earliestOffset, latestOffset, startOffset, endOffset);
                while (startOffset < endOffset) {
                    FetchRequest req = new FetchRequestBuilder()
                            .clientId(getClientId(topic, partitionId))
                            // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                            .addFetch(topic, partitionId, startOffset, bufferSize)
                            .build();
                    FetchResponse resp = consumer.fetch(req);
                    kafka.javaapi.FetchResponse response = new kafka.javaapi.FetchResponse(resp);
                    if (response.hasError()) {
                        // Something went wrong! ErrorMapping.maybeThrowException();
                        short code = response.errorCode(topic, partitionId);
                        logger.error("Error fetching data from the Broker:{} Reason: {}", leadBroker, code);
                        continue;
                    }
                    ByteBufferMessageSet msgSet = response.messageSet(topic, partitionId);
                    if (!msgSet.iterator().hasNext()) {
                        logger.warn("Fetching data from start:{} empty!", startOffset);
                        startOffset++;
                        continue;
                    }
                    int msgCount = 0;
                    for (MessageAndOffset messageAndOffset : msgSet) {
                        long currentOffset = messageAndOffset.offset();
                        if (currentOffset < startOffset) {
                            logger.error("Found an old offset: {}, Expecting: {}", currentOffset, startOffset);
                            continue;
                        }
                        startOffset = messageAndOffset.nextOffset();
                        ByteBuffer payload = messageAndOffset.message().payload();

                        byte[] bytes = new byte[payload.limit()];
                        payload.get(bytes);
                        String msg;
                        if (contentType == ContentType.JSON) {
                            msg = new String(bytes);
                            if (clazz != null) {
                                msg = JSONUtils.toJsonDateFormat(JSONUtils.fromJson(msg, clazz), DateUtils.Pattern.DATETIME_CN);
                            }
                        } else if (contentType == ContentType.PROTO_STUFF) {
                            Object o = KafkaSerializeUtils.deSerialize(bytes, clazz);
                            if (o == null) {
                                msg = new String(bytes);
                            } else {
                                msg = JSONUtils.toJsonDateFormat(o, DateUtils.Pattern.DATETIME_CN);
                            }
                        } else {
                            msg = new String(bytes);
                            if (clazz != null) {
                                msg = JSONUtils.toJsonDateFormat(JSONUtils.fromJson(msg, clazz), DateUtils.Pattern.DATETIME_CN);
                            }
                        }
                        if (searchType == SearchType.LATEST || searchType == SearchType.EARLIEST
                                || (searchType == SearchType.CONTENT && msg.contains(content))) {
                            logger.info("message\t=====>{}: {}", messageAndOffset.offset(), msg);
                            resultList.add(msg);
                        }
                        msgCount++;
                    }
                    logger.info("result\t=====> count:{}, read last offset: {}", msgCount, startOffset);
                }
            } finally {
                consumer.close();
            }
        }
        return resultList;
    }*/


    public TopicInfo info(String clusterId, String topicName) throws ExecutionException, InterruptedException {
        KafkaCluster config = kafkaClusterService.findByClusterName(clusterId);
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

}

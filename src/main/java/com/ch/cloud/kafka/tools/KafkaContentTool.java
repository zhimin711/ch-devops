package com.ch.cloud.kafka.tools;

import com.ch.cloud.kafka.pojo.PartitionInfo;
import com.ch.e.PubError;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.ExceptionUtils;
import com.ch.utils.JSONUtils;
import com.google.common.collect.Lists;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import kafka.api.*;
import kafka.common.TopicAndPartition;
import kafka.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author 01370603
 * @date 2018/9/21 15:48
 */
public class KafkaContentTool {

    private Logger logger = LoggerFactory.getLogger(KafkaContentTool.class);

    private int timeout = 100000;
    private int bufferSize = 64 * 1024;

    private String topic;

    private Map<String, Integer> brokers;

    private List<PartitionInfo> partitions;

    public enum SearchType {
        CONTENT, EARLIEST, LATEST
    }

    public enum ContentType {
        STRING, JSON, PROTOSTUFF
    }

    public KafkaContentTool(String zkUrl, String topic) {
        if (CommonUtils.isEmpty(zkUrl)) {
            throw ExceptionUtils.create(PubError.ARGS);
        }
        brokers = KafkaManager.getAllBrokersInCluster(zkUrl);
        this.topic = topic;
        partitions = getTopicPartitions();
    }

    /**
     * 获取指定 topic 的所有分区 offset
     *
     * @return
     */
    private List<PartitionInfo> getTopicPartitions() {
        List<PartitionInfo> partitions = Lists.newArrayList();
        Map<Integer, kafka.javaapi.PartitionMetadata> leaders = this.findLeader(brokers);
        for (int partitionId : leaders.keySet()) {
            kafka.javaapi.PartitionMetadata metadata = leaders.get(partitionId);
            PartitionInfo info = new PartitionInfo();
            info.setId(partitionId);
            info.setHost(metadata.leader().host());
            info.setPort(metadata.leader().port());
            try {
                SimpleConsumer consumer = new SimpleConsumer(info.getHost(), info.getPort(), timeout, bufferSize, getClientId(partitionId));
                long partitionOffset1 = getPartitionOffsets(consumer, partitionId, OffsetRequest.EarliestTime());
                long partitionOffset2 = getPartitionOffsets(consumer, partitionId, OffsetRequest.LatestTime());
                info.setBegin(partitionOffset1);
                info.setEnd(partitionOffset2);
                info.setTotal(partitionOffset2 - partitionOffset1);
                partitions.add(info);
                consumer.close();
            } catch (Exception ignored) {
            }
        }
        return partitions;
    }

    /**
     * 获取 offset
     *
     * @param consumer  SimpleConsumer
     * @param partition partition
     * @param whichTime 要获取offset的时间,-1 最新，-2 最早
     * @return
     */
    private long getPartitionOffsets(SimpleConsumer consumer, int partition, long whichTime) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<>();
        // PartitionOffsetRequestInfo(long time, int maxNumOffsets)
        // 第二个参数maxNumOffsets
        // 1 时返回whichTime 对应的offset，
        // 2 返回一个包含最大和最小offset的元组
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, OffsetRequest.CurrentVersion(), consumer.clientId());
        OffsetResponse resp = consumer.getOffsetsBefore(request.underlying());
        kafka.javaapi.OffsetResponse response = new kafka.javaapi.OffsetResponse(resp);
        if (response.hasError()) {
            logger.error("Error fetching data Offset Data the Broker. Reason:{}", response.errorCode(topic, partition));
            return 0;
        }
        return response.offsets(topic, partition)[0];
    }

    /***
     * 获取每个 partition 元数据信息
     * @param bootstraps (host,port)
     * @return
     */
    private Map<Integer, kafka.javaapi.PartitionMetadata> findLeader(Map<String, Integer> bootstraps) {
        Map<Integer, kafka.javaapi.PartitionMetadata> map = new TreeMap<>();
        for (Map.Entry<String, Integer> bootstrap : bootstraps.entrySet()) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(bootstrap.getKey(), bootstrap.getValue(), timeout, bufferSize, getClientId());
                List<String> topics = Collections.singletonList(topic);
                kafka.javaapi.TopicMetadataRequest req = new kafka.javaapi.TopicMetadataRequest(topics);

                TopicMetadataResponse resp = consumer.send(req.underlying());
                kafka.javaapi.TopicMetadataResponse response = new kafka.javaapi.TopicMetadataResponse(resp);

                List<kafka.javaapi.TopicMetadata> metaData = response.topicsMetadata();
                for (kafka.javaapi.TopicMetadata item : metaData) {
                    for (kafka.javaapi.PartitionMetadata part : item.partitionsMetadata()) {
                        map.put(part.partitionId(), part);
                    }
                }
            } catch (Exception e) {
                logger.error("Error communicating with Broker [{}] to find Leader for [{}] Reason: ", bootstrap, topic, e);
            } finally {
                if (consumer != null)
                    consumer.close();
            }
        }
        return map;
    }

    private String getClientId() {
        return "Client_" + topic;
    }

    private String getClientId(int partition) {
        return "Client_" + topic + "_" + partition;
    }


}

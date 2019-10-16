package com.ch.cloud.kafka.tools;

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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author 01370603
 * @date 2018/9/21 15:48
 */
public class KafkaTool {

    private Logger logger = LoggerFactory.getLogger(KafkaTool.class);

    private int timeout = 100000;
    private int bufferSize = 64 * 1024;

    private Map<String, Integer> brokers;

    public enum SearchType {
        CONTENT, EARLIEST, LATEST
    }

    public enum ContentType {
        STRING, JSON, PROTOSTUFF
    }

    public KafkaTool(String zkUrl) {
        if (CommonUtils.isEmpty(zkUrl)) {
            throw ExceptionUtils.create(PubError.ARGS);
        }
        brokers = KafkaManager.getAllBrokersInCluster(zkUrl);
    }

    public Map<Integer, Long> getEarliestOffset(String topic) {
        //kafka.api.OffsetRequest.EarliestTime() = -2
        return getTopicOffset(topic, kafka.api.OffsetRequest.EarliestTime());
    }

    /***
     * 获取指定 topic 的所有分区 offset
     * @param topic 主题
     * @param whichTime   要获取offset的时间,-1 最新，-2 最早
     * @return
     */

    public Map<Integer, Long> getTopicOffset(String topic, long whichTime) {
        HashMap<Integer, Long> offsets = new HashMap<>();
        Map<Integer, kafka.javaapi.PartitionMetadata> leaders = this.findLeader(brokers, topic);
        for (int partitionId : leaders.keySet()) {
            kafka.javaapi.PartitionMetadata metadata = leaders.get(partitionId);
            String leadBroker = metadata.leader().host();
            int leadPort = metadata.leader().port();
            SimpleConsumer consumer = new SimpleConsumer(leadBroker, leadPort, timeout, bufferSize, getClientId(topic, partitionId));
            long partitionOffset = this.getPartitionOffset(consumer, topic, partitionId, whichTime);
            offsets.put(partitionId, partitionOffset);
        }
        return offsets;
    }


    /**
     * 获取指定 topic 的所有分区 offset
     *
     * @param topic     主题
     * @param whichTime 要获取offset的时间,-1 最新，-2 最早
     * @return
     */
    public void getTopicContextOffset(String topic, long whichTime) {
        logger.info("\t\t=====> getTopicContextOffset: {}", whichTime);
        Map<Integer, kafka.javaapi.PartitionMetadata> leaders = this.findLeader(brokers, topic);
        for (int partitionId : leaders.keySet()) {
            kafka.javaapi.PartitionMetadata metadata = leaders.get(partitionId);
            String leadBroker = metadata.leader().host();
            int leadPort = metadata.leader().port();
            SimpleConsumer consumer = new SimpleConsumer(leadBroker, leadPort, timeout, bufferSize, getClientId(topic, partitionId));
            long readOffset = this.getPartitionOffset(consumer, topic, partitionId, whichTime);
            logger.info("info\t\t=====> partition: {} readOffset: {}", partitionId, readOffset);
            FetchRequest req = new FetchRequestBuilder()
                    .clientId(getClientId(topic, partitionId))
                    // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                    .addFetch(topic, partitionId, readOffset, bufferSize ^ 2)
                    .build();
            FetchResponse resp = consumer.fetch(req);
            kafka.javaapi.FetchResponse response = new kafka.javaapi.FetchResponse(resp);
            if (response.hasError()) {
                // Something went wrong!
                short code = response.errorCode(topic, partitionId);
                logger.error("Error fetching data from the Broker:{} Reason: {}", leadBroker, code);
                continue;
            }
            ByteBufferMessageSet msgSet = response.messageSet(topic, partitionId);
            int msgCount = 0;
            long lastOffset = 0;
            for (MessageAndOffset messageAndOffset : msgSet) {
                long currentOffset = messageAndOffset.offset();
                if (currentOffset < readOffset) {
                    logger.error("Found an old offset: {}, Expecting: {}", currentOffset, readOffset);
                    continue;
                }
                readOffset = messageAndOffset.nextOffset();
                ByteBuffer payload = messageAndOffset.message().payload();

                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
//                logger.info("message\t=====>{}: {}", messageAndOffset.offset(), new String(bytes));
                msgCount++;
                lastOffset = currentOffset;
            }
            logger.info("result\t\t=====> count:{}, last offset: {}", msgCount, lastOffset);
            consumer.close();
        }

    }

    /**
     * 获取 offset
     *
     * @param consumer  SimpleConsumer
     * @param topic     topic
     * @param partition partition
     * @param whichTime 要获取offset的时间,-1 最新，-2 最早
     * @return
     */
    private long getPartitionOffset(SimpleConsumer consumer, String topic, int partition, long whichTime) {
        long[] offsets = getPartitionOffsets(consumer, topic, partition, whichTime);
        return offsets[0];
    }

    /**
     * 获取 offset
     *
     * @param consumer  SimpleConsumer
     * @param topic     topic
     * @param partition partition
     * @param whichTime 要获取offset的时间,-1 最新，-2 最早
     * @return
     */
    private long[] getPartitionOffsets(SimpleConsumer consumer, String topic, int partition, long whichTime) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<>();
        // PartitionOffsetRequestInfo(long time, int maxNumOffsets)
        // 中的第二个参数maxNumOffsets，没弄明白是什么意思，但是测试后发现传入1 时返回whichTime 对应的offset，传入2 返回一个包含最大和最小offset的元组
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 2));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, OffsetRequest.CurrentVersion(), consumer.clientId());
        OffsetResponse resp = consumer.getOffsetsBefore(request.underlying());
        kafka.javaapi.OffsetResponse response = new kafka.javaapi.OffsetResponse(resp);
        if (response.hasError()) {
            logger.error("Error fetching data Offset Data the Broker. Reason:{}", response.errorCode(topic, partition));
            return new long[]{};
        }
        return response.offsets(topic, partition);
    }

    /***
     * 获取每个 partition 元数据信息
     * @param bootstraps (host,port)
     * @param topic topic
     * @return
     */
    private Map<Integer, kafka.javaapi.PartitionMetadata> findLeader(Map<String, Integer> bootstraps, String topic) {
        Map<Integer, kafka.javaapi.PartitionMetadata> map = new TreeMap<>();
        for (Map.Entry<String, Integer> bootstrap : bootstraps.entrySet()) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(bootstrap.getKey(), bootstrap.getValue(), timeout, bufferSize, getClientId(topic));
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

    private String getClientId(String topic) {
        return "Client_" + topic;
    }

    private String getClientId(String topic, int partition) {
        return "Client_" + topic + "_" + partition;
    }


    /**
     * 获取指定 topic 的所有分区 offset
     *
     * @param topic 主题
     * @return
     */
    public void getTopicContent(String topic) {
        Map<Integer, kafka.javaapi.PartitionMetadata> leaders = this.findLeader(brokers, topic);

        Map<Integer, Long> earliestOffsetMap = getEarliestOffset(topic);
        for (int partitionId : leaders.keySet()) {
            kafka.javaapi.PartitionMetadata metadata = leaders.get(partitionId);
            String leadBroker = metadata.leader().host();
            int leadPort = metadata.leader().port();
            SimpleConsumer consumer = new SimpleConsumer(leadBroker, leadPort, timeout, bufferSize, getClientId(topic, partitionId));
            long latestOffset = this.getPartitionOffset(consumer, topic, partitionId, OffsetRequest.LatestTime());
            long[] latestOffsets = this.getPartitionOffsets(consumer, topic, partitionId, OffsetRequest.LatestTime());
            long readOffset = earliestOffsetMap.get(partitionId);//this.getPartitionOffset(consumer, topic, partitionId, whichTime);
            logger.info("info\t\t=====> partition: {}, earliestOffset: {}, latestOffset: {}. {}", partitionId, readOffset, latestOffset, latestOffsets);
            while (readOffset < latestOffset) {
                FetchRequest req = new FetchRequestBuilder()
                        .clientId(getClientId(topic, partitionId))
                        // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                        .addFetch(topic, partitionId, readOffset, bufferSize)
                        .build();
                FetchResponse resp = consumer.fetch(req);
                kafka.javaapi.FetchResponse response = new kafka.javaapi.FetchResponse(resp);
                if (response.hasError()) {
                    // Something went wrong!
//                ErrorMapping.maybeThrowException();
                    short code = response.errorCode(topic, partitionId);
                    logger.error("Error fetching data from the Broker:{} Reason: {}", leadBroker, code);
                    continue;
                }
                ByteBufferMessageSet msgSet = response.messageSet(topic, partitionId);
                int msgCount = 0;
                for (MessageAndOffset messageAndOffset : msgSet) {
                    long currentOffset = messageAndOffset.offset();
                    if (currentOffset < readOffset) {
                        logger.error("Found an old offset: {}, Expecting: {}", currentOffset, readOffset);
                        continue;
                    }
                    readOffset = messageAndOffset.nextOffset();
                    ByteBuffer payload = messageAndOffset.message().payload();

                    byte[] bytes = new byte[payload.limit()];
                    payload.get(bytes);
                    logger.info("message\t=====>{}: {}", messageAndOffset.offset(), new String(bytes));
                    msgCount++;
                }
                logger.info("result\t\t=====> count:{}, read last offset: {}", msgCount, readOffset);
            }
            consumer.close();
        }

    }

    public List<String> searchTopicStringContent(String topic, String content, SearchType searchType, Class<?> clazz) {
        return searchTopicContent(ContentType.STRING, searchType, topic, content, clazz);
    }

    public List<String> searchTopicProtostuffContent(String topic, String content, Class<?> clazz, SearchType searchType) {
        return searchTopicContent(ContentType.PROTOSTUFF, searchType, topic, content, clazz);
    }

    private <T> T deSerialize(byte[] data, Class<T> clazz) {
        if (clazz != null && data != null) {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T t = null;
            try {
                t = clazz.newInstance();
                ProtostuffIOUtil.mergeFrom(data, t, schema);
            } catch (InstantiationException | IllegalAccessException var5) {
                logger.error("deSerialize error, Class=" + clazz, var5);
            }
            return t;
        } else {
            return null;
        }
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
                        } else if (contentType == ContentType.PROTOSTUFF) {
                            Object o = deSerialize(bytes, clazz);
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
    }


}

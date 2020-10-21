package com.ch.cloud.kafka.tools;

import com.ch.StatusS;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.model.BtContentSearch;
import com.ch.cloud.kafka.pojo.ContentType;
import com.ch.cloud.kafka.pojo.PartitionInfo;
import com.ch.cloud.kafka.pojo.SearchType;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.cloud.kafka.service.IContentSearchService;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.e.PubError;
import com.ch.pool.DefaultThreadPool;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.ExceptionUtils;
import com.ch.utils.JSONUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import kafka.api.*;
import kafka.common.TopicAndPartition;
import kafka.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author zhimin.ma
 * @date 2018/9/21 15:48
 */
@Slf4j
public class KafkaContentTool {

    private int timeout = 100000;
    private int bufferSize = 64 * 1024;

    private String cluster;
    private String topic;

    private String username;

    private Long searchId;

    private Map<String, Integer> brokers;

    private static final Map<String, Producer<String, byte[]>> producerMap = Maps.newConcurrentMap();

    private List<PartitionInfo> partitions;

    private long total;

    private boolean async = false;

    public KafkaContentTool(String zookeeper, String cluster, String topic) {
        if (CommonUtils.isEmpty(zookeeper)) {
            throw ExceptionUtils.create(PubError.ARGS);
        }
        this.cluster = cluster;
        this.topic = topic;

        brokers = KafkaManager.getAllBrokersInCluster(zookeeper);
        partitions = getTopicPartitions();
    }

    public KafkaContentTool(String zkUrl, String topic) {
        if (CommonUtils.isEmpty(zkUrl)) {
            throw ExceptionUtils.create(PubError.ARGS);
        }
        brokers = KafkaManager.getAllBrokersInCluster(zkUrl);
        this.topic = topic;
        partitions = getTopicPartitions();
    }


    private String getClientId() {
        return "Client_" + topic;
    }

    private String getClientId(int partition) {
        return "Client_" + topic + "_" + partition;
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
                total += info.getTotal();
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
            log.error("Error fetching data Offset Data the Broker. Reason:{}", response.errorCode(topic, partition));
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
        SimpleConsumer consumer = null;
        for (Map.Entry<String, Integer> bootstrap : bootstraps.entrySet()) {
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
                log.error("Error communicating with Broker [{}] to find Leader for [{}] Reason: ", bootstrap, topic, e);
            } finally {
                if (consumer != null)
                    consumer.close();
            }
        }
        return map;
    }

    /**
     * 添加同步（线程安全）
     *
     * @param contentType 内容类型
     * @param searchType  搜索类型
     * @param searchSize  搜索量
     * @param content     搜索关键字
     * @param clazz       类对象
     * @return
     */
    public synchronized List<BtContentRecord> searchTopicContent(ContentType contentType, SearchType searchType, int searchSize, String content, Class<?> clazz) {
        saveSearch(searchType, searchSize, content);
        List<BtContentRecord> list = Lists.newArrayList();
        if (total > 200000 && searchType == SearchType.ALL) {
            async = true;
            DefaultThreadPool.exe(() -> {
                contentSearchService.start(searchId);
                List<BtContentRecord> list1 = searchTopicContent2(contentType, searchType, searchSize, content, clazz);
                contentRecordService.batchSave(list1);
                contentSearchService.end(searchId, "2");
            });
        } else {
            contentSearchService.start(searchId);
            list = searchTopicContent2(contentType, searchType, searchSize, content, clazz);
            contentSearchService.end(searchId, "2");
        }
        if (!async) {
            list.sort(Comparator.comparing(BtContentRecord::getMessageOffset).reversed());
        }
        return list;
    }

    private void saveSearch(SearchType searchType, int searchSize, String content) {
        BtContentSearch record = new BtContentSearch();
        record.setCluster(cluster);
        record.setTopic(topic);
        record.setType(searchType.name());
        record.setSize(searchSize);
        record.setContent(content);
        record.setStatus(StatusS.BEGIN);
        record.setCreateBy(username);
        contentSearchService.save(record);
        searchId = record.getId();
    }

    public List<BtContentRecord> searchTopicContent2(ContentType contentType, SearchType searchType, int searchSize, String content, Class<?> clazz) {
        log.info("{} message total: {}", topic, total);
        List<BtContentRecord> resultList = Lists.newArrayList();
        int partitionSearchSize = 10;
        if ((searchType == SearchType.LATEST || searchType == SearchType.EARLIEST)) {
            if (searchSize > 0)
                partitionSearchSize = searchSize / partitions.size();
        }
        for (PartitionInfo partition : partitions) {
            SimpleConsumer consumer = new SimpleConsumer(partition.getHost(), partition.getPort(), timeout, bufferSize, getClientId(partition.getId()));
            try {
                long startOffset = partition.getBegin();
                long endOffset = partition.getEnd();
                if (searchType == SearchType.LATEST && (partition.getEnd() - partitionSearchSize) > startOffset) {
                    startOffset = partition.getEnd() - partitionSearchSize;
                } else if (searchType == SearchType.EARLIEST && (partition.getBegin() + partitionSearchSize) > partition.getEnd()) {
                    endOffset = partition.getBegin() + partitionSearchSize;
                }
                log.info("info\t=====> partition: {}, earliestOffset: {}, latestOffset: {}, startOffset: {}, endOffset: {}", partition.getId(), partition.getBegin(), partition.getEnd(), startOffset, endOffset);
                while (startOffset < endOffset) {
                    FetchRequest req = new FetchRequestBuilder()
                            .clientId(getClientId(partition.getId()))
                            // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                            .addFetch(topic, partition.getId(), startOffset, bufferSize)
                            .build();
                    FetchResponse resp = consumer.fetch(req);
                    kafka.javaapi.FetchResponse response = new kafka.javaapi.FetchResponse(resp);
                    if (response.hasError()) {
                        // Something went wrong! ErrorMapping.maybeThrowException();
                        short code = response.errorCode(topic, partition.getId());
                        log.error("Error fetching data from the Broker:{} Reason: {}", partition.getHost(), code);
                        continue;
                    }
                    ByteBufferMessageSet msgSet = response.messageSet(topic, partition.getId());
                    if (!msgSet.iterator().hasNext()) {
                        log.warn("Fetching data from start:{} empty!", startOffset);
                        startOffset++;
                        continue;
                    }
                    int msgCount = 0;
                    for (MessageAndOffset messageAndOffset : msgSet) {
                        long currentOffset = messageAndOffset.offset();
                        if (currentOffset < startOffset) {
                            log.error("Found an old offset: {}, Expecting: {}", currentOffset, startOffset);
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
                                msg = JSONUtils.toJson(JSONUtils.fromJson(msg, clazz));
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
                                msg = JSONUtils.toJson(JSONUtils.fromJson(msg, clazz));
                            }
                        }
                        if (((CommonUtils.isEmpty(content) || msg.contains(content))
                                && (searchType == SearchType.LATEST || searchType == SearchType.EARLIEST))
                                || (searchType == SearchType.ALL && msg.contains(content))) {
                            log.info("message\t=====>{} : {}", messageAndOffset.offset(), msg);
                            BtContentRecord record = new BtContentRecord();
                            record.setSid(searchId);
                            record.setPartitionId(partition.getId());
                            record.setMessageOffset(currentOffset);
                            record.setContent(msg);
                            resultList.add(record);
                            if (resultList.size() > 1000) {
                                log.warn("{} search size > 1000 return!", topic);
                                return resultList;
                            }
                        }
                        msgCount++;
                    }
                    log.info("result\t=====> count:{}, read last offset: {}", msgCount, startOffset);
                }
            } finally {
                consumer.close();
            }
        }
        return resultList;
    }

    private IContentSearchService contentSearchService;
    private IContentRecordService contentRecordService;

    public void setContentSearchService(IContentSearchService contentSearchService) {
        this.contentSearchService = contentSearchService;
    }

    public void setContentRecordService(IContentRecordService contentRecordService) {
        this.contentRecordService = contentRecordService;
    }

    public Long getSearchId() {
        return searchId;
    }

    public void send(byte[] data) {

        Producer<String, byte[]> producer = producerMap.get(cluster);
        if (producer == null) {
            synchronized (producerMap) {

                Properties props = new Properties();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getServers());
                props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("schema.registry.url", schemaUrl);//schema.registry.url指向射麻的存储位置
                producer = new KafkaProducer<>(props);
                producerMap.put(cluster, producer);
            }
        }
        //不断生成消息并发送

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, null, data);
        producer.send(record);//将customer作为消息的值发送出去，KafkaAvroSerializer会处理剩下的事情
//        producer.close();
    }

    private String getServers() {
        List<String> servers = Lists.newArrayList();
        brokers.forEach((k, v) -> servers.add(k + ":" + v));
        return servers.stream().reduce((r, e) -> r.concat("," + e)).get();
    }

    public boolean isAsync() {
        return async;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

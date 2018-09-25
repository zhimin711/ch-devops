package com.ch.test;

import com.ch.cloud.kafka.admin.TopicsManager;
import com.ch.cloud.kafka.tools.KafkaTool;
import kafka.api.OffsetRequest;
import kafka.api.OffsetResponse;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.api.TopicMetadataResponse;
import kafka.common.TopicAndPartition;
import kafka.consumer.SimpleConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.*;

/**
 * @author 01370603
 * @date 2018/9/19 17:27
 */
public class KafkaTests {

    final String zk = "10.202.34.30:2182/kafka/st";
    final String servers = "10.202.34.28:9093,10.202.34.29:9093,10.202.34.30:9093";
    final String servers2 = "10.202.24.5:9094,10.202.24.6:9094,10.202.24.7:9094,10.202.24.8:9094,10.202.24.9:9094";
    final String servers3 = "10.202.24.5:9096,10.202.24.6:9096,10.202.24.7:9096,10.202.24.8:9096,10.202.24.9:9096";
    //    final String group = "GROUND_DEV_01370603";
    final String group = "GRD_DEV_S01";
    Object o;

    @Test
    public void testLoad() {
//        TopicsManager.listAllTopic("10.202.34.28:2182/kafka1.1.0/default");
//        TopicsManager.listAllTopic("10.202.34.30:2182/kafka/st");
//        TopicsManager.listTopicAllConfig("10.202.34.30:2182/kafka/st");
        TopicsManager.listTopicAllConfig("10.202.24.5:2181,10.202.24.6:2181,10.202.24.7:2181/kafka/bus");

    }

    @Test
    public void testConsumer() {
        Consumer<String, String> consumer = new KafkaConsumer<>(getProp());
        TopicPartition p = new TopicPartition("GROUND_DEV_LOG_02", 1);
    }


    @Test
    public void testAdmin() {

    }

    @Test
    public void consumer() {
        String topic = "GROUND_DEV_LOG_02";
        SimpleConsumer consumer = new SimpleConsumer("10.202.34.28", 9093, 1000, 1000, group);
        List<String> topics = Collections.singletonList(topic);
        kafka.javaapi.TopicMetadataRequest req = new kafka.javaapi.TopicMetadataRequest(topics);
        TopicMetadataResponse resp = consumer.send(req.underlying());
        kafka.javaapi.TopicMetadataResponse response = new kafka.javaapi.TopicMetadataResponse(resp);
        for (kafka.javaapi.TopicMetadata meta : response.topicsMetadata()) {
            meta.partitionsMetadata().forEach(r -> {
                long offset = getLastOffset(consumer, topic, r.partitionId(), OffsetRequest.LatestTime(), group);
                System.out.println(r.partitionId() + ":" + offset);
            });

        }
//        KafkaConsumer<String, Object> consumer = new KafkaConsumer<String, Object>(getProp());

//        while (true) {
//            ConsumerRecords<String, Object> records = consumer.poll(100);
//            for (ConsumerRecord<String, Object> record : records) {
//                System.out.println(record.toString());
//            }
//        }

//        FetchRequest req = new FetchRequestBuilder().clientId(group)
//                .addFetch("GROUND_DEV_LOG_02", 0, 100, 100) // 1000000bytes
//                .build();
//        FetchResponse fetchResponse = consumer2.fetch(req);

    }

    public static long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime, String clientName) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse resp = consumer.getOffsetsBefore(request.underlying());
        kafka.javaapi.OffsetResponse response = new kafka.javaapi.OffsetResponse(resp);
        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }


    private Properties getProp() {

        Properties props = new Properties();

        props.put("zookeeper.connect", servers);
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.202.34.28:9093,10.202.34.29:9093,10.202.34.30:9093");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

//  smallest,earliest,largest
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "smallest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");

        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        return props;
    }

    @Test
    public void test() {
        KafkaTool kafkaTool = new KafkaTool(servers3);
        String topic = "GROUND_DEV_LOG_02";
        topic = "SHIVA_TRTMS_GROUND_TEMP_REQUIRE";
//        Map<Integer, Long> partOffset = kafkaTool.getTopicOffset(topic, -1);
//        System.out.println(partOffset);
//        kafkaTool.getTopicContextOffset(topic, OffsetRequest.EarliestTime());
        kafkaTool.getTopicContent(topic);
        o = kafkaTool.searchTopicStringContent(topic,"666666752360");
        System.out.println(o);
    }

}

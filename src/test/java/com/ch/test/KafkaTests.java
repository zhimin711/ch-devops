package com.ch.test;

import com.ch.cloud.kafka.pojo.ContentType;
import com.ch.cloud.kafka.pojo.PartitionInfo;
import com.ch.cloud.kafka.pojo.SearchType;
import com.ch.cloud.kafka.tools.KafkaContentTool;
import com.ch.cloud.kafka.tools.KafkaManager;
import com.ch.cloud.kafka.tools.KafkaTool;
import com.ch.cloud.kafka.tools.TopicManager;
import com.ch.utils.JSONUtils;
import kafka.api.OffsetRequest;
import kafka.api.OffsetResponse;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.api.TopicMetadataResponse;
import kafka.common.TopicAndPartition;
import kafka.consumer.SimpleConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.*;

/**
 * @author 01370603
 * @date 2018/9/19 17:27
 */
public class KafkaTests {

    //dev sfst
    final String zk = "10.202.34.30:2182/kafka/st";
    final String zk_dev_1 = "10.202.34.28:2182,10.202.34.29:2182,10.202.34.30:2182/kafka/other";
    final String zk4 = "100.80.129.164:2181/kafka/o2o_dds_kscs_ud7nr3lf01";
    //test bus
    final String zk2 = "10.202.24.5:2181,10.202.24.6:2181,10.202.24.7:2181/kafka/bus";
    //test bus
    final String zk3 = "10.202.24.5:2181,10.202.24.6:2181,10.202.24.7:2181/kafka/other";
    final String zk5 = "10.203.248.16:2181,10.203.248.17:2181,10.203.248.18:2181,10.203.248.19:2181,10.203.248.20:2181/kafka/airacpcore";
    //dev sfst
    final String servers = "10.202.34.28:9093,10.202.34.29:9093,10.202.34.30:9093";
    //test sfst
    final String servers2 = "10.202.24.5:9094,10.202.24.6:9094,10.202.24.7:9094,10.202.24.8:9094,10.202.24.9:9094";
    // test other2
    final String servers4 = "10.202.24.5:9095,10.202.24.6:9095,10.202.24.7:9095,10.202.24.8:9095,10.202.24.9:9095";
    //test bus
    final String servers3 = "10.202.24.5:9096,10.202.24.6:9096,10.202.24.7:9096,10.202.24.8:9096,10.202.24.9:9096";

    final String servers5 = "100.80.129.164:9092,100.80.129.165:9092,100.80.129.166:9092,100.80.129.166:9092,100.80.129.168:9092";


    //    final String group = "GROUND_DEV_01370603";
    final String group = "GRD_DEV_S01";
    Object o;

    @Test
    public void testLoad() {
//        TopicManager.listAllTopic("10.202.34.28:2182/kafka1.1.0/default");
//        TopicManager.listAllTopic("10.202.34.30:2182/kafka/st");
//        TopicManager.listTopicAllConfig("10.202.34.30:2182/kafka/st");
//        TopicManager.listTopicAllConfig("10.202.24.5:2181,10.202.24.6:2181,10.202.24.7:2181/kafka/bus");
//        TopicManager.listTopicAllConfig(zk2);
//        KafkaManager.getAllBrokersInCluster(zk4);
//        TopicManager.getInfo(zk2,"SHIVA_TRTMS_GROUND_TEMP_REQUIRE");
        TopicManager.getTopics(zk2);
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
        KafkaTool kafkaTool = new KafkaTool(zk_dev_1);
        String topic = "GRD_DEV_01370603_01";
//        topic = "SHIVA_TRTMS_GROUND_TEMP_REQUIRE";
//        topic = "SHIVA_OMCS_RUSSIAN_PLANNING_REQUIRE_INFO";
//        topic = "SHIVA_OMCS_RUSSIAN_TEMP_REQUIRE_INFO";
//        topic = "KSCS_SEND_REQUIRE";
//        Map<Integer, Long> partOffset = kafkaTool.getTopicOffset(topic, -1);
//        System.out.println(partOffset);
//        kafkaTool.getTopicContextOffset(topic, OffsetRequest.EarliestTime());
//        kafkaTool.getTopicContent(topic);
//        o = kafkaTool.searchTopicStringContent(topic, "100", KafkaTool.SearchType.LATEST);
        o = kafkaTool.searchTopicStringContent(topic, "10", KafkaTool.SearchType.LATEST, null);
//        o = kafkaTool.searchTopicStringContent(topic, "111", KafkaTool.SearchType.LATEST, null);
//        o = kafkaTool.searchTopicStringContent(topic,"666666752360");
//        try {
//            Class<?> clazz = JarUtils.loadClassForJar("file:C:\\Users\\01370603\\.gradle\\caches\\modules-2\\files-2.1\\com.sf.omcs\\omcs-output\\1.2.SP1-SNAPSHOT\\3aba13c4a55c6ed7b6ee079f560c1df50034aecf\\omcs-output-1.2.SP1-SNAPSHOT.jar", "com.sf.omcs.output.dto.russian.plan.PlanLineRequireInfoDto");
//            o = kafkaTool.searchTopicProtostuffContent(topic, "", clazz);
//        } catch (MalformedURLException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        kafkaTool.searchTopicProtostuffContent()
        System.out.println(o);
    }

    @Test
    public void getTopicInfo() {
        TopicManager.getInfo(zk2, "SHIVA_TRTMS_GROUND_TEMP_REQUIRE");
    }


    @Test
    public void testOffsets() {
        KafkaTool kafkaTool = new KafkaTool(zk2);
        String topic = "SHIVA_TRTMS_GROUND_TEMP_REQUIRE";

        List<PartitionInfo> partitions = kafkaTool.getTopicPartitions(topic);
        System.out.println(JSONUtils.toJson(partitions));
    }

    @Test
    public void testContentTool() {
        KafkaContentTool kafkaTool = new KafkaContentTool(zk2,"SHIVA_TRTMS_GROUND_TEMP_REQUIRE");

        kafkaTool.searchTopicContent2(ContentType.JSON, SearchType.LATEST,100,"19101510245766",null);
    }

    @Test
    public void testSend() throws InterruptedException {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers5);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("schema.registry.url", schemaUrl);//schema.registry.url指向射麻的存储位置
        String topic = "KSCS_SEND_REQUIRE";
        Producer<String, byte[]> producer = new KafkaProducer<>(props);
        //不断生成消息并发送
        String msg = "{\"applyDay\":\"5\",\"arriveTm\":1570516734048,\"bizType\":92,\"capacityLoad\":1.5,\"conveyanceType\":5,\"createTm\":1570516734048,\"creator\":\"O2O-DDS-KSCS\",\"crossDays\":0,\"cvyName\":\"755HAJ755HA01(二程接驳)\",\"departTm\":1570516734048,\"destCoordinate\":\"125.123456,35.2364565\",\"destNotZoneAddr\":\"深圳上号\",\"destZoneCode\":\"J755HA01\",\"effectDate\":1570516734048,\"invalidDate\":1570516734048,\"isStopOver\":0,\"lastestArriveTm\":1570516734048,\"lastestReachTm\":1570516734048,\"lineCode\":\"sch755HA01D\",\"lineDistance\":350.6,\"lineId\":72019100810000057,\"linePassZoneInfoGroundDtos\":[{\"arriveBatchDate\":1570516734048,\"arriveTm\":1570516734048,\"crossDays\":0,\"departTm\":1570516734048,\"jobType\":1,\"lastestArriveTm\":1570516734048,\"lineDistance\":0.0,\"linePassZoneId\":234758083695435782,\"lineRequireId\":72019100810000057,\"mainLineRequireId\":72019100810000057,\"passCoordinate\":\"125.123456,35.2364565\",\"passNotZoneAddr\":\"深圳上号\",\"passZoneCode\":\"755HA\",\"sortNum\":1,\"version\":1570706701618,\"waitTm\":5},{\"arriveBatchDate\":1570516734048,\"arriveTm\":1570516734048,\"crossDays\":0,\"departTm\":1570516734048,\"jobType\":2,\"lastestArriveTm\":1570516734048,\"lineDistance\":350.6,\"linePassZoneId\":234758083695435783,\"lineRequireId\":72019100810000057,\"mainLineRequireId\":72019100810000057,\"passCoordinate\":\"125.123456,35.2364565\",\"passNotZoneAddr\":\"深圳上号\",\"passZoneCode\":\"J755HA01\",\"sortNum\":2,\"version\":1570706701618,\"waitTm\":5}],\"lineRequireDate\":1570516734048,\"lineRequireId\":72019100810000057,\"mainLineRequireId\":72019100810000057,\"params\":{\"mobilePhone\":\"13500001111\",\"idCard\":\"453000000003211\",\"price\":\"232.1\"},\"planArriveTm\":1570516734048,\"requireType\":21,\"sendBatchDate\":1570516734048,\"sendWorkDay\":\"2\",\"srcCoordinate\":\"125.123456,35.2364565\",\"srcNotZoneAddr\":\"深圳上号\",\"srcZoneCode\":\"755HA\",\"transoportLevel\":4,\"type\":2,\"vehicleType\":\"厢式运输车\",\"version\":1570706701618}";

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, null, msg.getBytes());
        producer.send(record);//将customer作为消息的值发送出去，KafkaAvroSerializer会处理剩下的事情

        Thread.sleep(5000);
    }
}

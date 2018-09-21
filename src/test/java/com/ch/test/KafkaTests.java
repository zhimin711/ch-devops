package com.ch.test;

import com.ch.cloud.kafka.admin.TopicsManager;
import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.io.Charsets;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import scala.Tuple2;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * @author 01370603
 * @date 2018/9/19 17:27
 */
public class KafkaTests {

    final String zk = "10.202.34.30:2182/kafka/st";
    final String servers = "10.202.34.28:9093,10.202.34.29:9093,10.202.34.30:9093";
    final String group = "GROUND_DEV_01370603";

    @Test
    public void testLoad() {
//        TopicsManager.listAllTopic("10.202.34.28:2182/kafka1.1.0/default");
//        TopicsManager.listAllTopic("10.202.34.30:2182/kafka/st");
        TopicsManager.listTopicAllConfig("10.202.34.30:2182/kafka/st");

    }

    @Test
    public void testConsumer() {
        Properties prop = new Properties();
//        prop.put("zookeeper.connect", "10.202.34.30:2182/kafka/st");
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);//
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, group);
//        prop.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 100);
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
//如果是之前存在的group.id
//        ConsumerConfig.
        Consumer<String, String> consumer = new KafkaConsumer<>(prop);
        TopicPartition p = new TopicPartition("GROUND_DEV_LOG_02", 1);
//        指定消费topic的那个分区
//        consumer.assign(Collections.singletonList(p));

//        指定从topic的分区的某个offset开始消费
//        consumer.seekToBeginning(Arrays.asList(p));
//        consumer.seek(p, 0);
//        consumer.subscribe(Arrays.asList("test2"));

//如果是之前不存在的group.id
//        Map<TopicPartition, OffsetAndMetadata> hashMaps = new HashMap<TopicPartition, OffsetAndMetadata>();
//        hashMaps.put(new TopicPartition("test2", 0), new OffsetAndMetadata(0));
//        consumer.commitSync(hashMaps);
//        consumer.subscribe(Arrays.asList("test2"));
//        while (true) {
//            Duration timeout = Duration.ofMillis(100);
//            ConsumerRecords<String, String> c = consumer.poll(timeout);
//            for (ConsumerRecord<String, String> c1 : c) {
//                System.out.println("Key: " + c1.key() + " Value: " + c1.value() + " Offset: " + c1.offset() + " Partitions: " + c1.partition());
//
//            }
//        }
    }


    @Test
    public void testAdmin() throws ExecutionException, InterruptedException {
        ZkClient zkClient = new ZkClient(zk);
        ZkSerializer serializer = new ZkSerializer() {
            @Override
            public byte[] serialize(Object o) throws ZkMarshallingError {
                return new byte[0];
            }

            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return new String(bytes, Charsets.UTF_8);
            }
        };
        zkClient.setZkSerializer(serializer);
        Tuple2<String, Stat> t = ZkUtils.readData(zkClient, "/");

        Properties config = AdminUtils.fetchTopicConfig(zkClient, "GROUND_DEV_LOG_02");

//        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, servers);
//        AdminClient adminClient = AdminClient.create(props);
//        ListConsumerGroupOffsetsResult offsetsResult = adminClient.listConsumerGroupOffsets(group);
//        KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> kafkaFuture = offsetsResult.partitionsToOffsetAndMetadata();
//        Map<TopicPartition, OffsetAndMetadata> metaMap = kafkaFuture.get();
//        metaMap.forEach((k, v) -> {
//            System.out.println(k + ":" + v.offset());
//        });

    }
}

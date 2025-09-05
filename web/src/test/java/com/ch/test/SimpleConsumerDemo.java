package com.ch.test;


import com.ch.cloud.kafka.consumer.DemoConsumer;
import com.google.common.collect.Lists;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*官方文档的翻译：
 *一般情况下使用high level consumer即可，将数据依次读出来
 *使用simpleconsumer的情景：
 *		多次消费同一条数据；
 *		在一个处理过程中只消费某个topic部分分区的数据；
 *		自己写代码保证每一条消息仅被消费一次
 *使用该接口需要的额外工作：
 *		自己追踪消费的offset；
 *		找出一个topic每一个分区的leader broker
 *		自己处理leader broker的变迁，转移
 *使用步骤：
 *		找出你要消费的topic分区的leader broker
 *		找出topic和partition的replica broker
 *		构建request，封装定义感兴趣的数据，即指定topic和partition的消息
 *		获取数据
 *		应对broker的变化
 *
 *最简单的方法是：通过配置文件或者命令行传参，传递broker的信息。
 *当然不必要是所有的broker列表，一部分live的即可，通过他们找到leader
 **/
public class SimpleConsumerDemo {
    // 成员变量
    private List<String> m_replicaBrokers;

    // 构造函数
    public SimpleConsumerDemo() {
        m_replicaBrokers = new ArrayList<String>();
    }

    /*参数：
     **/
    public static void main(String args[]) {
        SimpleConsumerDemo example = new SimpleConsumerDemo();
        long maxReads = Long.parseLong(args[0]);
        String topic = args[1];
        int partition = Integer.parseInt(args[2]);
        List<String> seeds = new ArrayList<String>();
        seeds.add(args[3]);
        int port = Integer.parseInt(args[4]);
        try {
            example.run(maxReads, topic, partition, seeds, port);
        } catch (Exception e) {
            System.out.println("Oops:" + e);
            e.printStackTrace();
        }
    }

    @Test
    public void get() {
        SimpleConsumerDemo example = new SimpleConsumerDemo();
        long maxReads = 4;
        String topic = "";

        int partition = 0;
        List<String> seeds = Lists.newArrayList();
        int port = 9092;
        try {
            example.run(maxReads, topic, partition, seeds, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void get2() {
        String zkServer = "192.168.20.211:2181";
        DemoConsumer consumer = new DemoConsumer(zkServer,"KafkaZM","myuu_pft_evolute_group_product");

        consumer.nextTuple();

        try {
            Thread.sleep(10000);
            consumer.destroy();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run(long a_maxReads, String a_topic, int a_partition, List<String> a_seedBrokers, int a_port)
            throws Exception {
        //find the meta data about the topic and partition we are interested in
        //找到指定topic、指定分区的元信息，从中消费
        PartitionMetadata metadata = findLeader(a_seedBrokers, a_port, a_topic, a_partition);
        if (metadata == null) {
            System.out.println("Can't find metadata for Topic and Partition. Exiting");
            return;
        }
        if (metadata.leader() == null) {
            System.out.println("Can't find Leader for Topic and Partition. Exiting");
            return;
        }
        //此topic的该分区的leader broker
        String leadBroker = metadata.leader().host();
        String clientName = "Client_" + a_topic + "_" + a_partition;

        //真的是所有的broker默认使用了同一个port吗???
        SimpleConsumer consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
        //从最开始消费数据
        long readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(),
                clientName);
        System.out.println(readOffset);
        int numErrors = 0;
        while (a_maxReads > 0) {
            if (consumer == null) {
                consumer = new SimpleConsumer(leadBroker, a_port, 100000, 64 * 1024, clientName);
            }
            //Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
            FetchRequest req = new FetchRequestBuilder().clientId(clientName)
                    .addFetch(a_topic, a_partition, readOffset, 100000)
                    .build();
            FetchResponse fetchResponse = consumer.fetch(req);

            /*simpleconsumer自己没有处理leader broker坏掉的情况，所以需要自己处理
             **/
            if (fetchResponse.hasError()) {
                numErrors++;
                // Something went wrong!
                //得到错误代码
                short code = fetchResponse.errorCode(a_topic, a_partition);
                System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                //累计够5次错误，结束while循环，不读消息了
                if (numErrors > 5)
                    break;
                //如果错误是offset超出有效范围.即offset无效，最简单的解决方案：获取最新的offset值，读最新的消息
                if (code == ErrorMapping.OffsetOutOfRangeCode()) {
                    // We asked for an invalid offset. For simple case ask for
                    // the last element to reset
                    readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(),
                            clientName);
                    continue;
                }
                consumer.close();
                consumer = null;
                //其他错误，则直接关闭consumer，找一个新的leader，继续消费
                leadBroker = findNewLeader(leadBroker, a_topic, a_partition, a_port);
                continue;
            }//if error
            //没有错误，直接消费数据
            numErrors = 0;

            long numRead = 0;
            //读取消息
            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
                long currentOffset = messageAndOffset.offset();
                //消费到的offset不比刚开始申请的小，因为是从readOffset开始消费的，每消费一条，currentOffset++
                if (currentOffset < readOffset) {
                    System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
                    continue;
                }
                readOffset = messageAndOffset.nextOffset();
                //消息的负载
                ByteBuffer payload = messageAndOffset.message().payload();

                byte[] bytes = new byte[payload.limit()];
                payload.get(bytes);
                System.out.println(String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, StandardCharsets.UTF_8));
                numRead++;
                a_maxReads--;
            }

            if (numRead == 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }//while
        if (consumer != null)
            consumer.close();
    }

    /*消费者消费一个topic的指定partition时，从哪里开始读数据
     *kafka.api.OffsetRequest.EarliestTime()找到日志中数据的最开始头位置，从那里开始消费（hadoop-consumer中使用的应该就是这种方式）
     *kafka.api.OffsetRequest.LatestTime()只消费最新的数据
     *注意，不要假设0是offset的初始值
     *参数：long whichTime的取值即两种：
     *							kafka.api.OffsetRequest.LatestTime()
     *							kafka.api.OffsetRequest.LatestTime()
     *返回值：一个long类型的offset*/
    public static long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime,
                                     String clientName) {
        //topic和对应的partition封装成了一个对象，一个topic有好多个这样的对象
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        /*同样有request、response
         *获取这个topic的指定分区的offset信息*/
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo,
                kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            System.out.println(
                    "Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
            return 0;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }

    /*每一个topic的不同分区有不同的leader
     **/
    private String findNewLeader(String a_oldLeader, String a_topic, int a_partition, int a_port) throws Exception {
        for (int i = 0; i < 3; i++) {
            boolean goToSleep = false;
            PartitionMetadata metadata = findLeader(m_replicaBrokers, a_port, a_topic, a_partition);
            if (metadata == null) {
                goToSleep = true;
            } else if (metadata.leader() == null) {
                goToSleep = true;
            } else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
                /*第一次找出的新leader和旧leader一样，那么给zookeeper一定的恢复时间
                 *first time through if the leader hasn't changed give
                 *ZooKeeper a second to recover
                 *第二次，假定该旧的broker确实已经恢复正常了，同时又被选为leader，或者根本就不是leader的问题，
                 *无论哪种情况，都不设置goToSleep
                 *second time, assume the broker did recover before failover,
                 *or it was a non-Broker issue*/
                goToSleep = true;
            } else {
                return metadata.leader().host();//返回新leader的信息
            }
            if (goToSleep) {//goToSleep为true，那么休息一会，等待一下
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }//for 尝试三次吗？？
        //三次之后，自动放弃
        System.out.println("Unable to find new leader after Broker failure. Exiting");
        throw new Exception("Unable to find new leader after Broker failure. Exiting");
    }

    /*寻找leader，通过命令行或者配置文件传入一个broker的列表，
     *不需要是所有的broker，只需要从其中可以获得一个live broker以查询leader的信息即可
     *参数： a_seedBrokers：已知的broker的列表
     *但是只有一个端口号，有好多broker？？？
     *返回值：指定分区的元数据，应该包含该分区的leader以及消息数目等
     */
    private PartitionMetadata findLeader(List<String> a_seedBrokers, int a_port, String a_topic, int a_partition) {
        PartitionMetadata returnMetaData = null;
        loop: for (String seed : a_seedBrokers) {
            SimpleConsumer consumer = null;
            try {
                /*simpleConsumer的构造函数：参数broker的IP、port、soTimeout、bufferSize、clientId
                 *hadoop-consumer中是从配置文件中读取这两个参数的soTimeout、bufferSize*/
                consumer = new SimpleConsumer(seed, a_port, 100000, 64 * 1024, "leaderLookup");
                List<String> topics = Collections.singletonList(a_topic);
                //获取topic的元数据
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    /*从topic的元数据中获取partition的元数据
                     *topic的元数据中应该可以获知次topic有多少个分区
                     *int partition_num = item.partitionsMetadata().size();*/
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null)
                    consumer.close();
            }
        }
        if (returnMetaData != null) {
            m_replicaBrokers.clear();
//            for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
//                m_replicaBrokers.add(replica.host());
//            }
        }
        return returnMetaData;
    }
}
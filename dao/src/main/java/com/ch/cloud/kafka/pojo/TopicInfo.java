package com.ch.cloud.kafka.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author zhimin.ma
 * @since 2018/9/19 17:00
 */
@Data
public class TopicInfo {

    private Long clusterId;

    private String zookeeper;
    private String name;
    private int partitionSize = 0;
    private int replicaSize = 0;

    private List<Partition> partitions;

    private Long totalLogSize;

    @Data
    public static class Partition {
        private int partition;
        private long beginningOffset;
        private long endOffset;

        public Partition(int partition, long beginningOffset, long endOffset) {
            this.partition = partition;
            this.beginningOffset = beginningOffset;
            this.endOffset = endOffset;
        }
    }
}

package com.ch.cloud.kafka.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhimin
 * @since 2022/3/2 20:41 下午
 */
@Data
public class BrokerDTO {
    private int           id;
    private String        host;
    private int           port;
    private List<Integer> leaderPartitions   = new ArrayList<>();
    private List<Integer> followerPartitions = new ArrayList<>();
}

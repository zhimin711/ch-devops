package com.ch.cloud.redis.utils;

import com.ch.cloud.redis.entity.RedisNode;
import com.ch.utils.NetUtils;

import java.util.List;
import java.util.Objects;


/**
 * @author Jay.H.Zou
 * @date 4/5/2020
 */
public class RedisNodeUtil {

    private RedisNodeUtil() {
    }

    public static boolean equals(RedisNode redisNode1, RedisNode redisNode2) {
        return Objects.equals(redisNode1.getHost(), redisNode2.getHost())
                && redisNode1.getPort() == redisNode2.getPort();
    }

    public static void setRedisRunStatus(String redisModel, List<RedisNode> redisNodeList) {
        redisNodeList.forEach(redisNode -> setRedisRunStatus(redisModel, redisNode));
    }

    public static void setRedisRunStatus(String redisModel, RedisNode redisNode) {
        boolean telnet = NetUtils.telnet(redisNode.getHost(), redisNode.getPort());
        redisNode.setRunStatus(telnet);
        if (!Objects.equals(redisModel, RedisUtil.REDIS_MODE_CLUSTER)) {
            redisNode.setLinkState(telnet ? RedisNode.CONNECTED : RedisNode.UNCONNECTED);
        }
    }
}

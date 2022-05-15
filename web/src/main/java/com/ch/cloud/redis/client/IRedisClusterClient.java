package com.ch.cloud.redis.client;

import redis.clients.jedis.JedisCluster;

/**
 * @author Jay.H.Zou
 * @since 2019/7/18
 */
public interface IRedisClusterClient extends IDatabaseCommand {

    JedisCluster getRedisClusterClient();

    void close();
}

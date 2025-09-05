package com.ch.cloud.redis.client;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * decs:
 *
 * @author 01370603
 * @since 2021/1/13
 */
public class JedisSentinelPool {

    private static ThreadLocal<redis.clients.jedis.JedisSentinelPool> poolThreadLocal = new ThreadLocal<>();

    //可用连接实例的最大数目，默认为8；
    //如果赋值为-1，则表示不限制，如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)
    private static Integer MAX_TOTAL = 1024;
    //控制一个pool最多有多少个状态为idle(空闲)的jedis实例，默认值是8
    private static Integer MAX_IDLE = 200;
    //等待可用连接的最大时间，单位是毫秒，默认值为-1，表示永不超时。
    //如果超过等待时间，则直接抛出JedisConnectionException
    private static Integer MAX_WAIT_MILLIS = 10000;
    //客户端超时时间配置
    private static Integer TIMEOUT = 10000;
    //在borrow(用)一个jedis实例时，是否提前进行validate(验证)操作；
    //如果为true，则得到的jedis实例均是可用的
    private static Boolean TEST_ON_BORROW = true;
    //在空闲时检查有效性, 默认false
    private static Boolean TEST_WHILE_IDLE = true;
    //是否进行有效性检查
    private static Boolean TEST_ON_RETURN = true;

    /**
     * 创建连接池
     */
    public static void createJedisPool(RedisURI uri) {
        JedisPoolConfig config = new JedisPoolConfig();
        /*注意：
            在高版本的jedis jar包，比如本版本2.9.0，JedisPoolConfig没有setMaxActive和setMaxWait属性了
            这是因为高版本中官方废弃了此方法，用以下两个属性替换。
            maxActive  ==>  maxTotal
            maxWait==>  maxWaitMillis
         */
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT_MILLIS);
        config.setTestOnBorrow(TEST_ON_BORROW);
        config.setTestWhileIdle(TEST_WHILE_IDLE);
        config.setTestOnReturn(TEST_ON_RETURN);

        Set<String> sentinels = uri.getHostAndPortSet().stream().map(HostAndPort::toString).collect(Collectors.toSet());
        redis.clients.jedis.JedisSentinelPool pool = new redis.clients.jedis.JedisSentinelPool(uri.getSentinelMasterId(), sentinels, config, TIMEOUT, uri.getRequirePass());
        poolThreadLocal.set(pool);
    }

    /**
     * 获取一个redis对象
     *
     * @return
     */
    public static Jedis getJedis() {
        return poolThreadLocal.get().getResource();
    }
}

package com.ch.cloud.kafka.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/12/24
 */
@Component
public class OrderIdGenerator {

    public static final String REDIS_ORDER_NUMBER = "order:generate:number";
    public static final String REDIS_ORDER_NUMBER_LOCK = "order:generate:lock";

    public static final Integer REDIS_ORDER_NUMBER_TIMEOUT = 24;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public String generate() {
//        redisTemplate.expire(REDIS_ORDER_NUMBER, REDIS_ORDER_NUMBER_TIMEOUT, TimeUnit.HOURS);

        Long id = redisTemplate.opsForValue().increment(REDIS_ORDER_NUMBER);
        return "" + id;
    }

}

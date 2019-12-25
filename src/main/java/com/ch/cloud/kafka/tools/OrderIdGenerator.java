package com.ch.cloud.kafka.tools;

import com.ch.utils.DateUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

    private static final String REDIS_ORDER_NUMBER = "order:generate:number:";
    private static final String REDIS_ORDER_NUMBER_LOCK = "order:generate:lock";

    public static final Integer REDIS_ORDER_NUMBER_TIMEOUT = 24;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    /**
     * @param prefix 前缀
     * @param type   类型
     * @return
     */
    public String generate(String prefix, String type) {
//        redisTemplate.expire(REDIS_ORDER_NUMBER, REDIS_ORDER_NUMBER_TIMEOUT, TimeUnit.HOURS);
        String dateStr = DateUtils.format(DateUtils.currentTime(), DateUtils.Pattern.DATE_SHORT);

        long id = redissonClient.getAtomicLong(REDIS_ORDER_NUMBER + dateStr).incrementAndGet();

//        Long id = redisTemplate.opsForValue().increment(REDIS_ORDER_NUMBER + dateStr);
//        if (id == null) {
//            id = 1L;
//        }
        return String.format(prefix + dateStr + type + "%08d", id);
    }

    @Autowired
    private RedissonClient redissonClient;

    public String generate() {
        RLock lock = redissonClient.getLock(REDIS_ORDER_NUMBER_LOCK);
        lock.lock(15, TimeUnit.SECONDS);
        try {
            return generate("O", "B");
        } finally {
            lock.unlock();
        }
    }
}

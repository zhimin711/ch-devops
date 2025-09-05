package com.ch.cloud.kafka.pool;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * decs:
 *
 * @author 01370603
 * @since 2020/10/13
 */
@Component
public class MockPool {

    private Set<ThreadPoolExecutor> executors = Sets.newConcurrentHashSet();

    public boolean free() {
        int c = 0;
        for (ThreadPoolExecutor executor : executors) {
            c += executor.getActiveCount();
        }
        return c < (Runtime.getRuntime().availableProcessors() / 2);
    }
}

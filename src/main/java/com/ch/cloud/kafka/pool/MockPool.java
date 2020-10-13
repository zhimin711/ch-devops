package com.ch.cloud.kafka.pool;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/10/13
 */
@Component
public class MockPool {

    private List<ExecutorService> executors = Collections.emptyList();
}

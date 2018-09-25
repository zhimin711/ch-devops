package com.ch.cloud.kafka.api;

import com.ch.result.BaseResult;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */
public interface SearchService {
    BaseResult<String> searchStringContent(String servers, String topic, String pattern);
}

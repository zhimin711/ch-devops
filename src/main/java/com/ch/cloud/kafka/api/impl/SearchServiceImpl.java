package com.ch.cloud.kafka.api.impl;

import com.ch.cloud.kafka.api.SearchService;
import com.ch.cloud.kafka.tools.KafkaTool;
import com.ch.result.BaseResult;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Override
    public BaseResult<String> searchStringContent(String servers, String topic, String pattern) {
        KafkaTool kafkaTool = new KafkaTool(servers);
        List<String> list = kafkaTool.searchTopicStringContent(topic, pattern);

        return new BaseResult<>(list);
    }
}

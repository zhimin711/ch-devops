package com.ch.cloud.kafka.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.ch.cloud.kafka.service.ITestService;
import com.ch.utils.DateUtils;

//import com.ch.cloud.kafka.mapper.TestMapper;

@Service
public class TestServiceImpl implements ITestService {

//    @Autowired
//    TestMapper testMapper;

    @Override
    public int save(String name) {
        String tmp = name + "-" + DateUtils.currentTime().getTime();
//        test2Service.save(tmp);
//        int c = testMapper.save(tmp);

//        throw new RuntimeException("sys test error!");
        return 0;
    }

}

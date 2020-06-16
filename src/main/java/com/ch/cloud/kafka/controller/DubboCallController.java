package com.ch.cloud.kafka.controller;

import com.ch.Constants;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtContentRecord;
import com.ch.cloud.kafka.model.BtContentSearch;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.pojo.*;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.IContentRecordService;
import com.ch.cloud.kafka.service.IContentSearchService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.kafka.tools.KafkaContentTool;
import com.ch.cloud.kafka.utils.DubboCallUtils;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.e.PubError;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.ExceptionUtils;
import com.ch.utils.JSONUtils;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;

/**
 * @author 01370603
 * @date 2018/9/25 10:02
 */

@Api(tags = "Dubbo 服务接口")
@RestController
@RequestMapping("dubbo")
@Slf4j
public class DubboCallController {

    @Value("${share.path.libs}")
    private String libsDir;

    @ApiOperation(value = "Dubbo 调用")
    @PostMapping("call")
    public Object search(@RequestBody DubboCall record,
                            @RequestHeader(Constants.TOKEN_USER) String username) {
        return DubboCallUtils.invoke(record.getInterfaceName(), record.getMethod(), record.getParam(), record.getAddress(), record.getVersion());
    }

}

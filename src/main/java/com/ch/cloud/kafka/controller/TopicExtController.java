package com.ch.cloud.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.ch.Constants;
import com.ch.StatusS;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.kafka.pojo.TopicDto;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.ITopicExtService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.kafka.tools.KafkaContentTool;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.cloud.kafka.utils.MockUtil;
import com.ch.e.PubError;
import com.ch.pool.DefaultThreadPool;
import com.ch.result.InvokerPage;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.*;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author zhimin.ma
 * @date 2018/9/25 20:29
 */
@Api(tags = "KAFKA主题扩展信息配置模块")
@RestController
@RequestMapping("topic/ext")
@Slf4j
public class TopicExtController {

    @Autowired
    private ITopicExtService topicExtService;
    @Autowired
    private ClusterConfigService clusterConfigService;
    @Autowired
    private ITopicService topicService;

    @Value("${share.path.libs}")
    private String libsDir;

    @ApiOperation(value = "加载主题扩展信息", notes = "加载主题扩展信息")
    @GetMapping
    public Result<BtTopicExt> load(BtTopicExt record,
                                   @RequestHeader(Constants.TOKEN_USER) String username) {
        return ResultUtils.wrap(() -> {
            BtTopicExt record2 = topicExtService.findByClusterAndTopicAndCreateBy(record.getClusterName(), record.getTopicName(), username);
            if (record2 == null) {
                record2 = new BtTopicExt();
                record2.setClusterName(record.getClusterName());
                record2.setTopicName(record.getTopicName());
                record2.setThreadSize(4);
                record2.setBatchSize(10);

            }
            loadTopicProps(record2);
            return record2;
        });
    }

    private void loadTopicProps(BtTopicExt record) {

        List<BtTopicExtProp> props = topicExtService.findProps(record.getId());

        if (CommonUtils.isEmpty(props)) {
            BtTopic topicDto = topicService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());
            if (CommonUtils.isEmpty(topicDto.getClassName())) {
                return;
            }
            Class<?> clazz = KafkaSerializeUtils.loadClazz(libsDir + File.separator + topicDto.getClassFile(), topicDto.getClassName());
            if (clazz == null) {
                return;
            }
//            Object obj = clazz.newInstance();
            Map<String, Object> map = BeanExtUtils.getPropertyAndType(clazz);
            props = convert(map);
        }

        record.setProps(props);
    }

    private List<BtTopicExtProp> convert(Map<?, ?> map) {
        List<BtTopicExtProp> props = Lists.newArrayList();
        map.forEach((k, v) -> {
            BtTopicExtProp prop = new BtTopicExtProp();
            prop.setUid(UUIDGenerator.generate());
            prop.setCode(k.toString());
            if (v != null) {
                if (v instanceof String) {
                    prop.setType((String) v);
                } else if (v instanceof Map) {
                    prop.setType("{}");
                    prop.setChildren(convert((Map) v));
                }
            }
            props.add(prop);
        });
        return props;
    }

    @ApiOperation(value = "新增主题扩展信息", notes = "新增主题扩展信息")
    @PostMapping
    public Result<Integer> save(@RequestBody BtTopicExt record,
                                @RequestHeader(Constants.TOKEN_USER) String username) {

        return ResultUtils.wrapFail(() -> {
            if (CommonUtils.isEmptyOr(record.getClusterName(), record.getTopicName())) {
                ExceptionUtils._throw(PubError.NON_NULL, "集群或主题不能为空！");
            }
            BtTopic topicDto = topicService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());
            if (topicDto == null) {
                ExceptionUtils._throw(PubError.NOT_EXISTS, "集群+主题不存在！");
            }

            BtTopicExt r = topicExtService.findByClusterAndTopicAndCreateBy(record.getClusterName(), record.getTopicName(), username);

            if (r != null) {
                record.setId(r.getId());
                record.setUpdateBy(username);
                record.setUpdateAt(DateUtils.current());
            } else {
                record.setCreateBy(username);
                record.setStatus(StatusS.ENABLED);
            }
            return topicExtService.save(record);
        });
    }

    @ApiOperation(value = "生成主题数据", notes = "生成主题数据")
    @PostMapping("mock")
    public Result<?> mock(@RequestBody BtTopicExt record,
                          @RequestHeader(Constants.TOKEN_USER) String username) {

        return ResultUtils.wrapPage(() -> {
            if (CommonUtils.isEmpty(record.getProps())) {
                ExceptionUtils._throw(PubError.NON_NULL, "属性不存在！");
            }
            boolean checkOK = MockUtil.checkProps(record.getProps());
            long total = 0;
            List<Object> objects = Lists.newArrayList();
            if (checkOK) {
                int ts = 1;
                int bs = CommonUtils.isNotEmpty(record.getBatchSize()) ? record.getBatchSize() : 10;
                if (CommonUtils.isNotEmpty(record.getThreadSize())) {
                    ts = record.getThreadSize();
                }
                TopicDto topicDto = topicService.check(record.getClusterName(), record.getTopicName());

                KafkaContentTool contentTool = new KafkaContentTool(topicDto.getZookeeper(), topicDto.getClusterName(), topicDto.getTopicName());

                List<Future<List<Object>>> futures = Lists.newArrayList();

                int ss = 100 / ts;
                for (int i = 0; i < ts; i++) {
                    Future<List<Object>> f = DefaultThreadPool.submit(() -> {
                        List<Object> list = Lists.newArrayList();
                        for (int j = 0; j < bs; j++) {
                            Object o;
                            if (record.getProps().size() == 1) {
                                o = MockUtil.mockDataProp(record.getProps().get(0));
                            } else {
                                o = MockUtil.mockDataProps(record.getProps());
                            }
//                            log.info("mock: {}", o);
//                            if (list.size() < ss)
                            list.add(o);
                            contentTool.send(KafkaSerializeUtils.convertContent(topicDto, JSON.toJSONString(o)));
                        }

//                        log.info("mock size: {}", list.size());
                        return list;
                    });
                    futures.add(f);
                }
                for (Future<List<Object>> f : futures) {
                    try {
                        List<Object> list = f.get();
                        total += list.size();
                        objects.addAll(list.size() > ss ? list.subList(0, ss) : list);
                        log.info("mock list size: {}", list.size());
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Future error!", e);
                    }
                }

                log.info("mock objects size: {}", objects.size());
            }
            return InvokerPage.Page.build(total, objects);
        });
    }

}

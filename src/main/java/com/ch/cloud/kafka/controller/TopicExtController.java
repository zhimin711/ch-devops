package com.ch.cloud.kafka.controller;

import com.ch.Constants;
import com.ch.StatusS;
import com.ch.cloud.kafka.model.BtClusterConfig;
import com.ch.cloud.kafka.model.BtTopic;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.kafka.pojo.TopicConfig;
import com.ch.cloud.kafka.pojo.TopicInfo;
import com.ch.cloud.kafka.service.ClusterConfigService;
import com.ch.cloud.kafka.service.ITopicExtService;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.kafka.tools.TopicManager;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.cloud.mock.Mock;
import com.ch.cloud.mock.MockConfig;
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.toolkit.UUIDGenerator;
import com.ch.utils.*;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public Result<Integer> mock(@RequestBody BtTopicExt record,
                                @RequestHeader(Constants.TOKEN_USER) String username) {

        return ResultUtils.wrapFail(() -> {
            if (CommonUtils.isEmpty(record.getProps())) {
                ExceptionUtils._throw(PubError.NON_NULL, "属性不存在！");
            }
            boolean checkOK = checkProps(record.getProps());
            if (checkOK) {
                Object o = mackData(record.getProps());

                log.info("mock: {}", o);
            }
            return 0;
        });
    }

    private Object mackData(List<BtTopicExtProp> props) throws ClassNotFoundException {
        if (props.size() == 1) {
            BtTopicExtProp prop = props.get(0);
            if (BeanExtUtils.BasicType.isObject(prop.getType())) {
                if (CommonUtils.isEmpty(prop.getValRegex())) {
                    MockConfig config = new MockConfig();
                    config.setStringEnum(MockConfig.StringEnum.CHARACTER);
                    return Mock.mock(Class.forName(prop.getType()), config);
                } else {
                    return prop.getValRegex();
                }
            } else if (CommonUtils.isEquals("java.util.Date", prop.getType())) {
                MockConfig config = new MockConfig();
                if (CommonUtils.isNotEmpty(prop.getValRegex())) {
                    Date date = DateUtils.parse(prop.getValRegex());
                    if (date != null) return prop.getValRegex();
                    String[] dArr = prop.getValRegex().split(Constants.SEPARATOR_5);
                    Date ds;
                    Date de;
                    if (dArr.length > 1) {
                        ds = DateUtils.parse(dArr[0]);
                        de = DateUtils.parse(dArr[1]);
                        if (ds != null && de != null) {
                            config.dateRange(dArr[0], dArr[1]);
                        }
                    }

                }
                return Mock.mock(Class.forName(prop.getType()), config);
            } else if (CommonUtils.isEquals("{}", prop.getType())) {
                if (CommonUtils.isNotEmpty(prop.getValRegex())) {
                    Class<?> clazz = JarUtils.loadClass(prop.getValRegex());
                    if (clazz != null) {
                        if (CommonUtils.isNotEmpty(prop.getChildren())) {
                            //todo mock config
                        }
                        MockConfig config = new MockConfig();
                        config.setStringEnum(MockConfig.StringEnum.CHARACTER);
                        return Mock.mock(clazz, config);
                    }
                }
            }
        }
        return null;
    }

    private boolean checkProps(List<BtTopicExtProp> props) {
        boolean isSingle = false;
        boolean ok = true;
        for (BtTopicExtProp prop : props) {
            if (CommonUtils.isEmpty(prop.getCode())) {
                isSingle = true;
                break;
            }
        }
        if (isSingle && props.size() > 1) {
            return false;
        }
        return ok;
    }

}

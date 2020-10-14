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
import com.ch.e.PubError;
import com.ch.result.PageResult;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.BeanExtUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.ch.utils.ExceptionUtils;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author zhimin.ma
 * @date 2018/9/25 20:29
 */
@Api(tags = "KAFKA主题扩展信息配置模块")
@RestController
@RequestMapping("topic/ext")
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

                loadTopicProps(record2);
            }
            return record2;
        });
    }

    private void loadTopicProps(BtTopicExt record) {
        BtTopic topicDto = topicService.findByClusterAndTopic(record.getClusterName(), record.getTopicName());
        if (CommonUtils.isEmpty(topicDto.getClassName())) {
            return;
        }
        Class<?> clazz = KafkaSerializeUtils.loadClazz(libsDir + File.separator + topicDto.getClassFile(), topicDto.getClassName());
        if (clazz == null) {
            return;
        }
        try {
            Object obj = clazz.newInstance();
            Map<String, Object> map = BeanExtUtils.getDeclaredFieldValueMap(obj, false);
            List<BtTopicExtProp> props = Lists.newArrayList();
            map.forEach((k, v) -> {
                BtTopicExtProp prop = new BtTopicExtProp();
                prop.setCode(k);
                if(v!=null) prop.setType(v.getClass().getName());
                props.add(prop);
            });
            record.setProps(props);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @ApiOperation(value = "新增主题扩展信息", notes = "新增主题扩展信息")
    @PostMapping
    public Result<Integer> add(@RequestBody BtTopicExt record,
                               @RequestHeader(Constants.TOKEN_USER) String username) {
        BtTopicExt r = topicExtService.findByClusterAndTopicAndCreateBy(record.getClusterName(), record.getTopicName(), username);
        if (r != null && !CommonUtils.isEquals(r.getStatus(), StatusS.DELETE)) {
            return Result.error(PubError.EXISTS, "主题已存在！");
        }
        return ResultUtils.wrapFail(() -> {
            BtClusterConfig cluster = clusterConfigService.findByClusterName(record.getClusterName());

            record.setCreateBy(username);
            record.setStatus(StatusS.ENABLED);
            return topicExtService.save(record);
        });
    }

}

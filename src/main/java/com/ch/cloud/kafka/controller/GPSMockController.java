package com.ch.cloud.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ch.Constants;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.kafka.pojo.TopicDto;
import com.ch.cloud.kafka.service.ITopicService;
import com.ch.cloud.kafka.tools.KafkaContentTool;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.cloud.kafka.utils.MapUtils;
import com.ch.cloud.kafka.utils.MockUtil;
import com.ch.cloud.mock.pojo.MockProp;
import com.ch.cloud.mock.util.RandomUtils;
import com.ch.e.PubError;
import com.ch.pool.DefaultThreadPool;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.*;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/12/8
 */
@RestController
@RequestMapping("/gps")
@Slf4j
public class GPSMockController {

    @Autowired
    private ITopicService topicService;

    @ApiOperation(value = "生成GPS数据", notes = "生成GPS数据并发送Kafka")
    @PostMapping("mock")
    public Result<?> mock(@RequestBody BtTopicExt record,
                          @RequestHeader(Constants.TOKEN_USER) String username) {
        return ResultUtils.wrapPage(() -> {
            if (CommonUtils.isEmpty(record.getProps())) {
                ExceptionUtils._throw(PubError.ARGS, "mock字段不能为空！");
            }
            List<MockProp> props = MockUtil.convertGPSRules(record);

            boolean checkOK = MockUtil.checkGPSProps(record);
            long total = 0;
            List<Object> objects = Lists.newArrayList();
            if (checkOK) {

                TopicDto topicDto = topicService.check(record.getClusterName(), record.getTopicName());
                KafkaContentTool contentTool = new KafkaContentTool(topicDto.getZookeeper(), topicDto.getClusterName(), topicDto.getTopicName());
                List<Future<List<Object>>> futures = Lists.newArrayList();
                for (int i = 0; i < record.getThreadSize(); i++) {
                    int threadIndex = i;
                    Future<List<Object>> f = DefaultThreadPool.submit(() -> {
                        List<Object> o = mockDataProps(record, props, threadIndex);
                        contentTool.send(KafkaSerializeUtils.convertContent(topicDto, JSON.toJSONString(o)));
                        return o;
                    });
                    futures.add(f);
                }
                for (Future<List<Object>> f : futures) {
                    try {
                        List<Object> list = f.get();
                        total += list.size();
                        objects.addAll(list);
                        log.info("mock part list size: {}", list.size());
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Future error!", e);
                    }
                }
                List<JSONObject> listMap = objects.stream().map(r -> {
                    if (r instanceof JSONObject) return (JSONObject) r;
                    return new JSONObject();
                }).sorted((e1, e2) -> CommonUtils.compareTo(e1.get("ts"), e2.get("ts"))).collect(Collectors.toList());
                listMap.forEach(e -> System.out.println("[" + e.get("longitude") + "," + e.get("latitude") + "],"));

            }


            return null;
        });
    }

    private List<Object> mockDataProps(BtTopicExt record, List<MockProp> props, int threadIndex) throws Exception {


        List<Object> objs = Lists.newArrayList();

        long total = DateUtils.calcOffsetMinutes(record.getCreateAt(), record.getUpdateAt());//60
        int ss = (int) total / record.getThreadSize();
        int offsetS = threadIndex * ss;
        int offsetE = (threadIndex + 1) * ss;

        Date sd = DateUtils.addMinutes(record.getCreateAt(), offsetS);
        Date ed = DateUtils.addMinutes(record.getCreateAt(), offsetE);

        String sp = record.getPoints().get(0);
        String dp = record.getPoints().get(record.getPoints().size() - 1);

        double[] spa = MapUtils.parsePoint(sp);
        double[] dpa = MapUtils.parsePoint(dp);

        double ps1 = dpa[0] - spa[0];
        double ps2 = dpa[1] - spa[1];

        int count = offsetE - offsetS;

        double psu1 = ps1 / (total);
        double psu2 = ps2 / (total);

        double rs = RandomUtils.nextDouble(psu2, psu2 + 0.001);

        for (int i = 0; i < count; i++) {
            sd = DateUtils.addMinutes(sd, record.getBatchSize() * i);

            double lng = spa[0] + psu1 * (offsetS + i);
            double lat = spa[1] + psu2 * (offsetS + i);
            lat = RandomUtils.nextDouble(lat, lat + 0.004);

            JSONObject o = mockDataProps(record.getProps(), sd, lng, lat);
            for (MockProp p : props) {
                Object o1 = MockUtil.mockProp(p, count * threadIndex + i);
                o.put(p.getCode(), o1);
            }
            objs.add(o);
//            log.info("{}", o.toJSONString());
        }
        return objs;
    }


    private JSONObject mockDataProps(List<BtTopicExtProp> props, Date timestamp, double lng, double lat) throws Exception {
        JSONObject obj = new JSONObject();
        for (int i = 0; i < 4; i++) {
            BtTopicExtProp prop = props.get(i);
            String name = prop.getName().trim();
            if (CommonUtils.isEquals(name, MockUtil.GPS_NAME)) {
                obj.put(prop.getCode(), lng + "," + lat);
            } else if (CommonUtils.isEquals(name, MockUtil.GPS_LNG_NAME)) {
                obj.put(prop.getCode(), lng + "");
            } else if (CommonUtils.isEquals(name, MockUtil.GPS_LAT_NAME)) {
                obj.put(prop.getCode(), lat + "");
            } else if (CommonUtils.isEquals(name, MockUtil.GPS_TIME)) {
                obj.put(prop.getCode(), timestamp.getTime());
            }
        }
        return obj;
    }

}

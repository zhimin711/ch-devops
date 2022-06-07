package com.ch.cloud.kafka.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.kafka.dto.KafkaTopicDTO;
import com.ch.cloud.kafka.model.KafkaTopicExt;
import com.ch.cloud.kafka.model.KafkaTopicExtProp;
import com.ch.cloud.kafka.service.KafkaTopicService;
import com.ch.cloud.kafka.utils.MapUtils;
import com.ch.cloud.kafka.utils.MockUtil;
import com.ch.cloud.mock.pojo.MockProp;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.pool.DefaultThreadPool;
import com.ch.result.InvokerPage;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.CommonUtils;
import com.ch.utils.DateUtils;
import com.google.common.collect.Lists;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * decs:
 *
 * @author 01370603
 * @since 2020/12/8
 */
@RestController
@RequestMapping("/mock")
@Slf4j
public class MockController {

    @Autowired
    private KafkaTopicService kafkaTopicService;

    @ApiOperation(value = "生成数据", notes = "生成主题数据")
    @PostMapping
    public Result<?> mock(@RequestBody KafkaTopicExt record) {

        return ResultUtils.wrapPage(() -> {
            if (CommonUtils.isEmpty(record.getProps())) {
                ExceptionUtils._throw(PubError.ARGS, "mock字段不能为空！");
            }

            List<MockProp> props = MockUtil.convertRules(record, record.getProps(), false);

            boolean checkOK = MockUtil.checkProps(record.getProps());
            long total = 0;
            List<Object> objects = Lists.newArrayList();
            if (checkOK) {
                int ts = CommonUtils.isNotEmpty(record.getThreadSize()) ? record.getThreadSize() : 1;
                int bs = CommonUtils.isNotEmpty(record.getBatchSize()) ? record.getBatchSize() : 1;

                KafkaTopicDTO kafkaTopicDto = kafkaTopicService.check(record.getClusterId(), record.getTopicName());

                List<Future<List<Object>>> futures = Lists.newArrayList();

                int ss = 100 / ts;
                for (int i = 0; i < ts; i++) {
                    int cc = i * record.getThreadSize();
                    Future<List<Object>> f = DefaultThreadPool.submit(() -> {
                        List<Object> list = Lists.newArrayList();
                        for (int j = 0; j < bs; j++) {
                            Object o = null;
                            for (MockProp p : props) {
                                if (CommonUtils.isEmpty(p.getCode())) {
                                    o = MockUtil.mockProp(p, cc + j);
                                } else if (MockUtil.isArray(p.getCode())) {
                                    String code = MockUtil.getArrayPropCode(p.getCode());
                                    int len = MockUtil.getArrayLength(p.getCode());
                                    JSONArray array = new JSONArray(len);
                                    for (int k = 0; k < len; k++) {
                                        array.add(MockUtil.mockProp(p, cc + j));
                                    }
                                    if (CommonUtils.isEmpty(code)) {
                                        o = array;
                                    } else {
                                        JSONObject obj = new JSONObject();
                                        obj.put(code, array);
                                        o = obj;
                                    }
                                } else if (MockUtil.isCollection(p.getCode())) {
                                    String code = MockUtil.getCollectionPropCode(p.getCode());
                                    int len = MockUtil.getCollectionLength(p.getCode());

                                    List<Object> list2 = Lists.newArrayList();
                                    for (int k = 0; k < len; k++) {
                                        list2.add(MockUtil.mockProp(p, cc + j));
                                    }
                                    if (CommonUtils.isEmpty(code)) {
                                        o = list2;
                                    } else {
                                        JSONObject obj = new JSONObject();
                                        obj.put(code, list2);
                                        o = obj;
                                    }
                                } else {
                                    Object o1 = MockUtil.mockProp(p, cc + j);
                                    JSONObject obj = new JSONObject();
                                    obj.put(p.getCode(), o1);
                                    o = obj;
                                }
                            }
                            log.info("mock: {}", o);
//                            if (list.size() < ss)
                            list.add(o);
                            //todo send message queue
//                            contentTool.send(KafkaSerializeUtils.convertContent(kafkaTopicDto, JSON.toJSONString(o)));
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
            return InvokerPage.build(total, objects);
        });
    }


    @ApiOperation(value = "生成GPS数据", notes = "生成GPS数据并发送Kafka")
    @PostMapping("gps")
    public Result<?> gps(@RequestBody KafkaTopicExt record) {
        return ResultUtils.wrapPage(() -> {
            if (CommonUtils.isEmpty(record.getProps())) {
                ExceptionUtils._throw(PubError.ARGS, "mock字段不能为空！");
            }
            List<MockProp> props = MockUtil.convertGPSRules(record);

            boolean checkOK = MockUtil.checkGPSProps(record);
            long total = 0;
            List<Object> objects = Lists.newArrayList();
            if (checkOK) {

                KafkaTopicDTO kafkaTopicDto = kafkaTopicService.check(record.getClusterId(), record.getTopicName());
//                KafkaContentTool contentTool = new KafkaContentTool(kafkaTopicDto.getZookeeper(), kafkaTopicDto.getClusterId(), kafkaTopicDto.getTopicName());
                List<Future<List<Object>>> futures = Lists.newArrayList();
                for (int i = 0; i < record.getThreadSize(); i++) {
                    int threadIndex = i;
                    Future<List<Object>> f = DefaultThreadPool.submit(() -> {
                        List<Object> o = mockGPSDataProps(record, props, threadIndex);
//                        o.forEach(e -> contentTool.send(KafkaSerializeUtils.convertContent(kafkaTopicDto, JSON.toJSONString(e))));
//                        contentTool.send(KafkaSerializeUtils.convertContent(topicDto, JSON.toJSONString(o)));
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
                listMap.forEach(e -> System.out.println("[" + DateUtils.formatOfUTC(DateUtils.parseTimestamp(e.getLong("ts"))) + "]"));

            }
            return InvokerPage.build(total, objects);
        });
    }

    private List<Object> mockGPSDataProps(KafkaTopicExt record, List<MockProp> props, int threadIndex) throws Exception {

        long total = DateUtils.calcOffsetMinutes(record.getCreateAt(), record.getUpdateAt());//60

        double total2 = 0.0;
        List<Double> distances = Lists.newArrayList();
        for (int i = 0; i < record.getPoints().size() - 1; i++) {
            double distance = MapUtils.calcDistance(record.getPoints().get(i), record.getPoints().get(i + 1));
            distances.add(distance);
            total2 += distance;
        }
        double finalTotal = total2;
        List<Double> distanceRates = distances.stream().map(e -> e / finalTotal).collect(Collectors.toList());

        List<Object> objs = Lists.newArrayList();

        if (total < record.getPoints().size()) {
            total = record.getUpdateAt().getTime() - record.getCreateAt().getTime();
            int offset = (int) (total / record.getPoints().size());
            for (int i = 0; i < record.getPoints().size(); i++) {
                Date sd1 = DateUtils.addMilliseconds(record.getCreateAt(), offset * objs.size());

                double[] spa = MapUtils.parsePoint(record.getPoints().get(i));
                JSONObject o = mockGPSDataProps(record.getProps(), sd1, spa[0], spa[1]);
                for (MockProp p : props) {
                    Object o1 = MockUtil.mockProp(p, objs.size());
                    o.put(p.getCode(), o1);
                }
                objs.add(o);
            }
        } else {
            for (int i = 0; i < distanceRates.size(); i++) {
                int offsetE = (int) Math.floor(total * distanceRates.get(i));
                String sp = record.getPoints().get(i);
                String dp = record.getPoints().get(i + 1);

                double[] spa = MapUtils.parsePoint(sp);
                double[] dpa = MapUtils.parsePoint(dp);

                double ps1 = dpa[0] - spa[0];
                double ps2 = dpa[1] - spa[1];

                double psu1 = ps1 / (offsetE);
                double psu2 = ps2 / (offsetE);

                for (int j = 0; j < offsetE; j++) {
                    Date sd1 = DateUtils.addMinutes(record.getCreateAt(), objs.size());

                    double lng = spa[0] + psu1 * j;
                    double lat = spa[1] + psu2 * j;

                    JSONObject o = mockGPSDataProps(record.getProps(), sd1, lng, lat);
                    for (MockProp p : props) {
                        Object o1 = MockUtil.mockProp(p, objs.size());
                        o.put(p.getCode(), o1);
                    }
                    objs.add(o);
//            log.info("{}", o.toJSONString());
                }
            }
        }

        return objs;
    }


    private JSONObject mockGPSDataProps(List<KafkaTopicExtProp> props, Date timestamp, double lng, double lat) throws Exception {
        JSONObject obj = new JSONObject();
        for (int i = 0; i < 4; i++) {
            KafkaTopicExtProp prop = props.get(i);
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

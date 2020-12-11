package com.ch.cloud.kafka.controller;

import com.alibaba.fastjson.JSONObject;
import com.ch.Constants;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.kafka.utils.MapUtils;
import com.ch.cloud.kafka.utils.MockUtil;
import com.ch.cloud.mock.pojo.MockProp;
import com.ch.e.PubError;
import com.ch.pool.DefaultThreadPool;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.*;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

                List<Future<List<Object>>> futures = Lists.newArrayList();
                for (int i = 0; i < record.getThreadSize(); i++) {
                    int threadIndex = i;
                    Future<List<Object>> f = DefaultThreadPool.submit(() -> {
                        List<Object> o = mockDataProps(record, props, threadIndex);
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

        for (int i = 0; i < count; i++) {
            sd = DateUtils.addMinutes(sd, record.getBatchSize() * i);

            double lng = spa[0] + psu1 * (offsetS * i);
            double lat = spa[1] + psu2 * (offsetS * i);

            JSONObject o = mockDataProps(record.getProps(), sd, lng, lat);
            for (MockProp p : props) {
                Object o1 = MockUtil.mockProp(p, count * threadIndex + i);
                o.put(p.getCode(), o1);
            }
            objs.add(o);
            log.info("{}", o.toJSONString());
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

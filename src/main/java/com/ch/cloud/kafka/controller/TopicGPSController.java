package com.ch.cloud.kafka.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ch.Constants;
import com.ch.StatusS;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.kafka.utils.KafkaSerializeUtils;
import com.ch.cloud.kafka.utils.MapUtils;
import com.ch.cloud.kafka.utils.TopicExtUtil;
import com.ch.cloud.mock.Mock;
import com.ch.cloud.mock.MockConfig;
import com.ch.e.PubError;
import com.ch.pool.DefaultThreadPool;
import com.ch.result.Result;
import com.ch.result.ResultUtils;
import com.ch.utils.*;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/12/8
 */
@RestController
@RequestMapping("topic/gps")
@Slf4j
public class TopicGPSController {


    @ApiOperation(value = "生成GPS数据", notes = "生成GPS数据并发送Kafka")
    @PostMapping("mock")
    public Result<?> mock(@RequestBody BtTopicExt record,
                          @RequestHeader(Constants.TOKEN_USER) String username) {
        return ResultUtils.wrapPage(() -> {
            if (CommonUtils.isEmpty(record.getProps())) {
                ExceptionUtils._throw(PubError.NON_NULL, "属性不存在！");
            }
            boolean checkOK = TopicExtUtil.checkGPSProps(record);
            long total = 0;
            List<Object> objects = Lists.newArrayList();
            if (checkOK) {
                int ts = record.getThreadSize();
                if (CommonUtils.isNotEmpty(record.getThreadSize()) && record.getThreadSize() > 0) {
                    ts = record.getThreadSize();
                }
                Date sd = record.getCreateAt();
                Date ed = record.getUpdateAt();
                long minutes = DateUtils.calcOffsetMinutes(sd, ed);//60
                long ss = minutes / record.getThreadSize();//15

                List<Future<List<Object>>> futures = Lists.newArrayList();
                for (int i = 0; i < ts; i++) {
                    long offsetS = i * ss;
                    long offsetE = (i + 1) * ss;
                    Future<List<Object>> f = DefaultThreadPool.submit(() -> {

                        List<Object> o = mockDataProps(record, (int) offsetS, (int) offsetE);
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

    private List<Object> mockDataProps(BtTopicExt record, int offsetS, int offsetE) throws Exception {

        List<Object> objs = Lists.newArrayList();

        Date md = record.getUpdateAt();
        long total = DateUtils.calcOffsetMinutes(record.getCreateAt(), record.getUpdateAt());//60

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

        double totalD = MapUtils.calcDistance(sp, dp);
        double totalT = ed.getTime() - sd.getTime();

        while (sd.before(ed) && sd.before(md)) {
            JSONObject o = mockDataProps(record.getProps(), sd);


//            objs.add(o);
            log.info("{}", o.toJSONString());
            sd = DateUtils.addMinutes(sd, record.getBatchSize());

        }

        return objs;
    }


    private JSONObject mockDataProps(List<BtTopicExtProp> props, Date timestamp) throws Exception {
        JSONObject obj = new JSONObject();
        for (BtTopicExtProp prop : props) {
            if (CommonUtils.isEquals(prop.getName(), TopicExtUtil.GPS_NAME)) {
//                obj.put(prop.getCode(), mockGPS(prop));
            } else if (CommonUtils.isEquals(prop.getName(), TopicExtUtil.GPS_TIME)) {
                obj.put(prop.getCode(), timestamp.getTime());
            } else {
                obj.put(prop.getCode(), TopicExtUtil.mockDataProp(prop));
            }
        }
        return obj;
    }

}

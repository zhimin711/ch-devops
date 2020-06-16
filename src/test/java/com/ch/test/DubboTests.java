package com.ch.test;

import com.alibaba.fastjson.JSON;
import com.ch.cloud.kafka.utils.DubboCallUtils;
import com.ch.utils.DateUtils;
import com.sf.grd.trade.micro.dto.ddsPickG2cPush.ApiRequestParamsDto;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.Date;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/6/16
 */
public class DubboTests {
    String address = "zookeeper://10.202.39.19:2181?backup=10.202.39.20:2181,10.202.39.21:2181";

    @Test
    public void testCall() {

        ApiRequestParamsDto dto2 = new ApiRequestParamsDto();
        dto2.setRequireId(620050300001220L);
        dto2.setLineManageId(185610L);
        dto2.setPlanSendTime("2020-05-03 08:00:00");

        Object res = DubboCallUtils.invoke("com.sf.grd.trade.micro.service.DdsPickG2cPushService", "ddsPickRequireDelete", Lists.newArrayList(dto2), address, "");

        System.out.println(res);

        res = DubboCallUtils.invoke(address, "","com.sf.grd.trade.micro.service.DdsPickG2cPushService", "ddsPickRequireDelete", "com.sf.grd.trade.micro.dto.ddsPickG2cPush.ApiRequestParamsDto", JSON.toJSONString(dto2));
        System.out.println(res);
    }
}

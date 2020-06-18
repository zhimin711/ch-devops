package com.ch.test;

import com.ch.cloud.kafka.utils.DubboCallUtils;
import org.junit.Test;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/6/16
 */
public class DubboTests {
    String address = "zookeeper://10.202.39.19:2181?backup=10.202.39.20:2181,10.202.39.21:2181";

    Object o;

    @Test
    public void testCall() {

        String json = "{}";
        o = DubboCallUtils.invoke(address, "",
                "com.sf.grd.trade.micro.service.DdsPickG2cPushService",
                "ddsPickRequireDelete",
                "com.sf.grd.trade.micro.dto.ddsPickG2cPush.ApiRequestParamsDto",
                json);
        System.out.println(o);
    }
}

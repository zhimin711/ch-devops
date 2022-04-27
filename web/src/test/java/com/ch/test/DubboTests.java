package com.ch.test;

import com.ch.cloud.kafka.utils.DubboCallUtils;
import org.junit.Test;

/**
 * decs:
 *
 * @author zhimin.ma
 * @date 2020/6/16
 */
public class DubboTests {
    String address = "zookeeper://10.202.39.19:2181?backup=10.202.39.20:2181,10.202.39.21:2181";

    String address2 = "zookeeper://10.202.107.137:2181";

    String address3 = "dubbo://100.118.86.40:20880";

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
        o = DubboCallUtils.invoke(address, "1.0.1", "com.sf.shiva.trtms.ground.require.api.TempLineService",
                "checkCancel", "com.sf.shiva.trtms.ground.require.dto.temp.CancelRequire",
                "{\"requireIds\":[200618010471334]}");
        System.out.println(o);

    }
@Test
public void testSupplier(){

    o = DubboCallUtils.invoke(address, "1.0.1", "com.sf.shiva.trtms.ground.require.api.ISupplierDS",
            "findBySupplierCode", "java.lang.String",
            "G2C");
    System.out.println(o);
}

    @Test
    public void testCall2() {

        o = DubboCallUtils.invoke(address2, "1.0.1", "com.sf.shiva.trtms.ground.rs.api.IPlanDetailRequireDS",
                "updateBy", "com.sf.shiva.trtms.ground.rs.dto.planning.PlanLineDetailRequireDto,com.sf.shiva.trtms.ground.rs.dto.planning.PlanLineDetailRequireDto",
                "{\"ids\":[19265582,19265601,19265602]}", "{\"flag\":9}");
        System.out.println(o);
    }
}

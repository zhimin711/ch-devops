package com.ch.test.nacos;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.http.HttpUtils;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.vo.ServicesQueryVO;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.NetUtils;
import org.apache.http.client.utils.HttpClientUtils;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * 描述：
 *
 * @author Zhimin.Ma
 * @since 2022/4/29
 */
public class NacosServicesTests {
    String url = "http://192.168.0.204:8848";

    @Test
    public void page() {
        ServicesQueryVO queryVO = new ServicesQueryVO();
        String params = HttpUtil.toParams(BeanUtilsV2.objectToMap(queryVO));
        JSONObject resp = new RestTemplate().getForObject(url + NacosAPI.SERVICES + "?" + params, JSONObject.class);
        System.out.println(resp.toJSONString());
    }
}

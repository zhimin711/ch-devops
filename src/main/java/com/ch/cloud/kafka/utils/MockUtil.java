package com.ch.cloud.kafka.utils;

import com.alibaba.fastjson.JSONObject;
import com.ch.Constants;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.mock.Mock;
import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.MockRule;
import com.ch.cloud.mock.pojo.MockProp;
import com.ch.e.PubError;
import com.ch.utils.*;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/12/8
 */
public class MockUtil {

    public static final String range_regex = "[~]";

    public static boolean checkProps(List<BtTopicExtProp> props) {
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
        return true;
    }

    public static final String GPS_NAME = "经纬度";
    public static final String GPS_LNG_NAME = "经度";
    public static final String GPS_LAT_NAME = "经度";
    public static final String GPS_TIME = "上传时间";

    public static boolean checkGPSProps(BtTopicExt record) {
        if (record.getPoints().size() < 2) {
            return false;
        }
        List<BtTopicExtProp> props = record.getProps();
        if (props.size() < 4) {
            return false;
        }

        BtTopicExtProp posProp = props.get(0);
        if (CommonUtils.isEmptyOr(posProp.getCode(), posProp.getChildren())) {
//            return false;
        }
        for (BtTopicExtProp prop : props) {
            if (CommonUtils.isEmpty(prop.getCode())) {
                return false;
            }
        }
        return true;
    }


    public static Object mockDataProps(List<BtTopicExtProp> props) throws Exception {
        JSONObject obj = new JSONObject();
        for (BtTopicExtProp prop : props) {
            obj.put(prop.getCode(), mockDataProp(prop));
        }
        return obj;
    }


    public static Object mockDataProp(BtTopicExtProp prop) throws Exception {
        BeanExtUtils.BasicType type = BeanExtUtils.BasicType.fromObject(prop.getType());
        if (type != null) {
            if (CommonUtils.isEmpty(prop.getValRegex())) {
                MockConfig config = new MockConfig();
                config.setStringEnum(MockConfig.StringEnum.CHARACTER);
                return Mock.mock(Class.forName(prop.getType()), config);
            } else {
                boolean isRegex = true;
                MockConfig config = new MockConfig();

                if (NumberUtils.isNumeric(prop.getValRegex())) {
                    return prop.getValRegex();
                }
                String[] arr = prop.getValRegex().split(Constants.SEPARATOR);
                if (arr.length == 1) {
                    arr = prop.getValRegex().split(Constants.SEPARATOR_5);
                }
                if (arr.length == 1 && type != BeanExtUtils.BasicType.STRING) {
                    return prop.getValRegex();
                }

                switch (type) {
                    case INT:
                        config.intRange(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
                        break;
                    case LONG:
                        config.longRange(Long.parseLong(arr[0]), Long.parseLong(arr[1]));
                        break;
                    case DOUBLE:
                        config.doubleRange(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]));
                        break;
                    case FLOAT:
                        config.floatRange(Float.parseFloat(arr[0]), Float.parseFloat(arr[1]));
                        break;
                    case STRING:
                        config.setStringEnum(MockConfig.StringEnum.ARRAY);
                        config.stringSeed(prop.getValRegex().split(Constants.SEPARATOR_2));
                        break;
                    default:
                        isRegex = false;

                }
                if (isRegex) return Mock.mock(Class.forName(prop.getType()), config);
                return prop.getValRegex();
            }
        }
        if (CommonUtils.isEquals("{}", prop.getType())) {
            if (CommonUtils.isNotEmpty(prop.getValRegex())) {
                Class<?> clazz = JarUtils.loadClass(prop.getValRegex());
                if (clazz != null) {
                    if (CommonUtils.isNotEmpty(prop.getChildren())) {
                        //todo mock customer config
                    }
                    MockConfig config = new MockConfig();
                    config.setStringEnum(MockConfig.StringEnum.CHARACTER);
                    return Mock.mock(clazz, config);
                }
            } else if (CommonUtils.isNotEmpty(prop.getChildren())) {
                return mockDataProps(prop.getChildren());
            } else {
                return null;
            }
        }
        Class<?> clazz1 = KafkaSerializeUtils.loadClazz(null, prop.getType());
        if (BeanExtUtils.isDate(clazz1)) {
            MockConfig config = new MockConfig();
            if (CommonUtils.isNotEmpty(prop.getValRegex())) {
                String[] dArr = prop.getValRegex().split(Constants.SEPARATOR_5);
                if (dArr.length == 1) {
                    Date date = DateUtils.parse(prop.getValRegex());
                    if (date != null) return date;
                }
                Date ds = DateUtils.parse(dArr[0]);
                Date de = DateUtils.parse(dArr[1]);
                if (ds != null && de != null) {
                    config.dateRange(ds, de);
                }

            }
            return Mock.mock(Class.forName(prop.getType()), config);
        }
        return null;
    }

    public static List<MockProp> convertRules(BtTopicExt record, List<BtTopicExtProp> props) throws Exception {

        List<MockProp> props2 = Lists.newArrayList();
        for (BtTopicExtProp prop : props) {

            if (CommonUtils.isEmpty(prop.getCode())) {
                ExceptionUtils._throw(PubError.ARGS, "mock字段代码不能为空！");
            }
            MockProp prop2 = new MockProp();
            BeanUtils.copyProperties(prop, prop2);
            if (CommonUtils.isEquals(Constants.SEPARATOR, prop.getType()) || (CommonUtils.isEmpty(prop.getValRegex()) && CommonUtils.isEmpty(prop.getChildren()) && CommonUtils.isEquals("{}", prop.getType()))) {
                prop2.setRule(MockRule.EMPTY);
            } else if (!CommonUtils.isEquals("{}", prop.getType()) && CommonUtils.isEmpty(prop.getValRegex())) {
                prop2.setRule(MockRule.RANDOM);
            } else if (!CommonUtils.isEquals("{}", prop.getType())) {
                BeanExtUtils.BasicType type = BeanExtUtils.BasicType.fromObject(prop.getType());
                if (type == null) {
                    ExceptionUtils._throw(PubError.ARGS, "mock字段" + prop.getCode() + "代码类型错误！");
                }
                Class<?> clazz = Class.forName(prop.getType());
                if (Number.class.isAssignableFrom(clazz) && CommonUtils.isNumeric(prop.getValRegex())) {
                    prop2.setRule(MockRule.FIXED);
                }
            } else {
                prop2.setRule(MockRule.OBJECT);
                Class<?> clazz = null;
                if (CommonUtils.isNotEmpty(prop.getValRegex())) {
                    clazz = JarUtils.loadClass(prop.getValRegex());
                }
                if (clazz != null) {
                    prop2.setTargetClass(clazz);
                }
                if (CommonUtils.isNotEmpty(prop.getChildren())) {
                    prop2.setChildren(convertRules(record, prop.getChildren()));
                }
            }
            props2.add(prop2);
        }

        return props2;
    }

}

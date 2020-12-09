package com.ch.cloud.kafka.utils;

import com.alibaba.fastjson.JSONObject;
import com.ch.Constants;
import com.ch.cloud.kafka.model.BtTopicExt;
import com.ch.cloud.kafka.model.BtTopicExtProp;
import com.ch.cloud.mock.Mock;
import com.ch.cloud.mock.MockConfig;
import com.ch.utils.*;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/12/8
 */
public class TopicExtUtil {


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

                String[] arr = prop.getValRegex().split(Constants.SEPARATOR);
                if (NumberUtils.isNumeric(prop.getValRegex())) {
                    return prop.getValRegex();
                }
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
}

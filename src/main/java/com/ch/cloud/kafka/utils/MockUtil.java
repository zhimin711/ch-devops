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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/12/8
 */
@Slf4j
public class MockUtil {

    public static final String range_regex = "[~]";
    public static final String OBJ = "{}";

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

        long minutes = (int) DateUtils.calcOffsetMinutes(record.getCreateAt(), record.getUpdateAt());
        int ss = (int) minutes / record.getBatchSize() / record.getThreadSize();

        List<MockProp> props2 = Lists.newArrayList();
        for (BtTopicExtProp prop : props) {

            if (CommonUtils.isEmpty(prop.getCode())) {
                ExceptionUtils._throw(PubError.ARGS, "mock字段代码不能为空！");
            }
            MockProp prop2 = new MockProp();
            BeanUtils.copyProperties(prop, prop2);
            MockRule rule = MockRule.valueOf(prop.getRule());
            prop2.setRule(rule);
            BeanExtUtils.BasicType type = BeanExtUtils.BasicType.fromObject(prop.getType());
            if (!CommonUtils.isEquals(OBJ, prop.getType())) {
                if (type == null && !CommonUtils.isEquals(Date.class.getName(), prop.getType())) {
                    ExceptionUtils._throw(PubError.ARGS, "mock字段" + prop.getCode() + "类型错误！");
                }
                Class<?> clazz = Class.forName(prop.getType());
                prop2.setTargetClass(clazz);
            } else {
                Class<?> clazz = null;
                if (CommonUtils.isNotEmpty(prop.getValRegex())) {
                    clazz = JarUtils.loadClass(prop.getValRegex());
                }
                if (clazz != null) {
                    prop2.setTargetClass(clazz);
                } else if (CommonUtils.isEmpty(prop.getChildren())) {
                    prop2.setRule(MockRule.EMPTY);
                }
                if (CommonUtils.isNotEmpty(prop.getChildren())) {
                    prop2.setChildren(convertRules(record, prop.getChildren()));
                }
                continue;
            }
            boolean isDate = BeanExtUtils.isDate(prop2.getTargetClass());
            switch (rule) {
                case RANDOM:
                    if (isDate && CommonUtils.isNotEmpty(prop.getValRegex())) {
                        prop2.setPattern(parseDatePattern(prop.getValRegex()));
                    }
                    break;
                case RANDOM_LENGTH:
                    if (type == null) {
                        ExceptionUtils._throw(PubError.INVALID, "mock字段" + prop.getCode() + "随机类型错误！");
                    } else if (!CommonUtils.isNumeric(prop.getValRegex())) {
                        ExceptionUtils._throw(PubError.INVALID, "mock字段" + prop.getCode() + "随机长度错误，请输入为数字！");
                    }
                    prop2.setLen(Integer.parseInt(prop.getValRegex()));
                    break;
                case RANDOM_RANGE:
                    if (CommonUtils.isEmpty(prop)) {
                        ExceptionUtils._throw(PubError.INVALID, "mock字段" + prop.getCode() + "随机不能为空！");
                    }
                    String tmp = prop.getValRegex();
                    if (prop.getValRegex().startsWith("[")) {
                        tmp = tmp.substring(1);
                    }
                    if (prop.getValRegex().indexOf("]") > 0) {
                        tmp = tmp.substring(0, prop.getValRegex().indexOf("]") - 1);
                    }
                    String[] arr;
                    if (type == BeanExtUtils.BasicType.STRING) {
                        arr = tmp.split(Constants.SEPARATOR_2);
                    } else if (BeanExtUtils.isDate(prop2.getTargetClass())) {
                        arr = tmp.split(Constants.SEPARATOR_5);
                    } else {
                        arr = tmp.split(Constants.SEPARATOR_5);
                    }
                    if (arr.length < 2) {
                        ExceptionUtils._throw(PubError.INVALID, "mock字段" + prop.getCode() + "随机不能为空！");
                    }
                    if (type == BeanExtUtils.BasicType.STRING) {
                        prop2.setStrRange(arr);
                    } else if (isDate) {
                        Date d1 = DateUtils.parse(arr[0]);
                        Date d2 = DateUtils.parse(arr[1]);
                        if (CommonUtils.isEmptyOr(d1, d2)) {
                            ExceptionUtils._throw(PubError.INVALID, "mock字段" + prop.getCode() + "日期范围配置错误！");
                        }
                        prop2.setPattern(parseDatePattern(prop.getValRegex()));
                        prop2.setMin(d1.getTime());
                        prop2.setMax(d2.getTime());
                    } else {
                        prop2.setMin(Double.parseDouble(arr[0]));
                        prop2.setMax(Double.parseDouble(arr[1]));
                    }
                    break;
                case AUTO_INCR:
                case AUTO_DECR:
                    if (isDate) {
                        Date basic = parseDateBasic(prop.getValRegex());
                        long offset = parseDateOffset(prop.getValRegex());
                        prop2.setBaseD(Lists.newArrayList());
                        for (int i = 0; i < record.getThreadSize(); i++) {
                            prop2.getBaseD().add(DateUtils.addMinutes(basic, i * ss));
                        }
                        prop2.setOffset(offset + "");
                    }
                    break;
                case AUTO_INCR_RANGE:
                case AUTO_DECR_RANGE:
                    break;
                default:
            }
            if (prop.getValRegex().startsWith("*[") && prop.getValRegex().endsWith("]")) {
                String numS = prop.getValRegex().substring(2, prop.getValRegex().length() - 1);
                if (CommonUtils.isNumeric(numS)) {
                    prop2.setLen(Integer.parseInt(numS));
                }
            } else if (prop.getValRegex().startsWith("[") && prop.getValRegex().endsWith("]") && !prop.getValRegex().contains("][")) {
                String range = prop.getValRegex().substring(1, prop.getValRegex().length() - 1);
                if (type == BeanExtUtils.BasicType.STRING) {
                    prop2.setStrRange(range.split(Constants.SEPARATOR_2));
                } else {
                    String[] arr = range.split(Constants.SEPARATOR_5);
                    if (arr.length == 1) {
                        ExceptionUtils._throw(PubError.ARGS, "mock字段" + prop.getCode() + ": 范围配置错误[1~100]！");
                    }
                    if (CommonUtils.isNumeric(arr[0]) || CommonUtils.isNumeric(arr[1])) {
                        ExceptionUtils._throw(PubError.ARGS, "mock字段" + prop.getCode() + ": 范围开始或结束配置错误[1.0~100.0]！");
                    }
                    prop2.setMin(Double.parseDouble(arr[0]));
                    prop2.setMax(Double.parseDouble(arr[1]));
                }
            }
            props2.add(prop2);
        }

        return props2;
    }

    private static Date parseDateBasic(String regex) {
        int index = regex.indexOf("[");
        if (index < 0) {
            index = regex.indexOf("|");
        }
        if (index <= 0) {
            return DateUtils.current();
        }
        String b = regex.substring(0, index);
        Date bd = DateUtils.parse(b);
        if (bd != null) {
            return bd;
        }

        return DateUtils.current();
    }

    public static List<MockProp> convertGPSRules(BtTopicExt record) throws Exception {
        if (CommonUtils.isEmptyOr(record.getCreateAt(), record.getUpdateAt())) {
            ExceptionUtils._throw(PubError.ARGS, "GPS轨迹开始或结束时间为空！");
        }
        if (CommonUtils.isEmpty(record.getThreadSize())) {
            record.setThreadSize(1);
        }
        if (CommonUtils.isEmpty(record.getBatchSize())) {
            record.setBatchSize(10);
        }
        if (record.getProps().size() < 4) {
            ExceptionUtils._throw(PubError.ARGS, "mock字段代码不足！");
        } else if (record.getProps().size() == 4) {
            return Lists.newArrayList();
        }

        List<BtTopicExtProp> props = record.getProps().subList(4, record.getProps().size());
        return convertRules(record, props);
    }

    private static DateUtils.Pattern parseDatePattern(String regex) {
        int index = regex.indexOf("|");
        if (index > 0 && index < regex.length() && regex.endsWith("|")) {
            String p = regex.substring(index, regex.length() - 1);
            if (CommonUtils.isNotEmpty(p)) {
                return DateUtils.Pattern.fromPattern(p);
            }
        }
        return null;
    }

    private static long parseDateOffset(String regex) {
        int index = regex.indexOf("[");
        if (index > 0 && index < regex.length() && regex.endsWith("]")) {
            String offset = regex.substring(index, regex.length() - 1);
            try {
                Duration duration = Duration.parse(offset);
                return duration.getSeconds();
            } catch (Exception e) {
//                ExceptionUtils._throw(PubError.ARGS, "mock字段递增量解析失败！");
                log.error("Duration.parse error!" + regex, e);
            }

        }
        return 0;
    }
}

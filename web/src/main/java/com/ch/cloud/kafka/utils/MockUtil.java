package com.ch.cloud.kafka.utils;

import com.alibaba.fastjson.JSONObject;
import com.ch.Constants;
import com.ch.cloud.kafka.model.KafkaTopicExt;
import com.ch.cloud.kafka.model.KafkaTopicExtProp;
import com.ch.cloud.mock.Mock;
import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.MockRule;
import com.ch.cloud.mock.pojo.MockProp;
import com.ch.e.ExceptionUtils;
import com.ch.e.PubError;
import com.ch.utils.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * decs:
 *
 * @author 01370603
 * @since 2020/12/8
 */
@Slf4j
public class MockUtil {

    public static final String range_regex = "[~]";
    public static final String OBJ         = "{}";

    public static boolean checkProps(List<KafkaTopicExtProp> props) {
        boolean isSingle = false;
        boolean ok = true;
        for (KafkaTopicExtProp prop : props) {
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

    public static final String GPS_NAME     = "经纬度";
    public static final String GPS_LNG_NAME = "经度";
    public static final String GPS_LAT_NAME = "纬度";
    public static final String GPS_TIME     = "轨迹时间";

    public static boolean checkGPSProps(KafkaTopicExt record) {
        if (record.getPoints().size() < 2) {
            return false;
        }
        List<KafkaTopicExtProp> props = record.getProps();
        if (props.size() < 4) {
            return false;
        }

        KafkaTopicExtProp posProp = props.get(0);
        if (CommonUtils.isEmptyOr(posProp.getCode(), posProp.getChildren())) {
//            return false;
        }
        for (KafkaTopicExtProp prop : props) {
            if (CommonUtils.isEmpty(prop.getCode())) {
                return false;
            }
        }
        return true;
    }


    public static Object mockDataProps(List<KafkaTopicExtProp> props) throws Exception {
        JSONObject obj = new JSONObject();
        for (KafkaTopicExtProp prop : props) {
            obj.put(prop.getCode(), mockDataProp(prop));
        }
        return obj;
    }


    public static Object mockDataProp(KafkaTopicExtProp prop) throws Exception {
        BeanUtilsV2.BasicType type = BeanUtilsV2.BasicType.fromObject(prop.getType());
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
                if (arr.length == 1 && type != BeanUtilsV2.BasicType.STRING) {
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
        if (BeanUtilsV2.isDate(clazz1)) {
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

    public static List<MockProp> convertRules(KafkaTopicExt record, List<KafkaTopicExtProp> props, boolean isChild) throws Exception {

        long total = CommonUtils.isEquals("GPS", record.getDescription()) ?
                (int) DateUtils.calcOffsetMinutes(record.getCreateAt(), record.getUpdateAt()) : 0;
        int ss = CommonUtils.isEquals("GPS", record.getDescription()) ?
                (int) total / record.getBatchSize() / record.getThreadSize() : record.getThreadSize();

        List<MockProp> props2 = Lists.newArrayList();
        for (KafkaTopicExtProp prop : props) {

            if (CommonUtils.isEmpty(prop.getCode()) && (isChild || props.size() > 1)) {
                ExceptionUtils._throw(PubError.ARGS, "mock字段代码不能为空！");
            } else if (CommonUtils.isEmpty(prop.getRule())) {
                ExceptionUtils._throw(PubError.ARGS, "mock字段规则不能为空！");
            }
            MockProp prop2 = new MockProp();
            BeanUtils.copyProperties(prop, prop2);
            MockRule rule = MockRule.valueOf(prop.getRule());
            prop2.setRule(rule);
            BeanUtilsV2.BasicType type = BeanUtilsV2.BasicType.fromObject(prop.getType());
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
                    prop2.setChildren(convertRules(record, prop.getChildren(), true));
                }
                props2.add(prop2);
                continue;
            }
            boolean isDate = BeanUtilsV2.isDate(prop2.getTargetClass());
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
                case AUTO_INCR_RANGE:
                case AUTO_DECR_RANGE:
                    if (CommonUtils.isEmpty(prop.getValRegex())) {
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
                    if (type == BeanUtilsV2.BasicType.STRING || type == BeanUtilsV2.BasicType.CHAR) {
                        arr = tmp.split(Constants.SEPARATOR_2);
                    } else if (isDate) {
                        arr = tmp.split(Constants.SEPARATOR_5);
                    } else {
                        arr = tmp.split(Constants.SEPARATOR_5);
                    }
                    if (arr.length < 2) {
                        ExceptionUtils._throw(PubError.INVALID, "mock字段" + prop.getCode() + "随机不能为空！");
                    }
                    if (type == BeanUtilsV2.BasicType.STRING) {
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
                        prop2.setBaseD(Lists.newArrayList(d1, d2));
                    } else {
                        prop2.setMin(Double.parseDouble(arr[0]));
                        prop2.setMax(Double.parseDouble(arr[1]));
                        prop2.setBaseN(Lists.newArrayList(Double.parseDouble(arr[0]), Double.parseDouble(arr[1])));
                    }
                    if (prop2.getMin() > 0 && prop2.getMax() > 0) {
                        prop2.setOffset((prop2.getMax() - prop2.getMin()) / record.getThreadSize() / record.getBatchSize() + "");
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
                        prop2.setPattern(parseDatePattern(prop.getValRegex()));
                        break;
                    }
                    if (type == null || type == BeanUtilsV2.BasicType.STRING || type == BeanUtilsV2.BasicType.CHAR) {
                        ExceptionUtils._throw(PubError.INVALID, "mock字段" + prop.getCode() + "递增或减类型错误！");
                    }
                    String basic = parseNumberBasic(prop.getValRegex());
                    String offset = parseNumberOffset(prop.getValRegex());
                    prop2.setOffset(offset);

                    prop2.setBaseN(Lists.newArrayList());
                    switch (type) {
                        case FLOAT:
                        case DOUBLE:
                            double b2 = Double.parseDouble(basic);
                            double o2 = Double.parseDouble(offset);
                            for (int i = 0; i < record.getThreadSize(); i++) {
                                prop2.getBaseN().add((b2 + i * o2));
                            }
                        default:
                            int b1 = Integer.parseInt(basic);
                            int o1 = Integer.parseInt(offset);
                            for (int i = 0; i < record.getThreadSize(); i++) {
                                prop2.getBaseN().add((b1 + i * o1));
                            }
                    }
                    break;
                default:
            }
            props2.add(prop2);
        }

        return props2;
    }

    private static String parseNumberOffset(String regex) {
        int index = regex.indexOf("[");
        if (index > 0 && index < regex.length() && regex.endsWith("]")) {
            String offset = regex.substring(index, regex.length() - 1);
            if (CommonUtils.isNumeric(offset))
                return offset;
        }
        return "1";
    }

    private static String parseNumberBasic(String regex) {
        if (CommonUtils.isNumeric(regex)) return regex;
        int index = regex.indexOf("[");
        if (index < 0) {
            return "0";
        }
        return regex.substring(0, index);
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

    public static List<MockProp> convertGPSRules(KafkaTopicExt record) throws Exception {
        if (CommonUtils.isEmptyOr(record.getCreateAt(), record.getUpdateAt())) {
            ExceptionUtils._throw(PubError.ARGS, "GPS轨迹开始或结束时间为空！");
        }
        if (CommonUtils.isEmpty(record.getPoints()) || record.getPoints().size() < 2) {
            ExceptionUtils._throw(PubError.ARGS, "GPS轨迹点不足！");
        }
        if (CommonUtils.isEmpty(record.getThreadSize())) {
            record.setThreadSize(1);
        }
        if (CommonUtils.isEmpty(record.getBatchSize())) {
            record.setBatchSize(10);
        }
        int total = (int) DateUtils.calcOffsetMinutes(record.getCreateAt(), record.getUpdateAt());
        if (total / record.getBatchSize() < 2) {
            ExceptionUtils._throw(PubError.ARGS, "GPS轨迹开始和结束时间范围过小，不能生成GPS！");
        }

        if (record.getProps().size() < 4) {
            ExceptionUtils._throw(PubError.ARGS, "mock字段代码不足！");
        } else if (record.getProps().size() == 4) {
            return Lists.newArrayList();
        }

        List<KafkaTopicExtProp> props = record.getProps().subList(4, record.getProps().size());
        return convertRules(record, props, false);
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
        int indexS = regex.indexOf("]");
        if (index > 0 && index < indexS) {
            String offset = regex.substring(index + 1, indexS);
            if (CommonUtils.isNumeric(offset)) return Integer.parseInt(offset);
            try {
                Duration duration = Duration.parse(offset.toUpperCase());
                return duration.getSeconds();
            } catch (Exception e) {
//                ExceptionUtils._throw(PubError.ARGS, "mock字段递增量解析失败！");
                log.error("Duration.parse error!" + regex, e);
            }

        }
        return 60;
    }

    public static Object mockProp(MockProp prop, int offset) {
        Object o = null;

        boolean isDate = BeanUtilsV2.isDate(prop.getTargetClass());
        boolean isString = BeanUtilsV2.isString(prop.getTargetClass());

        MockConfig config = new MockConfig();
        switch (prop.getRule()) {
            case FIXED:
                o = prop.getValRegex();
                break;
            case RANDOM:
                o = Mock.mock(prop.getTargetClass(), config);
                break;
            case RANDOM_RANGE:
                if (isString) {
                    config.setStringEnum(MockConfig.StringEnum.ARRAY);
                    config.stringSeed(prop.getValRegex().split(Constants.SEPARATOR_2));
                } else {
                    config.doubleRange(prop.getMin(), prop.getMax());
                    config.intRange((int) prop.getMin(), (int) prop.getMax());
                    config.longRange((long) prop.getMin(), (long) prop.getMax());
                    config.dateRange((long) prop.getMin(), (long) prop.getMax());
                    config.floatRange((float) prop.getMin(), (float) prop.getMax());
                    config.byteRange((byte) prop.getMin(), (byte) prop.getMax());
                }
                o = Mock.mock(prop.getTargetClass(), config);
                break;
            case RANDOM_LENGTH:
                break;
            case OBJECT:
                if (prop.getTargetClass() != null) {
                    config.setStringEnum(MockConfig.StringEnum.CHARACTER);
                    o = Mock.mock(prop.getTargetClass(), config);
                }
                if (CommonUtils.isNotEmpty(prop.getChildren())) {
                    Map<String, Object> customVMap = Maps.newConcurrentMap();
                    for (MockProp p : prop.getChildren()) {
                        Object o1;
                        String code = p.getCode();
                        if (isArray(code)) {
                            int len = getArrayLength(code);
                            Object[] objs = new Object[len];
                            for (int i = 0; i < len; i++) {
                                objs[i] = mockProp(p, i);
                            }
                            o1 = objs;
                            code = getPropCode(code);
                        } else if (isCollection(code)) {
                            int len = getCollectionLength(code);
                            List<Object> objs = Lists.newArrayList();
                            for (int i = 0; i < len; i++) {
                                objs.add(mockProp(p, i));
                            }
                            o1 = objs;
                            code = getPropCode(code);
                        } else {
                            o1 = mockProp(p, offset);
                        }
                        customVMap.put(code, o1);
                    }
                    if (o != null) {
                        BeanUtilsV2.setFieldValue(o, customVMap, true);
                    } else {
                        o = new JSONObject(customVMap);
                    }
                }
                break;
            case AUTO_INCR:
            case AUTO_INCR_RANGE:
                if (isDate) {
                    Date baseD = prop.getBaseD().get(0);
                    int offsetD = Integer.parseInt(prop.getOffset()) * offset;
                    Date mvDate = DateUtils.addSeconds(baseD, offsetD);
                    if (prop.getPattern() != null) {
                        o = DateUtils.format(mvDate, prop.getPattern());
                    } else {
                        o = mvDate;
                    }
                } else {
                    BigDecimal baseN = new BigDecimal(prop.getBaseN().get(0).doubleValue());
                    BigDecimal offsetN = new BigDecimal(prop.getOffset());
                    o = NumberUtils.fixed2(baseN.add(offsetN.add(offsetN.multiply(new BigDecimal(offset)))));
                }
                break;
            case AUTO_DECR:
                if (isDate) {
                    Date baseD = prop.getBaseD().get(0);
                    int offsetD = Integer.parseInt(prop.getOffset()) * offset;
                    Date mvDate = DateUtils.addSeconds(baseD, -offsetD);
                    o = prop.getPattern() != null ? DateUtils.format(mvDate, prop.getPattern()) : mvDate;
                } else {
                    BigDecimal baseN = new BigDecimal(prop.getBaseN().get(0).doubleValue());
                    BigDecimal offsetN = new BigDecimal(prop.getOffset());
                    o = NumberUtils.fixed2(baseN.subtract(offsetN.subtract(offsetN.multiply(new BigDecimal(offset)))));
                }
                break;
            case AUTO_DECR_RANGE:
            default:
        }
        return o;
    }

    public static boolean isArray(String propCode) {
        int s = propCode.indexOf("[");
        int e = propCode.indexOf("]");
        return s >= 0 && e > 0;
    }

    public static String getPropCode(String propCode) {
        int s = propCode.indexOf(Constants.SEPARATOR_1);
        if (s < 0) return propCode;
        return propCode.substring(s + 1);
    }

    public static int getArrayLength(String propCode) {
        int s = propCode.indexOf("[");
        int e = propCode.indexOf("]");
        String len = propCode.substring(s + 1, e);
        if (CommonUtils.isNumeric(len)) return Integer.parseInt(len);
        return 1;
    }

    public static boolean isCollection(String propCode) {
        int s = propCode.indexOf("<");
        int e = propCode.indexOf(">");
        return s >= 0 && e > 0;
    }

    public static int getCollectionLength(String propCode) {
        int s = propCode.indexOf("<");
        int e = propCode.indexOf(">");
        String len = propCode.substring(s + 1, e);
        if (CommonUtils.isNumeric(len)) return Integer.parseInt(len);
        return 1;
    }

    public static String getArrayPropCode(String propCode) {
        int e = propCode.indexOf("]");
        if (e >= 0) return propCode.substring(e + 1);
        return propCode;
    }

    public static String getCollectionPropCode(String propCode) {
        int e = propCode.indexOf(">");
        if (e >= 0) return propCode.substring(e + 1);
        return propCode;
    }
}

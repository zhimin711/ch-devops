package com.ch.cloud.mock;

/**
 * desc: Mock 规则
 * 系统根据表达示计算出
 *
 * @author zhimi
 * @date 2020/12/9 22:28
 */
public enum MockRule {
    /**
     * 空值
     */
    EMPTY,
    /**
     * 固定值
     */
    FIXED,
    /**
     * 随机值:空
     */
    RANDOM,
    /**
     * 固定长度随机值: [长度]
     * [10]
     * [1~10]
     */
    RANDOM_LENGTH,
    /**
     * 范围-随机:
     * int: [1~100]
     * double: [1.0~100.0]
     * date: [2020-12-01~2020-12-31]
     * String|char: [a,b,c]
     */
    RANDOM_RANGE,
    /**
     * 自增：基数+...加数（不必要）
     * 只支持数字与时间类型
     * 不支持字符串与Object类型
     * int: 1[1]
     * double: 100.0[10.5]
     * date: 2020-12-01[1d]
     */
    AUTO_INCR,
    /**
     * 自减
     * 只支持数字与时间类型
     * int: 1[1]
     * double: 100.0[10.5]
     * date: 2020-12-01[1d]
     */
    AUTO_DECR,
    /**
     * 范围-递增
     * 只支持数字与时间类型
     * int: [1~100]
     * double: [1.0~100.0]
     * date: [2020-12-01~2020-12-31]
     */
    AUTO_INCR_RANGE,
    /**
     * 范围-递减
     * 只支持数字与时间类型
     * int: [1~100]
     * double: [1.0~100.0]
     * date: [2020-12-01~2020-12-31]
     */
    AUTO_DECR_RANGE,
    /**
     *对象
     */
    OBJECT
}

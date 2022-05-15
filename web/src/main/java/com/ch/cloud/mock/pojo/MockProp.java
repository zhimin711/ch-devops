package com.ch.cloud.mock.pojo;

import com.ch.cloud.mock.MockRule;
import com.ch.utils.DateUtils;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * desc:
 *
 * @author zhimi
 * @since 2020/12/10 0:21
 */
@Data
public class MockProp {

    private String code;

    private String valRegex;

    private String valEdit;

    private String valDel;

    private MockRule rule;

    private List<MockProp> children;

    private Class<?> targetClass;

    private int len;

    private double min;

    private double max;

    private String[] strRange;

    private DateUtils.Pattern pattern;

    private List<Number> baseN;

    private List<Date> baseD;

    private String offset;

    private int currIndex;
}

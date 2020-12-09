package com.ch.cloud.mock.pojo;

import com.ch.cloud.mock.MockRule;
import lombok.Data;

import java.util.List;

/**
 * desc:
 *
 * @author zhimi
 * @date 2020/12/10 0:21
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
}

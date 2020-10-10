package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

/**
 * Integer对象模拟器
 */
public class IntegerMocker implements Mocker<Integer> {

  @Override
  public Integer mock(MockConfig mockConfig) {
    return RandomUtils.nextInt(mockConfig.getIntRange()[0], mockConfig.getIntRange()[1]);
  }

}

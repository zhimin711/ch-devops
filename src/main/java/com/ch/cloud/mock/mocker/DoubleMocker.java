package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

/**
 * Double对象模拟器
 */
public class DoubleMocker implements Mocker<Double> {

  @Override
  public Double mock(MockConfig mockConfig) {
    return RandomUtils.nextDouble(mockConfig.getDoubleRange()[0], mockConfig.getDoubleRange()[1]);
  }

}

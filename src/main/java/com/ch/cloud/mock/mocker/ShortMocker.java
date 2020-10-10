package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

/**
 * 模拟Short对象
 */
public class ShortMocker implements Mocker<Short> {

  @Override
  public Short mock(MockConfig mockConfig) {
    return (short) RandomUtils.nextInt(mockConfig.getShortRange()[0], mockConfig.getShortRange()[1]);
  }

}

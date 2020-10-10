package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

/**
 * 模拟Long对象
 */
public class LongMocker implements Mocker<Long> {

  @Override
  public Long mock(MockConfig mockConfig) {
    return RandomUtils.nextLong(mockConfig.getLongRange()[0], mockConfig.getLongRange()[1]);
  }

}

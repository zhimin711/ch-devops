package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.util.RandomUtils;

/**
 * Byte对象模拟器
 */
public class ByteMocker implements Mocker<Byte> {

  @Override
  public Byte mock(MockConfig mockConfig) {
    return (byte) RandomUtils.nextInt(mockConfig.getByteRange()[0], mockConfig.getByteRange()[1]);
  }

}

package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

import java.math.BigInteger;

/**
 * BigInteger对象模拟器
 */
public class BigIntegerMocker implements Mocker<BigInteger> {

  @Override
  public BigInteger mock(MockConfig mockConfig) {
    return BigInteger.valueOf(RandomUtils.nextLong(mockConfig.getLongRange()[0], mockConfig.getLongRange()[1]));
  }

}

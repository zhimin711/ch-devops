package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

/**
 * Character对象模拟器
 */
public class CharacterMocker implements Mocker<Character> {

  @Override
  public Character mock(MockConfig mockConfig) {
    char[] charSeed = mockConfig.getCharSeed();
    return charSeed[RandomUtils.nextInt(0, charSeed.length)];
  }

}

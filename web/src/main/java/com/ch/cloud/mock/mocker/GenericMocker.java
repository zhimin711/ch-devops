package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;

import java.lang.reflect.ParameterizedType;

/**
 * 模拟泛型
 */
public class GenericMocker implements Mocker<Object> {

  private ParameterizedType type;

  GenericMocker(ParameterizedType type) {
    this.type = type;
  }

  @Override
  public Object mock(MockConfig mockConfig) {
    return new BaseMocker(type.getRawType(), type.getActualTypeArguments()).mock(mockConfig);
  }

}

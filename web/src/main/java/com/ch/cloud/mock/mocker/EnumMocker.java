package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.MockException;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

import java.lang.reflect.Field;

/**
 * Double对象模拟器
 */
public class EnumMocker<T extends Enum> implements Mocker<Object> {

  private Class<?> clazz;

  public EnumMocker(Class<?> clazz) {
    this.clazz = clazz;
  }

  @Override
  public T mock(MockConfig mockConfig) {
    Enum[] enums = mockConfig.getcacheEnum(clazz.getName());
    if (enums == null) {
      try {
        Field field = clazz.getDeclaredField("$VALUES");
        field.setAccessible(true);
        enums = (Enum[]) field.get(clazz);
        if (enums.length == 0) {
          throw new MockException("空的enum不能模拟");
        }
        mockConfig.cacheEnum(clazz.getName(), enums);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new MockException(e);
      }
    }
    return (T) enums[RandomUtils.nextInt(0, enums.length)];
  }

}

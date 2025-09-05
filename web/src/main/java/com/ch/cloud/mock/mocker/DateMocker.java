package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockConfig;
import com.ch.cloud.mock.Mocker;
import com.ch.cloud.mock.util.RandomUtils;

import java.util.Date;

/**
 * Date对象模拟器
 */
public class DateMocker extends AbstractDateMock implements Mocker<Date> {

  public DateMocker(String startTimePattern, String endTimePattern) {
    super(startTimePattern, endTimePattern);
  }

  public DateMocker(Date startTime, Date endTime) {
    super(startTime, endTime);
  }

  public DateMocker(long startTime, long endTime) {
    super(startTime, endTime);
  }

  @Override
  public Date mock(MockConfig mockConfig) {
    return new Date(RandomUtils.nextLong(startTime, endTime));
  }

}

package com.ch.cloud.mock.mocker;

import com.ch.cloud.mock.MockException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AbstractDateMock {

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    protected Long startTime;
    protected Long endTime;

    public AbstractDateMock(String startTimePattern, String endTimePattern) {
        try {
            this.startTime = FORMAT.parse(startTimePattern).getTime();
            this.endTime = FORMAT.parse(endTimePattern).getTime();
        } catch (ParseException e) {
            throw new MockException("时间格式设置错误，设置如下格式yyyy-MM-dd ", e);
        }
    }

    public AbstractDateMock(Date startTime, Date endTime) {
        this.startTime = startTime.getTime();
        this.endTime = endTime.getTime();
    }
    public AbstractDateMock(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}

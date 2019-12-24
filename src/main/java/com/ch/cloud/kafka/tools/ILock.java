package com.ch.cloud.kafka.tools;

/**
 * decs:
 *
 * @author 01370603
 * @date 2019/12/24
 */
public interface ILock {
    /**
     * 锁
     */
    void getLock();

    /**
     * 释放锁
     */
    void unLock();
}
